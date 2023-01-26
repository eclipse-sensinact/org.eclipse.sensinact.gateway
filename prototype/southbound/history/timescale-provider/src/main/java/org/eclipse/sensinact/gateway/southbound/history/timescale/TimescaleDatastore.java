/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.history.timescale;

import static org.osgi.service.typedevent.TypedEventConstants.TYPED_EVENT_TOPICS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.prototype.notification.ResourceDataNotification;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jdbc.JDBCConnectionProvider;
import org.osgi.service.transaction.control.jdbc.JDBCConnectionProviderFactory;
import org.osgi.service.typedevent.TypedEventHandler;
import org.osgi.service.typedevent.annotations.RequireTypedEvent;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component(service = {}, immediate = true, configurationPid = "sensinact.history.timescale", configurationPolicy = ConfigurationPolicy.REQUIRE)
@RequireTypedEvent
public class TimescaleDatastore implements TypedEventHandler<ResourceDataNotification> {

    private static final String INSERT_TEMPLATE = "INSERT INTO %s ( time, model, provider, service, resource, data ) values ( ?, ?, ?, ?, ?, %s );";
    private static final String NOT_SET = "<<NOT_SET>>";

    private static final Logger logger = LoggerFactory.getLogger(TimescaleDatastore.class);

    public @interface Config {

        String url();

        String user() default NOT_SET;

        String _password() default NOT_SET;
    }

    @Reference
    TransactionControl txControl;

    @Reference
    JDBCConnectionProviderFactory providerFactory;

    private Config config;

    private JDBCConnectionProvider provider;

    private final AtomicReference<Connection> connection = new AtomicReference<>();

    private ServiceRegistration<?> reg;

    private final ObjectMapper mapper = new ObjectMapper();

    @Activate
    void start(BundleContext ctx, Config config) {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting the TimescaleDB history store");
        }
        synchronized (this) {
            this.config = config;
        }
        doStart(ctx);
    }

    void doStart(BundleContext ctx) {
        try {
            setProvider(createProvider(config));
            setupTables();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.debug("An error occurred setting up database access", e);
            }
            safeUnregister();
            return;
        }
        registerListener(ctx);
    }

    private JDBCConnectionProvider createProvider(Config config) {
        PGSimpleDataSource datasource = new PGSimpleDataSource();
        datasource.setURL(config.url());
        if (!NOT_SET.equals(config.user()) && !config.user().isBlank()) {
            datasource.setUser(config.user());
            datasource.setPassword(config._password());
        }
        return providerFactory.getProviderFor(datasource, null);
    }

    private void setProvider(JDBCConnectionProvider provider) {
        Connection resource = provider != null ? provider.getResource(txControl) : null;
        JDBCConnectionProvider old;
        synchronized (this) {
            old = this.provider;
            this.provider = provider;
            connection.set(resource);
        }

        if (old != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Releasing configured Timescale DB connections");
            }
            providerFactory.releaseProvider(old);
        }
    }

    @Modified
    void update(BundleContext ctx, Config config) {
        Config oldConfig;
        synchronized (this) {
            oldConfig = this.config;
            this.config = config;
        }

        if (Objects.equals(oldConfig.url(), config.url()) && Objects.equals(oldConfig.user(), config.user())
                && Objects.equals(oldConfig._password(), config._password())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Not updating the Timescale DB connection as there is no need");
            }
            // No need to update the provider
            registerListener(ctx);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Restarting the Timescale DB connection due to a config change");
            }
            doStart(ctx);
        }
    }

    @Deactivate
    void stop() {
        if (logger.isDebugEnabled()) {
            logger.debug("Stopping the TimescaleDB history store");
        }
        setProvider(null);
        safeUnregister();
    }

    private void safeUnregister() {
        ServiceRegistration<?> reg;
        synchronized (this) {
            reg = this.reg;
            this.reg = null;
        }
        safeUnregister(reg);
    }

    private void safeUnregister(ServiceRegistration<?> reg) {
        if (reg != null) {
            try {
                reg.unregister();
            } catch (IllegalStateException ise) {
            }
        }
    }

    private void setupTables() {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating database tables if needed");
        }
        Connection conn = connection.get();
        txControl.required(() -> {
            Statement s = conn.createStatement();
            s.execute("CREATE SCHEMA IF NOT EXISTS sensinact;");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS sensinact.numeric_data ( time TIMESTAMPTZ NOT NULL, model VARCHAR(128) NOT NULL, provider VARCHAR(128) NOT NULL, service VARCHAR(128) NOT NULL, resource VARCHAR(128) NOT NULL, data NUMERIC )");
            s.execute("SELECT create_hypertable('sensinact.numeric_data', 'time');");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS sensinact.text_data ( time TIMESTAMPTZ NOT NULL, model VARCHAR(128) NOT NULL, provider VARCHAR(128) NOT NULL, service VARCHAR(128) NOT NULL, resource VARCHAR(128) NOT NULL, data VARCHAR(512) )");
            s.execute("SELECT create_hypertable('sensinact.text_data', 'time');");
            s.execute("CREATE EXTENSION Postgis;");
            s.execute(
                    "CREATE TABLE IF NOT EXISTS sensinact.geo_data ( time TIMESTAMPTZ NOT NULL, model VARCHAR(128) NOT NULL, provider VARCHAR(128) NOT NULL, service VARCHAR(128) NOT NULL, resource VARCHAR(128) NOT NULL, data geography(POINT,4326) )");
            s.execute("SELECT create_hypertable('sensinact.geo_data', 'time');");
            return null;
        });
        if (logger.isDebugEnabled()) {
            logger.debug("Database tables created");
        }
    }

    private void registerListener(BundleContext ctx) {
        ServiceRegistration<?> reg;
        synchronized (this) {
            reg = this.reg;
            this.reg = null;
        }
        if (reg == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Registering listener for data update events");
            }
            reg = ctx.registerService(TypedEventHandler.class, this,
                    new Hashtable<>(Map.of(TYPED_EVENT_TOPICS, "DATA/*")));
            synchronized (this) {
                if (this.reg == null) {
                    this.reg = reg;
                    reg = null;
                }
            }
            safeUnregister(reg);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Listener is already registered for data update events");
            }
        }
    }

    @Override
    public void notify(String topic, ResourceDataNotification event) {

        if (logger.isDebugEnabled()) {
            logger.debug("Update received for topic {} and the data will be stored", topic);
        }

        String command;
        Object value;

        if (isGeographic(event)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Event is geographic");
            }
            command = String.format(INSERT_TEMPLATE, "sensinact.geo_data",
                    "(SELECT ST_GeomFromGeoJSON( ? )::geography)");
            String tmpValue;
            if (event.newValue == null) {
                tmpValue = "{\"type\":\"Point\", \"coordinates\":[]}";
            } else if (event.newValue instanceof GeoJsonObject) {
                try {
                    tmpValue = mapper.writeValueAsString(event.newValue);
                } catch (JsonProcessingException e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Unable to serialize geographic data for {}", topic, e);
                    }
                    return;
                }
            } else {
                tmpValue = event.newValue.toString();
            }
            value = tmpValue;
        } else if (isNumber(event.type)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Event is numeric");
            }
            command = String.format(INSERT_TEMPLATE, "sensinact.numeric_data", "?");
            value = event.newValue;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Event is being treated as text");
            }
            command = String.format(INSERT_TEMPLATE, "sensinact.text_data", "?");
            value = event.newValue == null ? null : event.newValue.toString();
        }
        Connection conn = connection.get();

        try {
            txControl.required(() -> {

                PreparedStatement ps = conn.prepareStatement(command);
                ps.setTimestamp(1, Timestamp.from(event.timestamp));
                ps.setString(2, event.model);
                ps.setString(3, event.provider);
                ps.setString(4, event.service);
                ps.setString(5, event.resource);
                ps.setObject(6, value);

                return ps.execute();
            });
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Unable to store data for {}", topic, e);
            }
        }

    }

    private boolean isGeographic(ResourceDataNotification event) {
        // TODO this should be type based but isn't yet
        return "admin".equals(event.service) && "location".equals(event.resource);
    }

    private static final Set<Class<?>> primitiveNumbers = Set.of(byte.class, short.class, int.class, long.class,
            float.class, double.class);

    private boolean isNumber(Class<?> type) {
        return primitiveNumbers.contains(type) || Number.class.isAssignableFrom(type);
    }
}

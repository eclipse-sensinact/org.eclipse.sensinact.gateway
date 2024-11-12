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
import java.sql.Statement;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelectorFilterFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.transaction.control.ScopedWorkException;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jdbc.JDBCConnectionProvider;
import org.osgi.service.transaction.control.jdbc.JDBCConnectionProviderFactory;
import org.osgi.service.typedevent.TypedEventHandler;
import org.osgi.service.typedevent.annotations.RequireTypedEvent;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component(service = {}, immediate = true, configurationPid = "sensinact.history.timescale", configurationPolicy = ConfigurationPolicy.REQUIRE)
@RequireTypedEvent
public class TimescaleHistoricalStore {

    private static final String NOT_SET = "<<NOT_SET>>";

    private static final Logger logger = LoggerFactory.getLogger(TimescaleHistoricalStore.class);

    public @interface Config {

        String url();

        String user() default NOT_SET;

        String _password() default NOT_SET;

        String provider() default "timescale-history";

        /**
         * @return A list of JSON encoded {@link ResourceSelector} instances
         *         used to select the resources for which history should be
         *         stored
         */
        String[] include_resources() default "{}";

        /**
         * @return A list of JSON encoded {@link ResourceSelector} instances
         *         used to exclude resources from history storage. Applies
         *         after the <code>include.resources</code> selection.
         */
        String[] exclude_resources() default {};
    }

    @Reference
    TransactionControl txControl;

    @Reference
    JDBCConnectionProviderFactory providerFactory;

    @Reference
    GatewayThread gatewayThread;

    @Reference
    ResourceSelectorFilterFactory filterFactory;

    private final ObjectMapper mapper = new ObjectMapper();

    private Config config;

    private ICriterion include;

    private ICriterion exclude;

    private JDBCConnectionProvider provider;

    private final AtomicReference<Connection> connection = new AtomicReference<>();

    private ServiceRegistration<?> reg;

    @Activate
    void start(BundleContext ctx, Config config) {
        if (logger.isDebugEnabled()) {
            logger.debug("Starting the TimescaleDB history store");
        }

        String[] resources = config.include_resources();
        if(resources.length == 0) {
            throw new IllegalArgumentException("At least one include resource selector must be set");
        }

        ICriterion includeFilter = filterFactory.parseResourceSelector(Arrays.stream(resources).map(this::fromString));

        ICriterion excludeFilter;
        resources = config.exclude_resources();
        if(resources.length == 0) {
            excludeFilter = null;
        } else {
            excludeFilter = filterFactory.parseResourceSelector(Arrays.stream(resources).map(this::fromString));
        }

        synchronized (this) {
            this.config = config;
            this.include = includeFilter;
            this.exclude = excludeFilter;
        }

        doStart(ctx);
    }

    private ResourceSelector fromString(String s) {
        try {
            return mapper.readValue(s, ResourceSelector.class);
        } catch (JsonProcessingException j) {
            throw new IllegalArgumentException("Unable to read Resource Selector " + s);
        }
    }

    void doStart(BundleContext ctx) {
        try {
            setProvider(createProvider(config));
            setupTables();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("An error occurred setting up database access", e);
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
        try {
            txControl.required(() -> {
                Statement s = conn.createStatement();
                s.execute("CREATE SCHEMA IF NOT EXISTS sensinact;");
                s.execute(
                        "CREATE TABLE IF NOT EXISTS sensinact.numeric_data ( time TIMESTAMPTZ NOT NULL, modelpackageuri VARCHAR(128) NOT NULL, model VARCHAR(128) NOT NULL, provider VARCHAR(128) NOT NULL, service VARCHAR(128) NOT NULL, resource VARCHAR(128) NOT NULL, data NUMERIC )");
                s.execute("SELECT create_hypertable('sensinact.numeric_data', 'time', if_not_exists => TRUE);");
                s.execute(
                        "CREATE TABLE IF NOT EXISTS sensinact.text_data ( time TIMESTAMPTZ NOT NULL, modelpackageuri VARCHAR(128) NOT NULL, model VARCHAR(128) NOT NULL, provider VARCHAR(128) NOT NULL, service VARCHAR(128) NOT NULL, resource VARCHAR(128) NOT NULL, data text )");
                s.execute("SELECT create_hypertable('sensinact.text_data', 'time', if_not_exists => TRUE);");
                s.execute("CREATE EXTENSION IF NOT EXISTS Postgis;");
                s.execute(
                        "CREATE TABLE IF NOT EXISTS sensinact.geo_data ( time TIMESTAMPTZ NOT NULL, modelpackageuri VARCHAR(128) NOT NULL, model VARCHAR(128) NOT NULL, provider VARCHAR(128) NOT NULL, service VARCHAR(128) NOT NULL, resource VARCHAR(128) NOT NULL, data geography(POINT,4326) )");
                s.execute("SELECT create_hypertable('sensinact.geo_data', 'time', if_not_exists => TRUE);");
                return null;
            });
        } catch (ScopedWorkException e) {
            logger.error("Error setting up history tables. The history provider might not work", e.getCause());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Database tables created");
        }
    }

    private void registerListener(BundleContext ctx) {
        ServiceRegistration<?> reg;
        ICriterion include;
        ICriterion exclude;
        synchronized (this) {
            reg = this.reg;
            this.reg = null;
            include = this.include;
            exclude = this.exclude;
        }
        if (reg == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Registering listener for data update events");
            }
            reg = ctx.registerService(TypedEventHandler.class, new TimescaleDatabaseWorker(txControl, connection::get, include, exclude),
                    new Hashtable<>(Map.of(TYPED_EVENT_TOPICS, include.dataTopics(), "sensiNact.whiteboard.resource", true,
                            "sensiNact.provider.name", config.provider())));
            synchronized (this) {
                if (this.reg == null) {
                    this.reg = reg;
                    reg = null;
                }
            }
            safeUnregister(reg);

            gatewayThread.execute(new AbstractTwinCommand<Void>() {
                @Override
                protected Promise<Void> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                    if (twin.getProvider(config.provider()) == null) {
                        twin.createProvider("https://eclipse.org/sensinact/sensiNactHistory", "sensiNactHistory", config.provider());
                    }
                    return pf.resolved(null);
                }
            });

        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Listener is already registered for data update events");
            }
        }
    }
}

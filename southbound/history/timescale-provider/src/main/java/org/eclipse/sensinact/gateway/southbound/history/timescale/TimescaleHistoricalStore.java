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
*   Data In Motion - rework onto the history storage SPI
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.history.timescale;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryStorage;
import org.eclipse.sensinact.gateway.southbound.history.timescale.TimescaleHistoryStorage.TxRunner;
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
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lifecycle of the PostgreSQL/TimescaleDB history backend: manages the
 * DataSource and tx-control provider and registers a
 * {@link TimescaleHistoryStorage} as a {@link HistoryStorage} service. The
 * history-core engine wires it to the event bus and exposes it as a
 * HistoryProvider plus the legacy ACT facade.
 *
 * PID and configuration keys are unchanged from the pre-rework provider;
 * selector changes now take effect immediately because every configuration
 * update re-registers the service with fresh properties.
 */
@Component(service = {}, immediate = true, configurationPid = "sensinact.history.timescale", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class TimescaleHistoricalStore {

    private static final String NOT_SET = "<<NOT_SET>>";

    private static final Logger logger = LoggerFactory.getLogger(TimescaleHistoricalStore.class);

    public @interface Config {

        String url();

        String user() default NOT_SET;

        String _password() default NOT_SET;

        String provider() default "timescale-history";

        /**
         * @return A list of JSON encoded {@link ResourceSelector} instances used to
         *         select the resources for which history should be stored
         */
        String[] include_resources() default "{}";

        /**
         * @return A list of JSON encoded {@link ResourceSelector} instances used to
         *         exclude resources from history storage. Applies after the
         *         <code>include.resources</code> selection.
         */
        String[] exclude_resources() default {};

        /** @return the largest page a single range query returns */
        int max_page_size() default 10_000;
    }

    @Reference
    TransactionControl txControl;

    @Reference
    JDBCConnectionProviderFactory providerFactory;

    private BundleContext context;
    private Config config;
    private JDBCConnectionProvider provider;
    private final AtomicReference<Connection> connection = new AtomicReference<>();
    private ServiceRegistration<HistoryStorage> registration;

    @Activate
    void start(BundleContext context, Config config) {
        logger.debug("Starting the TimescaleDB history store");
        if (config.include_resources().length == 0) {
            throw new IllegalArgumentException("At least one include resource selector must be set");
        }
        synchronized (this) {
            this.context = context;
            this.config = config;
        }
        doStart();
    }

    @Modified
    void update(BundleContext context, Config config) {
        Config oldConfig;
        synchronized (this) {
            oldConfig = this.config;
            this.config = config;
        }

        if (Objects.equals(oldConfig.url(), config.url()) && Objects.equals(oldConfig.user(), config.user())
                && Objects.equals(oldConfig._password(), config._password())
                && oldConfig.max_page_size() == config.max_page_size()) {
            logger.debug("Re-registering the history storage with updated properties");
            // re-registration makes the engine re-read name and selectors
            registerStorage(currentStorage());
        } else {
            logger.debug("Restarting the Timescale DB connection due to a config change");
            doStart();
        }
    }

    @Deactivate
    void stop() {
        logger.debug("Stopping the TimescaleDB history store");
        safeUnregister();
        setProvider(null);
    }

    private void doStart() {
        TimescaleHistoryStorage storage;
        try {
            setProvider(createProvider(config));
            storage = new TimescaleHistoryStorage(this::inTransaction, connection::get, config.max_page_size());
            storage.initialize();
        } catch (Exception e) {
            logger.warn("An error occurred setting up database access", e);
            safeUnregister();
            return;
        }
        registerStorage(storage);
    }

    private <T> T inTransaction(Callable<T> operation) {
        try {
            return txControl.required(operation::call);
        } catch (ScopedWorkException e) {
            throw e.asRuntimeException();
        }
    }

    private synchronized TimescaleHistoryStorage currentStorage() {
        return registration == null ? null
                : (TimescaleHistoryStorage) context.getService(registration.getReference());
    }

    private void registerStorage(TimescaleHistoryStorage storage) {
        if (storage == null) {
            return;
        }
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put(HistoryStorage.PROP_NAME, config.provider());
        properties.put(HistoryStorage.PROP_INCLUDE, config.include_resources());
        if (config.exclude_resources().length > 0) {
            properties.put(HistoryStorage.PROP_EXCLUDE, config.exclude_resources());
        }

        ServiceRegistration<HistoryStorage> previous;
        synchronized (this) {
            previous = registration;
            registration = context.registerService(HistoryStorage.class, storage, properties);
        }
        safeUnregister(previous);
        logger.info("Timescale history storage {} registered", config.provider());
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
            logger.debug("Releasing configured Timescale DB connections");
            providerFactory.releaseProvider(old);
        }
    }

    private void safeUnregister() {
        ServiceRegistration<HistoryStorage> current;
        synchronized (this) {
            current = registration;
            registration = null;
        }
        safeUnregister(current);
    }

    private static void safeUnregister(ServiceRegistration<?> registration) {
        if (registration != null) {
            try {
                registration.unregister();
            } catch (IllegalStateException e) {
                // already unregistered
            }
        }
    }
}

/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.history.core.osgi;

import static org.osgi.service.typedevent.TypedEventConstants.TYPED_EVENT_TOPICS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelectorFilterFactory;
import org.eclipse.sensinact.gateway.southbound.history.api.HistoricalQueries;
import org.eclipse.sensinact.gateway.southbound.history.core.HistoricalQueriesFacade;
import org.eclipse.sensinact.gateway.southbound.history.core.HistoryProviderEngine;
import org.eclipse.sensinact.gateway.southbound.history.core.ingest.IngestionPipeline;
import org.eclipse.sensinact.gateway.southbound.history.core.ingest.IngestionPipeline.RetiredStep;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryProvider;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryIngestFilter;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryRecordingStep;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryRecordingStepFactory;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryStorage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.typedevent.TypedEventHandler;
import org.osgi.service.typedevent.annotations.RequireTypedEvent;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Exposes every {@link HistoryStorage} service as a history provider: wires
 * its ingestion pipeline to the typed event bus, registers the
 * {@link HistoryProvider} engine and the legacy ACT facade, and manages the
 * synthetic twin provider. Filter or storage changes re-register the affected
 * pieces — configuration updates take effect without a backend restart.
 */
@Component(immediate = true)
@RequireTypedEvent
public class HistoryStorageManager {

    private static final Logger logger = LoggerFactory.getLogger(HistoryStorageManager.class);

    private static final String MODEL_PACKAGE_URI = "https://eclipse.org/sensinact/sensiNactHistory";
    private static final String MODEL = "sensiNactHistory";

    @Reference
    GatewayThread gatewayThread;

    @Reference
    ResourceSelectorFilterFactory filterFactory;

    private final ObjectMapper mapper = new ObjectMapper();

    private final Map<ServiceReference<HistoryStorage>, ManagedBackend> backends = new LinkedHashMap<>();
    private final List<ServiceReference<HistoryStorage>> pendingStorages = new ArrayList<>();
    private final List<HistoryIngestFilter> filters = new ArrayList<>();
    private final List<ServiceReference<HistoryRecordingStepFactory>> stepFactories = new ArrayList<>();

    // closes and ungets run once the lock of the manager is released: a step
    // that writes what it holds must not delay the other service events
    private final List<Runnable> releases = new ArrayList<>();

    // serialises the releases, so that a step closed by one service event
    // has written what it holds before another event releases its storage
    private final Object releaseLock = new Object();

    private BundleContext context;

    @Activate
    void activate(BundleContext context) {
        change(() -> {
            this.context = context;
            pendingStorages.forEach(this::bindStorage);
            pendingStorages.clear();
            backends.values().forEach(ManagedBackend::start);
        });
    }

    @Deactivate
    void deactivate() {
        change(() -> {
            backends.values().forEach(ManagedBackend::stop);
            backends.keySet().forEach(this::releaseStorage);
            backends.clear();
            context = null;
        });
    }

    private void releaseStorage(ServiceReference<HistoryStorage> reference) {
        BundleContext bundleContext = context;
        releases.add(() -> bundleContext.ungetService(reference));
    }

    /**
     * Applies a change under the lock of the manager, then runs the releases
     * it queued; returns once they have run, whichever thread ran them. A
     * change that queued nothing does not wait for the releases of others.
     */
    private void change(Runnable change) {
        boolean queued;
        synchronized (this) {
            change.run();
            queued = !releases.isEmpty();
        }
        // a call nested in another change, for example from a service that
        // change registered, leaves its releases to that change
        if (queued && !Thread.holdsLock(this)) {
            runReleases();
        }
    }

    private void runReleases() {
        synchronized (releaseLock) {
            List<Runnable> toRun;
            synchronized (this) {
                toRun = List.copyOf(releases);
                releases.clear();
            }
            for (Runnable release : toRun) {
                try {
                    release.run();
                } catch (RuntimeException | LinkageError e) {
                    logger.warn("The history engine failed to release a recording step or a service", e);
                }
            }
        }
    }

    /**
     * Bound by service reference, not object: on a configuration restart the
     * same backend instance may be re-registered, and the new registration
     * binds before the old one unbinds.
     */
    @Reference(service = HistoryStorage.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void addStorage(ServiceReference<HistoryStorage> reference) {
        change(() -> {
            if (context == null) {
                pendingStorages.add(reference);
                return;
            }
            bindStorage(reference);
            ManagedBackend backend = backends.get(reference);
            if (backend != null) {
                backend.start();
            }
        });
    }

    private void bindStorage(ServiceReference<HistoryStorage> reference) {
        Object name = reference.getProperty(HistoryStorage.PROP_NAME);
        if (!(name instanceof String providerName) || providerName.isBlank()) {
            logger.error("Ignoring a HistoryStorage service without the {} property", HistoryStorage.PROP_NAME);
            return;
        }
        // the newest registration of a provider name wins
        backends.entrySet().stream().filter(entry -> entry.getValue().name.equals(providerName)).findFirst()
                .ifPresent(previous -> {
                    logger.warn("Replacing the history backend {} with a newer registration", providerName);
                    backends.remove(previous.getKey());
                    previous.getValue().stop();
                    releaseStorage(previous.getKey());
                });
        HistoryStorage storage = context.getService(reference);
        if (storage == null) {
            return;
        }
        ManagedBackend backend = new ManagedBackend(providerName, storage,
                parseSelectors(reference, HistoryStorage.PROP_INCLUDE),
                parseSelectors(reference, HistoryStorage.PROP_EXCLUDE));
        backends.put(reference, backend);
    }

    void removeStorage(ServiceReference<HistoryStorage> reference) {
        change(() -> {
            pendingStorages.remove(reference);
            ManagedBackend backend = backends.remove(reference);
            if (backend != null) {
                backend.stop();
                releaseStorage(reference);
            }
        });
    }

    @Reference(service = HistoryIngestFilter.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void addFilter(HistoryIngestFilter filter) {
        change(() -> {
            filters.add(filter);
            backends.values().forEach(ManagedBackend::updateFilters);
        });
    }

    void removeFilter(HistoryIngestFilter filter) {
        change(() -> {
            filters.remove(filter);
            backends.values().forEach(ManagedBackend::updateFilters);
        });
    }

    /**
     * Bound by service reference: the factory is only fetched, and released,
     * by the backends that use it.
     */
    @Reference(service = HistoryRecordingStepFactory.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, updated = "updatedRecordingStepFactory")
    void addRecordingStepFactory(ServiceReference<HistoryRecordingStepFactory> reference) {
        change(() -> {
            stepFactories.add(reference);
            backends.values().forEach(ManagedBackend::updateRecordingStep);
        });
    }

    /** A new ranking or provider name may change the step of any backend. */
    void updatedRecordingStepFactory(ServiceReference<HistoryRecordingStepFactory> reference) {
        change(() -> backends.values().forEach(ManagedBackend::updateRecordingStep));
    }

    void removeRecordingStepFactory(ServiceReference<HistoryRecordingStepFactory> reference) {
        change(() -> {
            stepFactories.remove(reference);
            backends.values().forEach(ManagedBackend::updateRecordingStep);
        });
    }

    private ICriterion parseSelectors(ServiceReference<HistoryStorage> reference, String key) {
        Object value = reference.getProperty(key);
        String[] selectors;
        if (value instanceof String[] array) {
            selectors = array;
        } else if (value instanceof String single) {
            selectors = new String[] { single };
        } else {
            return null;
        }
        if (selectors.length == 0) {
            return null;
        }
        return filterFactory.parseResourceSelector(Arrays.stream(selectors).map(this::selectorFromJson));
    }

    private ResourceSelector selectorFromJson(String json) {
        try {
            return mapper.readValue(json, ResourceSelector.class);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Unable to read resource selector " + json, e);
        }
    }

    private List<HistoryIngestFilter> applicableFilters(String providerName) {
        return filters.stream().filter(
                filter -> filter.targets().isEmpty() || filter.targets().contains(providerName))
                .toList();
    }

    private class ManagedBackend {

        private final String name;
        private final HistoryStorage storage;
        private final IngestionPipeline pipeline;
        private final HistoryProviderEngine engine;

        private ServiceReference<HistoryRecordingStepFactory> stepFactory;
        private ServiceRegistration<?> listenerRegistration;
        private ServiceRegistration<?> providerRegistration;
        private ServiceRegistration<?> facadeRegistration;
        private Set<String> listenerTopics = Set.of();
        private boolean started;

        ManagedBackend(String name, HistoryStorage storage, ICriterion include, ICriterion exclude) {
            this.name = name;
            this.storage = storage;
            this.pipeline = new IngestionPipeline(name, storage, include, exclude);
            this.pipeline.setFilters(applicableFilters(name));
            this.engine = new HistoryProviderEngine(name, storage);
            updateRecordingStep();
        }

        /**
         * Uses the step of the best factory serving this backend that gives
         * one: the highest service ranking first; the built-in step is used
         * without any. A factory that failed is tried again on the next
         * change of the factories.
         */
        void updateRecordingStep() {
            List<ServiceReference<HistoryRecordingStepFactory>> candidates = stepFactories.stream()
                    .filter(reference -> RecordingSteps
                            .serves(reference.getProperty(HistoryRecordingStepFactory.PROP_NAME), name))
                    .sorted(Comparator.reverseOrder()).toList();
            for (ServiceReference<HistoryRecordingStepFactory> candidate : candidates) {
                if (candidate.equals(stepFactory)) {
                    // no better factory gives a step: keep the current one
                    return;
                }
                Object factoryId = candidate.getProperty(Constants.SERVICE_ID);
                HistoryRecordingStepFactory factory = context.getService(candidate);
                HistoryRecordingStep step = factory == null ? null
                        : RecordingSteps.create(factory, factoryId, name, storage);
                if (step != null) {
                    useStep(step, candidate);
                    logger.info("History provider {} records with the step of the factory service {} (ranking {})",
                            name, factoryId,
                            Objects.requireNonNullElse(candidate.getProperty(Constants.SERVICE_RANKING), 0));
                    return;
                }
                if (factory != null) {
                    BundleContext bundleContext = context;
                    releases.add(() -> bundleContext.ungetService(candidate));
                }
            }
            if (stepFactory != null) {
                useStep(null, null);
                logger.info("History provider {} records with the built-in step", name);
            }
        }

        /**
         * Sets the step; the previous one is retired and closed after the
         * lock of the manager is released.
         */
        private void useStep(HistoryRecordingStep step, ServiceReference<HistoryRecordingStepFactory> factory) {
            RetiredStep previous = pipeline.replaceRecordingStep(step);
            ServiceReference<HistoryRecordingStepFactory> previousFactory = stepFactory;
            stepFactory = factory;
            BundleContext bundleContext = context;
            releases.add(() -> {
                try {
                    RecordingSteps.close(previous.retire(), name);
                } finally {
                    if (previousFactory != null) {
                        bundleContext.ungetService(previousFactory);
                    }
                }
            });
        }

        @SuppressWarnings("deprecation")
        void start() {
            registerListener();

            providerRegistration = context.registerService(HistoryProvider.class, engine,
                    new Hashtable<>(Map.of(HistoryProvider.PROP_NAME, name)));
            facadeRegistration = context.registerService(HistoricalQueries.class,
                    new HistoricalQueriesFacade(engine),
                    new Hashtable<>(Map.of("sensiNact.whiteboard.resource", true, "sensiNact.provider.name", name)));

            gatewayThread.execute(new AbstractTwinCommand<Void>() {
                @Override
                protected Promise<Void> call(SensinactDigitalTwin twin, PromiseFactory promiseFactory) {
                    if (twin.getProvider(name) == null) {
                        twin.createProvider(MODEL_PACKAGE_URI, MODEL, name);
                    }
                    return promiseFactory.resolved(null);
                }
            });
            started = true;
            logger.info("History provider {} is online", name);
        }

        void updateFilters() {
            pipeline.setFilters(applicableFilters(name));
            if (started && !pipeline.topics().equals(listenerTopics)) {
                registerListener();
            }
        }

        private void registerListener() {
            ServiceRegistration<?> previous = listenerRegistration;
            Set<String> topics = pipeline.topics();
            // a real class, not a lambda: the typed event bus reifies the
            // event type from the handler's generic signature
            listenerRegistration = context.registerService(TypedEventHandler.class,
                    new PipelineEventHandler(pipeline),
                    new Hashtable<>(Map.of(TYPED_EVENT_TOPICS, topics.toArray(String[]::new))));
            listenerTopics = topics;
            safeUnregister(previous);
        }

        void stop() {
            started = false;
            Stream.of(listenerRegistration, providerRegistration, facadeRegistration)
                    .forEach(HistoryStorageManager::safeUnregister);
            listenerRegistration = null;
            providerRegistration = null;
            facadeRegistration = null;

            // a late record goes to the storage, before the replaced step
            // writes what it holds and before the storage is released; without
            // a factory, nothing changes
            if (stepFactory != null) {
                useStep(null, null);
            }

            gatewayThread.execute(new AbstractTwinCommand<Void>() {
                @Override
                protected Promise<Void> call(SensinactDigitalTwin twin, PromiseFactory promiseFactory) {
                    SensinactProvider provider = twin.getProvider(name);
                    if (provider != null) {
                        provider.delete();
                    }
                    return promiseFactory.resolved(null);
                }
            });
            logger.info("History provider {} is offline", name);
        }
    }

    private static class PipelineEventHandler implements TypedEventHandler<ResourceDataNotification> {

        private final IngestionPipeline pipeline;

        PipelineEventHandler(IngestionPipeline pipeline) {
            this.pipeline = pipeline;
        }

        @Override
        public void notify(String topic, ResourceDataNotification event) {
            pipeline.handle(event);
        }
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

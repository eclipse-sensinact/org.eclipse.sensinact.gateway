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
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryProvider;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryIngestFilter;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryStorage;
import org.osgi.framework.BundleContext;
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

    private final Map<HistoryStorage, ManagedBackend> backends = new LinkedHashMap<>();
    private final List<HistoryIngestFilter> filters = new ArrayList<>();

    private BundleContext context;

    @Activate
    synchronized void activate(BundleContext context) {
        this.context = context;
        backends.values().forEach(ManagedBackend::start);
    }

    @Deactivate
    synchronized void deactivate() {
        backends.values().forEach(ManagedBackend::stop);
        context = null;
    }

    @Reference(service = HistoryStorage.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    synchronized void addStorage(HistoryStorage storage, Map<String, Object> properties) {
        Object name = properties.get(HistoryStorage.PROP_NAME);
        if (!(name instanceof String providerName) || providerName.isBlank()) {
            logger.error("Ignoring a HistoryStorage service without the {} property", HistoryStorage.PROP_NAME);
            return;
        }
        if (backends.values().stream().anyMatch(backend -> backend.name.equals(providerName))) {
            logger.error("Ignoring a HistoryStorage service with duplicate provider name {}", providerName);
            return;
        }
        ManagedBackend backend = new ManagedBackend(providerName, storage, parseSelectors(properties,
                HistoryStorage.PROP_INCLUDE), parseSelectors(properties, HistoryStorage.PROP_EXCLUDE));
        backends.put(storage, backend);
        if (context != null) {
            backend.start();
        }
    }

    synchronized void removeStorage(HistoryStorage storage) {
        ManagedBackend backend = backends.remove(storage);
        if (backend != null) {
            backend.stop();
        }
    }

    @Reference(service = HistoryIngestFilter.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    synchronized void addFilter(HistoryIngestFilter filter) {
        filters.add(filter);
        backends.values().forEach(ManagedBackend::updateFilters);
    }

    synchronized void removeFilter(HistoryIngestFilter filter) {
        filters.remove(filter);
        backends.values().forEach(ManagedBackend::updateFilters);
    }

    private ICriterion parseSelectors(Map<String, Object> properties, String key) {
        Object value = properties.get(key);
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
        private final IngestionPipeline pipeline;
        private final HistoryProviderEngine engine;

        private ServiceRegistration<?> listenerRegistration;
        private ServiceRegistration<?> providerRegistration;
        private ServiceRegistration<?> facadeRegistration;
        private Set<String> listenerTopics = Set.of();
        private boolean started;

        ManagedBackend(String name, HistoryStorage storage, ICriterion include, ICriterion exclude) {
            this.name = name;
            this.pipeline = new IngestionPipeline(name, storage, include, exclude);
            this.pipeline.setFilters(applicableFilters(name));
            this.engine = new HistoryProviderEngine(name, storage);
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

/*********************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Kentyou - initial implementation
 **********************************************************************/
package org.eclipse.sensinact.southbound.rules.impl;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.sensinact.southbound.rules.api.RuleDefinition.RULE_NAME_PROPERTY;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.metrics.IMetricMeter;
import org.eclipse.sensinact.core.metrics.IMetricTimer;
import org.eclipse.sensinact.core.metrics.IMetricsManager;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.southbound.rules.api.ResourceUpdater;
import org.eclipse.sensinact.southbound.rules.api.RuleDefinition;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.typedevent.TypedEventConstants;
import org.osgi.service.typedevent.TypedEventHandler;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleProcessor implements TypedEventHandler<ResourceDataNotification> {

    private static Logger LOG = LoggerFactory.getLogger(RuleProcessor.class);

    private final GatewayThread gateway;
    private final IMetricsManager metrics;
    private final PromiseFactory promiseFactory;
    private final ResourceUpdater updater;
    private final RuleDefinition rd;

    private final ICriterion criterion;
    private final Predicate<ResourceDataNotification> filter;
    private final String ruleName;

    private final IMetricMeter eventDelivery;
    private final IMetricMeter eventRejection;
    private final String timerName;

    private final ServiceRegistration<?> reg;

    private final Object lock = new Object();

    // We always start in a working state to initialise the snapshot
    private boolean working = true;
    private boolean closed;
    private Deque<ResourceDataNotification> unchecked = new ArrayDeque<>(128);
    private Map<String, ProviderSnapshot> map = Map.of();

    public RuleProcessor(BundleContext context, GatewayThread gateway,
            IMetricsManager metrics, PromiseFactory promiseFactory, ResourceUpdater updater,
            RuleDefinition rd, Map<String, Object> properties) {
        this.gateway = gateway;
        this.metrics = metrics;
        this.promiseFactory = promiseFactory;
        this.updater = updater;
        this.rd = rd;

        this.criterion = rd.getInputFilter();
        this.filter = criterion.dataEventFilter();
        this.ruleName = String.valueOf(properties.getOrDefault(RULE_NAME_PROPERTY, "unnamed_rule"));

        String sanitizedMetricPrefix = "sensinact.rules." + ruleName.replaceAll("\\s", "_");
        this.eventDelivery = metrics.getMeter(sanitizedMetricPrefix + ".delivery");
        this.eventRejection = metrics.getMeter(sanitizedMetricPrefix + ".rejection");
        this.timerName = sanitizedMetricPrefix + ".execution";

        reg = context.registerService(TypedEventHandler.class, this,
                new Hashtable<>(Map.of(TypedEventConstants.TYPED_EVENT_TOPICS, criterion.dataTopics())));

        updateSnapshot(1);
    }

    @Override
    public void notify(String topic, ResourceDataNotification event) {
        try {
            eventDelivery.mark();
            if(filter.test(event)) {
                if(LOG.isDebugEnabled()) {
                    LOG.debug("Rule {} received data event on topic {}", ruleName, topic);
                }
                boolean triggerUpdate;
                synchronized (lock) {
                    // If we're currently working then add the event to be checked later
                    if(closed) {
                        triggerUpdate = false;
                    } else if(working) {
                        unchecked.add(event);
                        triggerUpdate = false;
                    } else {
                        working = checkEventAgainstSnapshot(event);
                        triggerUpdate = working;
                        if(triggerUpdate) {
                            unchecked.clear();
                        }
                    }
                }
                if(triggerUpdate) {
                    if(LOG.isDebugEnabled()) {
                        LOG.debug("Updating snapshot data for rule", ruleName);
                    }
                    updateSnapshot(1);
                }
            } else {
                eventRejection.mark();
            }
        } catch (Exception e) {
            LOG.error("An error occurred processing an event on topic {}", topic, e);
        }
    }

    /**
     * Must be called while synchronized on {@link #lock}
     * @param event
     * @return
     */
    private boolean checkEventAgainstSnapshot(ResourceDataNotification event) {
        boolean update = true;
        if(closed) {
            return false;
        }

        ProviderSnapshot p = map.get(event.provider());
        if(p != null) {
            ResourceSnapshot r = p.getResource(event.service(), event.resource());
            if(r != null && r.isSet()) {
                TimedValue<?> tv = r.getValue();
                Instant snapshot = tv.getTimestamp();
                if(snapshot.isAfter(event.timestamp())) {
                    if(LOG.isDebugEnabled()) {
                        LOG.debug("Existing snapshot for data {}/{}/{} is newer than event {}",
                                event.provider(), event.service(), event.resource(), event.timestamp());
                    }
                    update = false;
                } else if (snapshot.equals(event.timestamp()) && Objects.equals(tv.getValue(), event.newValue())) {
                    // Check the metadata
                    Map<String, Object> snapshotMeta = cleanMetadataMap(r.getMetadata());
                    Map<String, Object> eventMeta = cleanMetadataMap(event.metadata());
                    if(snapshotMeta.equals(eventMeta)) {
                        if(LOG.isDebugEnabled()) {
                            LOG.debug("Existing snapshot for data {}/{}/{} is up to date",
                                    event.provider(), event.service(), event.resource());
                        }
                        update = false;
                    }
                }
            }
        }
        return update;
    }

    private Map<String, Object> cleanMetadataMap(Map<String, Object> map) {
        Set<String> forbidden = Set.of("timestamp", "value");
        return map.entrySet().stream()
                .filter(e -> !forbidden.contains(e.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private void updateSnapshot(int attempt) {
        // Always use our workers, don't steal the gateway thread
        promiseFactory.resolvedWith(gateway.execute(new AbstractSensinactCommand<List<ProviderSnapshot>>() {
            @Override
            protected Promise<List<ProviderSnapshot>> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                    PromiseFactory promiseFactory) {
                return promiseFactory.resolved(twin.filteredSnapshot(criterion.getLocationFilter(), criterion.getProviderFilter(),
                        criterion.getServiceFilter(), criterion.getResourceFilter()));
            }
        })).thenAccept(this::snapshotUpdate)
        .onFailure(t -> snapshotUpdateFailed(t, attempt));
    }

    private void snapshotUpdate(List<ProviderSnapshot> fromGateway) {
        ResourceValueFilter rvf = criterion.getResourceValueFilter();
        List<ProviderSnapshot> list = fromGateway.stream()
                .filter(p -> rvf == null || rvf.test(p, p.getServices().stream()
                    .flatMap(s -> s.getResources().stream())
                    .collect(toList())))
                .collect(toList());
        Map<String, ProviderSnapshot> map = list.stream()
                .collect(toMap(ProviderSnapshot::getName, Function.identity()));

        synchronized (lock) {
            if(closed) {
                return;
            }
            this.map = map;
        }

        try(IMetricTimer timer = metrics.withTimer(timerName)) {
            rd.evaluate(list, updater);
        } catch(Throwable t) {
            LOG.error("An error occurred executing the rule {}", ruleName, t);
        }

        boolean triggerUpdate = false;
        synchronized (lock) {
            working = false;
            while(!unchecked.isEmpty()) {
                // Check the latest first as its the most likely to trigger an update
                working = checkEventAgainstSnapshot(unchecked.pollLast());
                if(working) {
                    triggerUpdate = true;
                    unchecked.clear();
                }
            }
            if(closed) {
                triggerUpdate = false;
            }
        }
        if(triggerUpdate) {
            updateSnapshot(1);
        }
    }

    private void snapshotUpdateFailed(Throwable t, int attempt) {
        if(attempt >=6) {
            LOG.error("Failed to update the provider snapshots for rule {}. Abandoning this rule");
            close();
        }

        LOG.error("Failed to update the provider snapshots for rule {}. Retrying", ruleName, t);
        synchronized (lock) {
            if(closed) {
                return;
            }
            // Clear any pending checks as we're updating again anyway
            unchecked.clear();
        }
        updateSnapshot(attempt + 1);
    }

    public void close() {
        synchronized (lock) {
            closed = true;
            unchecked.clear();
            map = Map.of();
        }
        try {
            reg.unregister();
        } catch (Exception e) {
            // Swallow it
        }
    }
}

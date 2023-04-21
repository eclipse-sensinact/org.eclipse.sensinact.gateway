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
package org.eclipse.sensinact.prototype.impl;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sensinact.prototype.ProviderDescription;
import org.eclipse.sensinact.prototype.ResourceDescription;
import org.eclipse.sensinact.prototype.ResourceShortDescription;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.ServiceDescription;
import org.eclipse.sensinact.prototype.command.AbstractTwinCommand;
import org.eclipse.sensinact.prototype.command.GatewayThread;
import org.eclipse.sensinact.prototype.command.ResourceCommand;
import org.eclipse.sensinact.prototype.model.ResourceType;
import org.eclipse.sensinact.prototype.model.ValueType;
import org.eclipse.sensinact.prototype.notification.AbstractResourceNotification;
import org.eclipse.sensinact.prototype.notification.ClientActionListener;
import org.eclipse.sensinact.prototype.notification.ClientDataListener;
import org.eclipse.sensinact.prototype.notification.ClientLifecycleListener;
import org.eclipse.sensinact.prototype.notification.ClientMetadataListener;
import org.eclipse.sensinact.prototype.notification.LifecycleNotification;
import org.eclipse.sensinact.prototype.notification.ResourceActionNotification;
import org.eclipse.sensinact.prototype.notification.ResourceDataNotification;
import org.eclipse.sensinact.prototype.notification.ResourceMetaDataNotification;
import org.eclipse.sensinact.prototype.security.UserInfo;
import org.eclipse.sensinact.prototype.snapshot.ICriterion;
import org.eclipse.sensinact.prototype.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.prototype.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.prototype.twin.SensinactResource;
import org.eclipse.sensinact.prototype.twin.TimedValue;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.osgi.util.promise.Promises;

public class SensiNactSessionImpl implements SensiNactSession {

    private final Object lock = new Object();

    private final String sessionId = UUID.randomUUID().toString();

    private final Map<String, List<String>> listenerRegistrations = new HashMap<>();

    private final NavigableMap<String, List<SessionListenerRegistration>> listenersByWildcardTopic = new TreeMap<>();
    private final Map<String, List<SessionListenerRegistration>> listenersByTopic = new HashMap<>();

    private Instant expiry;

    private boolean expired;

    private final GatewayThread thread;

    public SensiNactSessionImpl(final GatewayThread thread) {
        expiry = Instant.now().plusSeconds(600);
        this.thread = thread;
    }

    @Override
    public Map<String, List<String>> activeListeners() {
        synchronized (lock) {
            return new HashMap<>(listenerRegistrations);
        }
    }

    @Override
    public String addListener(List<String> topics, ClientDataListener cdl, ClientMetadataListener cml,
            ClientLifecycleListener cll, ClientActionListener cal) {
        String subscriptionId = UUID.randomUUID().toString();

        synchronized (lock) {

            if (cdl != null) {
                addListenerTopic(topics, "DATA/", new SessionDataListener(subscriptionId, cdl));
            }

            if (cml != null) {
                addListenerTopic(topics, "METADATA/", new SessionMetadataListener(subscriptionId, cml));
            }

            if (cll != null) {
                addListenerTopic(topics, "LIFECYCLE/", new SessionLifecycleListener(subscriptionId, cll));
            }

            if (cal != null) {
                addListenerTopic(topics, "ACTION/", new SessionActionListener(subscriptionId, cal));
            }

            listenerRegistrations.put(subscriptionId, List.copyOf(topics));
        }

        return subscriptionId;
    }

    /**
     * Must be called holding {@link #lock}
     *
     * @param topics
     * @param prefix
     * @param reg
     */
    private void addListenerTopic(List<String> topics, String prefix, SessionListenerRegistration reg) {
        topics.stream().map(prefix::concat).forEach(s -> {
            if (s.endsWith("*")) {
                listenersByWildcardTopic.merge(s.substring(0, s.length() - 1), List.of(reg), this::mergeLists);
            } else {
                listenersByTopic.merge(s, List.of(reg), this::mergeLists);
            }
        });
    }

    private List<SessionListenerRegistration> mergeLists(List<SessionListenerRegistration> a,
            List<SessionListenerRegistration> b) {
        return Stream.of(a, b).flatMap(List::stream).collect(toList());
    }

    @Override
    public void removeListener(String id) {
        synchronized (lock) {
            List<String> topics = listenerRegistrations.remove(id);

            if (topics != null) {
                removeListener(id, "DATA/", topics);
                removeListener(id, "METADATA/", topics);
                removeListener(id, "LIFECYCLE/", topics);
                removeListener(id, "ACTION/", topics);
            }
        }
    }

    private void removeListener(String subscriptionId, String prefix, List<String> topics) {
        topics.stream().map(prefix::concat).forEach(s -> {
            if (s.endsWith("*")) {
                listenersByWildcardTopic.computeIfPresent(s.substring(0, s.length() - 1),
                        (topic, regs) -> this.removeSubscription(subscriptionId, regs));
            } else {
                listenersByTopic.computeIfPresent(s, (topic, regs) -> this.removeSubscription(subscriptionId, regs));
            }
        });
    }

    /**
     * Must be called holding {@link #lock}
     *
     * @param subscriptionId
     * @param regs
     */
    private List<SessionListenerRegistration> removeSubscription(String subscriptionId,
            List<SessionListenerRegistration> regs) {
        List<SessionListenerRegistration> list = regs.stream().filter(r -> !r.subscriptionId.equals(subscriptionId))
                .collect(toList());

        return list.isEmpty() ? null : list;
    }

    private <I, T> T executeGetCommand(Function<SensinactDigitalTwin, I> caller, Function<I, T> converter) {
        return executeGetCommand(caller, converter, null);
    }

    private <I, T> T executeGetCommand(Function<SensinactDigitalTwin, I> caller, Function<I, T> converter,
            T defaultValue) {
        return safeExecute(new AbstractTwinCommand<T>() {
            @Override
            protected Promise<T> call(SensinactDigitalTwin model, PromiseFactory pf) {
                I modelValue = caller.apply(model);
                T value;
                if (modelValue != null) {
                    value = converter.apply(modelValue);
                } else {
                    value = defaultValue;
                }
                return pf.resolved(value);
            }
        });
    }

    private <T> T safeExecute(AbstractTwinCommand<T> command) {
        return safeGetValue(thread.execute(command));
    }

    private <T> T safeGetValue(Promise<T> promise) {
        try {
            return promise.getValue();
        } catch (InvocationTargetException ite) {
            // Re-throw cause as a runtime exception
            throw new RuntimeException(ite.getCause());
        } catch (InterruptedException e) {
            // Re-throw as a runtime exception
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T getResourceValue(String provider, String service, String resource, Class<T> clazz) {
        return safeGetValue(executeGetCommand(m -> m.getResource(provider, service, resource),
                r -> r.getValue().map(t -> clazz.cast(t.getValue())), Promises.resolved(null)));
    }

    @Override
    public void setResourceValue(String provider, String service, String resource, Object o) {
        setResourceValue(provider, service, resource, o, Instant.now());
    }

    @Override
    public void setResourceValue(String provider, String service, String resource, Object o, Instant instant) {
        safeExecute(new ResourceCommand<Void>(provider, service, resource) {
            @Override
            protected Promise<Void> call(SensinactResource resource, PromiseFactory pf) {
                return resource.setValue(o, instant);
            }
        });
    }

    @Override
    public Map<String, Object> getResourceMetadata(String provider, String service, String resource) {

        return Map.copyOf(safeExecute(new ResourceCommand<Map<String, Object>>(provider, service, resource) {
            @Override
            protected Promise<Map<String, Object>> call(SensinactResource resource, PromiseFactory pf) {
                return resource.getMetadataValues();
            }
        }));
    }

    @Override
    public void setResourceMetadata(String provider, String service, String resource, Map<String, Object> metadata) {
        final Instant timestamp = Instant.now();
        safeExecute(new ResourceCommand<Void>(provider, service, resource) {
            @Override
            protected Promise<Void> call(SensinactResource resource, PromiseFactory pf) {
                return pf.all(metadata.entrySet().stream()
                        .map(e -> resource.setMetadataValue(e.getKey(), e.getValue(), timestamp))
                        .collect(Collectors.toList())).map(x -> null);
            }
        });
    }

    @Override
    public Map<String, Object> getResourceMetadataValue(String provider, String service, String resource,
            String metadata) {
        return safeExecute(new ResourceCommand<Map<String, Object>>(provider, service, resource) {
            @Override
            protected Promise<Map<String, Object>> call(SensinactResource resource, PromiseFactory pf) {
                return resource.getMetadataValues();
            }
        });
    }

    @Override
    public void setResourceMetadata(String provider, String service, String resource, String metadata, Object value) {
        final Instant timestamp = Instant.now();
        safeExecute(new ResourceCommand<Void>(provider, service, resource) {
            @Override
            protected Promise<Void> call(SensinactResource resource, PromiseFactory pf) {
                return resource.setMetadataValue(metadata, value, timestamp);
            }
        });
    }

    @Override
    public Object actOnResource(String provider, String service, String resource, Map<String, Object> parameters) {
        return safeExecute(new ResourceCommand<Object>(provider, service, resource) {
            @Override
            protected Promise<Object> call(SensinactResource resource, PromiseFactory pf) {
                return resource.act(parameters);
            }
        });
    }

    @Override
    public ResourceDescription describeResource(String provider, String service, String resource) {
        // Avoid ResourceCommand as we return null on missing resources, not fail
        return safeExecute(new AbstractTwinCommand<ResourceDescription>() {
            @Override
            protected Promise<ResourceDescription> call(SensinactDigitalTwin model, PromiseFactory pf) {
                final SensinactResource sensinactResource = model.getResource(provider, service, resource);
                if (sensinactResource != null) {
                    ResourceType resourceType = sensinactResource.getResourceType();
                    final Promise<TimedValue<?>> val = resourceType == ResourceType.ACTION ? pf.resolved(null)
                            : sensinactResource.getValue();
                    final Promise<Map<String, Object>> metadata = sensinactResource.getMetadataValues();

                    return val.then(x -> metadata).then(x -> {
                        ResourceDescription result = new ResourceDescription();
                        result.provider = provider;
                        result.service = service;
                        result.resource = resource;
                        if (resourceType != ResourceType.ACTION) {
                            result.value = val.getValue().getValue();
                            result.timestamp = val.getValue().getTimestamp();
                        }
                        // TODO - how do we say that this is an action and list the parameters?
                        result.metadata = metadata.getValue();
                        return pf.resolved(result);
                    });
                } else {
                    return pf.resolved(null);
                }
            }
        });
    }

    @Override
    public ResourceShortDescription describeResourceShort(String provider, String service, String resource) {
        return executeGetCommand((m) -> m.getResource(provider, service, resource), (rc) -> {
            final ResourceShortDescription result = new ResourceShortDescription();
            result.contentType = rc.getType();
            result.name = rc.getName();
            result.resourceType = rc.getResourceType();
            if (result.resourceType == ResourceType.ACTION) {
                result.actMethodArgumentsTypes = rc.getArguments();
            } else {
                // TODO: get it from the description
                result.valueType = ValueType.UPDATABLE;
            }
            return result;
        });
    }

    @Override
    public ServiceDescription describeService(String provider, String service) {
        return executeGetCommand((m) -> m.getService(provider, service), (snSvc) -> {
            final ServiceDescription description = new ServiceDescription();
            description.service = snSvc.getName();
            description.provider = snSvc.getProvider().getName();
            description.resources = new ArrayList<>(snSvc.getResources().keySet());
            return description;
        });
    }

    @Override
    public ProviderDescription describeProvider(String provider) {
        return executeGetCommand((m) -> m.getProvider(provider), (snProvider) -> {
            final ProviderDescription description = new ProviderDescription();
            description.provider = snProvider.getName();
            description.services = new ArrayList<>(snProvider.getServices().keySet());
            return description;
        });
    }

    @Override
    public List<ProviderDescription> listProviders() {
        return executeGetCommand((m) -> m.getProviders(), (providers) -> providers.stream().map((snProvider) -> {
            final ProviderDescription description = new ProviderDescription();
            description.provider = snProvider.getName();
            description.services = new ArrayList<>(snProvider.getServices().keySet());
            return description;
        }).collect(Collectors.toList()), List.of());
    }

    @Override
    public List<ProviderSnapshot> filteredSnapshot(ICriterion filter) {
        if (filter == null) {
            return executeGetCommand((m) -> m.filteredSnapshot(null, null, null, null), Function.identity());
        } else {
            return executeGetCommand((m) -> m.filteredSnapshot(filter.getLocationFilter(), filter.getProviderFilter(),
                    filter.getServiceFilter(), filter.getResourceFilter()), Function.identity());
        }
    }

    public void notify(String topic, AbstractResourceNotification event) {
        List<SessionListenerRegistration> toNotify;
        synchronized (lock) {
            if (!isExpired()) {
                toNotify = new ArrayList<>();
                toNotify.addAll(listenersByTopic.getOrDefault(topic, List.of()));
                for (Entry<String, List<SessionListenerRegistration>> e : listenersByWildcardTopic.headMap(topic, true)
                        .descendingMap().entrySet()) {
                    if (topic.startsWith(e.getKey())) {
                        toNotify.addAll(e.getValue());
                    } else {
                        break;
                    }
                }
            } else {
                toNotify = List.of();
            }
        }

        toNotify.forEach(s -> s.notify(topic, event));
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public boolean isExpired() {
        synchronized (lock) {
            if (!expired && !expiry.isAfter(Instant.now())) {
                expired = true;
            }
            return expired;
        }
    }

    @Override
    public Instant getExpiry() {
        synchronized (lock) {
            return isExpired() ? null : expiry;
        }
    }

    @Override
    public void extend(Duration duration) {
        if (duration.isNegative() || duration.isZero()) {
            throw new IllegalArgumentException("The extension period must be greater than zero");
        }
        synchronized (lock) {
            checkWithException();
            expiry = Instant.now().plus(duration);
        }
    }

    public void expire() {
        synchronized (lock) {
            expired = true;
        }
    }

    private void checkWithException() {
        if (isExpired()) {
            throw new IllegalStateException("Session is expired");
        }
    }

    private static abstract class SessionListenerRegistration {

        private final String subscriptionId;

        public SessionListenerRegistration(String subscriptionId) {
            this.subscriptionId = subscriptionId;
        }

        public abstract void notify(String topic, AbstractResourceNotification notification);

    }

    private static class SessionLifecycleListener extends SessionListenerRegistration {

        private final ClientLifecycleListener listener;

        public SessionLifecycleListener(String subscriptionId, ClientLifecycleListener listener) {
            super(subscriptionId);
            this.listener = listener;
        }

        public void notify(String topic, AbstractResourceNotification notification) {
            listener.notify(topic, (LifecycleNotification) notification);
        }
    }

    private static class SessionMetadataListener extends SessionListenerRegistration {

        private final ClientMetadataListener listener;

        public SessionMetadataListener(String subscriptionId, ClientMetadataListener listener) {
            super(subscriptionId);
            this.listener = listener;
        }

        public void notify(String topic, AbstractResourceNotification notification) {
            listener.notify(topic, (ResourceMetaDataNotification) notification);
        }
    }

    private static class SessionDataListener extends SessionListenerRegistration {

        private final ClientDataListener listener;

        public SessionDataListener(String subscriptionId, ClientDataListener listener) {
            super(subscriptionId);
            this.listener = listener;
        }

        public void notify(String topic, AbstractResourceNotification notification) {
            listener.notify(topic, (ResourceDataNotification) notification);
        }
    }

    private static class SessionActionListener extends SessionListenerRegistration {

        private final ClientActionListener listener;

        public SessionActionListener(String subscriptionId, ClientActionListener listener) {
            super(subscriptionId);
            this.listener = listener;
        }

        public void notify(String topic, AbstractResourceNotification notification) {
            listener.notify(topic, (ResourceActionNotification) notification);
        }
    }

    @Override
    public UserInfo getUserInfo() {
        // TODO Auto-generated method stub
        return null;
    }
}

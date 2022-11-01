/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
import java.util.stream.Stream;

import org.eclipse.sensinact.prototype.ProviderDescription;
import org.eclipse.sensinact.prototype.ResourceDescription;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.ServiceDescription;
import org.eclipse.sensinact.prototype.notification.AbstractResourceNotification;
import org.eclipse.sensinact.prototype.notification.ClientActionListener;
import org.eclipse.sensinact.prototype.notification.ClientDataListener;
import org.eclipse.sensinact.prototype.notification.ClientLifecycleListener;
import org.eclipse.sensinact.prototype.notification.ClientMetadataListener;
import org.eclipse.sensinact.prototype.notification.LifecycleNotification;
import org.eclipse.sensinact.prototype.notification.ResourceActionNotification;
import org.eclipse.sensinact.prototype.notification.ResourceDataNotification;
import org.eclipse.sensinact.prototype.notification.ResourceMetaDataNotification;

public class SensiNactSessionImpl implements SensiNactSession {

    private final Object lock = new Object();

    private final String sessionId = UUID.randomUUID().toString();

    private final Map<String, List<String>> listenerRegistrations = new HashMap<>();

    private final NavigableMap<String, List<SessionListenerRegistration>> listenersByWildcardTopic = new TreeMap<>();
    private final Map<String, List<SessionListenerRegistration>> listenersByTopic = new HashMap<>();

    private Instant expiry;

    private boolean expired;

    public SensiNactSessionImpl() {
        expiry = Instant.now().plusSeconds(600);
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
                removeListener("DATA/", topics);
                removeListener("METADATA/", topics);
                removeListener("LIFECYCLE/", topics);
                removeListener("ACTION/", topics);
            }
        }
    }

    private void removeListener(String prefix, List<String> topics) {
        topics.stream().map(prefix::concat).forEach(s -> {
            if (s.endsWith("*")) {
                listenersByWildcardTopic.computeIfPresent(s.substring(0, s.length() - 1), this::removeSubscription);
            } else {
                listenersByTopic.computeIfPresent(s, this::removeSubscription);
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

    @Override
    public <T> T getResourceValue(String provider, String service, String resource, Class<T> clazz) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setResourceValue(String provider, String service, String resource, Object o) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setResourceValue(String provider, String service, String resource, Object o, Instant instant) {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<String, Object> getResourceMetadata(String provider, String service, String resource) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setResourceMetadata(String provider, String service, String resource, Map<String, Object> metadata) {
        // TODO Auto-generated method stub

    }

    @Override
    public Map<String, Object> getResourceMetadataValue(String provider, String service, String resource,
            String metadata) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setResourceMetadata(String provider, String service, String resource, String metadata, Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object actOnResource(String provider, String service, String resource, Object[] parameters) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResourceDescription describeResource(String provider, String service, String resource) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServiceDescription describeService(String provider, String service) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProviderDescription describeProvider(String provider) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ProviderDescription> listProviders() {
        // TODO Auto-generated method stub
        return null;
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
}

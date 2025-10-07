/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.northbound.ws.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.ValueType;
import org.eclipse.sensinact.core.notification.ResourceNotification;
import org.eclipse.sensinact.core.notification.LifecycleNotification;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.snapshot.LinkedProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;

/**
 * Utility class to hold the snapshots made from a notification
 */
class NotificationSnapshot {
    /**
     * Provider snapshot
     */
    public ProviderSnapshot provider;

    /**
     * Service snapshot
     */
    public ServiceSnapshot service;

    /**
     * Resource snapshot
     */
    public ResourceSnapshot resource;

    /**
     * Value timestamp
     */
    private final Instant timestamp;

    /**
     * Parent notification
     */
    private final ResourceNotification notification;

    /**
     * Creates a snapshot from the given life cycle notification
     *
     * @param notif Life cycle notification
     */
    public NotificationSnapshot(final LifecycleNotification notif) {
        notification = notif;
        timestamp = Instant.now();
        provider = new ProviderSnapshotImpl();
        service = new ServiceSnapshotImpl();
        resource = new ResourceSnapshotImpl(notif.initialValue());
    }

    /**
     * Creates a snapshot from the given resource data notification
     *
     * @param notif Resource data update notification
     */
    public NotificationSnapshot(final ResourceDataNotification notif) {
        notification = notif;
        timestamp = notif.timestamp();
        provider = new ProviderSnapshotImpl();
        service = new ServiceSnapshotImpl();
        resource = new ResourceSnapshotImpl(notif.newValue());
    }

    @Override
    public String toString() {
        return resource.toString();
    }

    private class ProviderSnapshotImpl implements ProviderSnapshot {

        @Override
        public String getName() {
            return notification.provider();
        }

        @Override
        public Instant getSnapshotTime() {
            return timestamp;
        }

        @Override
        public String getModelPackageUri() {
            return "https://eclipse.org/sensinact/test/";
        }

        @Override
        public String getModelName() {
            return notification.model();
        }

        @Override
        public List<ServiceSnapshot> getServices() {
            return List.of(service);
        }

        @Override
        public ServiceSnapshot getService(String name) {
            if (service.getName().equals(name)) {
                return service;
            }
            return null;
        }

        @Override
        public ResourceSnapshot getResource(String service, String resource) {
            if (NotificationSnapshot.this.service.getName().equals(service)
                    && NotificationSnapshot.this.resource.getName().equals(resource)) {
                return NotificationSnapshot.this.resource;
            }
            return null;
        }

        @Override
        public List<LinkedProviderSnapshot> getLinkedProviders() {
            // No linked provider support
            return List.of();
        }
    }

    private class ServiceSnapshotImpl implements ServiceSnapshot {

        @Override
        public String getName() {
            return notification.service();
        }

        @Override
        public Instant getSnapshotTime() {
            return timestamp;
        }

        @Override
        public ProviderSnapshot getProvider() {
            return provider;
        }

        @Override
        public List<ResourceSnapshot> getResources() {
            return List.of(resource);
        }

        @Override
        public ResourceSnapshot getResource(String name) {
            if (resource.getName().equals(name)) {
                return resource;
            }
            return null;
        }
    }

    private class ResourceSnapshotImpl implements ResourceSnapshot {

        private final TimedValue<?> value;

        public ResourceSnapshotImpl(final Object value) {
            this.value = new DefaultTimedValue<>(value, timestamp);
        }

        @Override
        public String toString() {
            return String.format("ResourceNotif(%s/%s/%s=%s @ %s)", notification.provider(), notification.service(),
                    notification.resource(), value.getValue(), timestamp);
        }

        @Override
        public String getName() {
            return notification.resource();
        }

        @Override
        public Instant getSnapshotTime() {
            return value.getTimestamp();
        }

        @Override
        public ServiceSnapshot getService() {
            return service;
        }

        @Override
        public boolean isSet() {
            return true;
        }

        @Override
        public TimedValue<?> getValue() {
            return value;
        }

        @Override
        public Map<String, Object> getMetadata() {
            return Map.of();
        }

        @Override
        public ResourceType getResourceType() {
            return ResourceType.PROPERTY;
        }

        @Override
        public ValueType getValueType() {
            return ValueType.UPDATABLE;
        }

        @Override
        public Class<?> getType() {
            return resource.getType();
        }
    }
}

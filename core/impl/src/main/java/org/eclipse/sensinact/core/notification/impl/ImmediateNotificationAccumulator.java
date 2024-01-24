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
package org.eclipse.sensinact.core.notification.impl;

import static java.util.Collections.emptyMap;
import static org.eclipse.sensinact.core.notification.LifecycleNotification.Status.PROVIDER_CREATED;
import static org.eclipse.sensinact.core.notification.LifecycleNotification.Status.PROVIDER_DELETED;
import static org.eclipse.sensinact.core.notification.LifecycleNotification.Status.RESOURCE_CREATED;
import static org.eclipse.sensinact.core.notification.LifecycleNotification.Status.RESOURCE_DELETED;
import static org.eclipse.sensinact.core.notification.LifecycleNotification.Status.SERVICE_CREATED;
import static org.eclipse.sensinact.core.notification.LifecycleNotification.Status.SERVICE_DELETED;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.sensinact.core.notification.LifecycleNotification;
import org.eclipse.sensinact.core.notification.NotificationAccumulator;
import org.eclipse.sensinact.core.notification.ResourceActionNotification;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.notification.ResourceMetaDataNotification;
import org.osgi.service.typedevent.TypedEventBus;

/**
 * This class is used when no batching should occur. Events are immediately sent
 * with no duplication. Its use is rare, for internal actions happening on the
 * gateway thread with no command
 *
 * This type is not thread safe and must not be used concurrently.
 */
public class ImmediateNotificationAccumulator extends AbstractNotificationAccumulatorImpl
        implements NotificationAccumulator {

    private final TypedEventBus eventBus;

    public ImmediateNotificationAccumulator(TypedEventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Called to add a provider. The event is sent immediately
     *
     * @param name the provider name
     */
    @Override
    public void addProvider(String modelPackageUri, String model, String name) {
        LifecycleNotification ln = createLifecycleNotification(PROVIDER_CREATED, modelPackageUri, model, name, null, null, null, null);
        eventBus.deliver(ln.getTopic(), ln);
    }

    /**
     * Called to remove a provider. The event is sent immediately.
     *
     * @param name the provider name
     */
    @Override
    public void removeProvider(String modelPackageUri, String model, String name) {
        LifecycleNotification ln = createLifecycleNotification(PROVIDER_DELETED, modelPackageUri, model, name, null, null, null, null);
        eventBus.deliver(ln.getTopic(), ln);
    }

    /**
     * Called to add a service. The event is sent immediately.
     *
     * @param provider the provider name
     * @param name     the service name
     */
    @Override
    public void addService(String modelPackageUri, String model, String provider, String name) {
        LifecycleNotification ln = createLifecycleNotification(SERVICE_CREATED, modelPackageUri, model, provider, name, null, null,
                null);
        eventBus.deliver(ln.getTopic(), ln);
    }

    /**
     * Called to remove a service. The event is sent immediately.
     *
     * @param provider the provider name
     * @param name     the service name
     */
    @Override
    public void removeService(String modelPackageUri, String model, String provider, String name) {
        LifecycleNotification ln = createLifecycleNotification(SERVICE_DELETED, modelPackageUri, model, provider, name, null, null,
                null);
        eventBus.deliver(ln.getTopic(), ln);
    }

    /**
     * Called to add a resource. The event is sent immediately.
     *
     * @param provider the provider name
     * @param service  the service name
     * @param name     the resource name
     */
    @Override
    public void addResource(String modelPackageUri, String model, String provider, String service, String name) {
        LifecycleNotification ln = createLifecycleNotification(RESOURCE_CREATED, modelPackageUri, model, provider, service, name, null,
                null);
        eventBus.deliver(ln.getTopic(), ln);
    }

    /**
     * Called to remove a resource. The event is sent immediately.
     *
     * @param provider the provider name
     * @param service  the service name
     * @param name     the resource name
     */
    @Override
    public void removeResource(String modelPackageUri, String model, String provider, String service, String name) {
        LifecycleNotification ln = createLifecycleNotification(RESOURCE_DELETED, modelPackageUri, model, provider, service, name, null,
                null);
        eventBus.deliver(ln.getTopic(), ln);
    }

    /**
     * Called to update metadata - provides the complete snapshot of metadata before
     * and after. The event is sent immediately
     *
     * @param provider  the provider name
     * @param service   the service name
     * @param resource  the resource name
     * @param oldValues the metadata values before the update
     * @param newValues the metadata values after the update
     * @param timestamp the latest timestamp of the metadata after the update
     *
     * @throws NullPointerException if the timestamp is null
     */
    @Override
    public void metadataValueUpdate(String modelPackageUri, String model, String provider, String service, String resource,
            Map<String, Object> oldValues, Map<String, Object> newValues, Instant timestamp) {

        Map<String, Object> nonNullOldValues = oldValues == null ? emptyMap() : oldValues;
        Map<String, Object> nonNullNewValues = newValues == null ? emptyMap() : newValues;
        Objects.requireNonNull(timestamp);

        ResourceMetaDataNotification rmn = createResourceMetaDataNotification(modelPackageUri, model, provider, service, resource,
                nonNullOldValues, nonNullNewValues, timestamp);

        eventBus.deliver(rmn.getTopic(), rmn);
    }

    /**
     * Called to update a resource value. The event is sent immediately.
     *
     * @param provider  the provider name
     * @param service   the service name
     * @param resource  the resource name
     * @param type      the resource type
     * @param oldValue  the value before the update
     * @param newValue  the value after the update
     * @param timestamp the latest timestamp of the value after the update
     *
     * @throws NullPointerException if the timestamp is null
     */
    @Override
    public void resourceValueUpdate(String modelPackageUri, String model, String provider, String service, String resource, Class<?> type,
            Object oldValue, Object newValue, Instant timestamp) {
        Objects.requireNonNull(timestamp);

        ResourceDataNotification rdn = createResourceDataNotification(modelPackageUri, model, provider, service, resource, type,
                oldValue, newValue, timestamp);
        eventBus.deliver(rdn.getTopic(), rdn);
    }

    /**
     * Called to notify of a resource action - The event is sent immediately.
     *
     * @param provider  the provider name
     * @param service   the service name
     * @param resource  the resource name
     * @param oldValue  the value before the update
     * @param newValue  the value after the update
     * @param timestamp the latest timestamp of the value after the update
     */
    @Override
    public void resourceAction(String modelPackageUri, String model, String provider, String service, String resource, Instant timestamp) {
        Objects.requireNonNull(timestamp);

        ResourceActionNotification ran = createResourceActionNotification(modelPackageUri, model, provider, service, resource,
                timestamp);
        eventBus.deliver(ran.getTopic(), ran);
    }

    protected void doComplete() {
        // No op by default
    }
}

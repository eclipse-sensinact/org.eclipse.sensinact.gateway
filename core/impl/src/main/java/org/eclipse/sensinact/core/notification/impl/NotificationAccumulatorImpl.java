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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.notification.AbstractResourceNotification;
import org.eclipse.sensinact.core.notification.LifecycleNotification;
import org.eclipse.sensinact.core.notification.NotificationAccumulator;
import org.eclipse.sensinact.core.notification.ResourceActionNotification;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.notification.ResourceMetaDataNotification;
import org.eclipse.sensinact.core.notification.LifecycleNotification.Status;
import org.osgi.service.typedevent.TypedEventBus;

/**
 * This class is responsible for managing batches of update notifications. No
 * notifications are sent until the batch is completed
 *
 * If multiple events occur for the same target then the events will be
 * collapsed to "debounce" the notifications
 *
 * This type is not thread safe and must not be used concurrently.
 */
public class NotificationAccumulatorImpl extends AbstractNotificationAccumulatorImpl
        implements NotificationAccumulator {

    private final TypedEventBus eventBus;

    private final SortedMap<NotificationKey, List<AbstractResourceNotification>> notifications = new TreeMap<>();

    public NotificationAccumulatorImpl(TypedEventBus eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * Called to add a provider. If the latest event for this provider is:
     * <ul>
     * <li>CREATED: then the events are collapsed</li>
     * <li>DELETED: then an added event is added next</li>
     * </ul>
     *
     * @param model the provider model
     * @param name  the provider name
     * @throws IllegalStateException if this accumulator has been completed with
     *                               {@link #completeAndSend()}
     */
    @Override
    public void addProvider(String modelPackageUri, String model, String name) {
        doLifecycleMerge(PROVIDER_CREATED, modelPackageUri, model, name, null, null, null, null, false);
    }

    /**
     * Called to remove a provider. If the latest event for this provider is:
     * <ul>
     * <li>CREATED: then the created event is removed and no new event added</li>
     * <li>DELETED: then the events are collapsed</li>
     * </ul>
     *
     * @param model the provider model
     * @param name  the provider name
     * @throws IllegalStateException if this accumulator has been completed with
     *                               {@link #completeAndSend()}
     */
    @Override
    public void removeProvider(String modelPackageUri, String model, String name) {
        doLifecycleMerge(PROVIDER_DELETED, modelPackageUri, model, name, null, null, null, null, true);
    }

    /**
     * Called to add a service. If the latest event for this service is:
     * <ul>
     * <li>CREATED: then the events are collapsed</li>
     * <li>DELETED: then an added event is added next</li>
     * </ul>
     *
     * @param model    the provider model
     * @param provider the provider name
     * @param name     the service name
     * @throws IllegalStateException if this accumulator has been completed with
     *                               {@link #completeAndSend()}
     */
    @Override
    public void addService(String modelPackageUri, String model, String provider, String name) {
        doLifecycleMerge(SERVICE_CREATED, modelPackageUri, model, provider, name, null, null, null, false);
    }

    /**
     * Called to remove a service. If the latest event for this service is:
     * <ul>
     * <li>CREATED: then the created event is removed and no new event added</li>
     * <li>DELETED: then the events are collapsed</li>
     * </ul>
     *
     * @param model    the provider model
     * @param provider the provider name
     * @param name     the service name
     * @throws IllegalStateException if this accumulator has been completed with
     *                               {@link #completeAndSend()}
     */
    @Override
    public void removeService(String modelPackageUri, String model, String provider, String name) {
        doLifecycleMerge(SERVICE_DELETED, modelPackageUri, model, provider, name, null, null, null, true);
    }

    /**
     * Called to add a resource. If the latest event for this resource is:
     * <ul>
     * <li>CREATED: then the events are collapsed</li>
     * <li>DELETED: then an added event is added next</li>
     * </ul>
     *
     * @param model    the provider model
     * @param provider the provider name
     * @param service  the service name
     * @param name     the resource name
     * @throws IllegalStateException if this accumulator has been completed with
     *                               {@link #completeAndSend()}
     */
    @Override
    public void addResource(String modelPackageUri, String model, String provider, String service, String name) {
        doLifecycleMerge(RESOURCE_CREATED, modelPackageUri, model, provider, service, name, null, null, false);
    }

    /**
     * Called to remove a resource. If the latest event for this resource is:
     * <ul>
     * <li>CREATED: then the created event is removed and no new event added</li>
     * <li>DELETED: then the events are collapsed</li>
     * </ul>
     *
     * @param model    the provider model
     * @param provider the provider name
     * @param service  the service name
     * @param name     the resource name
     * @throws IllegalStateException if this accumulator has been completed with
     *                               {@link #completeAndSend()}
     */
    @Override
    public void removeResource(String modelPackageUri, String model, String provider, String service, String name) {
        doLifecycleMerge(RESOURCE_DELETED, modelPackageUri, model, provider, service, name, null, null, true);
    }

    private void doLifecycleMerge(Status status, String modelPackageUri, String model, String provider, String service, String resource,
            Object initialValue, Map<String, Object> initialMetadata, boolean isDelete) {
        check();
        notifications.compute(new NotificationKey(provider, service, resource, LifecycleNotification.class), (a, b) -> {
            LifecycleNotification ln = createLifecycleNotification(status, modelPackageUri, model, provider, service, resource,
                    initialValue, initialMetadata);
            if (b != null) {
                // Check the status of the last entry
                Status s = ((LifecycleNotification) b.get(b.size() - 1)).status;
                if (s == status) {
                    // Simply replace the final entry with the update
                    return b.size() == 2 ? List.of(b.get(0), ln) : List.of(ln);
                } else if (isDelete) {
                    // A create/delete is nothing, A delete/create/delete is a delete
                    return b.size() == 1 ? null : List.of(ln);
                } else {
                    // Must be a delete/create, as we can't have a create/delete/create
                    return List.of(b.get(0), ln);
                }
            }
            return List.of(ln);
        });
    }

    /**
     * Called to update metadata - provides the complete snapshot of metadata before
     * and after
     *
     * @param model     the provider model
     * @param provider  the provider name
     * @param service   the service name
     * @param resource  the resource name
     * @param oldValues the metadata values before the update
     * @param newValues the metadata values after the update
     * @param timestamp the latest timestamp of the metadata after the update
     *
     * @throws IllegalArgumentException if the timestamp is older than the latest
     *                                  known metadata update
     * @throws IllegalStateException    if this accumulator has been completed with
     *                                  {@link #completeAndSend()}
     */
    @Override
    public void metadataValueUpdate(String modelPackageUri, String model, String provider, String service, String resource,
            Map<String, Object> oldValues, Map<String, Object> newValues, Instant timestamp) {
        check();

        final Map<String, Object> nonNullOldValues = oldValues == null ? emptyMap() : oldValues;
        final Map<String, Object> nonNullNewValues = newValues == null ? emptyMap() : newValues;
        Objects.requireNonNull(timestamp);

        notifications.compute(new NotificationKey(provider, service, resource, ResourceMetaDataNotification.class),
                (a, b) -> {
                    Map<String, Object> oldValuesToUse;
                    Map<String, Object> newValuesToUse;
                    Instant timestampToUse;
                    if (b != null) {
                        ResourceMetaDataNotification previous = (ResourceMetaDataNotification) b.get(0);
                        oldValuesToUse = previous.oldValues;
                        if (previous.timestamp.isAfter(timestamp)) {
                            throw new IllegalArgumentException("Received metadata updates out of temporal order");
                        } else {
                            newValuesToUse = nonNullNewValues;
                            timestampToUse = timestamp;
                        }
                    } else {
                        oldValuesToUse = nonNullOldValues;
                        newValuesToUse = nonNullNewValues;
                        timestampToUse = timestamp;
                    }
                    return List.of(createResourceMetaDataNotification(modelPackageUri, model, provider, service, resource,
                            oldValuesToUse, newValuesToUse, timestampToUse));
                });
    }

    /**
     * Called to update a resource value. If multiple updates occur they will be
     * collapsed into single events
     *
     * @param model     the provider model
     * @param provider  the provider name
     * @param service   the service name
     * @param resource  the resource name
     * @param type      the resource type
     * @param oldValue  the value before the update
     * @param newValue  the value after the update
     * @param timestamp the latest timestamp of the value after the update
     *
     * @throws IllegalArgumentException if the timestamp is older than the latest
     *                                  known metadata update
     * @throws IllegalStateException    if this accumulator has been completed with
     *                                  {@link #completeAndSend()}
     */
    @Override
    public void resourceValueUpdate(String modelPackageUri, String model, String provider, String service, String resource, Class<?> type,
            Object oldValue, Object newValue, Instant timestamp) {
        check();
        Objects.requireNonNull(timestamp);
        notifications.compute(new NotificationKey(provider, service, resource, ResourceDataNotification.class),
                (a, b) -> {
                    Object oldValueToUse;
                    if (b != null) {
                        ResourceDataNotification previous = (ResourceDataNotification) b.get(0);
                        if (previous.timestamp.isAfter(timestamp)) {
                            throw new IllegalArgumentException("Received resource value updates out of temporal order");
                        }
                        oldValueToUse = previous.oldValue;
                    } else {
                        oldValueToUse = oldValue;
                    }
                    return List.of(createResourceDataNotification(modelPackageUri, model, provider, service, resource, type,
                            oldValueToUse, newValue, timestamp));
                });
    }

    /**
     * Called to notify of a resource action - if multiple actions occur they will
     * be sorted into timestamp order
     *
     * @param model     the provider model
     * @param provider  the provider name
     * @param service   the service name
     * @param resource  the resource name
     * @param oldValue  the value before the update
     * @param newValue  the value after the update
     * @param timestamp the latest timestamp of the value after the update
     *
     * @throws IllegalStateException if this accumulator has been completed with
     *                               {@link #completeAndSend()}
     */
    @Override
    public void resourceAction(String modelPackageUri, String model, String provider, String service, String resource, Instant timestamp) {
        check();
        Objects.requireNonNull(timestamp);
        notifications.compute(new NotificationKey(provider, service, resource, ResourceActionNotification.class),
                (a, b) -> {
                    ResourceActionNotification ran = createResourceActionNotification(modelPackageUri, model, provider, service,
                            resource, timestamp);
                    if (b != null) {
                        return Stream.concat(b.stream(), Stream.of(ran)).map(ResourceActionNotification.class::cast)
                                .sorted((i, j) -> i.timestamp.compareTo(j.timestamp)).collect(Collectors.toList());
                    }
                    return List.of(ran);
                });
    }

    @Override
    protected void doComplete() {
        notifications.values().stream().flatMap(List::stream).forEach(n -> eventBus.deliver(n.getTopic(), n));
    }
}

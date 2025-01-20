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

import java.time.Instant;
import java.util.Map;

public interface NotificationAccumulator {

    /**
     * Called to add a provider. If the latest event for this provider is:
     * <ul>
     * <li>CREATED: then the events are collapsed</li>
     * <li>DELETED: then an added event is added next</li>
     * </ul>
     *
     * @param model the provider model package uri
     * @param model the provider model
     * @param name  the provider name
     * @throws IllegalStateException if this accumulator has been completed with
     *                               {@link #completeAndSend()}
     */
    void addProvider(String modelPackageUri, String model, String name);

    /**
     * Called to remove a provider. If the latest event for this provider is:
     * <ul>
     * <li>CREATED: then the created event is removed and no new event added</li>
     * <li>DELETED: then the events are collapsed</li>
     * </ul>
     *
     * @param model the provider model package uri
     * @param model the provider model
     * @param name  the provider name
     * @throws IllegalStateException if this accumulator has been completed with
     *                               {@link #completeAndSend()}
     */
    void removeProvider(String modelPackageUri, String model, String name);

    /**
     * Called to add a service. If the latest event for this service is:
     * <ul>
     * <li>CREATED: then the events are collapsed</li>
     * <li>DELETED: then an added event is added next</li>
     * </ul>
     *
     * @param model the provider model package uri
     * @param model    the provider model
     * @param provider the provider name
     * @param name     the service name
     * @throws IllegalStateException if this accumulator has been completed with
     *                               {@link #completeAndSend()}
     */
    void addService(String modelPackageUri, String model, String provider, String name);

    /**
     * Called to remove a service. If the latest event for this service is:
     * <ul>
     * <li>CREATED: then the created event is removed and no new event added</li>
     * <li>DELETED: then the events are collapsed</li>
     * </ul>
     *
     * @param model the provider model package uri
     * @param model    the provider model
     * @param provider the provider name
     * @param name     the service name
     * @throws IllegalStateException if this accumulator has been completed with
     *                               {@link #completeAndSend()}
     */
    void removeService(String modelPackageUri, String model, String provider, String name);

    /**
     * Called to add a resource. If the latest event for this resource is:
     * <ul>
     * <li>CREATED: then the events are collapsed</li>
     * <li>DELETED: then an added event is added next</li>
     * </ul>
     *
     * @param model the provider model package uri
     * @param model    the provider model
     * @param provider the provider name
     * @param service  the service name
     * @param name     the resource name
     * @throws IllegalStateException if this accumulator has been completed with
     *                               {@link #completeAndSend()}
     */
    void addResource(String modelPackageUri, String model, String provider, String service, String name);

    /**
     * Called to remove a resource. If the latest event for this resource is:
     * <ul>
     * <li>CREATED: then the created event is removed and no new event added</li>
     * <li>DELETED: then the events are collapsed</li>
     * </ul>
     *
     * @param model the provider model package uri
     * @param model    the provider model
     * @param provider the provider name
     * @param service  the service name
     * @param name     the resource name
     * @throws IllegalStateException if this accumulator has been completed with
     *                               {@link #completeAndSend()}
     */
    void removeResource(String modelPackageUri, String model, String provider, String service, String name);

    /**
     * Called to update metadata - provides the complete snapshot of metadata before
     * and after
     *
     * @param model the provider model package uri
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
    void metadataValueUpdate(String modelPackageUri, String model, String provider, String service, String resource,
            Map<String, Object> oldValues, Map<String, Object> newValues, Instant timestamp);

    /**
     * Called to update a resource value. If multiple updates occur they will be
     * collapsed into single events
     *
     * @param model the provider model package uri
     * @param model     the provider model
     * @param provider  the provider name
     * @param service   the service name
     * @param resource  the resource name
     * @param type      the resource type
     * @param oldValue  the value before the update
     * @param newValue  the value after the update
     * @param metadata  the metadata for the resource
     * @param timestamp the latest timestamp of the value after the update
     *
     * @throws IllegalArgumentException if the timestamp is older than the latest
     *                                  known metadata update
     * @throws IllegalStateException    if this accumulator has been completed with
     *                                  {@link #completeAndSend()}
     */
    void resourceValueUpdate(String modelPackageUri, String model, String provider, String service, String resource, Class<?> type,
            Object oldValue, Object newValue, Map<String, Object> metadata, Instant timestamp);

    /**
     * Called to notify of a resource action - if multiple actions occur they will
     * be sorted into timestamp order
     *
     * @param model the provider model package uri
     * @param model     the provider model
     * @param provider  the provider name
     * @param service   the service name
     * @param resource  the resource name
     * @param timestamp the time at which the action was triggered
     *
     * @throws IllegalStateException if this accumulator has been completed with
     *                               {@link #completeAndSend()}
     */
    void resourceAction(String modelPackageUri, String model, String provider, String service, String resource, Instant timestamp);

    /**
     * Called to complete a batch of notifications and triggers sending.
     *
     * <br>
     *
     * Once completed an accumulator cannot be used again
     */
    void completeAndSend();

}

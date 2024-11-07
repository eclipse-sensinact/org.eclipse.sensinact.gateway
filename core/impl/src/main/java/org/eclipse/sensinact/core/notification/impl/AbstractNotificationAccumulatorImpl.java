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

import org.eclipse.sensinact.core.notification.LifecycleNotification;
import org.eclipse.sensinact.core.notification.NotificationAccumulator;
import org.eclipse.sensinact.core.notification.ResourceActionNotification;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.notification.ResourceMetaDataNotification;
import org.eclipse.sensinact.core.notification.LifecycleNotification.Status;

/**
 * This class is allows some level of implementation sharing between the
 * different types of {@link NotificationAccumulator}
 *
 */
public abstract class AbstractNotificationAccumulatorImpl implements NotificationAccumulator {

    private boolean complete = false;

    /**
     * Check whether the accumulator has been completed
     */
    protected void check() {
        if (complete) {
            throw new IllegalStateException("The accumulator is already complete");
        }
    }

    protected LifecycleNotification createLifecycleNotification(Status status, String modelPackageUri, String model, String provider,
            String service, String resource, Object initialValue, Map<String, Object> initialMetadata) {
        LifecycleNotification ln = new LifecycleNotification();
        ln.modelPackageUri = modelPackageUri;
        ln.model = model;
        ln.provider = provider;
        ln.service = service;
        ln.resource = resource;
        ln.status = status;
        ln.initialValue = initialValue;
        ln.initialMetadata = initialMetadata;
        return ln;
    }

    protected ResourceMetaDataNotification createResourceMetaDataNotification(String modelPackageUri, String model, String provider,
            String service, String resource, Map<String, Object> oldValues, Map<String, Object> newValues,
            Instant timestamp) {
        ResourceMetaDataNotification rn = new ResourceMetaDataNotification();
        rn.modelPackageUri = modelPackageUri;
        rn.model = model;
        rn.provider = provider;
        rn.service = service;
        rn.resource = resource;
        rn.oldValues = oldValues;
        rn.newValues = newValues;
        rn.timestamp = timestamp;
        return rn;
    }

    protected ResourceDataNotification createResourceDataNotification(String modelPackageUri, String model, String provider, String service,
            String resource, Class<?> type, Object oldValue, Object newValue, Map<String, Object> metadata, Instant timestamp) {
        ResourceDataNotification rn = new ResourceDataNotification();
        rn.modelPackageUri = modelPackageUri;
        rn.model = model;
        rn.provider = provider;
        rn.service = service;
        rn.resource = resource;
        rn.type = type;
        rn.oldValue = oldValue;
        rn.newValue = newValue;
        rn.metadata = metadata;
        rn.timestamp = timestamp;
        return rn;
    }

    protected ResourceActionNotification createResourceActionNotification(String modelPackageUri, String model, String provider, String service,
            String resource, Instant timestamp) {
        ResourceActionNotification rn = new ResourceActionNotification();
        rn.modelPackageUri = modelPackageUri;
        rn.model = model;
        rn.provider = provider;
        rn.service = service;
        rn.resource = resource;
        rn.timestamp = timestamp;
        return rn;
    }

    public final void completeAndSend() {
        check();
        complete = true;
        doComplete();
    }

    /**
     * Complete the accumulation
     */
    protected abstract void doComplete();
}

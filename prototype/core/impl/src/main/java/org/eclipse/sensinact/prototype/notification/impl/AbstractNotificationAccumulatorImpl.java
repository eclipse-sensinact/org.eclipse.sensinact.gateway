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
package org.eclipse.sensinact.prototype.notification.impl;

import java.time.Instant;
import java.util.Map;

import org.eclipse.sensinact.prototype.notification.LifecycleNotification;
import org.eclipse.sensinact.prototype.notification.LifecycleNotification.Status;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.eclipse.sensinact.prototype.notification.ResourceActionNotification;
import org.eclipse.sensinact.prototype.notification.ResourceDataNotification;
import org.eclipse.sensinact.prototype.notification.ResourceMetaDataNotification;

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

    protected LifecycleNotification createLifecycleNotification(Status status, String model, String provider,
            String service, String resource, Object initialValue, Map<String, Object> initialMetadata) {
        LifecycleNotification ln = new LifecycleNotification();
        ln.model = model;
        ln.provider = provider;
        ln.service = service;
        ln.resource = resource;
        ln.status = status;
        ln.initialValue = initialValue;
        ln.initialMetadata = initialMetadata;
        return ln;
    }

    protected ResourceMetaDataNotification createResourceMetaDataNotification(String model, String provider,
            String service, String resource, Map<String, Object> oldValues, Map<String, Object> newValues,
            Instant timestamp) {
        ResourceMetaDataNotification rn = new ResourceMetaDataNotification();
        rn.model = model;
        rn.provider = provider;
        rn.service = service;
        rn.resource = resource;
        rn.oldValues = oldValues;
        rn.newValues = newValues;
        rn.timestamp = timestamp;
        return rn;
    }

    protected ResourceDataNotification createResourceDataNotification(String model, String provider, String service,
            String resource, Class<?> type, Object oldValue, Object newValue, Instant timestamp) {
        ResourceDataNotification rn = new ResourceDataNotification();
        rn.model = model;
        rn.provider = provider;
        rn.service = service;
        rn.resource = resource;
        rn.type = type;
        rn.oldValue = oldValue;
        rn.newValue = newValue;
        rn.timestamp = timestamp;
        return rn;
    }

    protected ResourceActionNotification createResourceActionNotification(String model, String provider, String service,
            String resource, Instant timestamp) {
        ResourceActionNotification rn = new ResourceActionNotification();
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

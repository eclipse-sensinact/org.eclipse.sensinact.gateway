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
package org.eclipse.sensinact.core.notification.impl;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.notification.LifecycleNotification;
import org.eclipse.sensinact.core.notification.LifecycleNotification.Status;
import org.eclipse.sensinact.core.notification.LinkedProviderNotification;
import org.eclipse.sensinact.core.notification.LinkedProviderNotification.Action;
import org.eclipse.sensinact.core.notification.ResourceActionNotification;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.notification.ResourceMetaDataNotification;

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
        return new LifecycleNotification(modelPackageUri, model, provider, service,
                resource, status, initialValue, initialMetadata);
    }

    protected ResourceMetaDataNotification createResourceMetaDataNotification(String modelPackageUri, String model, String provider,
            String service, String resource, Map<String, Object> oldValues, Map<String, Object> newValues,
            Instant timestamp) {
        return new ResourceMetaDataNotification(modelPackageUri, model, provider, service,
                resource, oldValues, newValues, timestamp);
    }

    protected ResourceDataNotification createResourceDataNotification(String modelPackageUri, String model, String provider, String service,
            String resource, Class<?> type, Object oldValue, Object newValue, Map<String, Object> metadata, Instant timestamp) {
        return new ResourceDataNotification(modelPackageUri, model, provider, service,
                resource, oldValue, newValue, timestamp, type, metadata);
    }

    protected ResourceActionNotification createResourceActionNotification(String modelPackageUri, String model, String provider, String service,
            String resource, Instant timestamp) {
        return new ResourceActionNotification(modelPackageUri, model, provider, service,
                resource, timestamp);
    }

    protected LinkedProviderNotification createLinkedProviderNotification(String modelPackageUri, String model, String provider, String child,
            Action action, List<String> linkedProviders, Instant timestamp) {
        return new LinkedProviderNotification(modelPackageUri, model, provider, child,
                action, linkedProviders, timestamp);
    }

    @Override
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

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
package org.eclipse.sensinact.core.notification;

import java.util.Map;
import java.util.Objects;

/**
 * Lifecycle notifications are sent to indicate the creation or deletion of a
 * provider/service/resource
 *
 * Topic name is
 *
 * LIFECYCLE/&lt;model&gt;/&lt;provider&gt;[/&lt;service&gt;[/&lt;resource&gt;]]
 */
public record LifecycleNotification(String modelPackageUri, String model, String provider,
        String service, String resource, Status status, Object initialValue,
        Map<String, Object> initialMetadata) implements AbstractResourceNotification {

    @Override
    public String getTopic() {
        Objects.requireNonNull(status);
        Objects.requireNonNull(provider);
        int ordinal = status.ordinal();
        if (ordinal >= Status.SERVICE_CREATED.ordinal()) {
            Objects.requireNonNull(service);
        }
        if (ordinal >= Status.RESOURCE_CREATED.ordinal()) {
            Objects.requireNonNull(resource);
        }

        return "LIFECYCLE/".concat(String.format(status.template, model, provider, service, resource));
    }

    public enum Status {
        /**
         * Provider created,
         * <ul>
         * <li>{@link LifecycleNotification#service} will be null</li>
         * <li>{@link LifecycleNotification#resource} will be null</li>
         * <li>{@link LifecycleNotification#initialMetadata} will be null</li>
         * <li>{@link LifecycleNotification#initialValue} will be a List of String
         * service names for initial services</li>
         * </ul>
         */
        PROVIDER_CREATED("%s/%s"),

        /**
         * Provider deleted,
         * <ul>
         * <li>{@link LifecycleNotification#service} will be null</li>
         * <li>{@link LifecycleNotification#resource} will be null</li>
         * <li>{@link LifecycleNotification#initialMetadata} will be null</li>
         * <li>{@link LifecycleNotification#initialValue} will be null</li>
         * </ul>
         */
        PROVIDER_DELETED("%s/%s"),

        /**
         * Service created,
         * <ul>
         * <li>{@link LifecycleNotification#resource} will be null</li>
         * <li>{@link LifecycleNotification#initialMetadata} will be null</li>
         * <li>{@link LifecycleNotification#initialValue} will be a List of String
         * service names for initial resources</li>
         * </ul>
         */
        SERVICE_CREATED("%s/%s/%s"),

        /**
         * Service deleted,
         * <ul>
         * <li>{@link LifecycleNotification#resource} will be null</li>
         * <li>{@link LifecycleNotification#initialMetadata} will be null</li>
         * <li>{@link LifecycleNotification#initialValue} will be null</li>
         * </ul>
         */
        SERVICE_DELETED("%s/%s/%s"),

        /**
         * Resource created,
         * <ul>
         * <li>{@link LifecycleNotification#initialMetadata} will be the initial
         * metadata</li>
         * <li>{@link LifecycleNotification#initialValue} will be the initial value</li>
         * </ul>
         */
        RESOURCE_CREATED("%s/%s/%s/%s"),

        /**
         * Resource deleted,
         * <ul>
         * <li>{@link LifecycleNotification#initialMetadata} will be null</li>
         * <li>{@link LifecycleNotification#initialValue} will be null</li>
         * </ul>
         */
        RESOURCE_DELETED("%s/%s/%s/%s");

        private final String template;

        private Status(String template) {
            this.template = template;
        }
    }
}

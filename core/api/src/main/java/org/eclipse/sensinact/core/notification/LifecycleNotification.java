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
        Map<String, Object> initialMetadata) implements ResourceNotification {

    @Override
    public String getTopic() {
        Objects.requireNonNull(status);
        Objects.requireNonNull(model);
        Objects.requireNonNull(provider);
        int ordinal = status.ordinal();
        if (ordinal >= Status.SERVICE_CREATED.ordinal()) {
            Objects.requireNonNull(service);
        }
        if (ordinal >= Status.RESOURCE_CREATED.ordinal()) {
            Objects.requireNonNull(resource);
        }

        return String.format(status.template,
                TopicUtils.escapeTopicPart(model, false), TopicUtils.escapeTopicPart(provider, false),
                TopicUtils.escapeTopicPart(service, false), TopicUtils.escapeTopicPart(resource, false));
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
        PROVIDER_CREATED("LIFECYCLE/%s/%s"),

        /**
         * Provider deleted,
         * <ul>
         * <li>{@link LifecycleNotification#service} will be null</li>
         * <li>{@link LifecycleNotification#resource} will be null</li>
         * <li>{@link LifecycleNotification#initialMetadata} will be null</li>
         * <li>{@link LifecycleNotification#initialValue} will be null</li>
         * </ul>
         */
        PROVIDER_DELETED("LIFECYCLE/%s/%s"),

        /**
         * Service created,
         * <ul>
         * <li>{@link LifecycleNotification#resource} will be null</li>
         * <li>{@link LifecycleNotification#initialMetadata} will be null</li>
         * <li>{@link LifecycleNotification#initialValue} will be a List of String
         * service names for initial resources</li>
         * </ul>
         */
        SERVICE_CREATED("LIFECYCLE/%s/%s/%s"),

        /**
         * Service deleted,
         * <ul>
         * <li>{@link LifecycleNotification#resource} will be null</li>
         * <li>{@link LifecycleNotification#initialMetadata} will be null</li>
         * <li>{@link LifecycleNotification#initialValue} will be null</li>
         * </ul>
         */
        SERVICE_DELETED("LIFECYCLE/%s/%s/%s"),

        /**
         * Resource created,
         * <ul>
         * <li>{@link LifecycleNotification#initialMetadata} will be the initial
         * metadata</li>
         * <li>{@link LifecycleNotification#initialValue} will be the initial value</li>
         * </ul>
         */
        RESOURCE_CREATED("LIFECYCLE/%s/%s/%s/%s"),

        /**
         * Resource deleted,
         * <ul>
         * <li>{@link LifecycleNotification#initialMetadata} will be null</li>
         * <li>{@link LifecycleNotification#initialValue} will be null</li>
         * </ul>
         */
        RESOURCE_DELETED("LIFECYCLE/%s/%s/%s/%s");

        private final String template;

        private Status(String template) {
            this.template = template;
        }
    }
}

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

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Lifecycle notifications are sent to indicate the creation or deletion of a
 * provider/service/resource
 *
 * Topic name is
 *
 * LINKED/&lt;model&gt;/&lt;provider&gt;
 */
public record LinkedProviderNotification(String modelPackageUri, String model, String provider,
        String child, Action action, List<String> linkedProviders, Instant timestamp) implements ResourceNotification {

    @Override
    public String getTopic() {
        Objects.requireNonNull(action);
        Objects.requireNonNull(model);
        Objects.requireNonNull(provider);

        return "LINKED/".concat(String.format("%s/%s", model, provider));
    }

    @Override
    public String service() {
        return null;
    }

    @Override
    public String resource() {
        return null;
    }

    public enum Action {
        /**
         * Link added
         * <ul>
         * <li>{@link LinkedProviderNotification#service} will be null</li>
         * <li>{@link LinkedProviderNotification#resource} will be null</li>
         * <li>{@link LinkedProviderNotification#provider} will be the parent provider</li>
         * <li>{@link LinkedProviderNotification#child} will be the child provider
         * <li>{@link LinkedProviderNotification#linkedProviders} will be a List of String
         * service names for initial services</li>
         * </ul>
         */
        ADDED,

        /**
         * Link removed,
         * <ul>
         * <li>{@link LinkedProviderNotification#service} will be null</li>
         * <li>{@link LinkedProviderNotification#resource} will be null</li>
         * <li>{@link LinkedProviderNotification#provider} will be the parent provider</li>
         * <li>{@link LinkedProviderNotification#child} will be the child provider
         * <li>{@link LinkedProviderNotification#linkedProviders} will be a List of String
         * </ul>
         */
        REMOVED;

    }
}

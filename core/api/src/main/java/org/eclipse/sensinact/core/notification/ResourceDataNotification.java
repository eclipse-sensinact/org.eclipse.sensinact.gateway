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
import java.util.Map;
import java.util.Objects;

/**
 * Data notifications are sent to indicate the change in the value of a resource
 *
 * Topic name is
 *
 * DATA/&lt;model&gt;/&lt;provider&gt;/&lt;service&gt;/&lt;resource&gt;
 */
public record ResourceDataNotification(String modelPackageUri, String model, String provider,
        String service, String resource, Object oldValue, Object newValue,
        Instant timestamp, Class<?> type, Map<String, Object> metadata) implements AbstractResourceNotification {

    @Override
    public String getTopic() {
        Objects.requireNonNull(provider);
        Objects.requireNonNull(service);
        Objects.requireNonNull(resource);
        return String.format("DATA/%s/%s/%s/%s", model, provider, service, resource);
    }

}

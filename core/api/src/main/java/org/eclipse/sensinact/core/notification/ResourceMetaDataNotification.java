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
 * Metadata notifications are sent to indicate the change in the metadata for a
 * resource
 *
 * Topic name is
 *
 * METADATA/&lt;model&gt;/&lt;provider&gt;/&lt;service&gt;/&lt;resource&gt;
 */
public record ResourceMetaDataNotification(String modelPackageUri, String model, String provider,
        String service, String resource, Map<String, Object> oldValues,
        Map<String, Object> newValues, Instant timestamp) implements ResourceNotification {

    @Override
    public String getTopic() {
        Objects.requireNonNull(model);
        Objects.requireNonNull(provider);
        Objects.requireNonNull(service);
        Objects.requireNonNull(resource);
        return String.format("METADATA/%s/%s/%s/%s", model, provider, service, resource);
    }

}

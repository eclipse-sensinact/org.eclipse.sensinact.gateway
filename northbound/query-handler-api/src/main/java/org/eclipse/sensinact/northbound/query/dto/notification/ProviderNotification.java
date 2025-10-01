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
package org.eclipse.sensinact.northbound.query.dto.notification;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Common fields of a resource notification
 */
public record ProviderNotification(String subscriptionId, String provider,
        List<ServiceData> services) {

    public record ServiceData(String service, List<ResourceData> resources) {
    }
    public record ResourceData(String resource, Object value, Instant timestamp,
            Map<String, Object> metadata) {
    }
}

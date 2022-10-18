/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.prototype.notification;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Metadata notifications are sent to indicate the change in the metadata for a
 * resource
 * 
 * Topic name is
 * 
 * METADATA/&lt;provider&gt;/&lt;service&gt;/&lt;resource&gt;
 */
public class ResourceMetaDataNotification extends AbstractResourceNotification {

    public Map<String, Object> oldValues;

    public Map<String, Object> newValues;

    public Instant timestamp;

    @Override
    public String getTopic() {
        Objects.requireNonNull(provider);
        Objects.requireNonNull(service);
        Objects.requireNonNull(resource);
        return String.format("METADATA/%s/%s/%s", provider, service, resource);
    }

}

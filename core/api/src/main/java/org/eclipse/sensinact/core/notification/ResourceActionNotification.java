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
package org.eclipse.sensinact.core.notification;

import java.time.Instant;
import java.util.Objects;

/**
 * Action notifications are sent to indicate an action triggering on a resource
 *
 * Topic name is
 *
 * ACTION/&lt;model&gt;/&lt;provider&gt;/&lt;service&gt;/&lt;resource&gt;
 */
public class ResourceActionNotification extends AbstractResourceNotification {

    public Instant timestamp;

    @Override
    public String getTopic() {
        Objects.requireNonNull(provider);
        Objects.requireNonNull(service);
        Objects.requireNonNull(resource);
        return String.format("ACTION/%s/%s/%s/%s", model, provider, service, resource);
    }

}

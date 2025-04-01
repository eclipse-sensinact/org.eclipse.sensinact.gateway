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
package org.eclipse.sensinact.northbound.query.dto.notification;

import java.time.Instant;

import org.eclipse.sensinact.core.notification.LifecycleNotification;
import org.eclipse.sensinact.core.notification.LifecycleNotification.Status;

/**
 * Resource life-cycle notification
 */
public class ResourceLifecycleNotificationDTO extends AbstractResourceNotificationDTO {

    /**
     * Event type
     */
    public Status status;

    /**
     * Initial value of the resource
     */
    public Object initialValue;

    public ResourceLifecycleNotificationDTO() {
        // Do nothing
    }

    public ResourceLifecycleNotificationDTO(LifecycleNotification notif) {
        this.provider = notif.provider();
        this.service = notif.service();
        this.resource = notif.resource();
        this.timestamp = Instant.now().toEpochMilli();
        this.status = notif.status();
        this.initialValue = notif.initialValue();
    }
}

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
package org.eclipse.sensinact.northbound.rest.dto.notification;

import java.time.Instant;

import org.eclipse.sensinact.prototype.notification.LifecycleNotification;
import org.eclipse.sensinact.prototype.notification.LifecycleNotification.Status;

/**
 * Resource life-cycle notification
 */
public class ResourceLifecycleNotificationDTO {

    public String provider;

    public String service;

    public String resource;

    public long timestamp;

    public Status status;

    public Object initialValue;

    public ResourceLifecycleNotificationDTO() {
        // Do nothing
    }

    public ResourceLifecycleNotificationDTO(LifecycleNotification notif) {
        this.provider = notif.provider;
        this.service = notif.service;
        this.resource = notif.resource;
        this.timestamp = Instant.now().toEpochMilli();
        this.status = notif.status;
        this.initialValue = notif.initialValue;
    }
}

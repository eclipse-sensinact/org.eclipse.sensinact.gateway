/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.protocol.ssdp.model;

import org.eclipse.sensinact.gateway.protocol.ssdp.api.SSDPEvent;

/**
 * Message received on multicast interface to get some new information about a device
 */
public class NotifyMessage extends SSDPReceivedMessage {
    private String notificationType;

    public NotifyMessage() {
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public void setEvent(String nts) {
        if (nts.equalsIgnoreCase("ssdp:alive")) {
            super.event = SSDPEvent.ALIVE;
        } else if (nts.equalsIgnoreCase("ssdp:update")) {
            super.event = SSDPEvent.UPDATE;
        } else if (nts.equalsIgnoreCase("ssdp:byebye")) {
            super.event = SSDPEvent.GOODBYE;
        }
    }
}

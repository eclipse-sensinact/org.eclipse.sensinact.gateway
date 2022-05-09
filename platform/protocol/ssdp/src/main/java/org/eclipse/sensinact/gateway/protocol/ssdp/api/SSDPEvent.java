/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.protocol.ssdp.api;

/**
 * Summarize the SSDP events
 */
public enum SSDPEvent {
    DISCOVER("ssdp:discover"), ALIVE("ssdp:alive"), UPDATE("ssdp:update"), GOODBYE("ssdp:byebye");
    private String ssdpEvent;

    SSDPEvent(String ssdpEvent) {
        this.ssdpEvent = ssdpEvent;
    }

    public String toString() {
        return ssdpEvent;
    }
}

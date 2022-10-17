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

import org.eclipse.sensinact.gateway.protocol.ssdp.model.SSDPDescriptionPacket;

/**
 * This class has to be implemented in order to be notified when a new SSDP packet is received
 */
public interface SSDPDiscoveryListenerItf {
    /**
     * Notify the listeners about a new SSDP event
     *
     * @param ssdpEvent the type of event received by the notifier
     * @param packet    the content of the packet received by the notifier
     */
    void eventSSDP(SSDPEvent ssdpEvent, SSDPDescriptionPacket packet);
}

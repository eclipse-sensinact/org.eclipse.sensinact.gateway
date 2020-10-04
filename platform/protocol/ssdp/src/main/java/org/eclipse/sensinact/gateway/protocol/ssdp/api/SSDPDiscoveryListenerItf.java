/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
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

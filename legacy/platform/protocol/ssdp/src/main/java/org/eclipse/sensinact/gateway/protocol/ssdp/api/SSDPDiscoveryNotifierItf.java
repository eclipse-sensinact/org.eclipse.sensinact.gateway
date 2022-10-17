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

import java.util.List;

public interface SSDPDiscoveryNotifierItf {
    /**
     * Register a new listener to the SSDP notifier
     *
     * @param listener the listener to notify
     * @param filter   the optional filter for the events notification
     */
    void addListener(SSDPDiscoveryListenerItf listener, String filter);

    /**
     * Unregister a listener from the notifier
     *
     * @param listener the listen to remove
     */
    void removeListener(SSDPDiscoveryListenerItf listener);

    /**
     * Gets a list of stored SSDP description packet according to a specific filter
     *
     * @param filter the LDAP filter
     * @return the list of SSDP description packet
     */
    List<SSDPDescriptionPacket> getDescriptions(String filter);
}

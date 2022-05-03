/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
/**
 *
 */
package org.eclipse.sensinact.gateway.sthbnd.http.kodi.internal;

import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.packet.annotation.CommandID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Data;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ResourceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceProviderID;

/**
 *
 */
public class KodiRequestPacket extends KodiResponsePacket {
    @Data
    public final Object data;

    /**
     * @param content
     */
    public KodiRequestPacket(String serviceProvider, String service, String resource, Object data) {
        super(serviceProvider, service, resource);
        this.data = data;
    }

    @ServiceProviderID
    public String getServiceProvider() {
        return super.serviceProvider;
    }

    @ServiceID
    public String getService() {
        return super.service;
    }

    @ResourceID
    public String getResource() {
        return super.resource;
    }

    @CommandID
    public CommandType getCommand() {
        return super.getCommand();
    }
}

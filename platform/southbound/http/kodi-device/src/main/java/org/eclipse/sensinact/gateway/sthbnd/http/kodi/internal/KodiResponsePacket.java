/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.http.kodi.internal;

import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.packet.annotation.CommandID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Data;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ResourceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceProviderID;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponse;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpResponsePacket;
import org.eclipse.sensinact.gateway.util.UriUtils;
import org.json.JSONObject;

public class KodiResponsePacket extends HttpResponsePacket {
    @ServiceProviderID
    public final String serviceProvider;
    @ServiceID
    public final String service;
    @ResourceID
    public final String resource;
    @CommandID
    public final CommandType command;

    /**
     * @param content
     */
    public KodiResponsePacket(String serviceProvider, String service, String resource) {
        super(null);
        this.serviceProvider = serviceProvider;
        this.service = service;
        this.resource = resource;
        this.command = null;
    }

    public KodiResponsePacket(HttpResponse response) {
        super(response);
        String[] uriElements = UriUtils.getUriElements(getPath());
        this.serviceProvider = uriElements.length > 0 ? uriElements[0] : null;
        this.service = uriElements.length > 1 ? uriElements[1] : null;
        this.resource = uriElements.length > 2 ? uriElements[2] : null;
        this.command = super.getCommand();
    }

    @Data
    public Object getData() {
        try {
            KodiApi api = KodiApi.fromName(resource);
            return api.getData(new JSONObject(new String(super.content)).get("result"));

        } catch (Exception e) {
            return null;
        }
    }
}

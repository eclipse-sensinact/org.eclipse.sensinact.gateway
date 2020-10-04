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
package org.eclipse.sensinact.gateway.sthbnd.http.kodi.internal;

import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.sthbnd.http.kodi.osgi.KodiServiceMediator;
import org.eclipse.sensinact.gateway.sthbnd.http.smpl.SimpleHttpProtocolStackEndpoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class KodiRemoteControlHttpListener extends HttpServlet {
    SimpleHttpProtocolStackEndpoint connector;
    KodiServiceMediator mediator;

    public KodiRemoteControlHttpListener(SimpleHttpProtocolStackEndpoint connector, KodiServiceMediator mediator) {
        this.connector = connector;
        this.mediator = mediator;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (mediator.isDebugLoggable()) {
            mediator.debug(resp.toString());
        }
        String key = req.getParameter("key");
        if (key == null) {
            resp.sendError(400, "Missing parameter 'key'");
            return;
        }
        String serviceProvider = mediator.getKodiFriendlyName(req.getRemoteAddr());
        if (serviceProvider == null) {
            resp.sendError(400, "Wrong service provider");
            return;
        }
        try {
            long timestamp = System.currentTimeMillis();

            connector.process(new KodiRequestPacket(serviceProvider, "buttonpressed", "remote-control", key));

            connector.process(new KodiRequestPacket(serviceProvider, "buttonpressed", "lastevent", timestamp));

        } catch (InvalidPacketException e) {
            mediator.error(e);
        }
    }
}

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
package org.eclipse.sensinact.gateway.sthbnd.http.android;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@WebSocket(maxTextMessageSize = 64 * 1024)
public class AndroidWebSocketWrapper {
    private Logger LOG = LoggerFactory.getLogger(AndroidWebSocketWrapper.class.getName());
    private final LocalProtocolStackEndpoint<DevGenPacket> endpoint;
    private List<String> providers = new ArrayList<String>();
    protected Session session;
    protected Mediator mediator;

    protected AndroidWebSocketWrapper(Mediator mediator, LocalProtocolStackEndpoint<DevGenPacket> endpoint) {
        this.mediator = mediator;
        this.endpoint = endpoint;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        LOG.debug("new websocket connection was open");
        this.session = session;
    }

    /**
     * @inheritedDoc
     * @see AndroidWebSocketWrapper#
     * onClose(int, java.lang.String)
     */
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        for (String provider : providers) {
            DevGenPacket pack = new DevGenPacket(provider);
            pack.isGoodbye(true);
            try {
                endpoint.process(pack);
            } catch (InvalidPacketException e) {
                LOG.error("failed to remove all providers generated via websocket", e);
            }
        }
    }

    /**
     * Receives Text Message events
     *
     * @param message the received message
     */
    @OnWebSocketMessage
    public void onMessage(String message) {
        try {
            LOG.debug("Message received from the client {}, starting packet transformation", message);
            JSONObject jsonPayload = new JSONObject(message);
            String provider = jsonPayload.getString("provider");
            String service = jsonPayload.getString("service");
            String resource = jsonPayload.getString("resource");
            String value = null;
            if (jsonPayload.has("value")) {
                value = jsonPayload.get("value").toString();
            }
            DevGenPacket packet = new DevGenPacket(provider, service, resource);
            if (value != null) {
                packet.setCurrentState(value);
            }
            endpoint.process(packet);
            providers.add(provider);
            LOG.debug("Package {}/{}/{}/{} received from the client processed with success.", provider, service, resource, value);
        } catch (Exception e) {
            LOG.error("Failed to process the package received from the client", e);
        }
    }

    /**
     * Receives websocket errors (exceptions) that have
     * occurred internally in the websocket implementation.
     *
     * @param error the exception that occurred
     */
    @OnWebSocketError
    public void handleError(Throwable error) {
        error.printStackTrace();
    }

    protected void close() {
        if (this.session == null) {
            return;
        }
        if (this.session.isOpen()) {
            this.session.close();
        }
        this.session = null;
    }

    /**
     * @return
     */
    public InetSocketAddress getClientAddress() {
        return this.session.getRemoteAddress();
    }
}

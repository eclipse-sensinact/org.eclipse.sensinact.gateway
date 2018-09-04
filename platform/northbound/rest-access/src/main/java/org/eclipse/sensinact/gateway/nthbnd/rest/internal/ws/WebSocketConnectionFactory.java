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
package org.eclipse.sensinact.gateway.nthbnd.rest.internal.ws;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.sensinact.gateway.core.security.AuthenticationToken;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.LoginResponse;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundEndpoint;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebSocketConnectionFactory implements WebSocketCreator {
    private NorthboundMediator mediator;
    private List<WebSocketConnection> sessions;
    private Map<String, String> anonymous;

    /**
     * @param mediator
     */
    public WebSocketConnectionFactory(NorthboundMediator mediator) {
        this.mediator = mediator;
        this.sessions = new ArrayList<WebSocketConnection>();
        this.anonymous = new HashMap<String, String>();
    }

    /**
     * @param wsEndpoint
     */
    public void deleteSocketEndpoint(WebSocketConnection wsEndpoint) {
        synchronized (this.sessions) {
            if (this.sessions.remove(wsEndpoint)) {
                wsEndpoint.close();
            }
        }
    }

    /**
     *
     */
    public void close() {
        synchronized (this.sessions) {
            while (!this.sessions.isEmpty()) {
                this.sessions.remove(0).close();
            }
        }
    }

    /**
     * @inheritDoc
     * @see org.eclipse.jetty.websocket.servlet.WebSocketCreator#
     * createWebSocket(org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest,
     * org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse)
     */
    @Override
    public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
        HttpServletRequest request = req.getHttpServletRequest();
        String tokenHeader = request.getHeader("X-Auth-Token");
        String authorizationHeader = request.getHeader("Authorization");

        NorthboundEndpoint endpoint = null;
        AuthenticationToken token = null;
        Credentials credentials = null;
        String client = null;

        if (tokenHeader != null) {
            token = new AuthenticationToken(tokenHeader);

        } else if (authorizationHeader != null) {
            credentials = new Credentials(authorizationHeader);
        }
        if (token == null && credentials == null) {
            String clientAddress = request.getRemoteAddr();
            int clientPort = request.getRemotePort();

            client = new StringBuilder().append(clientAddress).append(":").append(clientPort).toString();

            String sid = this.anonymous.get(client);
            if (sid != null) {
                token = new AuthenticationToken(sid);

            } else {
                try {
                    endpoint = mediator.getNorthboundEndpoints().getEndpoint();
                    this.anonymous.put(client, endpoint.getSessionToken());

                } catch (InvalidCredentialException e) {
                    mediator.debug(e.getMessage());
                }
            }
        }
        if (credentials != null) {
            try {
                LoginResponse response = mediator.getAccessingEndpoint().createNorthboundEndpoint(credentials);
                token = new AuthenticationToken(response.getToken());

            } catch (InvalidCredentialException | NullPointerException e) {
                mediator.debug(e.getMessage());
            }
        }
        if (token != null) {
            try {
                endpoint = mediator.getNorthboundEndpoints().getEndpoint(token);

            } catch (InvalidCredentialException e) {
                mediator.debug(e.getMessage());
            }
        }
        if (endpoint == null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("statusCode", 403);
            jsonObject.put("message", "Authentication failed");
            try {
                resp.sendError(403, new String(jsonObject.toString().getBytes("UTF-8")));

            } catch (IOException e) {
                mediator.error(e);
            }
            return null;
        }
        WebSocketConnection wsConnection = new WebSocketConnection(this, endpoint, this.mediator);

        synchronized (this.sessions) {
            this.sessions.add(wsConnection);
        }
        return wsConnection;
    }
}

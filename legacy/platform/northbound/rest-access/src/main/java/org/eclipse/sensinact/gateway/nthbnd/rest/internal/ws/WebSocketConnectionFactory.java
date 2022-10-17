/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.rest.internal.ws;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.sensinact.gateway.core.security.SessionToken;
import org.eclipse.sensinact.gateway.core.security.AccessToken;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.LoginResponse;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundEndpoint;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.JsonObjectBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebSocketConnectionFactory implements WebSocketCreator {
	
	private static final Logger LOG = LoggerFactory.getLogger(WebSocketConnectionFactory.class);
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

    @Override
    public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
        HttpServletRequest request = req.getHttpServletRequest();
        String tokenHeader = request.getHeader("X-Auth-Token");
        String authorizationHeader = request.getHeader("Authorization");

        NorthboundEndpoint endpoint = null;
        SessionToken token = null;
        Credentials credentials = null;
        AccessToken accessToken = null;
        String client = null;

        if (tokenHeader != null)
            token = new SessionToken(tokenHeader);
        else if (authorizationHeader != null) {
          	if(authorizationHeader.startsWith("Basic "))   
          		credentials = new Credentials(authorizationHeader.substring(6));
           	else if(authorizationHeader.startsWith("Bearer "))           
           		accessToken = new AccessToken(authorizationHeader.substring(7));
        }       
        if (token == null && accessToken == null && credentials == null) {
            String clientAddress = request.getRemoteAddr();
            int clientPort = request.getRemotePort();
            client = new StringBuilder().append(clientAddress).append(":").append(clientPort).toString();
            String sid = this.anonymous.get(client);
            if (sid != null) 
                token = new SessionToken(sid);
            else {
                try {
                    endpoint = mediator.getNorthboundEndpoints().getEndpoint();
                    this.anonymous.put(client, endpoint.getSessionToken());

                } catch (InvalidCredentialException e) {
                    LOG.debug(e.getMessage());
                }
            }
        }
        if (credentials != null) {
            try {
                LoginResponse response = mediator.getAccessingEndpoint().createNorthboundEndpoint(credentials);
                token = new SessionToken(response.getToken());
            } catch (InvalidCredentialException | NullPointerException e) {
                LOG.debug(e.getMessage());
            }
        }
        if (accessToken != null) {
            try {
                LoginResponse response = mediator.getAccessingEndpoint().createNorthboundEndpoint(accessToken);
                token = new SessionToken(response.getToken());
            } catch (InvalidCredentialException | NullPointerException e) {
                LOG.debug(e.getMessage());
            }
        }
        if (token != null) {
            try {
                endpoint = mediator.getNorthboundEndpoints().getEndpoint(token);
            } catch (InvalidCredentialException e) {
                LOG.debug(e.getMessage());
            }
        }
        if (endpoint == null) {
            JsonObjectBuilder jsonObject = JsonProviderFactory.getProvider()
            		.createObjectBuilder()
            		.add("statusCode", 403)
            		.add("message", "Authentication failed");
            try {
                resp.sendError(403, jsonObject.build().toString());
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
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

/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.northbound.ws.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jetty.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.websocket.server.JettyServerUpgradeResponse;
import org.eclipse.jetty.websocket.server.JettyWebSocketCreator;
import org.eclipse.sensinact.northbound.query.api.IQueryHandler;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the web sockets pool
 */
public class WebSocketCreator implements JettyWebSocketCreator {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketCreator.class);

    /**
     * List of active sessions
     */
    private final List<WebSocketEndpoint> sessions = Collections.synchronizedList(new ArrayList<>());

    /**
     * Session manager, passed to web sockets
     */
    private final SensiNactSessionManager sessionManager;

    /**
     * Query handler, passed to web sockets
     */
    private final IQueryHandler queryHandler;

    /**
     * @param sessionManager Session manager
     * @param queryHandler   Query handler
     */
    public WebSocketCreator(final SensiNactSessionManager sessionManager, final IQueryHandler queryHandler) {
        this.sessionManager = sessionManager;
        this.queryHandler = queryHandler;
    }

    /**
     * @param wsEndpoint Endpoint to remove
     */
    public void deleteSocketEndpoint(WebSocketEndpoint wsEndpoint) {
        if (sessions.remove(wsEndpoint)) {
            wsEndpoint.close();
        }
    }

    /**
     * Destroy all sessions
     */
    public void close() {
        for (final WebSocketEndpoint session : sessions) {
            try {
                session.close();
            } catch (Throwable t) {
                logger.error("Error closing websocket: {}", t.getMessage(), t);
            }
        }
        sessions.clear();
    }

    @Override
    public Object createWebSocket(final JettyServerUpgradeRequest req, final JettyServerUpgradeResponse resp) {
        final WebSocketEndpoint wsConnection = new WebSocketEndpoint(this, sessionManager, queryHandler);
        sessions.add(wsConnection);
        return wsConnection;
    }
}

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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.websocket.server.JettyServerUpgradeResponse;
import org.eclipse.jetty.websocket.server.JettyWebSocketCreator;
import org.eclipse.sensinact.northbound.query.api.IQueryHandler;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionActivityChecker;
import org.eclipse.sensinact.northbound.session.SensiNactSessionExpirationListener;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the web sockets pool
 */
public class WebSocketCreator
        implements JettyWebSocketCreator, SensiNactSessionActivityChecker, SensiNactSessionExpirationListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketCreator.class);

    /**
     * Maps active session ID to web socket endpoint
     */
    private final Map<String, WebSocketEndpoint> sessions = Collections.synchronizedMap(new HashMap<>());

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
        if (sessions.remove(wsEndpoint.getSessionId(), wsEndpoint)) {
            wsEndpoint.close();
        }
    }

    /**
     * Destroy all sessions
     */
    public void close() {
        // Use a copy of the sessions as the map will be updated upon session closure
        for (final WebSocketEndpoint session : List.copyOf(sessions.values())) {
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
        UserInfo userInfo = (UserInfo) req.getServletAttribute(WebSocketJettyRegistrar.SENSINACT_USER_INFO);

        final SensiNactSession snaSession = sessionManager.createNewSession(userInfo, this);
        if (snaSession == null) {
            logger.error("Unable to create SensiNact session for user: {}", userInfo);
            return null;
        }
        snaSession.addExpirationListener(this);

        final WebSocketEndpoint wsConnection = new WebSocketEndpoint(this, snaSession, queryHandler);
        sessions.put(snaSession.getSessionId(), wsConnection);
        return wsConnection;
    }

    @Override
    public Promise<Boolean> checkActivity(final PromiseFactory pf, final SensiNactSession session) {
        WebSocketEndpoint wsEndpoint = sessions.get(session.getSessionId());
        if (wsEndpoint == null) {
            // No more websocket associated to this session
            return pf.resolved(false);
        }

        return wsEndpoint.checkActivity(pf);
    }

    @Override
    public void sessionExpired(SensiNactSession session) {
        WebSocketEndpoint wsEndpoint = sessions.remove(session.getSessionId());
        if (wsEndpoint != null) {
            // Close the associated web socket
            wsEndpoint.close();
        }
    }
}

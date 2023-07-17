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
package org.eclipse.sensinact.northbound.websocket.integration;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class WSHandler {
    Consumer<Session> onConnect;
    BiConsumer<Session, Integer> onClose;
    BiConsumer<Session, String> onMessage;
    BiConsumer<Session, Throwable> onError;

    @OnWebSocketConnect
    public void onConnect(Session session) {
        if (onConnect != null) {
            onConnect.accept(session);
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        if (onClose != null) {
            onClose.accept(session, statusCode);
        }
    }

    @OnWebSocketMessage
    public void onMsg(Session session, String msg) {
        if (onMessage != null) {
            onMessage.accept(session, msg);
        }
    }

    @OnWebSocketError
    public void onError(Session session, Throwable t) {
        if (onError != null) {
            onError.accept(session, t);
        }
    }
}

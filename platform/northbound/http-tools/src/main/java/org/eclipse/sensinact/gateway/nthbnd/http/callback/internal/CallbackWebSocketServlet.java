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
package org.eclipse.sensinact.gateway.nthbnd.http.callback.internal;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackService;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.WebSocketCallbackContext;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.WebSocketRequestWrapper;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.WebSocketResponseWrapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A CallbackWebSocketWrapper is a websocket {@link Session} wrapper
 *  
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class CallbackWebSocketServlet {
	
    private Logger LOG = LoggerFactory.getLogger(CallbackWebSocketServlet.class.getName());
    
	private CallbackService callbackService;
	private Mediator mediator;
    protected Session session;

    /**
     * Constructor
     * 
     * @param mediator the {@link Mediator} allowing the CallbackWebSocketServlet to be 
     * instantiated to interact with the OSGi host environment
     * @param callbackService the {@link CallbackService} allowing the
     * CallbackWebSocketServlet to be instantiated to process the callback request
     */ 
    protected CallbackWebSocketServlet(Mediator mediator, CallbackService callbackService) {
        this.callbackService = callbackService;
        this.mediator = mediator;
    }
    
    /**
     * Called when the associated websocket is connected and passed
     * the relative {@link Session} as parameter 
     * 
     * @param session the associated websocket's {@link Session}
     */
    @OnWebSocketConnect
    public void onConnect(Session session) {
        LOG.debug("new websocket connection was open");
        this.session = session;
    }

    /**
     * 
     * @param statusCode
     * @param reason
     */
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        LOG.debug("");
    }

    /**
     * Receives Text Message events
     *
     * @param message the received message
     */
    @OnWebSocketMessage
    public void onMessage(String message) {
        final WebSocketCallbackContext context = new WebSocketCallbackContext(mediator,
        new WebSocketRequestWrapper(message), new WebSocketResponseWrapper(this));
        try {
            if (this.callbackService != null) {
            	this.callbackService.process(context);
            }
        } catch (Exception | Error e) {
            e.printStackTrace();
            this.writeMessage(new JSONObject(
            	).put("statusCode",520
            	).put("message","Internal server error"
            	).toString());
        } 
    }

    /**
     * Receives websocket errors (exceptions) that have
     * occurred internally in the websocket implementation.
     *
     * @param error the {@link Throwable} error that occurred
     */
    @OnWebSocketError
    public void handleError(Throwable error) {
    	LOG.error("An error occurred:", error);
    }

    /**
     * Close the websocket {@link Session} wrapped by this CallbackWebSocketWrapper
     */
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
	 * @param message
	 */
	public void writeMessage(String message) {
        if (this.session == null || !this.session.isOpen()) {
            return;
        }
        try {
			this.session.getRemote().sendString(message);
		} catch (IOException | NullPointerException e) {
	    	LOG.error("An error occurred:", e);
		}
	}
}

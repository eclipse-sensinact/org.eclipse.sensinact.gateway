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
package org.eclipse.sensinact.gateway.nthbnd.http.callback.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.nthbnd.http.callback.CallbackService;

/**
 * A CallbackWebSocketPool is a {@link WebSocketCreator} that holds the Set 
 * of websockets wrapped by {@link CallbackWebSocketServlet}s it created
 *  
 * @author <a href="mailto:cmunilla@kentyou.com">Christophe Munilla</a>
 */
public class CallbackWebSocketPool implements WebSocketCreator {
	
    private List<CallbackWebSocketServlet> sessions;
	private CallbackService callbackService;
	private Mediator mediator;

    /**
     * Constructor
     */
    public CallbackWebSocketPool(Mediator mediator, CallbackService callbackService) {
        this.callbackService = callbackService;
        this.mediator = mediator;
        this.sessions = Collections.synchronizedList(new ArrayList<CallbackWebSocketServlet>());
    }

    /**
     * Closes this CallbackWebSocketPool by closing and removing all the held {@link CallbackWebSocketServlet}
     */
    public void close() {
    	while(true) {
    		if(sessions.isEmpty()) {
    			break;
    		}
    		this.sessions.remove(0).close();
    	}
    }

    @Override
    public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
        CallbackWebSocketServlet wrapper = new CallbackWebSocketServlet(mediator,callbackService);
        if (wrapper != null) {
            this.sessions.add(wrapper);
        }
        return wrapper;
    }
}

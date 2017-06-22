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

import org.json.JSONObject;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.nthbnd.rest.internal.Callback;

/**
 * This class is a wrapper for simple callback subscription
 */
public class WsCallback extends Callback
{
	private WebSocketWrapper socket;

    public WsCallback(
    		Mediator mediator,
    		WebSocketWrapper socket, 
    		JSONObject conditions)
    {
        super(mediator, conditions);
        this.socket = socket;    
    }

    /**
     * @inheritDoc
     *
     * @see Callback#
     * doCallback(java.lang.String, SnaMessage)
     */
    public void doCallback(String callbackId, SnaMessage message) 
    		throws Exception
    {
    	 this.socket.send(message.getJSON());
    }
    
}

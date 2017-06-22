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

package org.eclipse.sensinact.gateway.nthbnd.rest.internal.http;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.nthbnd.rest.internal.Callback;

/**
 * This class is a wrapper for long polling callback subscription
 */
public class LongPollingCallback extends Callback
{
	private HttpServletResponse response;
    
    /**
     * Constructor
     * 
     * @param response
     * @param mediator
     */
    public LongPollingCallback(HttpServletResponse response, Mediator mediator) 
    {
        super(mediator);
        this.response = response;
    }

    /**
     * @inheritDoc
     * 
     * @see Recipient#callback(String, SnaMessage[])
     */
    public void doCallback(String callbackId, SnaMessage message) 
    		throws Exception
    {
    	this.response.setStatus(200);
    	this.response.getWriter().println(message.getJSON());
    }
}

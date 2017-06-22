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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;


public class WebSocketWrapperPool implements WebSocketCreator
{    
	private NorthboundMediator mediator;	
    private List<WebSocketWrapper> sessions;

    /**
     * @param mediator
     */
    public WebSocketWrapperPool(NorthboundMediator mediator)
    {
    	this.mediator = mediator;
    	this.sessions = new ArrayList<WebSocketWrapper>();
    }    
	
    /**
     * @param wsWrapper
     */
    public void removeSensinactSocket(WebSocketWrapper wsWrapper)
    {
        this.mediator.info(String.format(
        		"Removing session for client address %s.", 
        		wsWrapper.getClientAddress()));   
        
        Boolean removedSuccessfully = true;

    	synchronized(this.sessions)
		{
    		if((removedSuccessfully = this.sessions.remove(wsWrapper)))
    		{
    			wsWrapper.close();
    		}
		}
        this.mediator.warn(String.format(
        		"Session removal %s executed.",
        		removedSuccessfully?"successfully":"not"));
    }
    
    /**
     * 
     */
    public void close()
    {
    	synchronized(this.sessions)
		{
    		while(!this.sessions.isEmpty())
    		{
    			this.sessions.remove(0).close();
    		}
		}
    }

	/** 
	 * @inheritDoc
	 * 
	 * @see org.eclipse.jetty.websocket.servlet.WebSocketCreator#
	 * createWebSocket(org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest, 
	 * org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse)
	 */
	@Override
	public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) 
	{
		WebSocketWrapper wrapper =  new WebSocketWrapper(this, this.mediator);
		if(wrapper != null)
		{
			synchronized(this.sessions)
			{
				this.sessions.add(wrapper);
			}
		}
		return wrapper;
	}
}

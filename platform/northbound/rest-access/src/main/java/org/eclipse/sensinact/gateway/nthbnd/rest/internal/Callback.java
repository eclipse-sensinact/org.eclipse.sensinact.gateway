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

package org.eclipse.sensinact.gateway.nthbnd.rest.internal;

import org.json.JSONObject;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaCallback;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRecipient;

/**
 * This abstract class is a wrapper for callback subscription
 */
public abstract class Callback extends NorthboundRecipient 
{
	public abstract void doCallback(String callbackId, SnaMessage message) 
			throws Exception;
	
    /**
     * Constructor
     */
    public Callback(Mediator mediator)
    {
    	super(mediator);
    }

    /**
     * @param mediator
     * @param jsonObject
     */
    public Callback(Mediator mediator, JSONObject jsonObject)
    {
        super(mediator, jsonObject);        
    }

    /**
     * @inheritDoc
     * @see Recipient#getSnaCallBackType()
     */
    public SnaCallback.Type getSnaCallBackType() {
        return SnaCallback.Type.UNARY;
    }

    /**
     * @inheritDoc
     * @see Recipient#getLifetime()
     */
    public long getLifetime() {
        return -1;
    }

    /**
     * @inheritDoc
     * @see Recipient#getBufferSize()
     */
    public int getBufferSize() {
        return 0;
    }

    /**
     * @inheritDoc
     * @see Recipient#getSchedulerDelay()
     */
    public int getSchedulerDelay() {
        return 0;
    }

    /**
     * @inheritDoc
     * @see JSONable#getJSON()
     */
    public String getJSON() {
        return null;
    }

    /**
     * @inheritDoc
     * @see Recipient#callback(String, SnaMessage[])
     */
    public void callback(final String callbackId, SnaMessage[] messages) 
    		throws Exception
    {    	
    	this.doCallback(callbackId, messages[0]);
    }
  
}

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

import org.json.JSONObject;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.nthbnd.rest.internal.Callback;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfiguration;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;

/**
 * This class is a wrapper for simple callback subscription
 */
public class ContentCallback extends Callback 
{
    private String urlCallback;
    private ConnectionConfigurationImpl<SimpleResponse,SimpleRequest> connectionBuilder;

    public ContentCallback(Mediator mediator,
    		String callback, JSONObject jsonObject)
    {
        super(mediator, jsonObject);        
        this.urlCallback = callback;
        this.connectionBuilder = 
        		new ConnectionConfigurationImpl<SimpleResponse,SimpleRequest>();
        this.connectionBuilder.setContentType("application/json");
        this.connectionBuilder.setHttpMethod(ConnectionConfiguration.POST);
    }

    /**
     * @inheritDoc
     * @see Recipient#callback(String, SnaMessage[])
     */
    public void doCallback(final String callbackId, final SnaMessage message) 
    		throws Exception
    {    	
		try 
	    {
			String separator = urlCallback.endsWith("/")?"":"/";
			String uri = new StringBuilder().append(urlCallback).append(separator
					).append(callbackId).toString();
			
	    	this.connectionBuilder.setUri(uri);
	    	this.connectionBuilder.setContent(message.getJSON());
	    	this.connectionBuilder.setHttpMethod("POST");
	    	this.connectionBuilder.setContentType("application/json");
	    	
	    	SimpleRequest request = new SimpleRequest(
	    			ContentCallback.this.connectionBuilder);	    	
	    	//SimpleResponse response = 
	    	request.send();	    	
	    	//super.mediator.debug(response.toString());

    	} catch(Exception e)  
	    {
    		ContentCallback.this.mediator.error(e.getMessage(),e);
	    }       
    }
}

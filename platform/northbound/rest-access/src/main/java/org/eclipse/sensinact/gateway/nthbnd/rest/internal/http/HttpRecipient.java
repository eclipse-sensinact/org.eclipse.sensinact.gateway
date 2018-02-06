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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRecipient;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfiguration;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.eclipse.sensinact.gateway.util.JSONUtils;

/**
 * This class is a wrapper for simple callback subscription
 */
public class HttpRecipient extends NorthboundRecipient 
{
    private String urlCallback;
    private ConnectionConfigurationImpl<SimpleResponse,SimpleRequest> connectionBuilder;

    public HttpRecipient(Mediator mediator,
    		String callback)
    {
        super(mediator);        
        this.urlCallback = callback;
        this.connectionBuilder = new ConnectionConfigurationImpl<SimpleResponse,SimpleRequest>();
        this.connectionBuilder.setContentType("application/json");
        this.connectionBuilder.setHttpMethod(ConnectionConfiguration.POST);
    }

    /**
     * @inheritDoc
     * @see Recipient#callback(String, SnaMessage[])
     */
    public void callback(String callbackId, SnaMessage[] messages)
    {    	
		int index = 0;
		int length = messages==null?0:messages.length;
		
		StringBuilder builder = new StringBuilder();
		builder.append(JSONUtils.OPEN_BRACE);
		builder.append("\"type\":\"CALLBACK\",");
		builder.append("\"callbackId\":");
		if(callbackId==null)
		{
			builder.append("null");			
		} else
		{
			builder.append("\"");
			builder.append(callbackId);
			builder.append("\"");			
		}
		builder.append(",\"messages\":");
		builder.append(JSONUtils.OPEN_BRACKET);
		for(;index < length; index++)
		{
			builder.append(index==0?"":",");
			builder.append(messages[index].getJSON());
		}
		builder.append(JSONUtils.CLOSE_BRACKET);
		builder.append(JSONUtils.CLOSE_BRACE);
		
		try 
	    {
			String uri = null;
			if(callbackId != null)
			{
				String separator = urlCallback.endsWith("/")?"":"/";
				uri = new StringBuilder().append(urlCallback
				).append(separator).append(callbackId
						).toString();
			} else
			{
				uri = urlCallback; 
			}
			
	    	this.connectionBuilder.setUri(uri);
	    	this.connectionBuilder.setContent(builder.toString());
	    	this.connectionBuilder.setHttpMethod("POST");
	    	this.connectionBuilder.setContentType("application/json");
	    	
	    	SimpleRequest request = new SimpleRequest(
	    			HttpRecipient.this.connectionBuilder);	    	
	    	//SimpleResponse response = 
	    	request.send();	    	
	    	//super.mediator.debug(response.toString());

    	} catch(Exception e)  
	    {
    		super.mediator.error(e);
	    }       
    }
}

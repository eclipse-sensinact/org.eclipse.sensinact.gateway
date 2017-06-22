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
package org.eclipse.sensinact.gateway.protocol.http.test;

import java.io.IOException;
//import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.sensinact.gateway.protocol.http.server.Content;
import org.eclipse.sensinact.gateway.protocol.http.server.RequestContent;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.protocol.http.server.HttpResponseContent;

class HttpServerTestCallback
{	
	//private AtomicBoolean available;
	
//	JettyTestServerCallback()
//	{
//		//this.available = new AtomicBoolean(false);
//	}

	@doPost
	public Content callbackPost(RequestContent request)
			throws IOException
	{		
		JSONObject requestDescription = new JSONObject();
		requestDescription.put("method", "POST");
		if(request.getQueryString()!=null)
		{
			requestDescription.put("url", 
				request.getRequestURI()+"?"+request.getQueryString());
			
		} else
		{
			requestDescription.put("url", request.getRequestURI());
		}
		requestDescription.put("content-type", 
				request.getHeaderAsString("Content-type"));
		requestDescription.put("content-length", Integer.parseInt(
				request.getHeaderAsString("Content-length")));	
		
		byte[] content = request.getContent();			
		String message = new String(content);
		requestDescription.put("message", new JSONObject(message));
		
		HttpResponseContent response = new HttpResponseContent();
		response.addHeader("Content-Type","application/json");
		response.setContent(requestDescription.toString().getBytes());
		return response;
	}
	
	@doGet
	public Content callbackGet(RequestContent request) 
			throws IOException
	{		
		JSONObject requestDescription = new JSONObject();
		requestDescription.put("method", "GET");
		if(request.getQueryString()!=null)
		{
			requestDescription.put("url", 
				request.getRequestURI()+"?"+request.getQueryString());
			
		} else
		{
			requestDescription.put("url", request.getRequestURI());
		}		
		HttpResponseContent response = new HttpResponseContent();
		response.addHeader("Content-Type","application/json");
		response.setContent(requestDescription.toString().getBytes());
		return response;
//		finally
//		{
//			this.setAvailable(true);
//		}
		
	}

//	private void setAvailable(boolean available) 
//	{
//		this.available.set(available);
//	}
//
//	public boolean isAvailable() 
//	{
//		return this.available.get();
//	}
}
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
package org.eclipse.sensinact.gateway.sthbnd.http.test;

import java.io.IOException;
//import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;

class JettyServerTestCallback
{
	private enum PATH
	{
		get,
		services,
		json1,
		json2,
		json3;
	}
	private JSONObject remoteEntity;

	@doPost
	public void callbackPost(HttpServletRequest request, HttpServletResponse response) 
			throws IOException, JSONException
	{		
		try
		{
			byte[] content = IOUtils.read(request.getInputStream());				
			JSONObject message = new JSONObject(new String(content));
			this.remoteEntity.put("data", message.get("value"));
			response.setStatus(200);
			
		} catch (IOException e) 
		{
			response.setStatus(520);
		} 
	}
	
	@doGet
	public void callbackGet(HttpServletRequest request, HttpServletResponse response) 
			throws IOException, JSONException
	{	
		String uri =  request.getRequestURI();
		
		String[] uriElements = UriUtils.getUriElements(uri);
		
		try
		{
			PATH path = PATH.valueOf(uriElements[uriElements.length-1]);
			switch(path)
			{
				case get:
					response.setContentType("application/json");
					response.setContentLength(this.remoteEntity.toString().length());
					
					if(response.getOutputStream().isReady())
					{
						response.getOutputStream().println(this.remoteEntity.toString());
					}
					response.setStatus(200);					
					break;
				case services:
					JSONObject object = new JSONObject();
					object.put("serviceProviderId", uriElements[1]);
					
					JSONArray services = new JSONArray();
					services.put("service1").put("service2").put("service3");
					object.put("services", services);
					try
					{
						response.setContentType("application/json");
						response.setContentLength(object.toString().length());
						
						if(response.getOutputStream().isReady())
						{
							response.getOutputStream().println(object.toString());
						}
						response.setStatus(200);
					} catch (IOException e) 
					{
						response.setStatus(520);			
					} 
					
					break;
				case json1:
					response.setContentType("application/json");

					object = new JSONObject();
					object.put("serviceProviderId", this.remoteEntity.getString("serviceProviderId"));
					response.setContentLength(object.toString().length());
					
					if(response.getOutputStream().isReady())
					{
						response.getOutputStream().println(object.toString());
					}
					response.setStatus(200);
					break;
				case json2:
					response.setContentType("text/plain");
					response.setContentLength(this.remoteEntity.getString("serviceId").length());
					
					if(response.getOutputStream().isReady())
					{
						response.getOutputStream().println(this.remoteEntity.getString("serviceId"));
					}
					response.setStatus(200);
					break;
				case json3:
					response.setContentType("application/json");
					JSONArray array = new JSONArray();
					array.put(this.remoteEntity.getString("resourceId"));
					array.put(this.remoteEntity.get("data"));
					response.setContentLength(array.toString().length());
					
					if(response.getOutputStream().isReady())
					{
						response.getOutputStream().println(array.toString());
					}
					response.setStatus(200);
					break;
				default:
					break;
			}
		} catch (IOException e) 
		{
			response.setStatus(520);			
		} 		
	}

	public void setRemoteEntity(JSONObject remoteEntity)
	{
		this.remoteEntity = remoteEntity;
	}
}
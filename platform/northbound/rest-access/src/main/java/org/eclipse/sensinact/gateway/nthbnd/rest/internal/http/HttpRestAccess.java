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

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponseWrapper;

import org.eclipse.sensinact.gateway.nthbnd.rest.internal.RestAccess;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequest;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestBuilder;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.format.JSONResponseFormat;

public class HttpRestAccess extends RestAccess
{
	public static String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
	public static String TEXT_CONTENT_TYPE = "text/plain; charset=utf-8";
	
	private HttpServletResponseWrapper response;

	public HttpRestAccess(HttpServletResponseWrapper response)
			throws IOException
	{
		this.response = response;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see RestAccess#
	 * respond(NorthboundRequestBuilder)
	 */
	@Override
	protected boolean respond(NorthboundRequestBuilder<JSONObject> builder) 
			throws IOException 
	{	
		response.addHeader("X-Auth-Token", super.endpoint.getSessionToken());
		
		NorthboundRequest<JSONObject> nthbndRequest = builder.build();	
		if(nthbndRequest == null)
		{
			sendError(500, "Internal server error");
			return false;
		}
		JSONObject result = super.endpoint.execute(nthbndRequest, 
				new JSONResponseFormat(mediator));

		if(result == null)
		{
			sendError(500, "Internal server error");
			return false;
		}	
		String resultStr = result.toString();
		byte[] resultBytes = resultStr.getBytes("UTF-8");
		int length = -1;
		
		if((length = resultBytes==null?0:resultBytes.length) > 0)
		{
			response.setContentType(JSON_CONTENT_TYPE);
			response.setContentLength(resultBytes.length);
			response.setBufferSize(resultBytes.length);
			
			ServletOutputStream output = this.response.getOutputStream();
			output.write(resultBytes, 0, length);	
		}
		response.setStatus(result.getInt("statusCode"));
		return true;
		
	}

	/**
	 * @inheritDoc
	 *
	 * @see RestAccess#
	 * sendError(int, java.lang.String)
	 */
	@Override
	@SuppressWarnings("deprecation")
	protected void sendError(int statusCode, String message) 
			throws IOException 
	{
		this.response.setStatus(statusCode, message);
	}

	/**
	 * Returns the called {@link AccessMethod.Type}
	 * 
	 * @return the called {@link AccessMethod.Type}
	 */
	public AccessMethod.Type getMethod() 
	{
		return super.method;
	}
}

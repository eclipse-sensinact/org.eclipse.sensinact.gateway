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

import javax.servlet.AsyncContext;
import javax.servlet.WriteListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;

/**
 * This class is the REST interface between each others classes 
 * that perform a task and jersey
 */
@SuppressWarnings("serial")
@WebServlet(asyncSupported=true)
public class HttpEndpoint extends HttpServlet
{		
	
	private static final String JSON_CONTENT_TYPE = "application/json";

	private NorthboundMediator mediator;

	/**
	 * Constructor
	 */
	public HttpEndpoint(NorthboundMediator mediator)
	{
		this.mediator = mediator;
	}

	/**
	 * @throws IOException 
	 * @inheritDoc
	 *
	 * @see javax.servlet.http.HttpServlet#
	 * doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
    public void doGet(HttpServletRequest request, 
    		HttpServletResponse response) 
    		throws IOException
	{
        this.doExecute(request, response, true);
	}
	
	/**
	 * @throws IOException 
	 * @inheritDoc
	 *
	 * @see javax.servlet.http.HttpServlet#
	 * doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
    public void doPost(HttpServletRequest request, 
    		HttpServletResponse response) throws IOException
	{
		this.doExecute(request, response, false);
	}
	

	private final void doExecute(HttpServletRequest request, 
    		HttpServletResponse response, final boolean doGet) 
    				throws IOException
	{
        if(response.isCommitted())
        {
        	return;
        }        
        final AsyncContext asyncContext = request.startAsync(request,response);
        response.getOutputStream().setWriteListener(new WriteListener()
        {
			@Override
			public void onWritePossible() throws IOException 
			{
				 HttpServletRequest request = (HttpServletRequest) 
		        		asyncContext.getRequest();
		        
				 HttpServletResponse response = (HttpServletResponse) 
		        		asyncContext.getResponse();
		        
			     try
			     {
					HttpRestAccess restAccess = new HttpRestAccess(
							new HttpServletResponseWrapper(
							response));	
					
					HttpRestAccessRequest wrapper = new HttpRestAccessRequest(
							mediator, request);
	
					if(restAccess.init(wrapper))
					{
				        AccessMethod.Type method  = restAccess.getMethod();			        
				        boolean getOrDescribe = method.equals(AccessMethod.Type.GET)
				        		|| method.equals(AccessMethod.Type.DESCRIBE);
				        
				        if((doGet && !getOrDescribe) || (!doGet && getOrDescribe))
						{
							restAccess.sendError(400, "Bad Request");						
						}
				        else if(!doGet && !JSON_CONTENT_TYPE.equals(request.getContentType()))
				        {
				    		restAccess.sendError(415, "Unsupported Media Type");
				    		
				    	} else
				        {
				        	restAccess.handle();
				        }
					}
				} catch (Exception e) 
				{
					mediator.error(e);
					
				} finally
				{
		            asyncContext.complete();
				}
			}

			@Override
			public void onError(Throwable t) {
				mediator.error(t);
			}
        	
        });
	}
}

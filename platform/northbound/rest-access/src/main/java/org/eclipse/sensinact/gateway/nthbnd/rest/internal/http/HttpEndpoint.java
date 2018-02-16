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
	 * @inheritDoc
	 *
	 * @see javax.servlet.http.HttpServlet#
	 * doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
    		throws IOException
	{
		if(!JSON_CONTENT_TYPE.equals(request.getHeader("Accept")))
		{
			response.sendError(406, "Not Acceptable");
		}
        this.doExecute(request, response);
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see javax.servlet.http.HttpServlet#
	 * doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    		throws IOException
	{
		if(!JSON_CONTENT_TYPE.equals(request.getContentType()))
		{
			response.sendError(415, "Unsupported Media Type");
		}
		if(!JSON_CONTENT_TYPE.equals(request.getHeader("Accept")))
		{
			response.sendError(406, "Not Acceptable");
		}
		this.doExecute(request, response);
	}
	

	private final void doExecute(HttpServletRequest request, 
    		HttpServletResponse response) throws IOException
	{
        if(response.isCommitted())
        {
        	return;
        } 
        final AsyncContext asyncContext;  
        if(request.isAsyncStarted())
        {
        	asyncContext = request.getAsyncContext();
        	
        } else
        {
        	asyncContext = request.startAsync(request,response);
        }        
        response.getOutputStream().setWriteListener(new WriteListener()
        {
			@Override
			public void onWritePossible() throws IOException 
			{
				 HttpServletRequest request = 
				     (HttpServletRequest) asyncContext.getRequest();		        
				 HttpServletResponse response = 
				    (HttpServletResponse) asyncContext.getResponse();
		        
			     try
			     {
					HttpRestAccess restAccess = new HttpRestAccess(
					    new HttpRestAccessRequest(mediator, request),
					    new HttpServletResponseWrapper(response));	

					restAccess.proceed();
					
				} catch (Exception e) 
				{
					mediator.error(e);
					
				} finally
				{
					if(request.isAsyncStarted())
					{
						asyncContext.complete();
					}
				}
			}

			@Override
			public void onError(Throwable t) {
				mediator.error(t);
			}
        	
        });
	}
}

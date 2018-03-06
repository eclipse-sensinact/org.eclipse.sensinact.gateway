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

import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.AuthenticationToken;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundEndpoint;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;

/**
 * This class is the REST interface between each others classes 
 * that perform a task and jersey
 */
@SuppressWarnings("serial")
@WebServlet(asyncSupported=true)
public class HttpLoginEndpoint extends HttpServlet
{		
	private NorthboundMediator mediator;

	/**
	 * Constructor
	 */
	public HttpLoginEndpoint(NorthboundMediator mediator)
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
				 Authentication<?> authentication = null;
				 try
			     {
			    	String tokenHeader = request.getHeader("X-Auth-Token");
					String authorizationHeader = request.getHeader("Authorization");
				
					NorthboundEndpoint endpoint = null;
					
					if(tokenHeader != null)
					{
						authentication = new AuthenticationToken(tokenHeader);

						if(mediator.getLoginEndpoint().reactivateEndpoint(
								(AuthenticationToken) authentication))
						{
							response.setHeader("X-Auth-Token", ((AuthenticationToken) 
									authentication).getAuthenticationMaterial());
							response.setStatus(200);
							
						} else
						{
							response.sendError(403, "Authentication failed");
						}						
					} else if(authorizationHeader != null)
					{
						endpoint = mediator.getLoginEndpoint().createNorthboundEndpoint(
							    new Credentials(authorizationHeader));
					
						if(endpoint != null)
						{
							response.setHeader("X-Auth-Token", endpoint.getSessionToken());
							response.setStatus(200);
							
						} else
						{
							response.sendError(403, "Authentication failed");
						}
					}
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

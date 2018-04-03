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
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.AuthenticationToken;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRecipient;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequest;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.json.JSONObject;

public class HttpRestAccessRequest extends HttpServletRequestWrapper 
implements NorthboundRequestWrapper
{	
	private NorthboundMediator mediator;
	private Map<String,List<String>> queryMap;
	private Authentication<?> authentication;
	private String content;
	
	/**
	 * Constructor
	 * 
	 * @param mediator the {@link NorthboundMediator} allowing to interact
	 * with the OSGi host environment
	 * 
	 * @param request the {@link HttpServletRequest} to be wrapped by
	 * the HttpRestAccessRequest to be instantiated
	 * 
	 * @throws InvalidCredentialException 
	 */
	public HttpRestAccessRequest(NorthboundMediator mediator, 
		HttpServletRequest request) throws InvalidCredentialException
	{
		super(request);
		this.mediator = mediator;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper#
	 * getMediator()
	 */
	@Override
	public NorthboundMediator getMediator() 
	{
		return this.mediator;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper#
	 * getQueryMap()
	 */
	@Override
	public Map<String,List<String>> getQueryMap() 
	{
		if(this.queryMap == null)
		{
			try
			{
				this.queryMap = NorthboundRequest.processRequestQuery(
						super.getQueryString());
			}
			catch (UnsupportedEncodingException e)
			{
				mediator.error(e.getMessage(),e);
				this.queryMap = 
					Collections.<String,List<String>>emptyMap();
			}
		}
		return queryMap;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper#
	 * getContent()
	 */
	@Override
	public String getContent() 
	{
		if(this.content == null)
		{
			try
			{
				ServletInputStream input = super.getInputStream();				
				byte[] stream = IOUtils.read(input,
						super.getContentLength(),true);
				this.content = new String(stream);
				
			} catch (IOException e)
			{
				this.mediator.error(e.getMessage(), e);
			}
		}
		return this.content;
	}

	/**
	 * Define the {@link Authentication} attached to this 
	 * HttpRestAccessRequest
	 * 
	 * @param authentication the {@link Authentication} of this
	 * HttpRestAccessRequest
	 */
	void setAuthentication(Authentication<?> authentication) 
	{
		this.authentication = authentication;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper#
	 * getAuthentication()
	 */
	@Override
	public Authentication<?> getAuthentication() 
	{
		if(this.authentication == null)
		{
			String tokenHeader = super.getHeader("X-Auth-Token");
			String authorizationHeader = super.getHeader("Authorization");
			
			if(tokenHeader != null)
			{
				this.authentication = new AuthenticationToken(tokenHeader);
		
			} else if(authorizationHeader != null)
			{
				this.authentication =  new Credentials(authorizationHeader);
			}
		}
		return this.authentication;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper#
	 * getRequestID(org.eclipse.sensinact.gateway.core.method.Parameter[])
	 */
	@Override
	public String getRequestID(Parameter[] parameters)
	{
		String rid = null;
		
		int index = 0;
		int length = parameters==null?0:parameters.length;
		for(;index < length; index++)
		{
			if("rid".equals(parameters[index].getName()))
			{
				rid = (String) parameters[index].getValue();
			}
		}
		if(rid == null)
		{
			rid = super.getHeader("rid");
		}
		return rid;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequestWrapper#
	 * createRecipient(org.eclipse.sensinact.gateway.core.method.Parameter[])
	 */
	@Override
	public NorthboundRecipient createRecipient(Parameter[] parameters)
	{
		NorthboundRecipient recipient = null;
		
		int index = 0;
		int length = parameters==null?0:parameters.length;				
		String callback = null;
		JSONObject conditions = null;
		
		for(;index < length; index++)
		{
			String name = parameters[index].getName();
			if("callback".equals(name))
			{
				callback = (String) parameters[index].getValue();
				break;
			}
		}
		if(callback != null)
		{
			recipient = new HttpRecipient(mediator, callback);
		}
		return recipient;
	}

	/**
	 * 
	 */
	public void destroy()
	{
		this.queryMap = null;
		this.authentication = null;
		this.content = null;
	}

}

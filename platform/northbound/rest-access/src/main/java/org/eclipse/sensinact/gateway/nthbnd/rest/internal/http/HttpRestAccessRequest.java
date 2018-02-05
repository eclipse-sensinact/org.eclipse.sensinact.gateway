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
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccessWrapper;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRecipient;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequest;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.json.JSONObject;

public class HttpRestAccessRequest extends HttpServletRequestWrapper 
implements NorthboundAccessWrapper
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
	 */
	public HttpRestAccessRequest(NorthboundMediator mediator, 
			HttpServletRequest request)
	{
		super(request);
		if(mediator == null)
		{
			throw new NullPointerException("Mediator needed");
		}
		this.mediator = mediator;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccessWrapper#getMediator()
	 */
	@Override
	public NorthboundMediator getMediator() 
	{
		return this.mediator;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccessWrapper#getQueryMap()
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
	 * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccessWrapper#getContent()
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
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccessWrapper#getAuthentication()
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
	 * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccessWrapper#getRequestID(org.eclipse.sensinact.gateway.core.method.Parameter[])
	 */
	@Override
	public String getRequestID(Parameter[] parameters)
	{
		int index = 0;
		int length = parameters==null?0:parameters.length;
		for(;index < length; index++)
		{
			if("rid".equals(parameters[index].getName()))
			{
				return (String) parameters[index].getValue();
			}
		}
		List<String> list = getQueryMap().get("rid");
		if(list!=null && list.size()>0)
		{
			return list.get(0);
		}
		return null;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccessWrapper#createRecipient(org.eclipse.sensinact.gateway.core.method.Parameter[])
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

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
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.eclipse.sensinact.gateway.core.method.Parameter;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.AuthenticationToken;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccess;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundAccessWrapper;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRecipient;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.json.JSONObject;

public class HttpRestAccessRequest extends HttpServletRequestWrapper 
implements NorthboundAccessWrapper
{
	/**
	 * Builds a map of parameters according to the query of 
	 * the URI of an HTTP request
	 * 
	 * @param queryString the query String to be converted 
	 * into a map of parameters
	 *   
	 * @throws UnsupportedEncodingException
	 */
	public static Map<String,List<String>> processRequestQuery(
			String queryString) 
			throws UnsupportedEncodingException
	{		
		if(queryString == null)
		{
			return Collections.<String,List<String>>emptyMap();
		}
		Map<String,List<String>> queryMap = 
				new HashMap<String,List<String>>();
		
		char[] characters = queryString.toCharArray();
		int index = 0;
		int length = characters.length;
		
		boolean escape = false;
		String name = null;
		String value = null;
		StringBuilder element = new StringBuilder();
		
		for(;index < length;index++)
		{
			char c = characters[index];
			if(escape)
			{
				escape = false;
				element.append(c);
				continue;
			}
			switch(c)
			{
				case '\\':
				  escape = true;
				  break;
				case '?':
				  break;
				case '=':
				  name = element.toString();
				  element = new StringBuilder();
				  break;
				case '&':	
				  value = element.toString();
				  addQueryParameter(queryMap, name, value);
				  element = new StringBuilder();
				  break;
				default:
				  element.append(c);
			}
		}
		value = element.toString();
		addQueryParameter(queryMap, name, value);
		return queryMap;
	}

	/**
	 * Adds a parameter to the map argument, created using 
	 * the name and value passed as parameters 
	 *  
	 * @param queryMap the map to which to add the 
	 * parameter to be created using the name and 
	 * value arguments
	 * @param name the name of the parameter to be
	 * added to the map argument
	 * @param value the value of the parameter to be
	 * added to the map argument 
	 * 
	 * @throws UnsupportedEncodingException
	 */
	private static void addQueryParameter(
			Map<String,List<String>> queryMap, 
			String name, String value) 
			throws UnsupportedEncodingException
	{
		if(name == null || name.length() == 0)
		{	
			name = NorthboundAccess.RAW_QUERY_PARAMETER;
			
		} else
		{
			name = URLDecoder.decode(name,"UTF-8");
		}
		List<String> values = queryMap.get(name);
		if(values == null)
		{
			values = new ArrayList<String>();
			queryMap.put(name, values);
		}
		values.add(URLDecoder.decode(value, "UTF-8"));
	}	
	
	private NorthboundMediator mediator;
	private Map<String,List<String>> queryMap;
	private Authentication<?> authentication;
	private String content;

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
	 * @see NorthboundAccessWrapper#getMediator()
	 */
	@Override
	public NorthboundMediator getMediator() 
	{
		return this.mediator;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see NorthboundAccessWrapper#getQueryMap()
	 */
	@Override
	public Map<String,List<String>> getQueryMap() 
	{
		if(this.queryMap == null)
		{
			try
			{
				this.queryMap = processRequestQuery(
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
	 * @see NorthboundAccessWrapper#getContent()
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
	 * @see NorthboundAccessWrapper#getAuthentication()
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
	 * @see NorthboundAccessWrapper#createRecipient(Parameter[])
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

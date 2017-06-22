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
package org.eclipse.sensinact.gateway.protocol.http.client.mid;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 */
@SuppressWarnings("serial")
public class HttpResponseException extends Exception
{
	private int statusCode;
	private Map<String, List<String>> headers;
	
	/**
	 * @param statusCode
	 * @param content
	 * @param map 
	 */
	public HttpResponseException(
			int statusCode, 
			byte[] content, 
			Map<String, List<String>> headers) 
	{
		super(content!=null?new String(content):"Http Error");
		this.statusCode = statusCode;
		this.headers = headers;
	}
	
	/**
	 * @return
	 */
	public int getErrorStatusCode()
	{
		return this.statusCode;
	}

	/**
	 * @return
	 */
	public Map<String,List<String>> getHeaders()
	{
		return this.headers;
	}
}

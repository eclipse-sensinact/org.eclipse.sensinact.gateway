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
package org.eclipse.sensinact.gateway.protocol.http.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.protocol.http.HeadersCollection;
import org.eclipse.sensinact.gateway.util.IOUtils;

/**
 * An Http Response
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class SimpleResponse extends HeadersCollection implements Response
{
	protected int responseCode = -1;
	protected byte[] content = null;
	protected List<Throwable> exceptions;
	
	public SimpleResponse(HttpURLConnection connection) throws IOException
	{
		super(connection.getHeaderFields());
		
		this.exceptions = new ArrayList<Throwable>();		
		this.responseCode = connection.getResponseCode();
		
		String contentLength = super.getHeaderAsString("Content-Length");			
		int length;
		try
		{
			length = contentLength==null
				?0:Integer.parseInt(contentLength.trim());
			
		} catch(NumberFormatException e)
		{
			length = 0;
		}
		byte[] bytes = null;
		try
		{
			InputStream is = connection.getInputStream(); 
			bytes = length==0?IOUtils.read(is, false)
					:IOUtils.read(is, length, false);
			
		} catch(IOException e)
		{
			this.addException(e);
		}		
		length = bytes==null?0:bytes.length;
		this.content =  new byte[length];
		
		if(length > 0)
		{
			System.arraycopy(bytes, 0, this.content, 0, length);
		}			
		connection.disconnect();
	}
	
	//TODO: allow to check catched exceptions
	//map exceptions to the different lifecycle steps
	private void addException(Exception e)
	{
		this.exceptions.add(e);
	}

	/**
	 * Returns the bytes array content of the wrapped 
	 * HTTP response
	 * 
	 * @return
	 * 		the bytes array content of the wrapped 
	 * 		HTTP response
	 */
	public byte[] getContent()
	{
		return this.content;
	}

	/**
	 * Returns the HTTP code of the wrapped 
	 * HTTP response
	 * 
	 * @return
	 * 		the wrapped HTTP response's code
	 */
	public int getStatusCode()
	{
		return this.responseCode;
	}
}

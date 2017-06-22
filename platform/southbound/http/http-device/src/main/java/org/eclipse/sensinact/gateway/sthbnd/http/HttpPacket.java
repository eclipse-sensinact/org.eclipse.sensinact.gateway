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

package org.eclipse.sensinact.gateway.sthbnd.http;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.protocol.http.HeadersCollection;

/**
 * Extended {@link Packet} wrapping an HTTP message
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class HttpPacket extends HeadersCollection implements Packet
{	
	/**
	 * the bytes array content of the wrapped
	 * HTTP message
	 */
	protected byte[] content;
	
	/**
     * Constructor
     * 
     * @param content	
	 * 		the bytes array content of the wrapped
	 * 		HTTP message
     */
    public HttpPacket (Map<String,List<String>> headers, byte[] content)
    {
    	super(headers);
    	int length = content==null?0:content.length;
    	this.content = new byte[length];
    	if(length > 0)
    	{
    		System.arraycopy(content, 0, this.content, 0, length);
    	}
    }
    
    /**
     * Constructor
     * 
     * @param content	
	 * 		the bytes array content of the wrapped
	 * 		HTTP message
     */
    public HttpPacket (byte[] content)
    {
    	this(null,content);
    }
    
    /** 
     * @inheritDoc
     *
     * @see org.eclipse.sensinact.gateway.core.model.data.api.com.Packet#getBytes()
     */
    @Override
	public byte[] getBytes() 
    {
    	int length = this.content==null?0:this.content.length;
    	byte[] content = new byte[length];
    	if(length > 0)
    	{
    		System.arraycopy(this.content, 0, content, 0, length);
    	}
    	return content;
	}
}

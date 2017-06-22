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
package org.eclipse.sensinact.gateway.protocol.http.server;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.protocol.http.HeadersCollection;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractContent extends HeadersCollection implements Content
{

	//********************************************************************//
	//						NESTED DECLARATIONS			  			      //
	//********************************************************************//

	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//

	//********************************************************************//
	//						STATIC DECLARATIONS							  //
	//********************************************************************//

	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//
	private byte[] content;
	
	/**
	 * 
	 */
	public AbstractContent()
	{
	}

	/**
	 * @param headers
	 */
	public AbstractContent(Map<String, List<String>> headers)
	{
		super(headers);
	}

	/**
	 * @inheritDoc
	 *
	 * @see Content#getContent()
	 */
	@Override
	public byte[] getContent()
	{
		int length = this.content==null?0:this.content.length;
		byte[] copy = new byte[length];
		
		if(length > 0)
		{
			System.arraycopy(this.content, 0, copy, 0, length);
		}
		return copy;
	}

	/**
	 * @inheritDoc
	 *
	 * @see Content#setContent(byte[])
	 */
	@Override
	public void setContent(byte[] content)
	{
		int length = content==null?0:content.length;
		byte[] copy = new byte[length];
		
		if(length > 0)
		{
			System.arraycopy(content, 0, copy, 0, length);
		}
		this.content = copy;
	}		
}

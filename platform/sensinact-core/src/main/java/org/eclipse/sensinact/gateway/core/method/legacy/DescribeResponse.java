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
package org.eclipse.sensinact.gateway.core.method.legacy;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.props.TypedKey;
import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.util.JSONUtils;

/**
 * Extended {@link AccessMethodResponse} returned by an 
 * {@link DescribeMethod} invocation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class DescribeResponse<T> extends AccessMethodResponse<T>
{
	protected DescribeMethod.DescribeType describeType;

	/**
	 * Constructor 
	 * 
	 * @param status
	 * 		the associated {@link Status}
	 */
    protected DescribeResponse(Mediator mediator, 
    		String uri, Status status, DescribeMethod.DescribeType describeType)
    {
	    this(mediator, uri, status, Status.SUCCESS.equals(status)
	    	?SnaErrorfulMessage.NO_ERROR:SnaErrorfulMessage.UNKNOWN_ERROR_CODE,
	    	     describeType);
    }	
    
    /**
	 * Constructor 
	 * 
	 * @param status
	 * 		the associated {@link Status}
	 * @param code
	 * 		the associated status code 
	 */
    public DescribeResponse(Mediator mediator, String uri, 
    	Status status, int code, DescribeMethod.DescribeType describeType)
    {
    	super(mediator, uri, AccessMethodResponse.Response.DESCRIBE_RESPONSE, 
    	    status, code);
		this.describeType = describeType;
    }

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.props.TypedProperties#getJSON()
	 */
	@Override
	public String getJSON()
	{
		return this.getJSON(false);
	}
	
	/**
	 * Returns the JSON formated String description of this 
	 * DescribeResponse
	 * 
	 * @param raw defines whether only the embedded response
	 * entry have to be output
	 * 
	 * @return this DescribeResponse String description 
	 */
	public String getJSON(boolean raw)
	{
		if(raw)
		{
			return JSONUtils.toJSONFormat(super.get(
					"response"));
		}
		StringBuilder builder = new StringBuilder();
		Iterator<Map.Entry<TypedKey<?>,Object>> iterator = 
			super.properties.entrySet().iterator();
		
		builder.append(JSONUtils.OPEN_BRACE);
		int index = 0;
		
		while(iterator.hasNext())
		{
			Map.Entry<TypedKey<?>,Object> entry = iterator.next();
			TypedKey<?> typedKey = entry.getKey();
			
			if(typedKey.isHidden())
			{
				continue;
			}		
			builder.append(index > 0?JSONUtils.COMMA:"");
			builder.append(JSONUtils.QUOTE);
			if("response".equals(typedKey.getName()))
			{
				builder.append(this.describeType.getResponseKey());
				
			} else
			{
				builder.append(typedKey.getName());
			}
			builder.append(JSONUtils.QUOTE);
			builder.append(JSONUtils.COLON);
			if("type".equals(typedKey.getName()))
			{
				builder.append(JSONUtils.toJSONFormat(
					this.describeType.getTypeName()));
			} else
			{
				builder.append(JSONUtils.toJSONFormat(
					entry.getValue()));
			}
			index++;
		}
		builder.append(JSONUtils.CLOSE_BRACE);
		return builder.toString();
	}
}

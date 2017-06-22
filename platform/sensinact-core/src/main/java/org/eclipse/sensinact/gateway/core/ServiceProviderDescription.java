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
package org.eclipse.sensinact.gateway.core;

import java.util.Iterator;
import java.util.List;

import org.eclipse.sensinact.gateway.common.primitive.Description;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.json.JSONArray;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.eclipse.sensinact.gateway.util.UriUtils;

/**
 * Service description.
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ServiceProviderDescription 
extends ModelElementDescription<ServiceDescription>
{	
	/**
	 * Constructor
	 * 
	 * @param description
	 * 		the service to describe
	 */
	public ServiceProviderDescription(Mediator mediator, String name,
			List<ServiceDescription> serviceProxyDescriptions)
	{
		super(mediator,new StringBuilder().append(UriUtils.PATH_SEPARATOR
				).append(name).toString(), serviceProxyDescriptions);
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see JSONable#getJSON()
	 */
    @Override
    public String getJSON()
    {
		StringBuilder buffer = new StringBuilder();
		buffer.append(JSONUtils.OPEN_BRACE);
		buffer.append(JSONUtils.QUOTE);
		buffer.append("name");
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(super.getName());
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COMMA);
		buffer.append(JSONUtils.QUOTE);
		buffer.append("services");
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(JSONUtils.OPEN_BRACKET);		
		int index = 0;
		
		synchronized(super.elements)
    	{
    		Iterator<ServiceDescription> iterator =
    				super.elements.iterator();
    		
    		while(iterator.hasNext())
    		{   
    			buffer.append(index>0?JSONUtils.COMMA:JSONUtils.EMPTY);
    			buffer.append(JSONUtils.QUOTE);
    			buffer.append(iterator.next().getName());
    			buffer.append(JSONUtils.QUOTE); 
    			index++;
    		}
    	}
		buffer.append(JSONUtils.CLOSE_BRACKET);		
		buffer.append(JSONUtils.CLOSE_BRACE);
		return buffer.toString();
    }

	/**
	 * @inheritDoc
	 *
	 * @see Description#getDescription()
	 */
    @Override
    public String getDescription()
    {
	    return getJSONObjectDescription().toString();
    }

	/**
	 * @return
	 */
    protected JSONObject getJSONObjectDescription()
    {
    	JSONObject jsonObject = new JSONObject();
    	jsonObject.put("name", this.getName());
    	
    	JSONArray servicesArray = new JSONArray();
    	jsonObject.put("services", servicesArray);
    	
    	synchronized(super.elements)
    	{
    		Iterator<ServiceDescription> iterator =
    				super.elements.iterator();
    		
    		while(iterator.hasNext())
    		{    	
	    		JSONObject serviceJsonObject = 
	    		iterator.next().getJSONObjectDescription();
	    		if(serviceJsonObject == null)
	    		{
	    			continue;
	    		}
    			servicesArray.put(serviceJsonObject);
    		}
    	}
    	return jsonObject;
    }
}

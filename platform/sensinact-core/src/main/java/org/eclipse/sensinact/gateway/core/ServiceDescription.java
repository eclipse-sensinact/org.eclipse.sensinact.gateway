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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.sensinact.gateway.common.primitive.Description;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.json.JSONArray;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.util.JSONUtils;

/**
 * Service proxy description.
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ServiceDescription extends 
ModelElementDescription<ResourceDescription>
{	
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param service
	 */
	public ServiceDescription(Mediator mediator, 
			final ServiceProxy service) 
	{
		super(mediator, service.getPath(),
			new ArrayList<ResourceDescription>()
			{{		
				Iterator<Resource> iterator = service.getResources(
						).iterator();	
				
				while(iterator.hasNext())
				{
					this.add(iterator.next(
					).<ResourceDescription>getDescription());
				}
			}});
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
		buffer.append(this.getName());
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COMMA);
		buffer.append(JSONUtils.QUOTE);
		buffer.append("resources");
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(JSONUtils.OPEN_BRACKET);		

		int index = 0;
    	synchronized(super.elements)
    	{
    		Iterator<ResourceDescription> iterator =
    				super.elements.iterator();
    		
    		while(iterator.hasNext())
    		{    			
    			ResourceDescription description = iterator.next();    			
    			buffer.append(index>0?JSONUtils.COMMA:JSONUtils.EMPTY);
    			buffer.append(JSONUtils.OPEN_BRACE);
    			buffer.append(JSONUtils.QUOTE);
    			buffer.append("name");
    			buffer.append(JSONUtils.QUOTE);
    			buffer.append(JSONUtils.COLON);
    			buffer.append(JSONUtils.QUOTE);
    			buffer.append(description.getName());
    			buffer.append(JSONUtils.QUOTE);
    			buffer.append(JSONUtils.COMMA);
    			buffer.append(JSONUtils.QUOTE);
    			buffer.append("type");
    			buffer.append(JSONUtils.QUOTE);
    			buffer.append(JSONUtils.COLON);
    			buffer.append(JSONUtils.QUOTE);
    			buffer.append(description.getType().name());
    			buffer.append(JSONUtils.QUOTE);
    			buffer.append(JSONUtils.CLOSE_BRACE);
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
     * @inheritDoc
     * 
     * @see ServiceDescription#
     * getJSONObjectDescription()
     */
    protected JSONObject getJSONObjectDescription()
    {
    	JSONObject jsonObject = new JSONObject();
    	jsonObject.put("uri", this.getPath());
    	
    	JSONArray resourcesArray = new JSONArray();
    	jsonObject.put("resources", resourcesArray);
    	
    	synchronized(super.elements)
    	{
    		Iterator<ResourceDescription> iterator =
    				super.elements.iterator();
    		while(iterator.hasNext())
    		{    			
	    		JSONObject resourceJsonObject = 
	    		iterator.next().getJSONObjectDescription();
	    		if(resourceJsonObject == null)
	    		{
	    			continue;
	    		}
	    		resourcesArray.put(resourceJsonObject);
    		}
    	}
    	return jsonObject;
    }
}

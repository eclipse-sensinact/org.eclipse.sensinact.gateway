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
import org.eclipse.sensinact.gateway.core.method.AccessMethodDescription;
import org.json.JSONArray;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.util.JSONUtils;

/**
 * Resource description.
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ResourceDescription extends 
ModelElementDescription<AttributeDescription>
{
	/**
	 * the array of descriptions of the {@link AccessMethod}s
	 * provided by the {@link ResourceImlpl} this
	 * ResourceProxyDescription is the description of
	 */
	private final AccessMethodDescription[] methods;
	
	/**
	 * the {@link Resource.Type} of the {@link ResourceImpl}
	 * this ResourceProxyDescription is the description of
	 */
	protected final Resource.Type type;
	
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param type
	 * @param uri
	 * @param descriptions
	 * @param methods
	 */
	public ResourceDescription(
			Mediator mediator,
			Resource.Type type,
			String uri,
			List<AttributeDescription> descriptions, 
			AccessMethodDescription[] methods)
	{
		super(mediator, uri, descriptions);
		this.type = type;
		this.methods = methods;
	}

	/**
     * Returns this ResourceProxyDescription as a
     * {@link JSONObject} 
     *  
     * @return
     * 		this this ResourceProxyDescription as 
     * 		a {@link JSONObject}
     */
    protected JSONObject getJSONObjectDescription()
    {
    	JSONObject jsonObject = new JSONObject();
    	jsonObject.put("name", this.getName());
    	jsonObject.put("type", this.getType());
    	
    	JSONArray attributesArray = new JSONArray();
    	jsonObject.put("attributes", attributesArray);

    	synchronized(super.elements)
    	{
	    	Iterator<AttributeDescription> iterator = 
	    			super.elements.iterator();
	    	
	    	while(iterator.hasNext())
	    	{	    	
	    		JSONObject attributeJsonObject = 
	    				iterator.next().getJSONObjectDescription();
	    		
	    		if(attributeJsonObject == null)
	    		{
	    			continue;
	    		}
	    		attributesArray.put(attributeJsonObject);
	    	}
    	}    	
    	JSONArray methodsArray = new JSONArray();
    	jsonObject.put("accessMethods", methodsArray);
    	
    	int length = this.methods==null
    			?0:this.methods.length;
    	
    	int index = 0;
    	for(;index < length; index++)
    	{
    		if(this.methods[index] == null)
    		{
    			continue;
    		}
    		JSONArray methodJsonArray = 
    			this.methods[index].getJSONObjectDescription();
    		
    		if(methodJsonArray == null)
    		{
    			continue;
    		}
    		int arrayIndex = 0;
    		int arrayLength = methodJsonArray.length();
    		for(;arrayIndex < arrayLength; arrayIndex++)
    		{    			
    			methodsArray.put(methodJsonArray.get(arrayIndex));
    		}
    	}
    	return jsonObject;
    }

	/**
	 * @inheritDoc
	 *
	 * @see Description#getDescription()
	 */
    @Override
    public String getDescription()
    {
	    return this.getJSONObjectDescription().toString();
    }

	/**
	 * Return the {@link Resource.Type} of the described 
	 * resource
	 * 
	 * @return		
	 * 		the described resoruce's type
	 */
    public Resource.Type getType()
    {
	    return this.type;
    } 
    
    /**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.common.primitive.utils.json.JSONable#getJSON()
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
		buffer.append("type");
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(this.getType().name());
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.CLOSE_BRACE);
		return buffer.toString();
	}
}

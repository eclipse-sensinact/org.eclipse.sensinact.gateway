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
package org.eclipse.sensinact.gateway.util.json;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * An extended {@link JSONArray} {@link JSONStatement}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class JSONArrayStatement extends JSONArray implements JSONStatement
{
    private Map<String, JSONVariable> variables;    
    
    /**
     * Constructor
     * 
     * @param json
     * 		the JSON formated String to parse
     * 
     * @throws JSONException
     */
    public JSONArrayStatement(String json) 
    		throws JSONException 
    {
    	this(new JSONTokenerStatement(json));
    }
    
	/**
     * Construct a JSONArray from a JSONTokener.
     *
     * @param tokener
     *            A JSONTokener
     * @throws JSONException
     *             If there is a syntax error.
     */
    public JSONArrayStatement(JSONTokenerStatement tokener) 
    		throws JSONException 
    {
        super();
		this.variables = new HashMap<String, JSONVariable>();
		
        if (tokener.nextClean() != '[') 
        {
            throw tokener.syntaxError("A JSONArray text must start with '['");
        }
        if (tokener.nextClean() != ']') {
            tokener.back();
            for (;;) {
                if (tokener.nextClean() == ',') {
                    tokener.back();
                    this.put(JSONObject.NULL);
                } else {
                    tokener.back();
                    this.put(tokener.nextValue());
                }
                switch (tokener.nextClean()) {
                case ',':
                    if (tokener.nextClean() == ']') {
                        return;
                    }
                    tokener.back();
                    break;
                case ']':
                    return;
                default:
                    throw tokener.syntaxError("Expected a ',' or ']'");
                }
            }
        }
    }
    
    /**
     * @inheritDoc
	 *
	 * @see org.json.JSONArray#put(java.lang.Object)
	 */
	@Override
    public JSONArray put(Object value) throws JSONException
	{
        super.put(value);
        if (value != null && JSONVariable.class.isAssignableFrom(
        			value.getClass()))
        {
        		JSONVariable jsonVariable = (JSONVariable)value;
        		this.variables.put(jsonVariable.getName(), jsonVariable);
        }
        return this;
    }

    /**
     * @inheritDoc
     *
     * @see org.json.JSONArray#remove(int)
     */
    public Object remove(int index)
    {
    	Object value = super.remove(index);
    	if(value!=null && JSONVariable.class.isAssignableFrom(
    			value.getClass()))
    	{
    		JSONVariable jsonVariable = (JSONVariable)value;
    		this.variables.remove(jsonVariable.getName());
    	}
    	return value;    	
    }
	
    /**
     * @inheritDoc
     *
     * @see JSONStatement#
     * apply(java.lang.String, java.lang.Object)
     */
    public boolean apply(String variable, Object value)
    {
    	JSONVariable jsonVariable = this.variables.get(variable);
    	if(jsonVariable!=null)
    	{
    		jsonVariable.setValue(value);
    		return true;
    		
    	} else
    	{
    		int index = 0;
    		int length = super.length();
    		for(;index < length; index++)
    		{
    			Object object = super.get(index);
    			if(JSONStatement.class.isAssignableFrom(
    					object.getClass()) && ((JSONStatement)object).apply(
    							variable, value))
    			{
    				return true;
    			}
    		} 		
    	}
    	return false;
    }

    /**
     * @inheritDoc
     *
     * @see JSONStatement#reset()
     */
    public void reset()
    {
    	Iterator<JSONVariable> variableIterator = 
    			this.variables.values().iterator();
    	while(variableIterator.hasNext())
    	{
    		variableIterator.next().reset();    		
    	}
		int index = 0;
		int length = super.length();
		for(;index < length; index++)
		{
			Object object = super.get(index);
			if(JSONStatement.class.isAssignableFrom(
					object.getClass()))
			{
				((JSONStatement)object).reset();
			}
		}
    }
}

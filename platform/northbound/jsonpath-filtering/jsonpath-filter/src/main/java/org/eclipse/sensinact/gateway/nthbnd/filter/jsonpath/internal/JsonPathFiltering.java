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
package org.eclipse.sensinact.gateway.nthbnd.filter.jsonpath.internal;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.Filtering;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.json.JSONObject;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

/**
 * {@link Filtering} implementation allowing to apply a JsonPath expression to
 * the result object to be filtered
 *  
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class JsonPathFiltering implements Filtering
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

	private Mediator mediator;

	/**
	 * @param mediator
	 */
	public JsonPathFiltering(Mediator mediator)
	{
		this.mediator = mediator;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Filtering#handle(java.lang.String)
	 */
	@Override
	public boolean handle(String type)
	{
		return "jsonpath".equals(type);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.Filtering#apply(java.lang.String, java.lang.Object)
	 */
	@Override
	public String apply(String definition, Object result)
	{
		if(definition == null)
		{
			return String.valueOf(result);
		}
	    try
	    { 
	    	DocumentContext dc = JsonPath.parse(
	    			String.valueOf(result));
	        Object object = dc.read(definition);

	        if(object.getClass() == String.class)
	        {
	        	return new StringBuilder().append("\"").append(
	        		object).append("\"").toString();
	        } else
	        {
	        	return String.valueOf(object);
	        }
	    } catch(Exception e)
	    {
	        mediator.error("Failed to process JsonPath", e);
	        throw e;
	    }
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.Filtering#getLDAPComponent()
	 */
	@Override
	public String getLDAPComponent(String definition) 
	{
		return null;
	}
}

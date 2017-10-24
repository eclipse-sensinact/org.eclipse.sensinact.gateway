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

package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintFactory;
import org.eclipse.sensinact.gateway.common.constraint.InvalidConstraintDefinitionException;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;


/**
 * This abstract class is a wrapper for callback subscription
 */
public abstract class NorthboundRecipient implements Recipient 
{
    protected Set<Constraint> constraints;
	protected Mediator mediator;
    
    /**
     * Constructor
     */
    public NorthboundRecipient(Mediator mediator)
    {
    	this.mediator = mediator;
        this.constraints = new HashSet<Constraint>();
    }
    
    /**
     * Constructor
     */
    public NorthboundRecipient(Mediator mediator, JSONObject jsonObject)
    {
        this(mediator);
        if(!JSONObject.NULL.equals(jsonObject))
        {
	        JSONArray conditions = jsonObject.optJSONArray("conditions");
	        int index = 0;
	        int length = conditions==null?0:conditions.length();
	        try 
	        {
		        for(;index < length;index++)
		        {
		        	constraints.add( ConstraintFactory.Loader.load(
						mediator.getClassLoader(), conditions.optJSONObject(
								index)));				
		        }
	        } catch (InvalidConstraintDefinitionException e) 
	        {
				this.mediator.error(e);
			}
        }
    }

    /**
     * Return the constraint on the subscription
     * @return the constraint
     */
    public Set<Constraint> getConstraints() 
    {
        return Collections.unmodifiableSet(this.constraints);
    }
}

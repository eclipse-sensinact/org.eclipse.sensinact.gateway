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
package org.eclipse.sensinact.gateway.core.method;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;

/**
 * Shortcut to a {@link Signature} of an {@link AccessMethod} 
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Shortcut extends Signature
{     
	private final Map<Integer, Parameter> fixedParameters;
	private final Stack<Shortcut> shortucts;
	 
    /**
     * Constructor 
     * 
     * @param methodType
     * 		the type of the {@link AccessMethod} associates
     * 		to the shortcut to instantiate
     * @param parameterTypes
     * 		the array of parameter types of the shortcut 
     * 		to instantiate
     * @param parameterNames
     * 		the array of parameter names of the shortcut 
     * 		to instantiate
     * @param fixedParameters
     * 		the set of fixed {@link Parameter}s mapped to their 
     * 		index in the method signature
     * @throws InvalidValueException 
     */
    public  Shortcut(Mediator mediator, 
    		AccessMethod.Type methodType,	Class<?>[] parameterTypes, 
   		String[] parameterNames, Map<Integer, Parameter> 
    		fixedParameters) throws InvalidValueException
    {
	    super(mediator, methodType, parameterTypes, parameterNames);
	    this.fixedParameters = Collections.unmodifiableMap(fixedParameters);
	    this.shortucts = new Stack<Shortcut>();
    }
    
    /**
     * Constructor
     * 
     * @param methodType
     * 		the type of the associated {@link AccessMethod}
     * @param responseType
     * 		the object type returned by the associated 
     * 		{@link AccessMethod}
     * @param parameters
     * 		this Signature {@link Parameter}s array
     * @param fixedParameters
     * 		the set of fixed {@link Parameter}s mapped to their 
     * 		index in the method signature
     */
    public Shortcut(Mediator mediator, 
    		AccessMethod.Type methodType, Parameter[] parameters, 
    		Map<Integer, Parameter> fixedParameters)
    {
	    super(mediator, methodType, parameters);
	    this.fixedParameters = Collections.unmodifiableMap(fixedParameters);
	    this.shortucts = new Stack<Shortcut>();
    }
    
    /**
     * Constructor
     * 
     * @param name
     * 		the type's name of the associated {@link AccessMethod}
     * @param responseType
     * 		the object type returned by the associated 
     * 		{@link AccessMethod}
     * @param parameters
     * 		this Signature {@link Parameter}s array
     * @param fixedParameters
     * 		the set of fixed {@link Parameter}s mapped to their 
     * 		index in the method signature
     */
    public Shortcut(Mediator mediator, 
    		String name, AccessMethodResponse.Response returnedType, 
    		Parameter[] parameters, Map<Integer, Parameter> fixedParameters)
    {
    	super(mediator, name, returnedType, parameters);
	    this.fixedParameters = Collections.unmodifiableMap(fixedParameters);
	    this.shortucts = new Stack<Shortcut>();
    }
    
    /**
     * Pushes the Shortcut whose fixed parameters
     * have to be used while building  the object
     * values array
     * 
     * @param shortcut
     * 		the Shortcut to push
     */
    public void push(Shortcut shortcut)
    {
    	if(shortcut == null)
    	{
    		return;
    	}
    	this.shortucts.push(shortcut);
    }
    
    /**
     * Returns the map of this Shortcut fixed
     * {@link Parameter}s
     * 
   	 * @return 
   	 *     the map of this Shortcut fixed
     * 	   {@link Parameter}s
   	 */
   	public Map<Integer, Parameter> getFixedParameters()
   	{
   		return this.fixedParameters;
   	}

	/**
	 * Returns the array of object values of this 
	 * Shortcut's set of {@link Parameter}s after
	 * completion
	 * 
	 * @return
	 * 		the array of this Shortcut's {@link 
	 * 		Parameter}s'object values
	 */
	@Override
    public Object[] values()
    {
    	int position = 0;
    	Map<Integer, Parameter> gathered = 
    			new HashMap<Integer, Parameter>();
    	
    	while(!this.shortucts.isEmpty())
    	{
    		gathered.putAll(this.shortucts.pop(
    				).getFixedParameters());
    	}
    	gathered.putAll(this.fixedParameters);    	
    	Object[] values = new Object[super.length()+gathered.size()];
    	
        Iterator<Parameter> iterator = super.iterator();
    	Parameter parameter = null;

        for(;position < values.length; position++)
        {
        	parameter = gathered.get(position);
        	if(parameter == null && iterator.hasNext())
        	{
        		parameter = iterator.next();
        	}
            values[position] = parameter.getValue();
        }
        return values;
    }
	
	/** 
	 * @inheritDoc
	 * 
	 * @see Signature#clone()
	 */
	public Object clone()
	{
    	return new Shortcut(super.mediator, super.name, 
    			super.returnedType, super.parameters,
    			this.fixedParameters);
	}
}

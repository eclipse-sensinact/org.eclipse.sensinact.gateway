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

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.common.primitive.Describable;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethod.Type;
import org.eclipse.sensinact.gateway.core.security.MethodAccessibility;

/**
 * A {@link Service} proxy
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ServiceProxy extends ModelElementProxy<Resource> 
implements ElementsProxy<Resource> , ResourceCollection
{
	/**
	 * {@link AccessMethod}s of this ModelElementProxy
	 */
	protected final Map<String, AccessMethod> methods;	

	/**
	 * this ModelElementProxy's description
	 */
	private ServiceDescription description;
	
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * @param name
	 * @param uri
	 * @param resources
	 * 
	 * @throws InvalidServiceException
	 */
	ServiceProxy(Mediator mediator, String name, String path, 
		List<Resource> resources, List<MethodAccessibility> methodAccessibilities,
			ErrorHandler handler) throws InvalidServiceException
	{
		super(mediator, Service.class, path);
		
		super.elements.addAll(resources);
		this.methods = new HashMap<String,AccessMethod>();		
	    try
        {
	        ServiceBuilder.buildMethods(mediator, this, 
	        		methodAccessibilities, handler);
        }
        catch (InvalidValueException e)
        {
	        if(super.mediator.isErrorLoggable())
	        {
	        	super.mediator.error(e,e.getMessage());
	        }
	        throw new InvalidServiceException(e);
        }
	}
    
    /**
     * @inheritDoc
     *
     * @see Describable#getDescription()
     */
    @Override	
	@SuppressWarnings("unchecked")
    public ServiceDescription getDescription()
    {
		if(this.description == null)
		{
			this.description = new ServiceDescription(
				super.mediator,this);
		}
		return this.description;
    }
    
    /**
     * @inheritDoc
     *
     * @see ResourceCollection#
     * getResources()
     */    
    public List<Resource> getResources()
    {
    	return Collections.<Resource>unmodifiableList(super.elements);
    }

    /**
     * @InheritedDoc
     *
     * @see ResourceCollection#
     * getResource(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public <R extends Resource> R getResource(String resourceName)
    {
    	return (R) super.element(resourceName);
    }
    
	/**
	 * @inheritDoc
	 *
     * @see SensiNactResourceModelElementProxy#
     * getAccessMethod(AccessMethod.Type)
     */
    @Override
	public AccessMethod getAccessMethod(String method)
    {
	    return this.methods.get(method);
    }
  
	/**
	 * Registers the {@link AccessMethod} passed as parameter, 
	 * mapped to the specified {@link AccessMethod.Type}
	 * 
	 * @param method
	 * 		the {@link AccessMethod.Type} of the {@link AccessMethod} 
	 * 		to register
	 * @param method
	 * 		the {@link AccessMethod} to register
	 */
    void registerMethod(String methodType, AccessMethod method)
    {
    	if(this.methods.get(methodType) == null)
    	{
    		this.methods.put(methodType, method);
    	}
    }
}

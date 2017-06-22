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

import java.lang.reflect.Method;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.common.primitive.Describable;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethod.Type;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;

/**
 * An unaccessible {@link ModelElementOld} proxy
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class UnaccessibleModelElement<R extends Nameable> 
extends ModelElementProxy<R> implements ElementsProxy<R>
{	
	/**
	 * Constructor
	 * 
	 * @param resource
	 * 		the proxied resource
	 */
	public UnaccessibleModelElement(
			Mediator mediator, Class<?> proxied, String uri)
	{
		super(mediator,proxied,uri);
	}
	
    /**
	 * @inheritDoc
	 *
	 * @see java.lang.reflect.InvocationHandler#
	 * invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
    @Override
    public Object invoke(Object proxy, Method method, 
    		Object[] parameters) throws Throwable
    {    
    	Object result = null;
    	AccessMethod.Type type = null;
    	
    	if(super.proxied.isAssignableFrom(method.getDeclaringClass()))
    	{
        	type = AccessMethod.Type.valueOf(method.getName().toUpperCase());
        	result = AccessMethodResponse.error(super.mediator, 
        		uri, type, SnaErrorfulMessage.FORBIDDEN_ERROR_CODE, 
    				"Unaccessible object", null);
    	} else
    	{
    		result = method.invoke(this, parameters);
    		if(result == this)
    		{
    			return proxy;
    		}
    	}
    	return result;
    }

	/**
	 * @inheritDoc
	 *
	 * @see Describable#getDescription()
	 */
    @Override
    public ModelElementDescription getDescription()
    {
	    return null;
    }

	/**
	 * @inheritDoc
	 *
     * @see SensiNactResourceModelElementProxy#
     * getAccessMethod(AccessMethod.Type)
     */
    @Override
	public AccessMethod getAccessMethod(Type type)
    {
	    return null;
    }

	/**
	 * @inheritDoc
	 *
	 * @see ElementsProxy#isAccessible()
	 */
	@Override
	public boolean isAccessible()
	{
		return false;
	}
}

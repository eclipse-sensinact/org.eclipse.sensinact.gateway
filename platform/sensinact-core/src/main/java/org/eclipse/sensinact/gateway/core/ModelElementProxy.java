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
import java.util.ArrayList;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.primitive.Elements;
import org.eclipse.sensinact.gateway.common.primitive.ElementsProxy;
import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.core.message.SnaErrorfulMessage;
import org.eclipse.sensinact.gateway.core.message.SnaMessage;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;

/**
 * Abstract {@link ModelElementProxyOld} implementation
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class ModelElementProxy<P extends Nameable>
extends Elements<P> implements SensiNactResourceModelElementProxy<P>
{
	/**
	 * the proxied class
	 */
	protected final Class<?> proxied;
	
	/**
	 * the {@link Mediator} allowing to interact
	 * with the OSGi host environment
	 */
	protected Mediator mediator;
	
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * 		the {@link Mediator} that will allow the {@link 
	 * 		ModelElementProxyOld} to instantiate to interact
	 * 		with the OSGi host environment
	 * @param proxied
	 * 		the extended {@link Resource} type the {@link 
	 * 		ModelElementProxyOld} to instantiate will be the proxy of
	 * @param uri
	 * 		the string uri path of the {@link ModelElementProxyOld}
	 * 		to instantiate
	 */
	protected ModelElementProxy(
			Mediator mediator, Class<?> proxied, String uri) 
	{
		super(uri);
		this.mediator = mediator;
		this.proxied = proxied;
		this.elements = new ArrayList<P>();
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
    	if(this.proxied.isAssignableFrom(method.getDeclaringClass()))
    	{
    		Object[] calledParameters = null;
    		
    		if(method.isVarArgs() 
    				&& parameters!=null 
    				&& parameters.length==1 
    				&& parameters[0].getClass().isArray())
    		{
    			calledParameters = (Object[]) parameters[0];
    			
    		} else
    		{
    			calledParameters = parameters;
    		}    		
    		return this.invoke(AccessMethod.Type.valueOf(
    				method.getName().toUpperCase()), 
    				calledParameters);
    	}
    	Object result = method.invoke(this, parameters);
    	if(result == this)
    	{
    		return proxy;
    	}
    	return result;
    }
    
    /**
   	 * Processes an {@link AccessMethod} method invocation on this {@link 
   	 * ModelElementProxy} instance and returns the appropriate {@link SnaMessage}.
	 * 
	 * @param method the {@link AccessMethod.Type} of the {@link AccessMethod} 
	 * to process
     * @param parameters an array of objects containing the values of the arguments 
     * passed in the method invocation on the proxy instance, or null if interface
     * method takes no arguments.
     * 
     * @return the appropriate {@link SnaMessage}
   	 */
     public AccessMethodResponse invoke(AccessMethod.Type type,
										Object[] parameters) throws Throwable
     {    	       		 		
		AccessMethod accessMethod = this.getAccessMethod(type);
		if (accessMethod == null) 
		{
	    	AccessMethodResponse message = AccessMethodResponse.error(
	    	this.mediator, uri, type, SnaErrorfulMessage.NOT_FOUND_ERROR_CODE,
	    		new StringBuilder().append(type.name()).append(
	    		" method not found").toString(), null);
	    	
			return message;			
		}
		return accessMethod.invoke(parameters);
     }
     
    /**
 	 * @inheritDoc
 	 *
 	 * @see ElementsProxy#isAccessible()
 	 */
 	@Override
 	public boolean isAccessible()
 	{
 		return true;
 	}
}

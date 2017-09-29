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

import java.util.List;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.ErrorHandler;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.message.Recipient;
import org.eclipse.sensinact.gateway.core.method.ServiceAccessMethod;
import org.eclipse.sensinact.gateway.core.method.Signature;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.core.method.UnaccessibleAccessMethod;
import org.eclipse.sensinact.gateway.core.security.MethodAccessibility;
import org.eclipse.sensinact.gateway.util.ReflectUtils;

/**
 * Builder of a {@link ServiceImpl}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ServiceBuilder
{	
	private Mediator mediator;
	
	protected final Class<? extends ServiceImpl> baseClass;
	private Class<? extends ServiceImpl> implementationClass;
	private String name;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 * 		the associated {@link Mediator}
	 */

    public ServiceBuilder(Mediator mediator) 
	{
		this(mediator, ServiceImpl.class);
	}
	
	/**
	 * Constructor
	 * 
	 * @param mediator
	 * 		the associated {@link Mediator}
	 * @param baseClass
	 * 		
	 */
	public ServiceBuilder(Mediator mediator, 
		Class<? extends ServiceImpl>  baseClass) 
	{
		this.mediator = mediator;
		this.baseClass = (Class<? extends ServiceImpl>) 
			baseClass;
	}

	/**
	 * Configures the name of the service to build
	 * 
	 * @param name
	 * 		the name of the service to build
	 */
	public void configureName(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the name of the service to build
	 * 
	 * @return
	 * 		the name of the service to build
	 */
	public String getConfiguredName() 
	{
		return this.name;
	}	
	
	/**
	 * Configures the extended {@link ServiceImpl} type
	 * used to build the service
	 * 
	 * @param implementationClass
	 * 		the extended {@link ServiceImpl} type of the 
	 * 		service to build
	 */
	public void configureImplementationClass(
	    Class<? extends ServiceImpl> implementationClass)
	{
		this.implementationClass = implementationClass;
	}
	
	/**
	 * Builds and returns a new {@link ServiceImpl}
	 * connected to the {@link ServiceProviderImpl} passed
	 * as parameter
	 * 
	 * @param provider
	 * 		the service provider to which the {@link 
	 * 		ServiceImpl} to create is connected to 
	 * @return
	 * 		a new {@link ServiceImpl}
	 * 
	 * @throws InvalidServiceException 
	 */
	public final ServiceImpl build(
			ModelInstance<?> snaModelInstance, 
			ServiceProviderImpl provider) 
			throws InvalidServiceException
	{	
		if(name == null)
		{
			throw new InvalidServiceException("Service's name is needed");
		}
		ServiceImpl serviceImpl = null;
		try
        {	 				
	        serviceImpl= ReflectUtils.getInstance(
	        baseClass, implementationClass==null
	        ?ServiceImpl.class:implementationClass,
	        new Object[]{snaModelInstance, name, provider});
        }
        catch (Exception e)
        {
	        this.mediator.error(e);
	        throw new InvalidServiceException(e.getMessage(),e);
        }
		return serviceImpl;
	}
	
	/**
	 * Builds the set of access methods of this
	 * resource
	 * 
	 * @throws InvalidResourceException
	 * @throws InvalidValueException 
	 */
	protected static final void buildMethods(Mediator mediator, 
		final ServiceProxy service, List<MethodAccessibility> methodAccessibilities,
		ErrorHandler handler) throws InvalidValueException
	{		
		int index = 0;
		int length = methodAccessibilities == null?0:methodAccessibilities.size();
		//Get method
		ServiceAccessMethod getMethod =  null;
		//Set method
		ServiceAccessMethod setMethod = null;
		//Subscribe method
		ServiceAccessMethod subscribeMethod =  null;
		//Unsubscribe method
		ServiceAccessMethod unsubscribeMethod =  null;	
		//Act method
		ServiceAccessMethod actMethod =  null;
		
		for(;index < length; index++)
		{
			MethodAccessibility methodAccessibility = 
					methodAccessibilities.get(index);
			
			if(!methodAccessibility.isAccessible())
			{
				continue;
			}
			switch(methodAccessibility.getMethod().name())
			{
				case "ACT":
					actMethod = getServiceAccessMethod(mediator, service, 
							methodAccessibility, handler);
					break;
				case "DESCRIBE":
					break;
				case "GET":
					getMethod = getServiceAccessMethod(mediator, service, 
							methodAccessibility, handler);
					break;
				case "SET":
					setMethod = getServiceAccessMethod(mediator, service, 
							methodAccessibility, handler);
					break;
				case "SUBSCRIBE":
					subscribeMethod = getServiceAccessMethod(mediator, service, 
							methodAccessibility, handler);
					break;
				case "UNSUBSCRIBE":
					unsubscribeMethod = getServiceAccessMethod(mediator, service, 
							methodAccessibility, handler);
					break;
				default:
					break;		
			}
		}
		AccessMethod.Type GET = AccessMethod.Type.valueOf(AccessMethod.GET);
		AccessMethod.Type SET = AccessMethod.Type.valueOf(AccessMethod.SET);
		AccessMethod.Type ACT = AccessMethod.Type.valueOf(AccessMethod.ACT);
		AccessMethod.Type SUBSCRIBE = AccessMethod.Type.valueOf(AccessMethod.SUBSCRIBE);
		AccessMethod.Type UNSUBSCRIBE = AccessMethod.Type.valueOf(AccessMethod.UNSUBSCRIBE);
		AccessMethod.Type DESCRIBE = AccessMethod.Type.valueOf(AccessMethod.DESCRIBE);
		
		Signature getSignature = new Signature(mediator, GET,
				new Class<?>[]{String.class,String.class}, 
				new String[]{"resourceName","attributeName"});
		
		Signature setSignature = new Signature(mediator, SET,
				new Class<?>[]{String.class, String.class, Object.class}, 
				new String[]{"resourceName","attributeName", "value"});
		
		Signature actSignature = new Signature(mediator, ACT,
				new Class<?>[]{String.class, Object[].class}, 
				new String[]{"resourceName", "arguments"});
		
		Signature subscribeSignature = new Signature(mediator, SUBSCRIBE,
				new Class<?>[]{String.class,String.class, Recipient.class, Set.class},
				new String[]{"resourceName","attributeName", "listener", "conditions"});
		
		Signature unsubscribeSignature = new Signature(mediator, UNSUBSCRIBE,
				new Class<?>[]{String.class, String.class, String.class}, 
				new String[]{"resourceName","attributeName", "subscriptionId"});		
		
		// Get method
		if(getMethod != null)
		{					
			getMethod.addSignature(getSignature);			
			Signature getAttributeShortcut = new Signature(mediator, GET,
					new Class<?>[]{String.class}, 
					new String[]{"resourceName"});
					
			getMethod.addSignature(getAttributeShortcut);
			service.registerMethod(AccessMethod.GET, getMethod);
		
		} else
		{
			AccessMethod snaMethod = new UnaccessibleAccessMethod(
				mediator, service.getPath(), AccessMethod.Type.valueOf(
						AccessMethod.GET));
			service.registerMethod(AccessMethod.GET, snaMethod);
		}
		// Set method
		if(setMethod!=null)
		{	
			setMethod.addSignature(setSignature);			
			Signature setAttributeShortcut = new Signature(mediator, SET, 
					new Class<?>[]{String.class,Object.class}, 
					new String[]{"resourceName","value"});					
			setMethod.addSignature(setAttributeShortcut);
			service.registerMethod(AccessMethod.SET, setMethod);
			
		} else
		{
			AccessMethod snaMethod = new UnaccessibleAccessMethod(
			    mediator, service.getPath(), AccessMethod.Type.valueOf(
						AccessMethod.SET));
			service.registerMethod(AccessMethod.SET, snaMethod);	
		}
		// Set method
		if(actMethod !=null)
		{		
			actMethod.addSignature(actSignature);
			service.registerMethod(AccessMethod.ACT, actMethod);
			
		} else
		{
			AccessMethod snaMethod = new UnaccessibleAccessMethod(
			    mediator, service.getPath(), AccessMethod.Type.valueOf(
						AccessMethod.ACT));
			service.registerMethod(AccessMethod.ACT, snaMethod);
		}	
		//Subscribe method
		if(subscribeMethod !=null)
		{	
			subscribeMethod.addSignature(subscribeSignature);
			
			Signature subscribeConditionsShortcut = new Signature(
					mediator, SUBSCRIBE,
					new Class<?>[]{String.class, String.class, Recipient.class}, 
					new String[]{"resourceName","attributeName", "listener"});
			
			subscribeMethod.addSignature(subscribeConditionsShortcut);	
			
			Signature subscribeNameConditionsShortcut = new Signature(
					mediator, SUBSCRIBE,
					new Class<?>[]{String.class,Recipient.class}, 
					new String[]{"resourceName","listener"});

			Signature subscribeNameShortcut = new Signature(
					mediator, SUBSCRIBE,
					new Class<?>[]{String.class, Recipient.class, Set.class}, 
					new String[]{"resourceName", "listener", "conditions"});

			subscribeMethod.addSignature(subscribeNameConditionsShortcut);		
			subscribeMethod.addSignature(subscribeNameShortcut);
			service.registerMethod(AccessMethod.SUBSCRIBE, subscribeMethod);

		} else
		{
			AccessMethod snaMethod = new UnaccessibleAccessMethod(
				mediator, service.getPath(), AccessMethod.Type.valueOf(
						AccessMethod.SUBSCRIBE));
			service.registerMethod(AccessMethod.SUBSCRIBE, snaMethod);
		}
		// Unsubscribe method		
		if(unsubscribeMethod!=null)
		{		
			unsubscribeMethod.addSignature(unsubscribeSignature);			
			Signature unsubscribeNameShortcut = new Signature(
					mediator, UNSUBSCRIBE,
					new Class<?>[]{String.class,String.class}, 
					new String[]{"resourceName","subscriptionId"});
			unsubscribeMethod.addSignature(unsubscribeNameShortcut);	
			service.registerMethod(AccessMethod.UNSUBSCRIBE, unsubscribeMethod);
			
		} else
		{
			AccessMethod snaMethod = new UnaccessibleAccessMethod(
					mediator,service.getPath(),AccessMethod.Type.valueOf(
							AccessMethod.UNSUBSCRIBE));	
			service.registerMethod(AccessMethod.UNSUBSCRIBE, snaMethod);
		}
	}

	/**
	 * Creates and returns the {@link ServiceAccessMethod} for the 
	 * {@link ServiceProxy} and {@link AccessMethod.Type} passed as 
	 * parameters
	 * 
	 * @param mediator
	 * 		the {@link Mediator} that will be used by the 
	 * 		ServiceAccessMethod to create to interact with the OSGi
	 * 		host environment
	 * @param service
	 * 		the {@link ServiceProxy} for which to create the 
	 * 		{@link ServiceAccessMethod}
	 * @param methodAccessibility
	 * 		the {@link AccessMethod.Type} of the {@link ServiceAccessMethod}
	 * 		to create
	 * @return
	 */
	private static final ServiceAccessMethod getServiceAccessMethod(
			Mediator mediator,
			final ServiceProxy service,
			MethodAccessibility methodAccessibility,
			final ErrorHandler errorHandler)
	{		
		String uri = service.getPath();
		return new ServiceAccessMethod(mediator, uri,
				methodAccessibility.getMethod())
		{
			/** 
			 * @inheritDoc
			 * 
			 * @see ServiceAccessMethod#
			 * getResource(java.lang.String)
			 */
			@Override
			protected Resource getResource(String resourceName)
			{
				return service.getResource(resourceName);
			}
			
			/**
			 * @inheritDoc
			 * 
			 * @see AccessMethod#
			 * getErrorHandler()
			 */
			@Override
            public ErrorHandler getErrorHandler()
            {
	            return errorHandler;
            }
		};	
	}
}

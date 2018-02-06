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

import java.util.Set;

import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.json.JSONArray;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class NorthboundRequestBuilder<F>
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
	
	protected NorthboundMediator mediator;
	
	protected String serviceProvider;
	protected String service;
	protected String resource;
	protected String attribute;

	protected AccessMethod.Type method;
	protected boolean listElements;

	private Object argument;
	
	
	/**
	 * @param mediator
	 * @param responseFormat
	 */
	public NorthboundRequestBuilder(NorthboundMediator mediator)
	{
		this.mediator = mediator;
		if(this.mediator == null)
		{
			throw new NullPointerException("Mediator needed");
		}
	}
	
	/**
	 * @param serviceProvider
	 * @return
	 */
	public NorthboundRequestBuilder<F> withServiceProvider(
			String serviceProvider)
	{
		this.serviceProvider = serviceProvider;
		return this;
	}
	
	/**
	 * @param service
	 * @return
	 */
	public NorthboundRequestBuilder<F> withService(String service)
	{
		this.service = service;
		return this;
	}
	
	/**
	 * @param resource
	 * @return
	 */
	public NorthboundRequestBuilder<F> withResource(String resource)
	{
		this.resource = resource;
		return this;
	}
	
	/**
	 * @param attribute
	 * @return
	 */
	public NorthboundRequestBuilder<F> withAttribute(String attribute)
	{
		this.attribute = attribute;
		return this;
	}

	/**
	 * @param method
	 * @return
	 */
	public NorthboundRequestBuilder<F> withMethod(AccessMethod.Type method)
	{
		this.method = method;
		return this;
	}
	
	/**
	 * @param argument
	 * @return
	 */
	public NorthboundRequestBuilder<F> withArgument(Object argument)
	{
		this.argument = argument;
		return this;
	}
	
	/**
	 * @param listElements
	 * @return
	 */
	public NorthboundRequestBuilder<F> isElementsList(boolean listElements)
	{
		this.listElements = listElements;
		return this;
	}
	
	public NorthboundRequest<F> build()
	{
		NorthboundRequest<F> request = null;
		if(this.method == null)
		{
			return request;
		}
		switch(this.method.name())
		{
			case "ACT":
				if(this.resource != null)
				{
					Object[] arguments = null;
					if(this.argument!=null)
					{
						if(this.argument.getClass().isArray())
						{
							arguments = (Object[]) this.argument;
							
						} else
						{
							arguments = new Object[]{this.argument};
						}
					}
					request = new ResourceActRequest<F>(
					    mediator, serviceProvider, service, resource,
						        arguments);
				}
				break;
			case "DESCRIBE":
				if(this.resource != null)
				{
					request = new ResourceRequest<F>(
					    mediator, serviceProvider, service, resource);
					
				} else if(service != null)
				{
					if(this.listElements)
					{
						request = new ResourcesRequest<F>(
							mediator, serviceProvider, service);
						
					} else
					{
						request = new ServiceRequest<F>(
							mediator, serviceProvider, service);
					}
				} else if(serviceProvider != null)
				{
					if(this.listElements)
					{
						request = new ServicesRequest<F>(
							mediator, serviceProvider);
						
					} else
					{
						request = new ServiceProviderRequest<F>(
							mediator, serviceProvider);
					}
				} else
				{
					request = new ServiceProvidersRequest<F>(mediator);
				}
				break;
			case "GET":
				if(this.attribute != null)
				{
					request = new AttributeGetRequest<F>(
					    mediator, serviceProvider, service, resource,
						        attribute);
				}
				break;
			case "SET":
				if(this.attribute != null)
				{
					request = new AttributeSetRequest<F>(
					    mediator, serviceProvider, service, resource,
						        attribute, argument);
				}
				break;
			case "SUBSCRIBE":				
				Object[] arguments = this.argument!=null?(this.argument.getClass(
					).isArray()?(Object[]) this.argument:new Object[]{this.argument})
						:null;
					
				if(arguments == null || arguments.length == 0 ||
					!NorthboundRecipient.class.isAssignableFrom(
							arguments[0].getClass()))
				{
					break;
				}				
				if(this.resource!=null)
				{
					request = new AttributeSubscribeRequest<F>( mediator,
					    serviceProvider, service, resource, attribute, 
					    (NorthboundRecipient) arguments[0], (arguments.length>1
					    ?((JSONArray)arguments[1]):new JSONArray()));
				} else
				{
					request = new RegisterAgentRequest<F>( mediator, 
						serviceProvider, service, (NorthboundRecipient) 
					    arguments[0],  (SnaFilter)(arguments.length>1
					    	?arguments[1]:null));
				}
				break;
			case "UNSUBSCRIBE":
				String argument = CastUtils.cast(mediator.getClassLoader(),
						String.class, this.argument);
				if(this.attribute != null)
				{
					request = new AttributeUnsubscribeRequest<F>(
					    mediator, serviceProvider, service, resource,
						        attribute, argument);
				} else
				{
					request = new UnregisterAgentRequest<F>(mediator,argument);
				}
				break;
			default:
				break;
		}
		return request;
	}
}

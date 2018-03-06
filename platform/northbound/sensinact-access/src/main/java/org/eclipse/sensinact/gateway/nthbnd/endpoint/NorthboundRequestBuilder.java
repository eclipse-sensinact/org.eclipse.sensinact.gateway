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

import org.eclipse.sensinact.gateway.core.FilteringDefinition;
import org.eclipse.sensinact.gateway.core.message.SnaFilter;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.json.JSONArray;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class NorthboundRequestBuilder
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
	protected String requestIdentifier;

	protected String method;
	protected boolean listElements;

	private Object argument;

	private FilteringDefinition filterDefinition;
	
	/**
	 * Constructor
	 * 
	 * @param mediator
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
	 * Defines the String identifier of the service provider 
	 * targeted by the request to be built
	 * 
	 * @param serviceProvider the String identifier of the 
	 * targeted service provider
	 * 
	 * @return this NorthboundRequestBuilder
	 */
	public NorthboundRequestBuilder withServiceProvider(
			String serviceProvider)
	{
		this.serviceProvider = serviceProvider;
		return this;
	}

	/**
	 * Defines the String identifier of the service targeted 
	 * by the request to be built
	 * 
	 * @param service the String identifier of the targeted 
	 * service 
	 * 
	 * @return this NorthboundRequestBuilder
	 */
	public NorthboundRequestBuilder withService(String service)
	{
		this.service = service;
		return this;
	}
	
	/**
	 * Defines the String identifier of the resource targeted 
	 * by the request to be built
	 * 
	 * @param resource the String identifier of the 
	 * targeted resource
	 * 
	 * @return this NorthboundRequestBuilder
	 */
	public NorthboundRequestBuilder withResource(String resource)
	{
		this.resource = resource;
		return this;
	}
	
	/**
	 * Defines the String identifier of the attribute targeted 
	 * by the request to be built
	 * 
	 * @param attribute the String identifier of the 
	 * targeted attribute
	 * 
	 * @return this NorthboundRequestBuilder
	 */
	public NorthboundRequestBuilder withAttribute(String attribute)
	{
		this.attribute = attribute;
		return this;
	}

	/**
	 * Defines the String identifier of the method to be executed
	 * by the request to be built
	 * 
	 * @param method the String identifier of the method to be 
	 * executed
	 * 
	 * @return this NorthboundRequestBuilder
	 */
	public NorthboundRequestBuilder withMethod(String method)
	{
		this.method = method;
		return this;
	}

	/**
	 * Returns the String identifier of the method to be executed
	 * by the request to be built
	 * 
	 * @return the String identifier of the method to be 
	 * executed
	 */
	public String getMethod()
	{
		return this.method;
	}
	
	/**
	 * @param argument
	 * @return
	 */
	public NorthboundRequestBuilder withArgument(Object argument)
	{
		this.argument = argument;
		return this;
	}

	/**
	 * @param rid
	 */
	public NorthboundRequestBuilder withRequestId(String rid)
	{
		this.requestIdentifier = rid;
		return this;
	}
	
	/**
	 * @return
	 */
	public String getRequestIdentifier()
	{
		return this.requestIdentifier;
	}
	
	/**
	 * @param listElements
	 * @return
	 */
	public NorthboundRequestBuilder isElementsList(boolean listElements)
	{
		this.listElements = listElements;
		return this;
	}
	
	/**
	 * @param filterDefinition
	 */
	public void withFilter(
	     FilteringDefinition filterDefinition)
	{
		this.filterDefinition = filterDefinition;
	}
	
	/**
	 * @return
	 */
	public NorthboundRequest build()
	{
		NorthboundRequest request = null;
		if(this.method == null)
		{
			return request;
		}
		switch(this.method)
		{
			case "ALL":
				request = new AllRequest(mediator, getRequestIdentifier(), this.filterDefinition);
				break;
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
					request = new ResourceActRequest(mediator, 
						getRequestIdentifier(), serviceProvider, service, 
						    resource, arguments);
				}
				break;
			case "DESCRIBE":
				if(this.resource != null)
				{
					request = new ResourceRequest(
					    mediator, getRequestIdentifier(), serviceProvider, 
					    service, resource);
					
				} else if(service != null)
				{
					if(this.listElements)
					{
						request = new ResourcesRequest( mediator, 
							getRequestIdentifier(), serviceProvider, service, 
							    this.filterDefinition);
						
					} else
					{
						request = new ServiceRequest(
							mediator, getRequestIdentifier(), serviceProvider, 
							    service, null);
					}
				} else if(serviceProvider != null)
				{
					if(this.listElements)
					{
						request = new ServicesRequest( mediator, 
							getRequestIdentifier(), serviceProvider, this.filterDefinition);
						
					} else
					{
						request = new ServiceProviderRequest(
							mediator, getRequestIdentifier(), 
							    serviceProvider, null);
					}
				} else
				{
					request = new ServiceProvidersRequest(mediator, 
						getRequestIdentifier(), this.filterDefinition);
				}
				break;
			case "GET":
				if(this.attribute != null)
				{
					request = new AttributeGetRequest( mediator, 
						getRequestIdentifier(), serviceProvider, service, 
						    resource, attribute);
				}
				break;
			case "SET":
				if(this.attribute != null)
				{
					request = new AttributeSetRequest(mediator, 
						getRequestIdentifier(), serviceProvider, service, 
						    resource, attribute, argument);
				}
				break;
			case "SUBSCRIBE":				
				Object[] arguments = this.argument!=null
				?(this.argument.getClass().isArray()?(Object[]) 
					this.argument:new Object[]{this.argument})
						:null;
					
				if(arguments == null || arguments.length == 0 ||
					!NorthboundRecipient.class.isAssignableFrom(
							arguments[0].getClass()))
				{
					break;
				}				
				if(this.resource!=null)
				{
					request = new AttributeSubscribeRequest( mediator, 
						getRequestIdentifier(), serviceProvider, service, 
						resource, attribute, (NorthboundRecipient) arguments[0],
					    (arguments.length>1?((JSONArray)arguments[1]):new JSONArray()));
				} else
				{
					request = new RegisterAgentRequest( mediator, 
						getRequestIdentifier(), serviceProvider, service, 
						(NorthboundRecipient) arguments[0],  (SnaFilter)(
							arguments.length>1?arguments[1]:null));
				}
				break;
			case "UNSUBSCRIBE":
				String arg = CastUtils.cast(mediator.getClassLoader(),
						String.class, this.argument);
				if(this.resource != null)
				{
					request = new AttributeUnsubscribeRequest(mediator,
						getRequestIdentifier(), serviceProvider, service, 
						    resource, attribute, arg);
				} else
				{
					request = new UnregisterAgentRequest(mediator, 
							getRequestIdentifier(), arg);
				}
				break;
			default:
				break;
		}
		return request;
	}
}

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

public class AttributeUnsubscribeRequest extends AttributeRequest
{	
	private String subscriptionId;

	/**
	 * @param mediator
	 * @param responseFormat
	 * @param serviceProvider
	 * @param service
	 * @param resource
	 */
	public AttributeUnsubscribeRequest(NorthboundMediator mediator,
			String serviceProvider, String service, String resource, 
			String attribute, String subscriptionId)
	{
		super(mediator, serviceProvider, service, resource, attribute);
		
		this.subscriptionId = subscriptionId;
		if(this.subscriptionId == null)
		{
			throw new NullPointerException("Subscription identifier mising");
		}
	}

	/**
	 * @inheritDoc
	 *
	 * @see ServiceProvidersRequest#getExecutionArguments()
	 */
	@Override
	protected Argument[] getExecutionArguments() 
	{
		Argument[] superArguments = super.getExecutionArguments();
		int length = superArguments==null?0:superArguments.length;
		Argument[] arguments = new Argument[length+1];
		if(length > 0)
		{
			System.arraycopy(superArguments, 0, arguments, 0, length);
		}
		arguments[length] = new Argument(String.class,this.subscriptionId);
	    return arguments;
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see ResourceRequest#getMethod()
	 */
	@Override
	protected String getMethod()
	{
		return "unsubscribe";
	}
}

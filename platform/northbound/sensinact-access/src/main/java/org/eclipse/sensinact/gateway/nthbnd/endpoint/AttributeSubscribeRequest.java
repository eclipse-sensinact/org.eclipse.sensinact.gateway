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

import org.json.JSONArray;


public class AttributeSubscribeRequest<F> extends AttributeRequest<F> 
{	
	private NorthboundRecipient recipient;
	private JSONArray conditions;

	/**
	 * @param mediator
	 * @param serviceProvider
	 * @param service
	 * @param resource
	 * @param attribute
	 * @param recipient
	 */
	public AttributeSubscribeRequest(NorthboundMediator mediator, 
			String serviceProvider, String service, String resource, 
			String attribute, NorthboundRecipient recipient,
			JSONArray conditions)
	{
		super(mediator, serviceProvider, service, resource, attribute);
		this.recipient = recipient;
		this.conditions = conditions;
		if(this.recipient == null)
		{
			throw new NullPointerException("Recipient missing");
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
		Argument[] arguments = new Argument[length+2];
		if(length > 0)
		{
			System.arraycopy(superArguments, 0, arguments, 0, length);
		}
		arguments[length] = new Argument(NorthboundRecipient.class, this.recipient);
		arguments[length+1] = new Argument(JSONArray.class, this.conditions);
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
		return "subscribe";
	}
}

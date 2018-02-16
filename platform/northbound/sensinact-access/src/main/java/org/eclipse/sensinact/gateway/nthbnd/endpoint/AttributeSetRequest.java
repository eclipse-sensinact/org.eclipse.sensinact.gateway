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

public class AttributeSetRequest extends AttributeRequest
{	
	private Object argument;

	/**
	 * @param mediator
	 * @param serviceProvider
	 * @param service
	 * @param resource
	 * @param attribute
	 * @param argument
	 */
	public AttributeSetRequest(NorthboundMediator mediator,
			String serviceProvider, String service, String resource, 
			String attribute, Object argument)
	{
		super(mediator, serviceProvider, service, resource, attribute);
		this.argument = argument;
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
		arguments[length] =  new Argument(Object.class,this.argument);
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
		return "set";
	}
}

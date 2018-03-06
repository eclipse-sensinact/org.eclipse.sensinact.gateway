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

import org.eclipse.sensinact.gateway.util.UriUtils;

public class ResourceRequest extends ResourcesRequest
{
	private String resource;

	/**
	 * @param mediator
	 * @param responseFormat
	 * @param serviceProvider
	 * @param service
	 */
	public ResourceRequest(NorthboundMediator mediator, 
		String requestIdentifier, String serviceProvider,
	        String service, String resource)
	{
		super(mediator, requestIdentifier, serviceProvider, service, null);
		this.resource = resource;
		if(this.resource == null)
		{
			throw new NullPointerException("Resource missing");
		}

	}

	/**
	 * @inheritDoc
	 *
	 * @see ResourcesRequest#getName()
	 */
	public String getName()
	{
		return resource;
	}

	/**
	 * @inheritDoc
	 *
	 * @see ResourcesRequest#getPath()
	 */
	@Override
	public String getPath()
	{
		return new StringBuilder().append(super.getPath()
			).append(UriUtils.PATH_SEPARATOR).append(
				this.getName()).toString();
	}

	/**
	 * @inheritDoc
	 *
	 * @see ResourcesRequest#getMethod()
	 */
	@Override
	protected String getMethod()
	{
		return "resourceDescription";
	}

	/**
	 * @inheritDoc
	 *
	 * @see NorthboundRequest#getExecutionArguments()
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
		arguments[length] = new Argument(String.class, resource);
	    return arguments;
	}

}

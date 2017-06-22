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

public class ResourcesRequest<F> extends ServiceRequest<F> 
{
	/**
	 * @param mediator
	 * @param responseFormat
	 * @param serviceProvider
	 * @param service
	 */
	public ResourcesRequest(NorthboundMediator mediator,
			String serviceProvider,
	        String service)
	{
		super(mediator, serviceProvider, service);
	}
	
	/**
	 * @inheritDoc
	 *
	 * @see ServiceRequest#getName()
	 */
	public String getName()
	{
		return null;
	}

	/**
	 * @inheritDoc
	 *
	 * @see ServiceRequest#getPath()
	 */
	@Override
	public String getPath() 
	{
		return new StringBuilder().append(super.getPath()
			).append(UriUtils.PATH_SEPARATOR).append(
			"resources").toString();
	}

	/**
	 * @inheritDoc
	 *
	 * @see ServiceRequest#getMethod()
	 */
	@Override
	protected String getMethod()
	{
		return "resourcesList";
	}

	/**
	 * @inheritDoc
	 *
	 * @see NorthboundRequest#getExecutionArguments()
	 */
	@Override
	protected Argument[] getExecutionArguments() 
	{
		return super.getExecutionArguments();
	}
	
}

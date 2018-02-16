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
import org.eclipse.sensinact.gateway.util.UriUtils;

public class ServiceProvidersRequest extends NorthboundRequest
{

	public ServiceProvidersRequest(NorthboundMediator mediator, 
			FilteringDefinition filterDefinition) 
	{
		super(mediator, filterDefinition);
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see ServiceProvidersRequest#
	 * getName()
	 */
	public String getName()
	{
		return null;
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see ServiceProvidersRequest#
	 * getPath()
	 */
	@Override
	public String getPath() 
	{
		return new StringBuilder().append(super.getPath()
			).append(UriUtils.PATH_SEPARATOR).append(
			"providers").toString();
	}

	/**
	 * @inheritDoc
	 *
	 * @see NorthboundRequest#getExecutionArguments()
	 */
	@Override
	protected Argument[] getExecutionArguments() 
	{
		if(this.getClass() == ServiceProvidersRequest.class 
				&& super.filterDefinition!=null)
		{
			Argument[] arguments = new Argument[1];
			arguments[0] = new Argument(FilteringDefinition.class, 
					super.filterDefinition);
		    return arguments;
		}
		return super.getExecutionArguments();
	}
	
	/** 
	 * @inheritDoc
	 * 
	 * @see NorthboundRequest#getMethod()
	 */
	@Override
	protected String getMethod()
	{
		return "serviceProvidersList";
	}
	
}

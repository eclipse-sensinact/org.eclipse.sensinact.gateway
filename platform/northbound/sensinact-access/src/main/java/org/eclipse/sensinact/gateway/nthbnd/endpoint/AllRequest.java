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

public class AllRequest extends NorthboundRequest
{	
	/**
	 * @param mediator
	 * @param requestIdentifier 
	 * @param filterDefinition 
	 */
	public AllRequest(NorthboundMediator mediator, 
			String requestIdentifier, FilteringDefinition filterDefinition)
	{
		super(mediator, requestIdentifier, filterDefinition);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.primitive.Nameable#getName()
	 */
	public String getName()
	{
		return "all";
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundRequest#getMethod()
	 */
	@Override
	protected String getMethod()
	{
		return this.getName();
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
		Argument[] arguments = new Argument[length+2];
		if(length > 0)
		{
			System.arraycopy(superArguments, 0, arguments, 0, length);
		}
		arguments[length] = new Argument(String.class, null);
		arguments[length + 1] = new Argument(FilteringDefinition.class, 
			super.filterDefinition);
	    return arguments;
	}
}

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
	 * @param filterDefinition 
	 */
	public AllRequest(NorthboundMediator mediator, 
			FilteringDefinition filterDefinition)
	{
		super(mediator, filterDefinition);
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
}

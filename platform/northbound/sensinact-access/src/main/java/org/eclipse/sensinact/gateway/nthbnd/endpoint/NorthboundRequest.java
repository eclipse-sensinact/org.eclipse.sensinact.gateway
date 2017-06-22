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

import org.eclipse.sensinact.gateway.common.primitive.Nameable;
import org.eclipse.sensinact.gateway.common.primitive.PathElement;

public abstract class NorthboundRequest<F> implements PathElement, Nameable
{		
	public static final String ROOT = "/sensinact";
	
	/**
	 * @return
	 */
	protected abstract String getMethod();

	protected NorthboundMediator mediator;

	/**
	 * @param mediator
	 * @param responseFormat
	 * @param authentication
	 */
	public NorthboundRequest(NorthboundMediator mediator)
	{
		this.mediator = mediator;
	}
	
	/** 
	 * @inheritedDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.util.common.primitive.PathElement#getPath()
	 */
	@Override
	public String getPath() 
	{
		return ROOT;
	}
	
	/**
	 * @return
	 */
	protected Argument[] getExecutionArguments()
	{
		return null;
	}
}

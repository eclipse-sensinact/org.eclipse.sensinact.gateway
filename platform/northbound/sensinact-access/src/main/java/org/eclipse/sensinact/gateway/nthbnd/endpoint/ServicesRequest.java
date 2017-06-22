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

public class ServicesRequest<F> extends ServiceProviderRequest<F>
{
	public ServicesRequest(NorthboundMediator mediator,
			String serviceProvider) 
	{
		super(mediator, serviceProvider);
	}

	/**
	 * @inheritDoc
	 *
	 * @see ServiceProviderRequest#getName()
	 */
	public String getName()
	{
		return null;
	}

	/**
	 * @inheritDoc
	 *
	 * @see ServiceProviderRequest#getPath()
	 */
	@Override
	public String getPath() 
	{
		return new StringBuilder().append(super.getPath()
			).append(UriUtils.PATH_SEPARATOR).append(
			"services").toString();
	}

	/** 
	 * @inheritDoc
	 * 
	 * @see ServiceProvidersRequest#getMethod()
	 */
	@Override
	protected String getMethod()
	{
		return "servicesList";
	}

}

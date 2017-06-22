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
package org.eclipse.sensinact.gateway.sthbnd.http.smpl;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpChainedTask;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpChainedTasks;

public class DefaultHttpChainedTaskProcessingContextFactory 
implements HttpChainedTaskProcessingContextFactory
{
	private Mediator mediator;

	/**
	 * @param mediator
	 */
	public DefaultHttpChainedTaskProcessingContextFactory(Mediator mediator)
	{
		this.mediator = mediator;
	}

	/**
	 * @inheritDoc
	 *
	 * @see HttpChainedTaskProcessingContextFactory#newInstance(HttpChainedTasks, HttpChainedTask)
	 */
	@Override
	public <CHAINED extends HttpChainedTask<?>> HttpTaskProcessingContext 
	newInstance(HttpTaskConfigurator httpTaskConfigurator,  String endpointId, 
			HttpChainedTasks<?, CHAINED> tasks, CHAINED task)
	{
		return new DefaultHttpChainedTaskProcessingContext(
				this.mediator, httpTaskConfigurator, 
				endpointId, tasks, task);
	}
}

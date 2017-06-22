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
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpChainedTask;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpChainedTasks;
import org.eclipse.sensinact.gateway.util.CastUtils;

public class DefaultHttpChainedTaskProcessingContext extends 
DefaultHttpTaskProcessingContext implements HttpTaskProcessingContext 
{
	/**
	 * @param mediator
	 * @param task
	 */
	public <CHAINED extends HttpChainedTask<?>>
	DefaultHttpChainedTaskProcessingContext(Mediator mediator, 
		HttpTaskConfigurator httpTaskConfigurator,
		final String endpointId, final HttpChainedTasks<?,CHAINED> tasks,
		final CHAINED task) 
	{ 
		super(mediator, httpTaskConfigurator, endpointId, task);

		this.properties.put("task.intermediate", new Executable<Void,String>()
		{
			@Override
			public String execute(Void parameter) throws Exception
			{
				return CastUtils.cast(
					DefaultHttpChainedTaskProcessingContext.this.mediator.getClassLoader(), 
					String.class, tasks.getIntermediateResult());
			}			
		});
	}
}

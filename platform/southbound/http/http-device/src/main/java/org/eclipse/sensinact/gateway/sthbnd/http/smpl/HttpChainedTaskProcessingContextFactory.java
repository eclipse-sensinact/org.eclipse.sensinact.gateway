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

import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpChainedTask;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpChainedTasks;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;

/**
 * A factory of {@link HttpTaskProcessingContext} for {@link HttpChainedTask}s
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface HttpChainedTaskProcessingContextFactory
{
	/**
	 * Creates and returns a {@link HttpTaskProcessingContext} for the 
	 * {@link HttpChainedTask} passed as parameter
	 * 
	 * @param httpTaskConfigurator
	 * 
	 * @param endpointId the string identifier of the {@link 
	 * ProtocolStackEndpoint} that instantiated the {@link HttpTask}
	 * for which to create a  new {@link HttpTaskProcesingContext}
	 * 
	 * @param tasks the parent {@link HttpChainedTasks} of the {@link
	 * HttpChainedTask} for which to create a new {@link 
	 * HttpTaskProcessingContext}
	 * 
	 * @param task the {@link HttpChainedTask} for which to create
	 * a new {@link HttpTaskProcessingContext}
	 * 
	 * @return a new {@link HttpTaskProcessingContext} for the 
	 * specified {@link HttpChainedTask}
	 */
	<CHAINED extends HttpChainedTask<?>> HttpTaskProcessingContext 
	newInstance(HttpTaskConfigurator httpTaskConfigurator,
		String endpointId, HttpChainedTasks<?,CHAINED> tasks, 
		CHAINED task);
}
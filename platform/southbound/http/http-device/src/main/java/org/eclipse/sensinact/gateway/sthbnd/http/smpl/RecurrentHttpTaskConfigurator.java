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

import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface RecurrentHttpTaskConfigurator extends HttpTaskBuilder
{
	/**
	 * @return the period
	 */
	long getPeriod();

	/**
	 * @return the delay
	 */
	long getDelay();

	/**
	 * @return the timeout
	 */
	long getTimeout();
	
	/**
	 * @return 
	 *     the taskType
	 */
	Class<? extends HttpTask> getTaskType();
}

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

import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.sthbnd.http.task.HttpTask;

/**
 * Configuration applying on an {@link HttpTask}s
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface HttpTaskUrlConfigurator extends HttpTaskConfigurator
{
	/**
	 * Configures the {@link HttpTask} passed as parameter
	 * @param <T>
	 * 
	 * @param task the {@link HttpTask} to be configured
	 * @return 
	 */
	CommandType[] handled();
}

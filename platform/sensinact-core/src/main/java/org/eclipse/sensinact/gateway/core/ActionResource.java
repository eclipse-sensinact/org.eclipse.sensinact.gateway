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

package org.eclipse.sensinact.gateway.core;

import org.eclipse.sensinact.gateway.core.method.legacy.ActResponse;

/**
 * Extended {@link Resource} for Action
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface ActionResource extends Resource
{
	/**
	 * The action resource type.
	 */
	public static final Resource.Type TYPE_VALUE = Resource.Type.ACTION;
	
	/**
	 * Asks for this ActionResource execution
	 * 
	 * @param parameters
	 * 		objects array parameterizing the action 
	 * 		invocation
	 * @return 
	 * 		the action execution {@link ActResponse}
	 */
	ActResponse act(Object... parameters);
}

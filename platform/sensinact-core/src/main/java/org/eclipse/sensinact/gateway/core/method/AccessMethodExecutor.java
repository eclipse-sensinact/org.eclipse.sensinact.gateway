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
package org.eclipse.sensinact.gateway.core.method;

import org.eclipse.sensinact.gateway.common.execution.Executable;

/**
 * Extended {@link Executable} dedicated to an {@link AbstractModelElement}'s 
 * {@link AccessMethod} invocation
 * 
 * @param <V>
 * 		the executor returned type
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface AccessMethodExecutor 
extends Executable<AccessMethodResult, Void>
{
	enum ExecutionPolicy
	{
		BEFORE,
		AFTER,
		REPLACE;		
	}
}

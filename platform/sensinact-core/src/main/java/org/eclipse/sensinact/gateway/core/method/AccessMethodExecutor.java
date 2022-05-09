/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.method;

import org.eclipse.sensinact.gateway.common.execution.Executable;

/**
 * Extended {@link Executable} dedicated to an {@link AbstractModelElement}'s
 * {@link AccessMethod} invocation
 * 
 * @param <V>
 *            the executor returned type
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface AccessMethodExecutor extends Executable<AccessMethodResponseBuilder<?, ?>, Void> {
	enum ExecutionPolicy {
		BEFORE, AFTER, REPLACE;
	}
}

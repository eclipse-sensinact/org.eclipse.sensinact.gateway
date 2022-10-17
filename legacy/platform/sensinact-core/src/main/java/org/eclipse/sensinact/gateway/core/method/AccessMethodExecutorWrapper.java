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

/**
 * {@link AccessMethodExecutor} wrapper
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AccessMethodExecutorWrapper extends AbstractAccessMethodExecutor {
	/**
	 * the wrapped {@link AccessMethodExecutor}
	 */
	private final AccessMethodExecutor executor;

	/**
	 * Constructor
	 * 
	 * @param executor
	 *            the wrapped {@link AccessMethodExecutor}
	 */
	AccessMethodExecutorWrapper(AccessMethodExecutor executor) {
		this.executor = executor;
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.method.AbstractAccessMethodExecutor#
	 *      doExecute(org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder)
	 */
	@Override
	void doExecute(AccessMethodResponseBuilder<?, ?> responseBuilder) throws Exception {
		this.executor.execute(responseBuilder);
	}
}

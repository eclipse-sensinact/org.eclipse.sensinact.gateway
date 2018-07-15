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

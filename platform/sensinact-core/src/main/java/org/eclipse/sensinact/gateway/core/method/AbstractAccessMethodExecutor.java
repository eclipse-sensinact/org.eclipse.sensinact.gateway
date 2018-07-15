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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.constraint.Fixed;
import org.eclipse.sensinact.gateway.common.execution.Executable;

/**
 * Abstract {@link AccessMethodExecutor} implementation allowing to condition
 * its execution to the validation of a set of {@link Fixed} constraints which
 * apply to the parameters of the call
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractAccessMethodExecutor implements AccessMethodExecutor {
	/**
	 * Executes this extended {@link Executor} if the parameters wrapped by the
	 * {@link AccessMethodResponseBuilder} argument comply the registered
	 * constraints
	 * 
	 * @param responseBuilder
	 *            the {@link AccessMethodResponseBuilder} parameterizing the
	 *            execution, in which to stack the <code>&lt;V&gt;</code> typed
	 *            result object of the execution
	 * 
	 * @throws Exception
	 */
	abstract void doExecute(AccessMethodResponseBuilder<?, ?> responseBuilder) throws Exception;

	final Map<Integer, Fixed> executionConditions;

	/**
	 * Constructor
	 */
	public AbstractAccessMethodExecutor() {
		this.executionConditions = new HashMap<Integer, Fixed>();
	}

	/**
	 * Applies the {@link Fixed} constraint argument on the parameter of the call
	 * whose index is passed as parameter
	 * 
	 * @param index
	 *            index of the parameter on which to apply the constraint
	 * @param constraint
	 *            the {@link Fixed} constraint to apply
	 */
	public void put(int index, Fixed<?> constraint) {
		this.executionConditions.put(index, constraint);
	}

	/**
	 * @inheritDoc
	 *
	 * @see Executable#execute(java.lang.Object)
	 */
	@Override
	public Void execute(AccessMethodResponseBuilder<?, ?> responseBuilder) throws Exception {
		Iterator<Map.Entry<Integer, Fixed>> iterator = this.executionConditions.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry<Integer, Fixed> entry = iterator.next();
			if (!entry.getValue().complies(responseBuilder.getParameter(entry.getKey()))) {
				return null;
			}
		}
		this.doExecute(responseBuilder);
		return null;
	}

}

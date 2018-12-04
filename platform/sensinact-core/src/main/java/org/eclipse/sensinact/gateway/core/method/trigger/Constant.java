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
package org.eclipse.sensinact.gateway.core.method.trigger;

/**
 * Extended {@link AccessMethodTrigger} whose execution result is a constant
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Constant extends AbstractAccessMethodTrigger {
	public static final String NAME = "CONSTANT";

	/**
	 * Constructor
	 * 
	 * @param constant
	 *            constant object of this ConstantCalculation
	 */
	public Constant(Object argument,boolean passOn) {
		super(argument,TriggerArgumentBuilder.EMPTY,passOn);
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.common.execution.Executable#execute(java.lang.Object)
	 */
	@Override
	public Object execute(Object parameter) throws Exception {
		return super.getArgument();
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.method.trigger.AbstractAccessMethodTrigger#doGetJSON()
	 */
	@Override
	public String doGetJSON() {
		return null;
	}

	/**
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger#getName()
	 */
	@Override
	public String getName() {
		return Constant.NAME;
	}
}

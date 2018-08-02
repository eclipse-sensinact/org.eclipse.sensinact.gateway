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

import org.eclipse.sensinact.gateway.util.JSONUtils;

/**
 * Extended {@link AccessMethodTrigger} whose execution result is a constant
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Constant implements AccessMethodTrigger<Void> {
	public static final String NAME = "CONSTANT";

	/**
	 * constant object of this ConstantCalculation
	 */
	private final Object constant;

	private final boolean doPassOn;

	/**
	 * Constructor
	 * 
	 * @param constant
	 *            constant object of this ConstantCalculation
	 */
	public Constant(Object constant, boolean doPassOn) {
		this.constant = constant;
		this.doPassOn = doPassOn;
	}

	/**
	 * @inheritDoc
	 *
	 * @see Executable#execute(java.lang.Object)
	 */
	@Override
	public Object execute(Void v) throws Exception {
		return this.constant;
	}

	/**
	 * @inheritDoc
	 *
	 * @see JSONable#getJSON()
	 */
	@Override
	public String getJSON() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(JSONUtils.OPEN_BRACE);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(TRIGGER_TYPE_KEY);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(this.getName());
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COMMA);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(TRIGGER_PASS_ON);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(this.doPassOn);
		buffer.append(JSONUtils.COMMA);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(TRIGGER_CONSTANT_KEY);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(JSONUtils.toJSONFormat(this.constant));
		buffer.append(JSONUtils.CLOSE_BRACE);
		return buffer.toString();
	}

	/**
	 * @inheritDoc
	 * 
	 * @see AccessMethodTrigger#getName()
	 */
	@Override
	public String getName() {
		return Constant.NAME;
	}

	/**
	 * @inheritDoc
	 *
	 * @see AccessMethodTrigger#getParameters()
	 */
	@Override
	public AccessMethodTrigger.Parameters getParameters() {
		return AccessMethodTrigger.Parameters.EMPTY;
	}

	/**
	 * @inheritDoc
	 *
	 * @see AccessMethodTrigger#passOn()
	 */
	@Override
	public boolean passOn() {
		return this.doPassOn;
	}
}

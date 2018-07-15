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

import java.util.Iterator;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintConstantPair;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;

/**
 * Extended {@link AccessMethodTrigger} whose execution result depends on the
 * validation of a {@link Constraint}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ConditionalConstant implements AccessMethodTrigger<Object[]> {
	public static final String NAME = "CONDITIONAL";

	/**
	 * Map of {@link Constraint}s to associated constants
	 */
	private List<ConstraintConstantPair> constants;

	/**
	 * the index of the execution parameter on which to validate the constraint(s)
	 */
	private final int index;

	private final boolean doPassOn;

	/**
	 * Constructor
	 * 
	 * @param index
	 *            the index of the execution parameter on which to validate the
	 *            constraint(s)
	 * @param doPassOn
	 *            defines whether the result object has to be pass on for treatment
	 * @throws InvalidValueException
	 */
	public ConditionalConstant(Mediator mediator, int index, List<ConstraintConstantPair> constraints, boolean doPassOn)
			throws InvalidValueException {
		this.doPassOn = doPassOn;
		this.index = index;
		this.constants = constraints;
	}

	/**
	 * @inheritDoc
	 *
	 * @see Executable#execute(java.lang.Object)
	 */
	@Override
	public Object execute(Object[] parameters) throws Exception {
		if (this.constants == null) {
			return null;
		}
		Object object = parameters[this.index];

		Iterator<ConstraintConstantPair> iterator = this.constants.iterator();

		while (iterator.hasNext()) {
			ConstraintConstantPair entry = iterator.next();
			if (entry.constraint.complies(object)) {
				return entry.constant;
			}
		}
		return null;
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
		buffer.append(TRIGGER_INDEX_KEY);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(this.index);
		buffer.append(JSONUtils.COMMA);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(TRIGGER_CONSTANTS_KEY);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(JSONUtils.OPEN_BRACKET);

		if (this.constants != null) {
			Iterator<ConstraintConstantPair> iterator = this.constants.iterator();

			int index = 0;

			while (iterator.hasNext()) {
				ConstraintConstantPair entry = iterator.next();
				buffer.append(index > 0 ? JSONUtils.COMMA : JSONUtils.EMPTY);
				buffer.append(JSONUtils.OPEN_BRACE);
				buffer.append(JSONUtils.QUOTE);
				buffer.append(TRIGGER_CONSTANT_KEY);
				buffer.append(JSONUtils.QUOTE);
				buffer.append(JSONUtils.COLON);
				buffer.append(JSONUtils.toJSONFormat(entry.constant));
				buffer.append(JSONUtils.COMMA);
				buffer.append(JSONUtils.QUOTE);
				buffer.append(TRIGGER_CONSTRAINT_KEY);
				buffer.append(JSONUtils.QUOTE);
				buffer.append(JSONUtils.COLON);
				buffer.append(entry.constraint.getJSON());
				buffer.append(JSONUtils.CLOSE_BRACE);
				index++;
			}
		}
		buffer.append(JSONUtils.CLOSE_BRACKET);
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
		return ConditionalConstant.NAME;
	}

	/**
	 * @inheritDoc
	 *
	 * @see AccessMethodTrigger#getParameters()
	 */
	@Override
	public AccessMethodTrigger.Parameters getParameters() {
		return AccessMethodTrigger.Parameters.PARAMETERS;
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

/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.method.trigger;

import java.util.Iterator;
import java.util.List;

import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintConstantPair;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.util.JSONUtils;

/**
 * Extended {@link AccessMethodTrigger} whose execution result depends on the
 * validation of a {@link Constraint}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ConditionalConstant extends AbstractAccessMethodTrigger {
	public static final String NAME = "CONDITIONAL";

	/**
	 * Map of {@link Constraint}s to associated constants
	 */
	private List<ConstraintConstantPair> constants;

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
	public ConditionalConstant(Object argument, String argumentBuilder, boolean passOn, List<ConstraintConstantPair> constraints)
			throws InvalidValueException {
		super(argument,argumentBuilder,passOn);
		this.constants = constraints;
	}

	/**
	 * @inheritDoc
	 *
	 * @see Executable#execute(java.lang.Object)
	 */
	@Override
	public Object execute(Object parameter) throws Exception {
		if (this.constants == null) {
			return null;
		}
		Iterator<ConstraintConstantPair> iterator = this.constants.iterator();

		while (iterator.hasNext()) {
			ConstraintConstantPair entry = iterator.next();
			if (entry.constraint.complies(parameter)) {
				return entry.constant;
			}
		}
		return null;
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
	 * @see org.eclipse.sensinact.gateway.core.method.trigger.AbstractAccessMethodTrigger#doGetJSON()
	 */
	@Override
	public String doGetJSON() {
		StringBuilder builder = new StringBuilder();
		builder.append(JSONUtils.QUOTE);
		builder.append(TRIGGER_CONSTANTS_KEY);
		builder.append(JSONUtils.QUOTE);
		builder.append(JSONUtils.COLON);
		builder.append(JSONUtils.OPEN_BRACKET);

		if (this.constants != null) {
			Iterator<ConstraintConstantPair> iterator = this.constants.iterator();
			int index = 0;
			while (iterator.hasNext()) {
				ConstraintConstantPair entry = iterator.next();
				builder.append(index > 0 ? JSONUtils.COMMA : JSONUtils.EMPTY);
				builder.append(JSONUtils.OPEN_BRACE);
				builder.append(JSONUtils.QUOTE);
				builder.append(TRIGGER_CONSTANT_KEY);
				builder.append(JSONUtils.QUOTE);
				builder.append(JSONUtils.COLON);
				builder.append(JSONUtils.toJSONFormat(entry.constant));
				builder.append(JSONUtils.COMMA);
				builder.append(JSONUtils.QUOTE);
				builder.append(TRIGGER_CONSTRAINT_KEY);
				builder.append(JSONUtils.QUOTE);
				builder.append(JSONUtils.COLON);
				builder.append(entry.constraint.getJSON());
				builder.append(JSONUtils.CLOSE_BRACE);
				index++;
			}
		}
		builder.append(JSONUtils.CLOSE_BRACKET);
		return builder.toString();
	}
}

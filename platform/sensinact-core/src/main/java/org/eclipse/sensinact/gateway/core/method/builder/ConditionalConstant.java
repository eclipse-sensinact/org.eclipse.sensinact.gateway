/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.method.builder;

import java.util.Iterator;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintConstantPair;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.common.primitive.JSONable;
import org.eclipse.sensinact.gateway.core.method.DynamicParameterValue;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger;
import org.eclipse.sensinact.gateway.util.JSONUtils;

/**
 * Extended {@link DynamicParameterValue} whose execution result depends on the
 * validation of a {@link Constraint}
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ConditionalConstant extends AbstractDynamicParameterValue {
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
	public ConditionalConstant(Mediator mediator, String parameterName, String resourceName,
			List<ConstraintConstantPair> constraints) throws InvalidValueException {
		super(mediator, parameterName, resourceName);
		this.constants = constraints;
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
		buffer.append(DynamicParameterValue.BUILDER_TYPE_KEY);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(this.getName());
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COMMA);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(DynamicParameterValue.BUILDER_PARAMETER_KEY);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(super.parameterName);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COMMA);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(DynamicParameterValue.BUILDER_RESOURCE_KEY);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COLON);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(super.resourceName);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(JSONUtils.COMMA);
		buffer.append(JSONUtils.QUOTE);
		buffer.append(ConditionalConstant.BUILDER_CONSTANTS_KEY);
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
				buffer.append(DynamicParameterValue.BUILDER_CONSTANT_KEY);
				buffer.append(JSONUtils.QUOTE);
				buffer.append(JSONUtils.COLON);
				buffer.append(JSONUtils.toJSONFormat(entry.constant));
				buffer.append(JSONUtils.COMMA);
				buffer.append(JSONUtils.QUOTE);
				buffer.append(ConditionalConstant.BUILDER_CONSTRAINT_KEY);
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
	 * @InheritedDoc
	 *
	 * @see DynamicParameterValue#getResource()
	 */
	@Override
	public String getResource() {
		return this.resourceName;
	}

	/**
	 * @InheritedDoc
	 *
	 * @see DynamicParameterValue#getValue()
	 */
	@Override
	public Object getValue() {
		if (this.constants == null) {
			return null;
		}
		Object object = this.getResourceValue();

		Iterator<ConstraintConstantPair> iterator = this.constants.iterator();

		while (iterator.hasNext()) {
			ConstraintConstantPair entry = iterator.next();
			if (entry.constraint.complies(object)) {
				return entry.constant;
			}
		}
		return null;
	}
}

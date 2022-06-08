/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.method.trigger;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.Constraint;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintConstantPair;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintFactory;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

/**
 * Default core's {@link AccessMethodTriggerFactory} implementation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class DefaultAccessMethodTriggerFactory implements AccessMethodTriggerFactory {
	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTriggerFactory#
	 *      handle(java.lang.String)
	 */
	@Override
	public boolean handle(String type) {
		try {
			return AccessMethodTrigger.Type.valueOf(type) != null;

		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTriggerFactory#
	 *      newInstance(org.eclipse.sensinact.gateway.common.bundle.Mediator,
	 *      org.json.JSONObject)
	 */
	@Override
	public AccessMethodTrigger newInstance(Mediator mediator, JsonObject jsonTrigger)
			throws InvalidValueException {
		if (jsonTrigger == null) {
			throw new InvalidValueException("Null JSON trigger definition");
		}
		AccessMethodTrigger trigger = null;
		try {
			String jsonType = jsonTrigger.getString(AccessMethodTrigger.TRIGGER_TYPE_KEY);

			AccessMethodTrigger.Type type = AccessMethodTrigger.Type.valueOf(jsonType);
			String builder = jsonTrigger.getString(AccessMethodTrigger.TRIGGER_BUILDER_KEY);
			boolean passOn = jsonTrigger.getBoolean(AccessMethodTrigger.TRIGGER_PASSON_KEY, false);
			Object argument = Constraint.toConstantValue(jsonTrigger.get(AccessMethodTrigger.TRIGGER_ARGUMENT_KEY));

			switch (type) {
			case CONDITIONAL:
				List<ConstraintConstantPair> constraints = new ArrayList<ConstraintConstantPair>();
				JsonArray constants = jsonTrigger.getJsonArray(Constant.TRIGGER_CONSTANTS_KEY);
				
				int constantsIndex = 0;
				int length = constants == null ? 0 : constants.size();

				for (; constantsIndex < length; constantsIndex++) {
					JsonObject constantObject = constants.getJsonObject(constantsIndex);

					constraints.add(new ConstraintConstantPair(
							ConstraintFactory.Loader.load(mediator.getClassLoader(),
									constantObject.get(Constant.TRIGGER_CONSTRAINT_KEY)),
							constantObject.get(Constant.TRIGGER_CONSTANT_KEY)));
				}
				trigger = new ConditionalConstant(argument, builder, passOn, constraints);
				break;
			case CONSTANT:
				trigger = new Constant(argument, passOn);
				break;
			case COPY:
				trigger = new Copy(argument, builder, passOn);
				break;
			default:
				throw new InvalidValueException(
						new StringBuilder().append("Unknown calculation identifier :").append(jsonType).toString());
			}
		} catch (Exception e) {
			throw new InvalidValueException(e);
		}
		return trigger;
	}
}

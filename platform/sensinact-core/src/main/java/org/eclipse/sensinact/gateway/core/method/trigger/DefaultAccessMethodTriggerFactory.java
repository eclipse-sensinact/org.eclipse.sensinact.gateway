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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintConstantPair;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintFactory;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;

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
	public AccessMethodTrigger newInstance(Mediator mediator, JSONObject jsonTrigger)
			throws InvalidValueException {
		if (JSONObject.NULL.equals(jsonTrigger)) {
			throw new InvalidValueException("Null JSON trigger definition");
		}
		AccessMethodTrigger trigger = null;
		try {
			String jsonType = jsonTrigger.getString(AccessMethodTrigger.TRIGGER_TYPE_KEY);

			AccessMethodTrigger.Type type = AccessMethodTrigger.Type.valueOf(jsonType);
			String builder = jsonTrigger.getString(AccessMethodTrigger.TRIGGER_BUILDER_KEY);
			boolean passOn = jsonTrigger.optBoolean(AccessMethodTrigger.TRIGGER_PASSON_KEY);
			Object argument = jsonTrigger.opt(AccessMethodTrigger.TRIGGER_ARGUMENT_KEY);

			switch (type) {
			case CONDITIONAL:
				List<ConstraintConstantPair> constraints = new ArrayList<ConstraintConstantPair>();
				JSONArray constants = jsonTrigger.optJSONArray(Constant.TRIGGER_CONSTANTS_KEY);
				
				int constantsIndex = 0;
				int length = constants == null ? 0 : constants.length();

				for (; constantsIndex < length; constantsIndex++) {
					JSONObject constantObject = constants.getJSONObject(constantsIndex);

					constraints.add(new ConstraintConstantPair(
							ConstraintFactory.Loader.load(mediator.getClassLoader(),
									constantObject.opt(Constant.TRIGGER_CONSTRAINT_KEY)),
							constantObject.opt(Constant.TRIGGER_CONSTANT_KEY)));
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

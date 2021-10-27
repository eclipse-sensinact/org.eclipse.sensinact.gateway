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
package org.eclipse.sensinact.gateway.core.method.builder;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintConstantPair;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintFactory;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.method.DynamicParameterValue;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Default core's {@link DynamicParameterValueFactory} implementation
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class DefaultDynamicParameterValueFactory implements DynamicParameterValueFactory {
	/**
	 * @inheritDoc
	 *
	 * @see AccessMethodTriggerFactory# handle(java.lang.String)
	 */
	@Override
	public boolean handle(String type) {
		try {
			return DynamicParameterValue.Type.valueOf(type) != null;

		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * @inheritDoc
	 *
	 * @see AccessMethodTriggerFactory# newInstance(org.json.JSONObject)
	 */
	@Override
	public DynamicParameterValue newInstance(Mediator mediator, Executable<Void, Object> resourceValueExtractor,
			JSONObject jsonBuilder) throws InvalidValueException {
		if (JSONObject.NULL.equals(jsonBuilder)) {
			throw new InvalidValueException("Null JSON trigger definition");
		}
		DynamicParameterValue builder = null;
		try {
			String jsonType = jsonBuilder.getString(DynamicParameterValue.BUILDER_TYPE_KEY);
			DynamicParameterValue.Type type = DynamicParameterValue.Type.valueOf(jsonType);
			String resourceName = jsonBuilder.optString(DynamicParameterValue.BUILDER_RESOURCE_KEY);
			String parameterName = jsonBuilder.optString(DynamicParameterValue.BUILDER_PARAMETER_KEY);

			switch (type) {
			case CONDITIONAL:
				List<ConstraintConstantPair> constraints = new ArrayList<ConstraintConstantPair>();

				JSONArray constants = jsonBuilder.optJSONArray(DynamicParameterValue.BUILDER_CONSTANTS_KEY);

				int constantsIndex = 0;
				int length = constants == null ? 0 : constants.length();

				for (; constantsIndex < length; constantsIndex++) {
					JSONObject constantObject = constants.getJSONObject(constantsIndex);

					constraints.add(new ConstraintConstantPair(
							ConstraintFactory.Loader.load(mediator.getClassLoader(),
									constantObject.opt(DynamicParameterValue.BUILDER_CONSTRAINT_KEY)),
							constantObject.opt(DynamicParameterValue.BUILDER_CONSTANT_KEY)));
				}
				builder = new ConditionalConstant(mediator, parameterName, resourceName, constraints);
				break;
			case COPY:
				builder = new Copy(mediator, parameterName, resourceName);
				break;
			default:
				throw new InvalidValueException(
						new StringBuilder().append("Unknown builder identifier :").append(jsonType).toString());
			}
		} catch (Exception e) {
			throw new InvalidValueException(e);
		}
		((AbstractDynamicParameterValue) builder).setResourceValueExtractor(resourceValueExtractor);
		return builder;
	}
}

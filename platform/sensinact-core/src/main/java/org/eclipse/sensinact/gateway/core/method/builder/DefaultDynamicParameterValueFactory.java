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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintConstantPair;
import org.eclipse.sensinact.gateway.common.constraint.ConstraintFactory;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.common.primitive.InvalidValueException;
import org.eclipse.sensinact.gateway.core.method.DynamicParameterValue;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

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
	 * @see AccessMethodTriggerFactory# newInstance(jakarta.json.JsonObject)
	 */
	@Override
	public DynamicParameterValue newInstance(Mediator mediator, Executable<Void, Object> resourceValueExtractor,
			JsonObject jsonBuilder) throws InvalidValueException {
		if (jsonBuilder == null) {
			throw new InvalidValueException("Null JSON trigger definition");
		}
		DynamicParameterValue builder = null;
		try {
			String jsonType = jsonBuilder.getString(DynamicParameterValue.BUILDER_TYPE_KEY);
			DynamicParameterValue.Type type = DynamicParameterValue.Type.valueOf(jsonType);
			String resourceName = jsonBuilder.getString(DynamicParameterValue.BUILDER_RESOURCE_KEY, null);
			String parameterName = jsonBuilder.getString(DynamicParameterValue.BUILDER_PARAMETER_KEY, null);

			switch (type) {
			case CONDITIONAL:
				List<ConstraintConstantPair> constraints = new ArrayList<ConstraintConstantPair>();

				JsonArray constants = jsonBuilder.getJsonArray(DynamicParameterValue.BUILDER_CONSTANTS_KEY);

				int constantsIndex = 0;
				int length = constants == null ? 0 : constants.size();

				for (; constantsIndex < length; constantsIndex++) {
					JsonObject constantObject = constants.getJsonObject(constantsIndex);

					constraints.add(new ConstraintConstantPair(
							ConstraintFactory.Loader.load(mediator.getClassLoader(),
									constantObject.get(DynamicParameterValue.BUILDER_CONSTRAINT_KEY)),
							constantObject.get(DynamicParameterValue.BUILDER_CONSTANT_KEY)));
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

/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/

package org.eclipse.sensinact.gateway.core.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.method.DynamicParameterValue;
import org.eclipse.sensinact.gateway.core.method.builder.DynamicParameterValueFactory;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;

import jakarta.json.JsonObject;

/**
 * test Constraint
 */
@ExtendWith(BundleContextExtension.class)
public class DynamicParameterTest {
	public static final String BUILDER_0 = "{\"type\":\"CONDITIONAL\",\"resource\":\"fake\",\"parameter\":\"fake\","
			+ "\"constants\":[" + "{\"constant\":100,"
			+ " \"constraint\":{\"operator\":\"in\",\"operand\":[22,23,18,3], \"type\":\"int\", \"complement\":false}},"
			+ "{\"constant\":1000,"
			+ " \"constraint\":{\"operator\":\">=\",\"operand\":5, \"type\":\"int\", \"complement\":false}},"
			+ "{\"constant\":0,"
			+ " \"constraint\":{\"operator\":\">=\",\"operand\":5, \"type\":\"int\", \"complement\":true}}]}";

	public static final String BUILDER_2 = "{\"type\":\"COPY\",\"resource\":\"fake\",\"parameter\":\"fake\"}";

	public static final String BUILDER_3 = "{\"type\":\"VARIABLE_PARAMETER_BUILDER\",\"resource\":\"fake\",\"parameter\":\"fake\"}";

	private Mediator mediator;

	@BeforeEach
	public void init(@InjectBundleContext BundleContext context) throws InvalidSyntaxException {
		mediator = new Mediator(context);
	}

	@Test
	public void testFactory() throws Exception {
		DynamicParameterValueFactory.Loader loader = DynamicParameterValueFactory.LOADER.get();
		try {
			DynamicParameterValueFactory factory = loader.load(mediator, DynamicParameterValue.Type.CONDITIONAL.name());

			JsonObject jsonBuilder = JsonProviderFactory.readObject(DynamicParameterTest.BUILDER_0);
			DynamicParameterValue trigger = factory.newInstance(mediator, new Executable<Void, Object>() {
				private int n = 0;
				private int[] ns = new int[] { 2, 22, 18, 55 };

				@Override
				public Object execute(Void parameter) throws Exception {
					return ns[n++];
				}
			}, jsonBuilder);

			assertEquals(0, trigger.getValue());
			assertEquals(100, trigger.getValue());
			assertEquals(100, trigger.getValue());
			assertEquals(1000, trigger.getValue());

			String triggerJSON = trigger.getJSON();
			assertEquals(JsonProviderFactory.readObject(DynamicParameterTest.BUILDER_0), 
					JsonProviderFactory.readObject(triggerJSON));

			jsonBuilder = JsonProviderFactory.readObject(DynamicParameterTest.BUILDER_2);
			trigger = factory.newInstance(mediator, new Executable<Void, Object>() {
				private int n = 0;
				private Object[] ns = new Object[] { "value", 2, "copy" };

				@Override
				public Object execute(Void parameter) throws Exception {
					return ns[n++];
				}
			}, jsonBuilder);

			assertEquals("value", trigger.getValue());
			assertEquals(2, trigger.getValue());
			assertEquals("copy", trigger.getValue());

			assertEquals(JsonProviderFactory.readObject(DynamicParameterTest.BUILDER_2), 
					JsonProviderFactory.readObject(trigger.getJSON()));

			jsonBuilder = JsonProviderFactory.readObject(DynamicParameterTest.BUILDER_3);
			factory = loader.load(mediator, jsonBuilder.getString("type"));
			trigger = factory.newInstance(mediator, new Executable<Void, Object>() {
				@Override
				public Object execute(Void parameter) throws Exception {
					return 20;
				}
			}, jsonBuilder);

			assertEquals(0.2f, (Float) trigger.getValue(), 0.0f);
		} finally {
			DynamicParameterValueFactory.LOADER.remove();
		}
	}

}

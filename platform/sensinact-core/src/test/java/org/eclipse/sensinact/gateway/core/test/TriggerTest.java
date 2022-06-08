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
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTriggerFactory;
import org.eclipse.sensinact.gateway.core.method.trigger.TriggerArgumentBuilder;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.skyscreamer.jsonassert.JSONAssert;

import jakarta.json.JsonObject;

/**
 * test Constraint
 */
@ExtendWith(BundleContextExtension.class)
public class TriggerTest {
	public static final String TRIGGER_0 = "{\"type\":\"CONDITIONAL\",\"passOn\":false,\"argument\": 0,\"builder\":\"PARAMETER\","
			+ "\"constants\":[" + "{\"constant\":100,"
			+ " \"constraint\":{\"operator\":\"in\",\"operand\":[22,23,18,3], \"type\":\"int\", \"complement\":false}},"
			+ "{\"constant\":1000,"
			+ " \"constraint\":{\"operator\":\">=\",\"operand\":5, \"type\":\"int\", \"complement\":false}},"
			+ "{\"constant\":0,"
			+ " \"constraint\":{\"operator\":\">=\",\"operand\":5, \"type\":\"int\", \"complement\":true}}]}";

	public static final String TRIGGER_1 = "{\"type\":\"CONSTANT\",\"passOn\":false, \"argument\":\"constant\", \"builder\":\"EMPTY\"}";

	public static final String TRIGGER_2 = "{\"type\":\"COPY\",\"passOn\":false,\"argument\": 2, \"builder\":\"PARAMETER\"}";

	public static final String TRIGGER_3 = "{\"type\":\"VARIATIONTEST_TRIGGER\",\"passOn\":false,\"argument\":0, \"builder\":\"EMPTY\"}";

	private Mediator mediator;

	@BeforeEach
	public void init(@InjectBundleContext BundleContext context) throws InvalidSyntaxException {
		mediator = new Mediator(context);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testFactory() throws Exception {
		AccessMethodTriggerFactory.Loader loader = AccessMethodTriggerFactory.LOADER.get();
		try {
			AccessMethodTriggerFactory factory = loader.load(mediator, AccessMethodTrigger.Type.CONDITIONAL.name());

			JsonObject jsonTrigger = JsonProviderFactory.readObject(TriggerTest.TRIGGER_0);
			AccessMethodTrigger trigger = factory.newInstance(mediator, jsonTrigger);

			String triggerJSON = trigger.getJSON();

			assertEquals(0, trigger.execute(new TriggerArgumentBuilder.Parameter(
				trigger.<Integer>getArgument()).build(
					new AccessMethodResponseBuilder("/", new Object[]{2}) {
					private static final long serialVersionUID = 1L;

					@Override
					public Class getComponentType() {
						return null;
					}

					@Override
					public AccessMethodResponse createAccessMethodResponse(Status status) {
						return null;
					}					
				})));
			assertEquals(100,trigger.execute(new TriggerArgumentBuilder.Parameter(
				trigger.<Integer>getArgument()).build(
					new AccessMethodResponseBuilder("/", new Object[]{22}) {
					private static final long serialVersionUID = 1L;

					@Override
					public Class getComponentType() {
						return null;
					}

					@Override
					public AccessMethodResponse createAccessMethodResponse(Status status) {
						return null;
					}					
				})));
			assertEquals(100, trigger.execute(new TriggerArgumentBuilder.Parameter(
				trigger.<Integer>getArgument()).build(
					new AccessMethodResponseBuilder("/", new Object[]{18}) {
					private static final long serialVersionUID = 1L;

					@Override
					public Class getComponentType() {
						return null;
					}

					@Override
					public AccessMethodResponse createAccessMethodResponse(Status status) {
						return null;
					}					
				})));
			assertEquals(1000,trigger.execute(new TriggerArgumentBuilder.Parameter(
				trigger.<Integer>getArgument()).build(
					new AccessMethodResponseBuilder("/", new Object[]{55}) {
					private static final long serialVersionUID = 1L;

					@Override
					public Class getComponentType() {
						return null;
					}

					@Override
					public AccessMethodResponse createAccessMethodResponse(Status status) {
						return null;
					}					
				})));
			
			assertEquals(JsonProviderFactory.readObject(TriggerTest.TRIGGER_0), JsonProviderFactory.readObject(triggerJSON));

			trigger = factory.newInstance(mediator, JsonProviderFactory.readObject(TriggerTest.TRIGGER_1));
			assertEquals("constant", trigger.execute(new TriggerArgumentBuilder.Empty().build(null)));

			JSONAssert.assertEquals(TriggerTest.TRIGGER_1, trigger.getJSON(), false);

			trigger = factory.newInstance(mediator, JsonProviderFactory.readObject(TriggerTest.TRIGGER_2));
			assertEquals("value", trigger.execute(new TriggerArgumentBuilder.Parameter(trigger.<Integer>getArgument()).build(
				new AccessMethodResponseBuilder( "/", new Object[]{ 2, "copy", "value"}) {

					private static final long serialVersionUID = 1L;

					@Override
					public Class getComponentType() {
						return null;
					}

					@Override
					public AccessMethodResponse createAccessMethodResponse(Status status) {
						return null;
					}					
				})));
			assertEquals(2, trigger.execute(new TriggerArgumentBuilder.Parameter(trigger.<Integer>getArgument()).build(
				new AccessMethodResponseBuilder( "/", new Object[]{ "copy", "value", 2}) {

					private static final long serialVersionUID = 1L;

					@Override
					public Class getComponentType() {
						return null;
					}

					@Override
					public AccessMethodResponse createAccessMethodResponse(Status status) {
						return null;
					}					
				})));
			assertEquals("copy", trigger.execute(new TriggerArgumentBuilder.Parameter(trigger.<Integer>getArgument()).build(
				new AccessMethodResponseBuilder("/", new Object[]{ "value", 2, "copy"}) {

					private static final long serialVersionUID = 1L;

					@Override
					public Class getComponentType() {
						return null;
					}

					@Override
					public AccessMethodResponse createAccessMethodResponse(Status status) {
						return null;
					}					
				})));

			JSONAssert.assertEquals(TriggerTest.TRIGGER_2, trigger.getJSON(), false);

			jsonTrigger =  JsonProviderFactory.readObject(TriggerTest.TRIGGER_3);
			factory = loader.load(mediator, jsonTrigger.getString("type"));
			trigger = factory.newInstance(mediator, jsonTrigger);

			assertEquals(0.2f, (Float) trigger.execute(new TriggerArgumentBuilder.Empty().build(null)), 0.0f);
		} finally {
			AccessMethodTriggerFactory.LOADER.remove();
		}
	}

}

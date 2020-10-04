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

package org.eclipse.sensinact.gateway.core.test;

import static org.junit.Assert.assertEquals;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.skyscreamer.jsonassert.JSONAssert;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponse.Status;
import org.eclipse.sensinact.gateway.core.method.AccessMethodResponseBuilder;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTrigger;
import org.eclipse.sensinact.gateway.core.method.trigger.AccessMethodTriggerFactory;
import org.eclipse.sensinact.gateway.core.method.trigger.TriggerArgumentBuilder;

/**
 * test Constraint
 */
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

	private static final String LOG_FILTER = "(" + Constants.OBJECTCLASS + "=" + LogService.class.getCanonicalName()
			+ ")";

	private static final String MOCK_BUNDLE_NAME = "MockedBundle";
	private static final long MOCK_BUNDLE_ID = 1;

	private final BundleContext context = Mockito.mock(BundleContext.class);
	private final Bundle bundle = Mockito.mock(Bundle.class);

	private Mediator mediator;

	@Before
	public void init() throws InvalidSyntaxException {
		Filter filter = Mockito.mock(Filter.class);
		Mockito.when(filter.toString()).thenReturn(LOG_FILTER);

		Mockito.when(context.createFilter(LOG_FILTER)).thenReturn(filter);
		Mockito.when(context.getServiceReferences((String) Mockito.eq(null), Mockito.eq(LOG_FILTER))).thenReturn(null);
		Mockito.when(context.getServiceReference(LOG_FILTER)).thenReturn(null);

		Mockito.when(context.getServiceReferences(Mockito.anyString(), Mockito.anyString()))
				.then(new Answer<ServiceReference[]>() {
					@Override
					public ServiceReference[] answer(InvocationOnMock invocation) throws Throwable {
						Object[] arguments = invocation.getArguments();
						if (arguments == null || arguments.length != 2) {
							return null;
						}
						return null;
					}
				});
		Mockito.when(context.getService(Mockito.any(ServiceReference.class))).then(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] arguments = invocation.getArguments();
				if (arguments == null || arguments.length != 1) {
					return null;
				}
				return null;
			}
		});
		Mockito.when(context.getBundle()).thenReturn(bundle);
		Mockito.when(bundle.getSymbolicName()).thenReturn(MOCK_BUNDLE_NAME);
		Mockito.when(bundle.getBundleId()).thenReturn(MOCK_BUNDLE_ID);

		mediator = new Mediator(context);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testFactory() throws Exception {
		AccessMethodTriggerFactory.Loader loader = AccessMethodTriggerFactory.LOADER.get();
		try {
			AccessMethodTriggerFactory factory = loader.load(mediator, AccessMethodTrigger.Type.CONDITIONAL.name());

			JSONObject jsonTrigger = new JSONObject(TriggerTest.TRIGGER_0);
			AccessMethodTrigger trigger = factory.newInstance(mediator, jsonTrigger);

			String triggerJSON = trigger.getJSON();

			assertEquals(0, trigger.execute(new TriggerArgumentBuilder.Parameter(
				trigger.<Integer>getArgument()).build(
					new AccessMethodResponseBuilder(mediator, "/", new Object[]{2}) {
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
					new AccessMethodResponseBuilder(mediator, "/", new Object[]{22}) {
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
					new AccessMethodResponseBuilder(mediator, "/", new Object[]{18}) {
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
					new AccessMethodResponseBuilder(mediator, "/", new Object[]{55}) {
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
			
			JSONAssert.assertEquals(TriggerTest.TRIGGER_0, triggerJSON, false);

			trigger = factory.<Object>newInstance(mediator, new JSONObject(TriggerTest.TRIGGER_1));
			assertEquals("constant", trigger.execute(new TriggerArgumentBuilder.Empty().build(null)));

			JSONAssert.assertEquals(TriggerTest.TRIGGER_1, trigger.getJSON(), false);

			trigger = factory.<Object>newInstance(mediator, new JSONObject(TriggerTest.TRIGGER_2));
			assertEquals("value", trigger.execute(new TriggerArgumentBuilder.Parameter(trigger.<Integer>getArgument()).build(
				new AccessMethodResponseBuilder(mediator, "/", new Object[]{ 2, "copy", "value"}) {

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
				new AccessMethodResponseBuilder(mediator, "/", new Object[]{ "copy", "value", 2}) {

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
				new AccessMethodResponseBuilder(mediator, "/", new Object[]{ "value", 2, "copy"}) {

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

			jsonTrigger = new JSONObject(TriggerTest.TRIGGER_3);
			factory = loader.load(mediator, jsonTrigger.getString("type"));
			trigger = factory.newInstance(mediator, jsonTrigger);

			assertEquals(0.2f, (Float) trigger.execute(new TriggerArgumentBuilder.Empty().build(null)), 0.0f);
		} finally {
			AccessMethodTriggerFactory.LOADER.remove();
		}
	}

}

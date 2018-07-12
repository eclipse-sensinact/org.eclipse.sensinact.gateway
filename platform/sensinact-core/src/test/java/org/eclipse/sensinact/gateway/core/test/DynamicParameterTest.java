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
package org.eclipse.sensinact.gateway.core.test;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.method.DynamicParameterValue;
import org.eclipse.sensinact.gateway.core.method.builder.DynamicParameterValueFactory;
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

import static org.junit.Assert.assertEquals;

/**
 * test Constraint
 */
public class DynamicParameterTest {
    public static final String BUILDER_0 = "{\"type\":\"CONDITIONAL\",\"resource\":\"fake\",\"parameter\":\"fake\"," + "\"constants\":[" + "{\"constant\":100," + " \"constraint\":{\"operator\":\"in\",\"operand\":[22,23,18,3], \"type\":\"int\", \"complement\":false}}," + "{\"constant\":1000," + " \"constraint\":{\"operator\":\">=\",\"operand\":5, \"type\":\"int\", \"complement\":false}}," + "{\"constant\":0," + " \"constraint\":{\"operator\":\">=\",\"operand\":5, \"type\":\"int\", \"complement\":true}}]}";
    public static final String BUILDER_2 = "{\"type\":\"COPY\",\"resource\":\"fake\",\"parameter\":\"fake\"}";

    public static final String BUILDER_3 = "{\"type\":\"VARIABLE_PARAMETER_BUILDER\",\"resource\":\"fake\",\"parameter\":\"fake\"}";

    private static final String LOG_FILTER = "(" + Constants.OBJECTCLASS + "=" + LogService.class.getCanonicalName() + ")";

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
        Mockito.when(context.getServiceReferences(Mockito.anyString(), Mockito.anyString())).then(new Answer<ServiceReference[]>() {
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

    @Test
    public void testFactory() throws Exception {
        DynamicParameterValueFactory.Loader loader = DynamicParameterValueFactory.LOADER.get();
        try {
            DynamicParameterValueFactory factory = loader.load(mediator, DynamicParameterValue.Type.CONDITIONAL.name());

            JSONObject jsonBuilder = new JSONObject(DynamicParameterTest.BUILDER_0);
            DynamicParameterValue trigger = factory.newInstance(mediator, new Executable<Void, Object>() {
                private int n = 0;
                private int[] ns = new int[]{2, 22, 18, 55};

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
            JSONAssert.assertEquals(DynamicParameterTest.BUILDER_0, triggerJSON, false);

            jsonBuilder = new JSONObject(DynamicParameterTest.BUILDER_2);
            trigger = factory.newInstance(mediator, new Executable<Void, Object>() {
                private int n = 0;
                private Object[] ns = new Object[]{"value", 2, "copy"};

                @Override
                public Object execute(Void parameter) throws Exception {
                    return ns[n++];
                }
            }, jsonBuilder);

            assertEquals("value", trigger.getValue());
            assertEquals(2, trigger.getValue());
            assertEquals("copy", trigger.getValue());

            JSONAssert.assertEquals(DynamicParameterTest.BUILDER_2, trigger.getJSON(), false);

            jsonBuilder = new JSONObject(DynamicParameterTest.BUILDER_3);
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

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
package org.eclipse.sensinact.gateway.util.json.test;

import org.eclipse.sensinact.gateway.util.PropertyUtils;
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
import org.osgi.service.log.LogService;

import static org.junit.Assert.assertEquals;

/**
 * test Constraint
 */
public class PropertyTest {
    private static final String MATCH = "match";
    private static final String PROPERTY = "this.is.$($(sub.sub.property).property).property";
    private static final String ALL_PROPERTY = "this.is.the.all.property";
    private static final String SUB_PROPERTY = "sub.property";
    private static final String SUB_SUB_PROPERTY = "sub.sub.property";

    private static final String LOG_FILTER = "(" + Constants.OBJECTCLASS + "=" + LogService.class.getCanonicalName() + ")";

    private static final String MOCK_BUNDLE_NAME = "MockedBundle";
    private static final long MOCK_BUNDLE_ID = 1;

    private final BundleContext context = Mockito.mock(BundleContext.class);
    private final Bundle bundle = Mockito.mock(Bundle.class);

    @Before
    public void init() throws InvalidSyntaxException {
        Filter filter = Mockito.mock(Filter.class);
        Mockito.when(filter.toString()).thenReturn(LOG_FILTER);

        Mockito.when(context.createFilter(LOG_FILTER)).thenReturn(filter);
        Mockito.when(context.getServiceReferences((String) Mockito.eq(null), Mockito.eq(LOG_FILTER))).thenReturn(null);
        Mockito.when(context.getServiceReference(LOG_FILTER)).thenReturn(null);
        Mockito.when(context.getProperty(Mockito.anyString())).then(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                if (arguments == null || arguments.length != 1) {
                    return null;
                }
                if (((String) arguments[0]).intern() == SUB_SUB_PROPERTY.intern()) {
                    return "sub";
                }
                if (((String) arguments[0]).intern() == SUB_PROPERTY.intern()) {
                    return "the.all";
                }
                if (((String) arguments[0]).intern() == ALL_PROPERTY.intern()) {
                    return "match";
                }
                return null;
            }
        });
        Mockito.when(context.getBundle()).thenReturn(bundle);
        Mockito.when(bundle.getSymbolicName()).thenReturn(MOCK_BUNDLE_NAME);
        Mockito.when(bundle.getBundleId()).thenReturn(MOCK_BUNDLE_ID);
    }

    @Test
    public void testProperty() throws Exception {
        String result = (String) PropertyUtils.getProperty(context, null, PROPERTY);
        assertEquals(MATCH, result);
    }
}

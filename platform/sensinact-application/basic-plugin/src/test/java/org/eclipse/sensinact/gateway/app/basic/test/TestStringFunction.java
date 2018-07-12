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
package org.eclipse.sensinact.gateway.app.basic.test;

import junit.framework.TestCase;
import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.app.api.function.FunctionUpdateListener;
import org.eclipse.sensinact.gateway.app.basic.string.ConcatenateFunction;
import org.eclipse.sensinact.gateway.app.basic.string.SubstringFunction;
import org.eclipse.sensinact.gateway.app.manager.component.data.ConstantData;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
public class TestStringFunction extends TestCase {
    @Mock
    private Mediator mediator;
    @Mock
    private FunctionUpdateListener listener;

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(mediator.isDebugLoggable()).thenReturn(false);
    }

    public void testConcatenate() {
        ConcatenateFunction function = new ConcatenateFunction();
        function.setListener(listener);
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData("This is a ", String.class));
        variables.add(new ConstantData("concatenation with the value ", String.class));
        variables.add(new ConstantData(100, Integer.class));
        function.process(variables);
        Mockito.verify(listener).updatedResult("This is a concatenation with the value 100");
    }

    public void testSubstringFromStart() {
        SubstringFunction function = new SubstringFunction(mediator);
        function.setListener(listener);
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData("Something to substring", String.class));
        variables.add(new ConstantData(13, Integer.class));
        function.process(variables);
        Mockito.verify(listener).updatedResult("substring");
    }

    public void testSubstringStartEnd() {
        SubstringFunction function = new SubstringFunction(mediator);
        function.setListener(listener);
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData("Something to substring", String.class));
        variables.add(new ConstantData(0, Integer.class));
        variables.add(new ConstantData(9, Integer.class));
        function.process(variables);
        Mockito.verify(listener).updatedResult("Something");
    }
}

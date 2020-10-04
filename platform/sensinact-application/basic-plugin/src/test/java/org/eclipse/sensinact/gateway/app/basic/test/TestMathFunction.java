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
package org.eclipse.sensinact.gateway.app.basic.test;

import junit.framework.TestCase;
import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.app.api.function.FunctionUpdateListener;
import org.eclipse.sensinact.gateway.app.basic.math.AdditionFunction;
import org.eclipse.sensinact.gateway.app.basic.math.AssignmentFunction;
import org.eclipse.sensinact.gateway.app.basic.math.DivisionFunction;
import org.eclipse.sensinact.gateway.app.basic.math.ModuloFunction;
import org.eclipse.sensinact.gateway.app.basic.math.MultiplicationFunction;
import org.eclipse.sensinact.gateway.app.basic.math.SubtractionFunction;
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
public class TestMathFunction extends TestCase {

    @Mock
    private Mediator mediator;
    @Mock
    private FunctionUpdateListener listener;

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(mediator.isDebugLoggable()).thenReturn(false);
    }

    public void testAddition() {
        AdditionFunction function = new AdditionFunction(mediator);
        function.setListener(listener);
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(1, Integer.class));
        variables.add(new ConstantData(2, Integer.class));
        variables.add(new ConstantData(3, Integer.class));
        function.process(variables);
        Mockito.verify(listener).updatedResult(6d);
    }

    public void testSubtraction() {
        SubtractionFunction function = new SubtractionFunction(mediator);
        function.setListener(listener);
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(10, Integer.class));
        variables.add(new ConstantData(4, Integer.class));
        function.process(variables);
        Mockito.verify(listener).updatedResult(6d);
    }

    public void testMultiplication() {
        MultiplicationFunction function = new MultiplicationFunction(mediator);
        function.setListener(listener);
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(10, Integer.class));
        variables.add(new ConstantData(10, Integer.class));
        variables.add(new ConstantData(4, Integer.class));
        function.process(variables);
        Mockito.verify(listener).updatedResult(400d);
    }

    public void testDivision() {
        DivisionFunction function = new DivisionFunction(mediator);
        function.setListener(listener);
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(11, Integer.class));
        variables.add(new ConstantData(4, Integer.class));
        function.process(variables);
        Mockito.verify(listener).updatedResult(2.75d);
    }

    public void testModulo() {
        ModuloFunction function = new ModuloFunction(mediator);
        function.setListener(listener);
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(11, Integer.class));
        variables.add(new ConstantData(4, Integer.class));
        function.process(variables);
        Mockito.verify(listener).updatedResult(3d);
    }

    public void testAssignment() {
        AssignmentFunction function = new AssignmentFunction(mediator);
        function.setListener(listener);
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(11, Integer.class));
        assertTrue(variables.get(0).getValue() instanceof Integer);
        assertTrue(variables.get(0).getValue().equals(11));
        function.process(variables);
        Mockito.verify(listener).updatedResult(11);
        variables.clear();
        variables.add(new ConstantData(true, Boolean.class));
        assertTrue(variables.get(0).getValue() instanceof Boolean);
        assertTrue(variables.get(0).getValue().equals(true));
        function.process(variables);
        Mockito.verify(listener).updatedResult(true);
        variables.clear();
        variables.add(new ConstantData(1.0f, Float.class));
        assertTrue(variables.get(0).getValue() instanceof Float);
        assertTrue(variables.get(0).getValue().equals(1.0f));
        function.process(variables);
        Mockito.verify(listener).updatedResult(1.0f);
    }
}

/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.basic.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.app.api.function.FunctionUpdateListener;
import org.eclipse.sensinact.gateway.app.basic.math.AdditionFunction;
import org.eclipse.sensinact.gateway.app.basic.math.AssignmentFunction;
import org.eclipse.sensinact.gateway.app.basic.math.DivisionFunction;
import org.eclipse.sensinact.gateway.app.basic.math.ModuloFunction;
import org.eclipse.sensinact.gateway.app.basic.math.MultiplicationFunction;
import org.eclipse.sensinact.gateway.app.basic.math.SubtractionFunction;
import org.eclipse.sensinact.gateway.app.manager.component.data.ConstantData;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMathFunction  {
	
	private static final Logger LOG = LoggerFactory.getLogger(TestMathFunction.class);
    
    
    private FunctionUpdateListener listener=Mockito.mock(FunctionUpdateListener.class);

    @BeforeEach
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        Mockito.when(LOG.isDebugEnabled()).thenReturn(false);
    }

    public void testAddition() {
        AdditionFunction function = new AdditionFunction();
        function.setListener(listener);
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(1, Integer.class));
        variables.add(new ConstantData(2, Integer.class));
        variables.add(new ConstantData(3, Integer.class));
        function.process(variables);
        Mockito.verify(listener).updatedResult(6d);
    }

    public void testSubtraction() {
        SubtractionFunction function = new SubtractionFunction();
        function.setListener(listener);
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(10, Integer.class));
        variables.add(new ConstantData(4, Integer.class));
        function.process(variables);
        Mockito.verify(listener).updatedResult(6d);
    }

    public void testMultiplication() {
        MultiplicationFunction function = new MultiplicationFunction();
        function.setListener(listener);
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(10, Integer.class));
        variables.add(new ConstantData(10, Integer.class));
        variables.add(new ConstantData(4, Integer.class));
        function.process(variables);
        Mockito.verify(listener).updatedResult(400d);
    }

    public void testDivision() {
        DivisionFunction function = new DivisionFunction();
        function.setListener(listener);
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(11, Integer.class));
        variables.add(new ConstantData(4, Integer.class));
        function.process(variables);
        Mockito.verify(listener).updatedResult(2.75d);
    }

    public void testModulo() {
        ModuloFunction function = new ModuloFunction();
        function.setListener(listener);
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(11, Integer.class));
        variables.add(new ConstantData(4, Integer.class));
        function.process(variables);
        Mockito.verify(listener).updatedResult(3d);
    }

    public void testAssignment() {
        AssignmentFunction function = new AssignmentFunction();
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

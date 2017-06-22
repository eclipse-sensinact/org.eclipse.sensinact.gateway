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

import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.app.api.function.FunctionUpdateListener;
import org.eclipse.sensinact.gateway.app.basic.logic.BetweenFunction;
import org.eclipse.sensinact.gateway.app.basic.logic.DoubleConditionFunction;
import org.eclipse.sensinact.gateway.app.basic.logic.SimpleConditionFunction;
import org.eclipse.sensinact.gateway.app.manager.component.data.ConstantData;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
public class TestLogicFunction extends TestCase {

    @Mock
    private Mediator mediator;

    @Mock
    private FunctionUpdateListener listener;

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this); 
    }

    public void testIntegerSimpleConditionEquals() {
        SimpleConditionFunction function = new SimpleConditionFunction(mediator, "equal");
        function.setListener(listener);

        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(10, Integer.class));
        variables.add(new ConstantData(10, Integer.class));

        function.process(variables);

        Mockito.verify(listener).updatedResult(true);
    }

    public void testStringSimpleConditionEquals() {
        SimpleConditionFunction function = new SimpleConditionFunction(mediator, "equal");
        function.setListener(listener);

        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData("sNa test", String.class));
        variables.add(new ConstantData("sNa test", String.class));

        function.process(variables);

        Mockito.verify(listener).updatedResult(true);
    }

    public void testBooleanSimpleConditionEquals() {
        SimpleConditionFunction function = new SimpleConditionFunction(mediator, "equal");
        function.setListener(listener);

        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(false, Boolean.class));
        variables.add(new ConstantData(false, Boolean.class));

        function.process(variables);

        Mockito.verify(listener).updatedResult(true);
    }

    public void testFloatSimpleConditionEquals() {
        SimpleConditionFunction function = new SimpleConditionFunction(mediator, "equal");
        function.setListener(listener);

        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(1.0f, Float.class));
        variables.add(new ConstantData(1.0f, Float.class));

        function.process(variables);

        Mockito.verify(listener).updatedResult(true);
    }

    public void testIntegerSimpleConditionGreater() {
        SimpleConditionFunction function = new SimpleConditionFunction(mediator, "greaterThan");
        function.setListener(listener);

        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(2, Integer.class));
        variables.add(new ConstantData(1, Integer.class));

        function.process(variables);

        Mockito.verify(listener).updatedResult(true);
    }

    public void testIntegerSimpleConditionGreaterOrEquals() {
        SimpleConditionFunction function = new SimpleConditionFunction(mediator, "greaterEqual");
        function.setListener(listener);

        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(2, Integer.class));
        variables.add(new ConstantData(2, Integer.class));

        function.process(variables);

        Mockito.verify(listener).updatedResult(true);
    }

    public void testIntegerSimpleConditionLesser() {
        SimpleConditionFunction function = new SimpleConditionFunction(mediator, "lesserThan");
        function.setListener(listener);

        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(1, Integer.class));
        variables.add(new ConstantData(2, Integer.class));

        function.process(variables);

        Mockito.verify(listener).updatedResult(true);
    }

    public void testIntegerSimpleConditionLesserOrEquals() {
        SimpleConditionFunction function = new SimpleConditionFunction(mediator, "lesserEqual");
        function.setListener(listener);

        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(2, Integer.class));
        variables.add(new ConstantData(2, Integer.class));

        function.process(variables);

        Mockito.verify(listener).updatedResult(true);
    }

    public void testRegexSimpleCondition() {
        SimpleConditionFunction function = new SimpleConditionFunction(mediator, "regex");
        function.setListener(listener);

        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData("Hello", String.class));
        variables.add(new ConstantData("([A-Z])\\w+", String.class));

        function.process(variables);

        Mockito.verify(listener).updatedResult(true);
    }

    /*public void testAbsoluteSimpleCondition() {
        SimpleConditionFunction function = new SimpleConditionFunction(
                new AppParameter("output", boolean.class.getCanonicalName()), "abs");

        List<AppVariable> variables = new ArrayList<AppVariable>();
        variables.add(new AppStaticVariable(new AppParameter(1.0, "double")));
        variables.add(new AppStaticVariable(new AppParameter(new JSONArray().put(0.0).put(2.0),
                "array")));

        assertTrue(function.process(variables));
    }*/

    /*public void testDeltaSimpleCondition() {
        SimpleConditionFunction function = new SimpleConditionFunction(
                new AppParameter("output", boolean.class.getCanonicalName()), "diff");

        List<AppVariable> variables = new ArrayList<AppVariable>();
        variables.add(new AppStaticVariable(new AppParameter(1.0, "double")));
        variables.add(new AppStaticVariable(new AppParameter(new JSONArray().put(0.0).put(2.0),
                "array")));

        assertTrue(function.process(variables));
    }*/

    public void testCorrectDoubleConditionAnd() {
        DoubleConditionFunction function = new DoubleConditionFunction("and");
        function.setListener(listener);

        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(true, Boolean.class));
        variables.add(new ConstantData(true, Boolean.class));

        function.process(variables);

        Mockito.verify(listener).updatedResult(true);
    }

    public void testWringDoubleConditionAnd() {
        DoubleConditionFunction function = new DoubleConditionFunction("and");
        function.setListener(listener);

        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(false, Boolean.class));
        variables.add(new ConstantData(true, Boolean.class));

        function.process(variables);

        Mockito.verify(listener).updatedResult(false);
    }

    public void testCorrectDoubleConditionOr() {
        DoubleConditionFunction function = new DoubleConditionFunction("or");
        function.setListener(listener);

        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(true, Boolean.class));
        variables.add(new ConstantData(false, Boolean.class));

        function.process(variables);

        Mockito.verify(listener).updatedResult(true);
    }

    public void testWringDoubleConditionOr() {
        DoubleConditionFunction function = new DoubleConditionFunction("or");
        function.setListener(listener);

        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(false, Boolean.class));
        variables.add(new ConstantData(false, Boolean.class));

        function.process(variables);

        Mockito.verify(listener).updatedResult(false);
    }

    public void testCorrectDoubleConditionBetween() {
        BetweenFunction function = new BetweenFunction();
        function.setListener(listener);

        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(20, Integer.class));
        variables.add(new ConstantData(0, Integer.class));
        variables.add(new ConstantData(100, Integer.class));

        function.process(variables);

        Mockito.verify(listener).updatedResult(true);
    }

    public void testWrongDoubleConditionBetween() {
        BetweenFunction function = new BetweenFunction();
        function.setListener(listener);

        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(200, Integer.class));
        variables.add(new ConstantData(0, Integer.class));
        variables.add(new ConstantData(100, Integer.class));

        function.process(variables);

        Mockito.verify(listener).updatedResult(false);
    }
}

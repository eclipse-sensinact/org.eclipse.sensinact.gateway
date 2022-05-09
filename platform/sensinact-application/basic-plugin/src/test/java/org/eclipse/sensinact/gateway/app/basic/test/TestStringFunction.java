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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.app.api.function.DataItf;
import org.eclipse.sensinact.gateway.app.api.function.FunctionUpdateListener;
import org.eclipse.sensinact.gateway.app.basic.string.ConcatenateFunction;
import org.eclipse.sensinact.gateway.app.basic.string.SubstringFunction;
import org.eclipse.sensinact.gateway.app.manager.component.data.ConstantData;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestStringFunction {
    
	private static final Logger LOG = LoggerFactory.getLogger(TestStringFunction.class);
    private Mediator mediator=Mockito.mock(Mediator.class);
    
    private FunctionUpdateListener listener=Mockito.mock(FunctionUpdateListener.class);

    @BeforeEach
    public void init() throws Exception {
        Mockito.when(LOG.isDebugEnabled()).thenReturn(false);
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
        SubstringFunction function = new SubstringFunction();
        function.setListener(listener);
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData("Something to substring", String.class));
        variables.add(new ConstantData(13, Integer.class));
        function.process(variables);
        Mockito.verify(listener).updatedResult("substring");
    }

    public void testSubstringStartEnd() {
        SubstringFunction function = new SubstringFunction();
        function.setListener(listener);
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData("Something to substring", String.class));
        variables.add(new ConstantData(0, Integer.class));
        variables.add(new ConstantData(9, Integer.class));
        function.process(variables);
        Mockito.verify(listener).updatedResult("Something");
    }
}

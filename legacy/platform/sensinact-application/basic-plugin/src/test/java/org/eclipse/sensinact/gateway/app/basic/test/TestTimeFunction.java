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
import org.eclipse.sensinact.gateway.app.basic.time.SleepFunction;
import org.eclipse.sensinact.gateway.app.manager.component.data.ConstantData;
import org.mockito.Mockito;

public class TestTimeFunction {
    
    
    private FunctionUpdateListener listener=Mockito.mock(FunctionUpdateListener.class);



    public void testSleep() {
        SleepFunction function = new SleepFunction();
        function.setListener(listener);
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(100, Integer.class));
        function.process(variables);
        Mockito.verify(listener).updatedResult(true);
    }
}

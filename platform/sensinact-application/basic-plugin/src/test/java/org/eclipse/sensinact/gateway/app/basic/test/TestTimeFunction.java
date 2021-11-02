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

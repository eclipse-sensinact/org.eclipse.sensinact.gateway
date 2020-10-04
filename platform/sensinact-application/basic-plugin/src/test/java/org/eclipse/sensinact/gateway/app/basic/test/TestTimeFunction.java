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
import org.eclipse.sensinact.gateway.app.basic.time.SleepFunction;
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
public class TestTimeFunction extends TestCase {
    @Mock
    private Mediator mediator;
    @Mock
    private FunctionUpdateListener listener;

    @Before
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    public void testSleep() {
        SleepFunction function = new SleepFunction(mediator);
        function.setListener(listener);
        List<DataItf> variables = new ArrayList<DataItf>();
        variables.add(new ConstantData(100, Integer.class));
        function.process(variables);
        Mockito.verify(listener).updatedResult(true);
    }
}

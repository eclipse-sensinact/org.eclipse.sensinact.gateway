/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation 
**********************************************************************/
package org.eclipse.sensinact.prototype.command.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.prototype.command.AbstractSensinactCommand;
import org.eclipse.sensinact.prototype.command.SensinactModel;
import org.eclipse.sensinact.prototype.emf.util.EMFTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.service.typedevent.TypedEventBus;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

@ExtendWith(MockitoExtension.class)
public class GatewayThreadImplTest {

    @Mock
    TypedEventBus typedEventBus;

    @Spy
    SensiNactPackage sensinactPackage = SensiNactPackage.eINSTANCE;
    @Spy
    ResourceSet resourceSet = EMFTestUtil.createResourceSet();;

    @InjectMocks
    GatewayThreadImpl thread = new GatewayThreadImpl();

    @BeforeEach
    void setup() {

        resourceSet = EMFTestUtil.createResourceSet();

        thread.activate();
    }

    @AfterEach
    void teardown() {
        thread.deactivate();
    }

    @Test
    void testExecute() throws Exception {
        final int delay = 100;
        final int testValue = 5;
        final int threadWaitTime = 200;
        Semaphore sem = new Semaphore(0);

        AbstractSensinactCommand<Integer> command = new AbstractSensinactCommand<Integer>() {

            @Override
            protected Promise<Integer> call(SensinactModel model, PromiseFactory promiseFactory) {
                try {
                    Thread.sleep(threadWaitTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return promiseFactory.resolved(testValue);
            }
        };

        Promise<Integer> result = thread.execute(command).onResolve(sem::release);

        assertFalse(result.isDone());
        assertTrue(sem.tryAcquire(threadWaitTime + delay, TimeUnit.MILLISECONDS));

        assertEquals(testValue, result.getValue());
    }
}

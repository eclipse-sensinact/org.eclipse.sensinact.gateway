/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.prototype.command.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.sensinact.model.core.SensiNactPackage;
import org.eclipse.sensinact.prototype.command.AbstractSensinactCommand;
import org.eclipse.sensinact.prototype.command.SensinactModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.service.typedevent.TypedEventBus;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

@ExtendWith(MockitoExtension.class)
public class GatewayThreadImplTest {

    @Mock
    TypedEventBus eventBus;
    @Mock
    SensiNactPackage sensinactPackage;
    @Mock
    ResourceSet resourceSet;
    @Mock
    Resource resource;

    GatewayThreadImpl thread;

    @BeforeEach
    void setup() {

        Mockito.when(resourceSet.createResource(Mockito.any(URI.class)))
                .thenAnswer(i -> new ResourceImpl((URI) i.getArgument(0)));

        thread = new GatewayThreadImpl();
        thread.typedEventBus = eventBus;
        thread.sensinactPackage = sensinactPackage;
        thread.resourceSet = resourceSet;
        thread.activate();
    }

    @AfterEach
    void teardown() {
        thread.deactivate();
    }

    @Test
    void testExecute() throws Exception {
        Semaphore sem = new Semaphore(0);
        AbstractSensinactCommand<Integer> command = new AbstractSensinactCommand<Integer>() {

            @Override
            protected Promise<Integer> call(SensinactModel model, PromiseFactory promiseFactory) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return promiseFactory.resolved(5);
            }
        };

        Promise<Integer> result = thread.execute(command).onResolve(sem::release);

        assertFalse(result.isDone());
        assertTrue(sem.tryAcquire(200, TimeUnit.MILLISECONDS));

        assertEquals(5, result.getValue());
    }

}

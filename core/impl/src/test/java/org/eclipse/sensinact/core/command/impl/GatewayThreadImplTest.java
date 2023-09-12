/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.core.command.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.metrics.IMetricCounter;
import org.eclipse.sensinact.core.metrics.IMetricTimer;
import org.eclipse.sensinact.core.metrics.IMetricsHistogram;
import org.eclipse.sensinact.core.metrics.IMetricsManager;
import org.eclipse.sensinact.core.model.Model;
import org.eclipse.sensinact.core.model.Resource;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.model.Service;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.core.emf.util.EMFTestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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
    ProviderPackage providerPackage = ProviderPackage.eINSTANCE;

    @Spy
    ResourceSet resourceSet = EMFTestUtil.createResourceSet();

    GatewayThreadImpl thread = null;

    @BeforeEach
    void setup() throws IOException {

        Path path = Paths.get("data", "instances");
        if (Files.isDirectory(path)) {
            Files.walk(path, 1).filter(Files::isRegularFile).forEach(t -> {
                try {
                    Files.delete(t);
                } catch (IOException e) {
                }
            });
        }
        resourceSet = EMFTestUtil.createResourceSet();

        IMetricsManager metrics = mock(IMetricsManager.class);
        IMetricCounter counter = mock(IMetricCounter.class);
        IMetricsHistogram histogram = mock(IMetricsHistogram.class);
        IMetricTimer timer = mock(IMetricTimer.class);
        lenient().when(metrics.getCounter(anyString())).thenReturn(counter);
        lenient().when(metrics.getHistogram(anyString())).thenReturn(histogram);
        lenient().when(metrics.withTimer(anyString())).thenReturn(timer);
        lenient().when(metrics.withTimers(any())).thenReturn(timer);

        thread = new GatewayThreadImpl(metrics, typedEventBus, resourceSet, providerPackage);
    }

    @AfterEach
    void teardown() throws IOException {
        thread.deactivate();
        Path path = Paths.get("data", "instances");
        if (Files.isDirectory(path)) {
            Files.walk(path, 1).filter(Files::isRegularFile).forEach(t -> {
                try {
                    Files.delete(t);
                } catch (IOException e) {
                }
            });
        }
    }

    @Test
    void testExecute() throws Exception {
        final int delay = 100;
        final int testValue = 5;
        final int threadWaitTime = 200;
        Semaphore sem = new Semaphore(0);

        AbstractSensinactCommand<Integer> command = new AbstractTwinCommand<Integer>() {

            @Override
            protected Promise<Integer> call(SensinactDigitalTwin model, PromiseFactory promiseFactory) {
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

    @Nested
    class LifecycleTests {
        @Nested
        class TwinLifecycleTests {

            @Test
            void testSensinactProviderClosed() throws Exception {

                SensinactProvider sp = thread.execute(new AbstractSensinactCommand<SensinactProvider>() {

                    @Override
                    protected Promise<SensinactProvider> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                            PromiseFactory promiseFactory) {
                        modelMgr.createModel("providerModel").withService("bar").withResource("foobar")
                                .withType(Integer.class).withInitialValue(42).build().build().build();
                        twin.createProvider("providerModel", "providerFoo");
                        return promiseFactory.resolved(twin.getProvider("providerFoo"));
                    }
                }).getValue();

                assertFalse(sp.isValid());
            }

            @Test
            void testSensinactServiceClosed() throws Exception {

                SensinactService ss = thread.execute(new AbstractSensinactCommand<SensinactService>() {

                    @Override
                    protected Promise<SensinactService> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                            PromiseFactory promiseFactory) {
                        modelMgr.createModel("serviceModel").withService("bar").withResource("foobar")
                                .withType(Integer.class).withInitialValue(42).build().build().build();
                        twin.createProvider("serviceModel", "serviceFoo");
                        return promiseFactory.resolved(twin.getService("serviceFoo", "bar"));
                    }
                }).getValue();

                assertFalse(ss.isValid());
            }

            @Test
            void testSensinactResourceClosed() throws Exception {

                SensinactResource sr = thread.execute(new AbstractSensinactCommand<SensinactResource>() {

                    @Override
                    protected Promise<SensinactResource> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                            PromiseFactory promiseFactory) {
                        modelMgr.createModel("resourceModel").withService("bar").withResource("foobar")
                                .withType(Integer.class).withInitialValue(42).build().build().build();
                        twin.createProvider("resourceModel", "resourceFoo");
                        return promiseFactory.resolved(twin.getResource("resourceFoo", "bar", "foobar"));
                    }
                }).getValue();

                assertFalse(sr.isValid());
            }
        }

        @Nested
        class ModelLifecycleTests {

            @Test
            void testModelClosed() throws Exception {

                Model m = thread.execute(new AbstractSensinactCommand<Model>() {

                    @Override
                    protected Promise<Model> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                            PromiseFactory promiseFactory) {
                        Model mo = modelMgr.createModel("providerModel").withService("bar").withResource("foobar")
                                .withType(Integer.class).withInitialValue(42).build().build().build();
                        return promiseFactory.resolved(mo);
                    }
                }).getValue();

                assertFalse(m.isValid());
            }

            @Test
            void testServiceClosed() throws Exception {

                Service s = thread.execute(new AbstractSensinactCommand<Service>() {

                    @Override
                    protected Promise<Service> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                            PromiseFactory promiseFactory) {
                        modelMgr.createModel("serviceModel").withService("bar").withResource("foobar")
                                .withType(Integer.class).withInitialValue(42).build().build().build();
                        return promiseFactory.resolved(modelMgr.getModel("serviceModel").getServices().get("bar"));
                    }
                }).getValue();

                assertFalse(s.isValid());
            }

            @Test
            void testResourceClosed() throws Exception {

                Resource r = thread.execute(new AbstractSensinactCommand<Resource>() {

                    @Override
                    protected Promise<Resource> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                            PromiseFactory promiseFactory) {
                        modelMgr.createModel("resourceModel").withService("bar").withResource("foobar")
                                .withType(Integer.class).withInitialValue(42).build().build().build();
                        return promiseFactory.resolved(modelMgr.getModel("resourceModel").getServices().get("bar")
                                .getResources().get("foobar"));
                    }
                }).getValue();

                assertFalse(r.isValid());
            }
        }
    }
}

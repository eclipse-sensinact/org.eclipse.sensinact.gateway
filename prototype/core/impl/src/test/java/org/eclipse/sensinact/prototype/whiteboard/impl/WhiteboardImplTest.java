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
package org.eclipse.sensinact.prototype.whiteboard.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.prototype.annotation.verb.ACT;
import org.eclipse.sensinact.prototype.annotation.verb.ActParam;
import org.eclipse.sensinact.prototype.annotation.verb.UriParam;
import org.eclipse.sensinact.prototype.annotation.verb.UriParam.UriSegment;
import org.eclipse.sensinact.prototype.command.AbstractSensinactCommand;
import org.eclipse.sensinact.prototype.command.GatewayThread;
import org.eclipse.sensinact.prototype.emf.util.EMFTestUtil;
import org.eclipse.sensinact.prototype.model.SensinactModelManager;
import org.eclipse.sensinact.prototype.model.impl.SensinactModelManagerImpl;
import org.eclipse.sensinact.prototype.model.nexus.ModelNexus;
import org.eclipse.sensinact.prototype.notification.NotificationAccumulator;
import org.eclipse.sensinact.prototype.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.prototype.twin.SensinactProvider;
import org.eclipse.sensinact.prototype.twin.SensinactResource;
import org.eclipse.sensinact.prototype.twin.SensinactService;
import org.eclipse.sensinact.prototype.twin.impl.SensinactDigitalTwinImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

@ExtendWith(MockitoExtension.class)
public class WhiteboardImplTest {

    private static final String PROVIDER_A = "providerA";

    private static final String PROVIDER_B = "providerB";

    @Mock
    GatewayThread thread;

    @Mock
    NotificationAccumulator accumulator;

    private PromiseFactory promiseFactory = new PromiseFactory(PromiseFactory.inlineExecutor());

    private ResourceSet resourceSet;

    private ModelNexus nexus;

    private SensinactModelManagerImpl manager;

    private SensinactDigitalTwinImpl twinImpl;

    private SensinactWhiteboard whiteboard;

    @BeforeEach
    void start() throws NoSuchMethodException, SecurityException {
        resourceSet = EMFTestUtil.createResourceSet();
        whiteboard = new SensinactWhiteboard(thread);
        nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator, whiteboard::act);
        manager = new SensinactModelManagerImpl(nexus);
        twinImpl = new SensinactDigitalTwinImpl(nexus, promiseFactory);

        Mockito.when(thread.getPromiseFactory()).thenReturn(promiseFactory);

        Method m = AbstractSensinactCommand.class.getDeclaredMethod("call", SensinactDigitalTwin.class,
                SensinactModelManager.class, PromiseFactory.class);
        m.setAccessible(true);
        Mockito.when(thread.execute(Mockito.any()))
                .then(i -> m.invoke(i.getArgument(0), twinImpl, manager, promiseFactory));

    }

    // Use argX naming to match defaults if compiled without parameter names
    static abstract class BaseActionTest {
        @ACT(model = "foo", service = "actions", resource = "1")
        public byte noArgs() {
            return 1;
        }

        @ACT(model = "foo", service = "actions", resource = "2")
        public Short stringArg(String arg0) {
            return (short) (2 * Short.valueOf(arg0));
        }

        // Invert numbering to show we use the parameter names from the annotations
        @ACT(model = "foo", service = "actions", resource = "3")
        public Integer namedArgs(@ActParam(name = "arg1") Integer arg0, @ActParam(name = "arg0") Instant arg1) {
            return (arg1.isAfter(Instant.now()) ? 3 : 5) * arg0;
        }

        @ACT(model = "foo", service = "actions", resource = "4")
        public Double uriArgs(Byte arg0, @UriParam(UriSegment.PROVIDER) String arg1, Integer arg2) {
            switch (arg1) {
            case PROVIDER_A:
                return 4D * arg0.doubleValue() * arg2.doubleValue();
            case PROVIDER_B:
                return 4.4d * arg0.doubleValue() * arg2.doubleValue();
            default:
                return -4D;
            }
        }
    }

    static class ExtendedActionTest extends BaseActionTest {
        @ACT(model = "foo", service = "multi-actions", resource = "a")
        @ACT(model = "foo", service = "multi-actions", resource = "b")
        @ACT(model = "foo", service = "multi-actions", resource = "c")
        public String doMultiAction(@UriParam(UriSegment.RESOURCE) String resource) {
            switch (resource) {
            case "a":
            case "b":
            case "c":
                return resource;
            default:
                return "x";
            }
        }
    }

    @Nested
    class ActionTests {

        @Test
        void testAddActionResource() throws Exception {
            ExtendedActionTest actionProvider = new ExtendedActionTest();
            whiteboard.addWhiteboardService(actionProvider,
                    Map.of("service.id", 42L, "sensiNact.whiteboard.resource", true));

            SensinactProvider providerA = twinImpl.createProvider("foo", PROVIDER_A);
            twinImpl.createProvider("foo", PROVIDER_B);

            // Test the actions from the supertype

            SensinactService service = providerA.getServices().get("actions");
            assertNotNull(service);

            SensinactResource r = service.getResources().get("1");

            assertEquals(List.of(), r.getArguments());
            Promise<Object> result = r.act(Map.of());
            assertEquals(Byte.valueOf("1"), result.getValue());

            r = service.getResources().get("2");

            assertEquals(List.of(new SimpleEntry<>("arg0", String.class)), r.getArguments());
            result = r.act(Map.of("arg0", 16));
            assertEquals(Short.valueOf("32"), result.getValue());

            r = service.getResources().get("3");

            assertEquals(List.of(new SimpleEntry<>("arg1", Integer.class), new SimpleEntry<>("arg0", Instant.class)),
                    r.getArguments());
            result = r.act(Map.of("arg1", "5", "arg0", Instant.now().plusSeconds(60)));
            assertEquals(Integer.valueOf("15"), result.getValue());
            result = r.act(Map.of("arg1", "5", "arg0", Instant.now().minusSeconds(60)));
            assertEquals(Integer.valueOf("25"), result.getValue());

            r = service.getResources().get("4");

            assertEquals(List.of(new SimpleEntry<>("arg0", Byte.class), new SimpleEntry<>("arg2", Integer.class)),
                    r.getArguments());
            result = r.act(Map.of("arg0", 7, "arg2", 2));
            assertEquals(Double.valueOf("56"), result.getValue());

            r = twinImpl.getResource(PROVIDER_B, "actions", "4");

            assertEquals(List.of(new SimpleEntry<>("arg0", Byte.class), new SimpleEntry<>("arg2", Integer.class)),
                    r.getArguments());
            result = r.act(Map.of("arg0", 7, "arg2", 2));
            assertEquals(Double.valueOf("61.6").doubleValue(), ((Double) result.getValue()).doubleValue(), 0.0000001d);

            // Test the actions from the repeated annotations

            service = providerA.getServices().get("multi-actions");

            r = service.getResources().get("a");

            assertEquals(List.of(), r.getArguments());
            result = r.act(Map.of());
            assertEquals("a", result.getValue());

            r = service.getResources().get("b");

            assertEquals(List.of(), r.getArguments());
            result = r.act(Map.of());
            assertEquals("b", result.getValue());

            r = service.getResources().get("c");

            assertEquals(List.of(), r.getArguments());
            result = r.act(Map.of());
            assertEquals("c", result.getValue());

        }
    }
}

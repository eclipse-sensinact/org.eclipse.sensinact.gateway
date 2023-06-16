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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sensinact.core.annotation.verb.ACT;
import org.eclipse.sensinact.core.annotation.verb.ActParam;
import org.eclipse.sensinact.core.annotation.verb.GET;
import org.eclipse.sensinact.core.annotation.verb.GetParam;
import org.eclipse.sensinact.core.annotation.verb.GetParam.GetSegment;
import org.eclipse.sensinact.core.annotation.verb.SET;
import org.eclipse.sensinact.core.annotation.verb.SetParam;
import org.eclipse.sensinact.core.annotation.verb.SetParam.SetSegment;
import org.eclipse.sensinact.core.annotation.verb.UriParam;
import org.eclipse.sensinact.core.annotation.verb.UriParam.UriSegment;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.command.GetLevel;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.notification.NotificationAccumulator;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.prototype.emf.util.EMFTestUtil;
import org.eclipse.sensinact.prototype.model.impl.SensinactModelManagerImpl;
import org.eclipse.sensinact.prototype.model.nexus.ModelNexus;
import org.eclipse.sensinact.prototype.twin.impl.SensinactDigitalTwinImpl;
import org.eclipse.sensinact.prototype.twin.impl.TimedValueImpl;
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
        nexus = new ModelNexus(resourceSet, ProviderPackage.eINSTANCE, () -> accumulator, whiteboard);
        manager = new SensinactModelManagerImpl(nexus);
        twinImpl = new SensinactDigitalTwinImpl(nexus, promiseFactory);

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

    static class BasePullResourceTest {
        @GET(model = "bar", service = "pull", resource = "a")
        @GET(model = "bar", service = "pull", resource = "b")
        public String doMultiResource(@UriParam(UriSegment.RESOURCE) String resource,
                @GetParam(GetSegment.RESULT_TYPE) Class<?> type,
                @GetParam(GetSegment.CACHED_VALUE) TimedValue<?> cached) {
            switch (resource) {
            case "a":
            case "b":
                if (cached.getValue() != null) {
                    return "resource:" + cached.getValue();
                } else {
                    return resource;
                }
            default:
                return "x";
            }
        }
    }

    static class CachedPullResourceTest {
        @GET(model = "bar", service = "pull", resource = "cache", cacheDuration = 1, cacheDurationUnit = ChronoUnit.SECONDS)
        @GET(model = "bar", service = "pull", resource = "forced-cache", cacheDuration = 1, cacheDurationUnit = ChronoUnit.SECONDS)
        public Integer doCachedResource(@UriParam(UriSegment.RESOURCE) String resource,
                @GetParam(GetSegment.RESULT_TYPE) Class<?> type,
                @GetParam(GetSegment.CACHED_VALUE) TimedValue<?> cached) {
            if (cached.getValue() == null) {
                return 1;
            }

            return ((Integer) cached.getValue()).intValue() * 2;
        }
    }

    @Nested
    class PullBasedResourceTest {

        @Test
        void testBasicPullResource() throws Exception {
            BasePullResourceTest resourceProvider = new BasePullResourceTest();
            whiteboard.addWhiteboardService(resourceProvider,
                    Map.of("service.id", 256L, "sensiNact.whiteboard.resource", true));

            SensinactProvider providerA = twinImpl.createProvider("bar", PROVIDER_A);
            twinImpl.createProvider("bar", PROVIDER_B);

            SensinactService service = providerA.getServices().get("pull");
            assertNotNull(service);

            for (String rc : List.of("a", "b")) {
                SensinactResource r = service.getResources().get(rc);
                assertThrows(IllegalArgumentException.class, () -> r.getArguments());

                // No value at first
                TimedValue<String> result = r.getValue(String.class, GetLevel.STRONG).getValue();
                assertEquals(rc, result.getValue());
                assertNotNull(result.getTimestamp(), "No timestamp returned");
                final Instant initialTimestamp = result.getTimestamp();

                // Wait a bit
                Thread.sleep(200);

                result = r.getValue(String.class, GetLevel.STRONG).getValue();
                assertEquals("resource:" + rc, result.getValue());
                assertNotNull(result.getTimestamp(), "No timestamp returned");
                final Instant secondTimestamp = result.getTimestamp();
                assertTrue(secondTimestamp.isAfter(initialTimestamp),
                        secondTimestamp + " should be after " + initialTimestamp);
            }
        }

        @Test
        void testCachedPullResource() throws Exception {
            CachedPullResourceTest resourceProvider = new CachedPullResourceTest();
            whiteboard.addWhiteboardService(resourceProvider,
                    Map.of("service.id", 256L, "sensiNact.whiteboard.resource", true));

            SensinactProvider providerA = twinImpl.createProvider("bar", PROVIDER_A);
            twinImpl.createProvider("bar", PROVIDER_B);

            SensinactService service = providerA.getServices().get("pull");
            assertNotNull(service);

            SensinactResource r = service.getResources().get("cache");
            assertThrows(IllegalArgumentException.class, () -> r.getArguments());

            // No value at first: should get a 1
            TimedValue<Integer> result = r.getValue(Integer.class, GetLevel.NORMAL).getValue();
            assertEquals(1, result.getValue());
            assertNotNull(result.getTimestamp(), "No timestamp returned");
            final Instant initialTimestamp = result.getTimestamp();

            // Second cache call should have the same value and timestamp
            Thread.sleep(200);
            result = r.getValue(Integer.class, GetLevel.NORMAL).getValue();
            assertEquals(1, result.getValue());
            assertEquals(initialTimestamp, result.getTimestamp());

            // Hard call
            result = r.getValue(Integer.class, GetLevel.STRONG).getValue();
            assertEquals(2, result.getValue());
            final Instant secondTimestamp = result.getTimestamp();
            assertTrue(initialTimestamp.isBefore(secondTimestamp), "Timestamp wasn't updated");

            // Weak call
            result = r.getValue(Integer.class, GetLevel.WEAK).getValue();
            assertEquals(2, result.getValue());
            assertEquals(secondTimestamp, result.getTimestamp());

            // Cached call
            result = r.getValue(Integer.class, GetLevel.NORMAL).getValue();
            assertEquals(2, result.getValue());
            assertEquals(secondTimestamp, result.getTimestamp());

            // Wait more than the cache period
            Thread.sleep(1200);

            // Weak call must return the same value
            result = r.getValue(Integer.class, GetLevel.WEAK).getValue();
            assertEquals(2, result.getValue());
            assertEquals(secondTimestamp, result.getTimestamp());

            // Cached call must recall the value
            result = r.getValue(Integer.class, GetLevel.NORMAL).getValue();
            assertEquals(4, result.getValue());
            assertTrue(secondTimestamp.isBefore(result.getTimestamp()), "Timestamp wasn't updated");
        }

        @Test
        void testForcedInitialValue() throws Exception {
            CachedPullResourceTest resourceProvider = new CachedPullResourceTest();
            whiteboard.addWhiteboardService(resourceProvider,
                    Map.of("service.id", 256L, "sensiNact.whiteboard.resource", true));

            SensinactProvider providerA = twinImpl.createProvider("bar", PROVIDER_A);
            SensinactService service = providerA.getServices().get("pull");
            assertNotNull(service);

            SensinactResource r = service.getResources().get("forced-cache");
            assertThrows(IllegalArgumentException.class, () -> r.getArguments());

            // Force the value
            final Instant initialTimesamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);
            r.setValue(42, initialTimesamp);

            // Check the WEAK call behavior
            TimedValue<Integer> result = r.getValue(Integer.class, GetLevel.WEAK).getValue();
            assertEquals(42, result.getValue());
            assertNotNull(result.getTimestamp(), "No timestamp returned");
            assertEquals(initialTimesamp, result.getTimestamp());

            // Check the CACHED call behavior
            result = r.getValue(Integer.class, GetLevel.NORMAL).getValue();
            assertEquals(42, result.getValue());
            assertNotNull(result.getTimestamp(), "No timestamp returned");
            assertEquals(initialTimesamp, result.getTimestamp());

            Thread.sleep(110);

            // Check the HARD call
            result = r.getValue(Integer.class, GetLevel.STRONG).getValue();
            assertEquals(84, result.getValue());
            assertNotNull(result.getTimestamp(), "No timestamp returned");
            assertTrue(initialTimesamp.isBefore(result.getTimestamp()), "Timestamp not updated");
        }
    }

    static class BasePushResourceTest {
        @SET(model = "bar", service = "push", resource = "a", type = String.class)
        @SET(model = "bar", service = "push", resource = "b", type = String.class)
        public TimedValue<String> doMultiResource(@UriParam(UriSegment.RESOURCE) String resource,
                @SetParam(SetSegment.RESULT_TYPE) Class<?> type,
                @SetParam(SetSegment.CACHED_VALUE) TimedValue<String> cached,
                @SetParam(SetSegment.NEW_VALUE) TimedValue<String> newValue) {

            final String value;
            switch (resource) {
            case "a":
            case "b":
                if (cached.getValue() != null) {
                    value = "resource:" + cached.getValue();
                } else {
                    value = newValue.getValue();
                }
                break;
            default:
                value = "x";
                break;
            }

            return new TimedValueImpl<String>(value, newValue.getTimestamp());
        }
    }

    @Nested
    class PushBasedResourceTest {

        @Test
        void testPush() throws Exception {
            BasePushResourceTest resourceProvider = new BasePushResourceTest();
            whiteboard.addWhiteboardService(resourceProvider,
                    Map.of("service.id", 257L, "sensiNact.whiteboard.resource", true));

            SensinactProvider providerA = twinImpl.createProvider("bar", PROVIDER_A);
            twinImpl.createProvider("bar", PROVIDER_B);

            SensinactService service = providerA.getServices().get("push");
            assertNotNull(service);

            for (String rc : List.of("a", "b")) {
                SensinactResource r = service.getResources().get(rc);
                assertThrows(IllegalArgumentException.class, () -> r.getArguments());

                // No value at first
                TimedValue<String> result = r.getValue(String.class).getValue();
                assertNull(result.getValue());
                assertNull(result.getTimestamp());

                // Set the value
                final Instant setTimestamp = Instant.now().minus(Duration.ofMinutes(5));
                r.setValue("toto", setTimestamp).getValue();

                // Get the value
                result = r.getValue(String.class, GetLevel.STRONG).getValue();
                assertEquals("toto", result.getValue());
                assertNotNull(result.getTimestamp(), "No timestamp returned");
                assertEquals(setTimestamp, result.getTimestamp());

                // Second set: the computed value is the one that must be returned
                final Instant setTimestamp2 = Instant.now().minus(Duration.ofMinutes(1));
                r.setValue("titi", setTimestamp2).getValue();

                // Get the value
                result = r.getValue(String.class, GetLevel.STRONG).getValue();
                assertEquals("resource:toto", result.getValue());
                assertNotNull(result.getTimestamp(), "No timestamp returned");
                assertEquals(setTimestamp2, result.getTimestamp());
            }
        }
    }

    static class Content {
        String oldValue;
        String newValue;

        @Override
        public String toString() {
            return "Content(old=" + oldValue + " ;; new=" + newValue + ")";
        }
    }

    static class PullPushResourceTest {

        @GET(model = "foobar", service = "svc", resource = "a", type = Content.class)
        @GET(model = "foobar", service = "svc", resource = "b", type = Content.class)
        public TimedValue<Content> doGet(@UriParam(UriSegment.RESOURCE) String resource,
                @GetParam(GetSegment.CACHED_VALUE) TimedValue<Content> cached) {
            Content content = new Content();
            final Content oldContent = cached.getValue();
            if (oldContent != null) {
                content.oldValue = oldContent.newValue;
                content.newValue = "+" + oldContent.newValue;
            } else {
                content.oldValue = null;
                content.newValue = resource;
            }
            return new TimedValueImpl<Content>(content,
                    cached.getTimestamp() == null ? Instant.now() : cached.getTimestamp());
        }

        @SET(model = "foobar", service = "svc", resource = "a", type = Content.class)
        @SET(model = "foobar", service = "svc", resource = "b", type = Content.class)
        public TimedValue<Content> doSet(@UriParam(UriSegment.RESOURCE) String resource,
                @SetParam(SetSegment.CACHED_VALUE) TimedValue<Content> cached,
                @SetParam(SetSegment.NEW_VALUE) TimedValue<String> newValue) {
            Content content = new Content();
            content.oldValue = cached.getValue() != null ? cached.getValue().newValue : null;
            content.newValue = newValue.getValue();
            return new TimedValueImpl<Content>(content, newValue.getTimestamp());
        }
    }

    @Nested
    class PushPullBasedResourceTest {

        @Test
        void testPushPull() throws Exception {
            PullPushResourceTest resourceProvider = new PullPushResourceTest();
            whiteboard.addWhiteboardService(resourceProvider,
                    Map.of("service.id", 258L, "sensiNact.whiteboard.resource", true));

            SensinactProvider providerA = twinImpl.createProvider("foobar", PROVIDER_A);
            twinImpl.createProvider("foobar", PROVIDER_B);

            SensinactService service = providerA.getServices().get("svc");
            assertNotNull(service);

            for (String rc : List.of("a", "b")) {
                SensinactResource r = service.getResources().get(rc);
                assertThrows(IllegalArgumentException.class, () -> r.getArguments());

                // Initial value from the getter
                TimedValue<Content> result = r.getValue(Content.class).getValue();
                assertNotNull(result.getValue(), "No value");
                assertNotNull(result.getTimestamp(), "No timestamp");
                assertNull(result.getValue().oldValue);
                assertEquals(rc, result.getValue().newValue);

                // Set the value
                final Instant setTimestamp = Instant.now().plus(Duration.ofMinutes(5));
                r.setValue("toto", setTimestamp).getValue();

                // Get the value
                result = r.getValue(Content.class, GetLevel.STRONG).getValue();
                assertNotNull(result.getValue(), "No value");
                assertEquals(setTimestamp, result.getTimestamp());
                assertEquals("toto", result.getValue().oldValue);
                assertEquals("+toto", result.getValue().newValue);

                // Second set: the computed value is the one that must be returned
                final Instant setTimestamp2 = Instant.now().plus(Duration.ofMinutes(10));
                r.setValue("titi", setTimestamp2).getValue();

                // Get the value
                result = r.getValue(Content.class, GetLevel.STRONG).getValue();
                assertEquals(setTimestamp2, result.getTimestamp());
                assertEquals("titi", result.getValue().oldValue);
                assertEquals("+titi", result.getValue().newValue);
            }
        }
    }
}

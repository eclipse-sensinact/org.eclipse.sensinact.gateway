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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.sensinact.core.annotation.dto.NullAction;
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
import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GetLevel;
import org.eclipse.sensinact.core.command.ResourceCommand;
import org.eclipse.sensinact.core.metrics.IMetricCounter;
import org.eclipse.sensinact.core.metrics.IMetricTimer;
import org.eclipse.sensinact.core.metrics.IMetricsHistogram;
import org.eclipse.sensinact.core.metrics.IMetricsManager;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.core.emf.util.EMFTestUtil;
import org.eclipse.sensinact.core.twin.impl.TimedValueImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.service.typedevent.TypedEventBus;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

@ExtendWith(MockitoExtension.class)
public class WhiteboardImplTest {

    private static final String PROVIDER_A = "providerA";

    private static final String PROVIDER_B = "providerB";

    GatewayThreadImpl thread;

    @Mock
    TypedEventBus typedEventBus;

    private ResourceSet resourceSet;

    @BeforeEach
    void start() throws NoSuchMethodException, SecurityException {
        resourceSet = EMFTestUtil.createResourceSet();

        IMetricsManager metrics = mock(IMetricsManager.class);
        IMetricCounter counter = mock(IMetricCounter.class);
        IMetricsHistogram histogram = mock(IMetricsHistogram.class);
        IMetricTimer timer = mock(IMetricTimer.class);
        lenient().when(metrics.getCounter(anyString())).thenReturn(counter);
        lenient().when(metrics.getHistogram(anyString())).thenReturn(histogram);
        lenient().when(metrics.withTimer(anyString())).thenReturn(timer);
        lenient().when(metrics.withTimers(any(String[].class))).thenReturn(timer);

        thread = new GatewayThreadImpl(metrics, typedEventBus, resourceSet, ProviderPackage.eINSTANCE);
    }

    void createProviders(final String modelName, final String serviceName) throws Throwable {
        thread.execute(new AbstractTwinCommand<Void>() {
            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                SensinactProvider providerA = twin.createProvider(modelName, PROVIDER_A);
                twin.createProvider(modelName, PROVIDER_B);
                SensinactService service = providerA.getServices().get(serviceName);
                assertNotNull(service);
                return pf.resolved(null);
            }
        }).getValue();
    }

    void runRcCommand(String provider, String service, String resource, Function<SensinactResource, Void> call)
            throws Throwable {
        try {
            thread.execute(new ResourceCommand<Void>(provider, service, resource) {
                @Override
                protected Promise<Void> call(SensinactResource resource, PromiseFactory pf) {
                    call.apply(resource);
                    return pf.resolved(null);
                }
            }).getValue();
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    <T> TimedValue<T> getValue(String provider, String service, String resource, Class<T> type) throws Throwable {
        try {
            return thread.execute(new ResourceCommand<TimedValue<T>>(provider, service, resource) {
                @Override
                protected Promise<TimedValue<T>> call(SensinactResource resource, PromiseFactory pf) {
                    return resource.getValue(type);
                }
            }).getValue();
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    <T> TimedValue<T> getValue(String provider, String service, String resource, Class<T> type, GetLevel getLevel)
            throws Throwable {
        try {
            return thread.execute(new ResourceCommand<TimedValue<T>>(provider, service, resource) {
                @Override
                protected Promise<TimedValue<T>> call(SensinactResource resource, PromiseFactory pf) {
                    return resource.getValue(type, getLevel);
                }
            }).getValue();
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    Object setValue(String provider, String svc, String rc, Function<SensinactResource, Promise<?>> setter)
            throws Throwable {
        try {
            return thread.execute(new ResourceCommand<Object>(provider, svc, rc) {
                @Override
                protected Promise<Object> call(SensinactResource resource, PromiseFactory pf) {
                    return pf.resolvedWith(setter.apply(resource));
                }
            }).getValue();
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    Object act(String provider, String service, String resource, Map<String, Object> params) throws Throwable {
        try {
            return thread.execute(new ResourceCommand<Object>(provider, service, resource) {
                @Override
                protected Promise<Object> call(SensinactResource resource, PromiseFactory pf) {
                    return resource.act(params);
                }
            }).getValue();
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    // Use argX naming to match defaults if compiled without parameter names
    public static abstract class BaseActionTest {
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
        public Integer namedArgs(@ActParam("arg1") Integer arg0, @ActParam("arg0") Instant arg1) {
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

    public static class ExtendedActionTest extends BaseActionTest {
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
        void testAddActionResource() throws Throwable {
            ExtendedActionTest actionProvider = new ExtendedActionTest();
            thread.addWhiteboardService(actionProvider,
                    Map.of("service.id", 42L, "sensiNact.whiteboard.resource", true));

            String svc = "actions";
            createProviders("foo", svc);

            // RC 1
            String rc = "1";
            runRcCommand(PROVIDER_A, svc, rc, (r) -> {
                assertEquals(List.of(), r.getArguments());
                return null;
            });

            Object result = act(PROVIDER_A, svc, rc, Map.of());
            assertEquals(Byte.valueOf("1"), result);

            // RC 2
            rc = "2";
            runRcCommand(PROVIDER_A, svc, rc, (r) -> {
                assertEquals(List.of(new SimpleEntry<>("arg0", String.class)), r.getArguments());
                return null;
            });

            result = act(PROVIDER_A, svc, rc, Map.of("arg0", 16));
            assertEquals(Short.valueOf("32"), result);

            // RC 3
            rc = "3";
            runRcCommand(PROVIDER_A, svc, rc, (r) -> {
                assertEquals(
                        List.of(new SimpleEntry<>("arg1", Integer.class), new SimpleEntry<>("arg0", Instant.class)),
                        r.getArguments());
                return null;
            });

            result = act(PROVIDER_A, svc, rc, Map.of("arg1", "5", "arg0", Instant.now().plusSeconds(60)));
            assertEquals(Integer.valueOf("15"), result);

            result = act(PROVIDER_A, svc, rc, Map.of("arg1", "5", "arg0", Instant.now().minusSeconds(60)));
            assertEquals(Integer.valueOf("25"), result);

            // RC 4
            rc = "4";
            runRcCommand(PROVIDER_A, svc, rc, (r) -> {
                assertEquals(List.of(new SimpleEntry<>("arg0", Byte.class), new SimpleEntry<>("arg2", Integer.class)),
                        r.getArguments());
                return null;
            });

            result = act(PROVIDER_A, svc, rc, Map.of("arg0", 7, "arg2", 2));
            assertEquals(Double.valueOf("56"), result);

            runRcCommand(PROVIDER_B, svc, rc, (r) -> {
                assertEquals(List.of(new SimpleEntry<>("arg0", Byte.class), new SimpleEntry<>("arg2", Integer.class)),
                        r.getArguments());
                return null;
            });

            result = act(PROVIDER_B, svc, rc, Map.of("arg0", 7, "arg2", 2));
            assertEquals(Double.valueOf("61.6").doubleValue(), ((Double) result).doubleValue(), 0.0000001d);

            // Test the actions from the repeated annotations
            svc = "multi-actions";
            rc = "a";
            runRcCommand(PROVIDER_A, svc, rc, (r) -> {
                assertEquals(List.of(), r.getArguments());
                return null;
            });

            result = act(PROVIDER_A, svc, rc, Map.of());
            assertEquals("a", result);

            rc = "b";
            runRcCommand(PROVIDER_A, svc, rc, (r) -> {
                assertEquals(List.of(), r.getArguments());
                return null;
            });
            result = act(PROVIDER_A, svc, rc, Map.of());
            assertEquals("b", result);

            rc = "c";
            runRcCommand(PROVIDER_A, svc, rc, (r) -> {
                assertEquals(List.of(), r.getArguments());
                return null;
            });
            result = act(PROVIDER_A, svc, rc, Map.of());
            assertEquals("c", result);
        }
    }

    public static class BasePullResourceTest {

        public boolean bReturnNull = true;

        @GET(model = "bar", service = "pull", resource = "a")
        @GET(model = "bar", service = "pull", resource = "b", onNull = NullAction.UPDATE_IF_PRESENT)
        public String doMultiResource(@UriParam(UriSegment.RESOURCE) String resource,
                @GetParam(GetSegment.RESULT_TYPE) Class<?> type,
                @GetParam(GetSegment.CACHED_VALUE) TimedValue<?> cached) {
            switch (resource) {
            case "b":
                if(bReturnNull)
                    return null;
            case "a":
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

    public static class CachedPullResourceTest {
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
        void testBasicPullResource() throws Throwable {
            BasePullResourceTest resourceProvider = new BasePullResourceTest();
            thread.addWhiteboardService(resourceProvider,
                    Map.of("service.id", 256L, "sensiNact.whiteboard.resource", true));

            final String svc = "pull";
            createProviders("bar", svc);

            runRcCommand(PROVIDER_A, svc, "a", (r) -> {
                assertThrows(IllegalArgumentException.class, () -> r.getArguments());
                return null;
            });

            // No value at first
            TimedValue<String> result = getValue(PROVIDER_A, svc, "a", String.class, GetLevel.STRONG);
            assertEquals("a", result.getValue());
            assertNotNull(result.getTimestamp(), "No timestamp returned");
            Instant initialTimestamp = result.getTimestamp();

            // Wait a bit
            Thread.sleep(200);

            result = getValue(PROVIDER_A, svc, "a", String.class, GetLevel.STRONG);
            assertEquals("resource:" + "a", result.getValue());
            assertNotNull(result.getTimestamp(), "No timestamp returned");
            Instant secondTimestamp = result.getTimestamp();
            assertTrue(secondTimestamp.isAfter(initialTimestamp),
                    secondTimestamp + " should be after " + initialTimestamp);


            // Now try for resource b
            runRcCommand(PROVIDER_A, svc, "b", (r) -> {
                assertThrows(IllegalArgumentException.class, () -> r.getArguments());
                return null;
            });

            // No value at first
            result = getValue(PROVIDER_A, svc, "b", String.class, GetLevel.WEAK);
            assertNull(result.getValue());
            assertNull(result.getTimestamp());

            // Still no value
            result = getValue(PROVIDER_A, svc, "b", String.class, GetLevel.NORMAL);
            assertNull(result.getValue());
            assertNull(result.getTimestamp());

            resourceProvider.bReturnNull = false;
            result = getValue(PROVIDER_A, svc, "b", String.class, GetLevel.STRONG);
            assertEquals("b", result.getValue());
            assertNotNull(result.getTimestamp(), "No timestamp returned");
            initialTimestamp = result.getTimestamp();

            // Wait a bit
            Thread.sleep(200);
            resourceProvider.bReturnNull = true;

            result = getValue(PROVIDER_A, svc, "b", String.class, GetLevel.STRONG);
            assertEquals(null, result.getValue());
            assertNotNull(result.getTimestamp(), "No timestamp returned");
            secondTimestamp = result.getTimestamp();
            assertTrue(secondTimestamp.isAfter(initialTimestamp),
                    secondTimestamp + " should be after " + initialTimestamp);

        }

        @Test
        void testCachedPullResource() throws Throwable {
            CachedPullResourceTest resourceProvider = new CachedPullResourceTest();
            thread.addWhiteboardService(resourceProvider,
                    Map.of("service.id", 256L, "sensiNact.whiteboard.resource", true));

            final String svc = "pull";
            createProviders("bar", svc);

            final String rc = "cache";
            runRcCommand(PROVIDER_A, svc, rc, (r) -> {
                assertThrows(IllegalArgumentException.class, () -> r.getArguments());
                return null;
            });

            // No value at first: should get a 1
            TimedValue<Integer> result = getValue(PROVIDER_A, svc, rc, Integer.class, GetLevel.NORMAL);
            assertEquals(1, result.getValue());
            assertNotNull(result.getTimestamp(), "No timestamp returned");
            final Instant initialTimestamp = result.getTimestamp();

            // Second cache call should have the same value and timestamp
            Thread.sleep(200);
            result = getValue(PROVIDER_A, svc, rc, Integer.class, GetLevel.NORMAL);
            assertEquals(1, result.getValue());
            assertEquals(initialTimestamp, result.getTimestamp());

            // Strong call
            result = getValue(PROVIDER_A, svc, rc, Integer.class, GetLevel.STRONG);
            assertEquals(2, result.getValue());
            final Instant secondTimestamp = result.getTimestamp();
            assertTrue(initialTimestamp.isBefore(secondTimestamp), "Timestamp wasn't updated");

            // Weak call
            result = getValue(PROVIDER_A, svc, rc, Integer.class, GetLevel.WEAK);
            assertEquals(2, result.getValue());
            assertEquals(secondTimestamp, result.getTimestamp());

            // Cached call
            result = getValue(PROVIDER_A, svc, rc, Integer.class, GetLevel.NORMAL);
            assertEquals(2, result.getValue());
            assertEquals(secondTimestamp, result.getTimestamp());

            // Wait more than the cache period
            Thread.sleep(1200);

            // Weak call must return the same value
            result = getValue(PROVIDER_A, svc, rc, Integer.class, GetLevel.WEAK);
            assertEquals(2, result.getValue());
            assertEquals(secondTimestamp, result.getTimestamp());

            // Cached call must recall the value
            result = getValue(PROVIDER_A, svc, rc, Integer.class, GetLevel.NORMAL);
            assertEquals(4, result.getValue());
            assertTrue(secondTimestamp.isBefore(result.getTimestamp()), "Timestamp wasn't updated");
        }

        @Test
        void testForcedInitialValue() throws Throwable {
            CachedPullResourceTest resourceProvider = new CachedPullResourceTest();
            thread.addWhiteboardService(resourceProvider,
                    Map.of("service.id", 256L, "sensiNact.whiteboard.resource", true));

            final String svc = "pull";
            createProviders("bar", svc);

            final String rc = "forced-cache";
            runRcCommand(PROVIDER_A, svc, rc, (r) -> {
                assertThrows(IllegalArgumentException.class, () -> r.getArguments());
                return null;
            });

            // Force the value
            final Instant initialTimesamp = Instant.now().truncatedTo(ChronoUnit.MILLIS);
            setValue(PROVIDER_A, svc, rc, (r) -> r.setValue(42, initialTimesamp));

            // Check the WEAK call behavior
            TimedValue<Integer> result = getValue(PROVIDER_A, svc, rc, Integer.class, GetLevel.WEAK);
            assertEquals(42, result.getValue());
            assertNotNull(result.getTimestamp(), "No timestamp returned");
            assertEquals(initialTimesamp, result.getTimestamp());

            // Check the NORMAL call behavior
            result = getValue(PROVIDER_A, svc, rc, Integer.class, GetLevel.NORMAL);
            assertEquals(42, result.getValue());
            assertNotNull(result.getTimestamp(), "No timestamp returned");
            assertEquals(initialTimesamp, result.getTimestamp());

            Thread.sleep(110);

            // Check the STRONG call
            result = getValue(PROVIDER_A, svc, rc, Integer.class, GetLevel.STRONG);
            assertEquals(84, result.getValue());
            assertNotNull(result.getTimestamp(), "No timestamp returned");
            assertTrue(initialTimesamp.isBefore(result.getTimestamp()), "Timestamp not updated");
        }
    }

    public static class BasePushResourceTest {
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
        void testPush() throws Throwable {
            BasePushResourceTest resourceProvider = new BasePushResourceTest();
            thread.addWhiteboardService(resourceProvider,
                    Map.of("service.id", 257L, "sensiNact.whiteboard.resource", true));

            final String svc = "push";
            createProviders("bar", svc);

            for (String rc : List.of("a", "b")) {
                runRcCommand(PROVIDER_A, svc, rc, (r) -> {
                    assertThrows(IllegalArgumentException.class, () -> r.getArguments());
                    return null;
                });

                // No value at first
                TimedValue<String> result = getValue(PROVIDER_A, svc, rc, String.class);
                assertNull(result.getValue());
                assertNull(result.getTimestamp());

                // Set the value
                final Instant setTimestamp = Instant.now().minus(Duration.ofMinutes(5));
                setValue(PROVIDER_A, svc, rc, (r) -> r.setValue("toto", setTimestamp));

                // Get the value
                result = getValue(PROVIDER_A, svc, rc, String.class, GetLevel.STRONG);
                assertEquals("toto", result.getValue());
                assertNotNull(result.getTimestamp(), "No timestamp returned");
                assertEquals(setTimestamp, result.getTimestamp());

                // Second set: the computed value is the one that must be returned
                final Instant setTimestamp2 = Instant.now().minus(Duration.ofMinutes(1));
                setValue(PROVIDER_A, svc, rc, (r) -> r.setValue("titi", setTimestamp2));

                // Get the value
                result = getValue(PROVIDER_A, svc, rc, String.class, GetLevel.STRONG);
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

    public static class PullPushResourceTest {

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
        void testPushPull() throws Throwable {
            PullPushResourceTest resourceProvider = new PullPushResourceTest();
            thread.addWhiteboardService(resourceProvider,
                    Map.of("service.id", 258L, "sensiNact.whiteboard.resource", true));

            final String svc = "svc";
            createProviders("foobar", svc);

            for (String rc : List.of("a", "b")) {
                runRcCommand(PROVIDER_A, svc, rc, (r) -> {
                    assertThrows(IllegalArgumentException.class, () -> r.getArguments());
                    return null;
                });

                // Initial value from the getter
                TimedValue<Content> result = getValue(PROVIDER_A, svc, rc, Content.class);
                assertNotNull(result.getValue(), "No value");
                assertNotNull(result.getTimestamp(), "No timestamp");
                assertNull(result.getValue().oldValue);
                assertEquals(rc, result.getValue().newValue);

                // Set the value
                final Instant setTimestamp = Instant.now().plus(Duration.ofMinutes(5));
                setValue(PROVIDER_A, svc, rc, (r) -> r.setValue("toto", setTimestamp));

                // Get the value
                result = getValue(PROVIDER_A, svc, rc, Content.class, GetLevel.STRONG);
                assertNotNull(result.getValue(), "No value");
                assertEquals(setTimestamp, result.getTimestamp());
                assertEquals("toto", result.getValue().oldValue);
                assertEquals("+toto", result.getValue().newValue);

                // Second set: the computed value is the one that must be returned
                final Instant setTimestamp2 = Instant.now().plus(Duration.ofMinutes(10));
                setValue(PROVIDER_A, svc, rc, (r) -> r.setValue("titi", setTimestamp2));

                // Get the value
                result = getValue(PROVIDER_A, svc, rc, Content.class, GetLevel.STRONG);
                assertEquals(setTimestamp2, result.getTimestamp());
                assertEquals("titi", result.getValue().oldValue);
                assertEquals("+titi", result.getValue().newValue);
            }
        }
    }

    public static class TwoPullResourceTest {

        @GET(model = "fizz", service = "buzz", resource = "version")
        public String version() {
            return "1.0.0";
        }

        @GET(model = "fizz", service = "buzz", resource = "count")
        public Integer count() {
            return 42;
        }
    }

    @Nested
    class TwoPullBasedResourceTest {

        @Test
        void testPushPull() throws Throwable {
            TwoPullResourceTest resourceProvider = new TwoPullResourceTest();
            thread.addWhiteboardService(resourceProvider,
                    Map.of("service.id", 259L, "sensiNact.whiteboard.resource", true));

            final String svc = "buzz";
            createProviders("fizz", svc);

            runRcCommand(PROVIDER_A, svc, "version", (r) -> {
                assertThrows(IllegalArgumentException.class, () -> r.getArguments());
                return null;
            });
            runRcCommand(PROVIDER_A, svc, "count", (r) -> {
                assertThrows(IllegalArgumentException.class, () -> r.getArguments());
                return null;
            });

            // Initial values from the getter
            TimedValue<String> result = getValue(PROVIDER_A, svc, "version", String.class);
            assertNotNull(result.getValue(), "No value");
            assertNotNull(result.getTimestamp(), "No timestamp");
            assertEquals("1.0.0", result.getValue());

            TimedValue<Integer> result2 = getValue(PROVIDER_A, svc, "count", Integer.class);
            assertNotNull(result2.getValue(), "No value");
            assertNotNull(result2.getTimestamp(), "No timestamp");
            assertEquals(42, result2.getValue());
        }
    }
}

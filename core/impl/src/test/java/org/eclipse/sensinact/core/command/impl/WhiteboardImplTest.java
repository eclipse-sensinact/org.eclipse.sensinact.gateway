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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
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
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GetLevel;
import org.eclipse.sensinact.core.command.ResourceCommand;
import org.eclipse.sensinact.core.emf.util.EMFTestUtil;
import org.eclipse.sensinact.core.metrics.IMetricCounter;
import org.eclipse.sensinact.core.metrics.IMetricTimer;
import org.eclipse.sensinact.core.metrics.IMetricsHistogram;
import org.eclipse.sensinact.core.metrics.IMetricsManager;
import org.eclipse.sensinact.core.model.Model;
import org.eclipse.sensinact.core.model.Resource;
import org.eclipse.sensinact.core.model.ResourceBuilder;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.model.Service;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.SensinactService;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.core.twin.impl.TimedValueImpl;
import org.eclipse.sensinact.core.whiteboard.AbstractDescriptiveAct;
import org.eclipse.sensinact.core.whiteboard.AbstractDescriptiveReadOnly;
import org.eclipse.sensinact.core.whiteboard.AbstractDescriptiveReadWrite;
import org.eclipse.sensinact.core.whiteboard.WhiteboardAct;
import org.eclipse.sensinact.core.whiteboard.WhiteboardGet;
import org.eclipse.sensinact.core.whiteboard.WhiteboardSet;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.Constants;
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
                if (bReturnNull)
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

    @Nested
    class WhiteboardHandlerAutoCreateTest {

        Map<String, Object> makeProps(String model, String service, String resource) {
            return Map.of(Constants.SERVICE_ID, 42L, "sensiNact.whiteboard.model", model,
                    "sensiNact.whiteboard.service", service, "sensiNact.whiteboard.resource", resource,
                    "sensiNact.whiteboard.create", true);
        }

        @Test
        void testReadOnly() throws Throwable {
            AbstractDescriptiveReadOnly<Integer> getHandler = new AbstractDescriptiveReadOnly<>() {
                @Override
                public Promise<TimedValue<Integer>> doPullValue(PromiseFactory pf, String modelPackageUri, String model,
                        String provider, String service, String resource, TimedValue<Integer> cachedValue) {
                    return pf.resolved(new TimedValueImpl<>(42));
                }
            };

            assertEquals(Integer.class, getHandler.getResourceType());

            final String modelName = "wbHandlerROTest";
            final String svcName = "svc";
            thread.addWhiteboardResourceHandler(getHandler, makeProps(modelName, svcName, "rc"));

            createProviders(modelName, svcName);

            TimedValue<Integer> result = getValue(PROVIDER_A, svcName, "rc", Integer.class);
            assertNotNull(result.getValue(), "No value");
            assertNotNull(result.getTimestamp(), "No timestamp");
            assertEquals(42, result.getValue());

            // FIXME: Change exception type
            assertThrows(Exception.class,
                    () -> setValue(PROVIDER_A, svcName, "rc", (r) -> r.setValue(1234, Instant.now())));
        }

        @Test
        void testReadWrite() throws Throwable {
            AbstractDescriptiveReadWrite<Long> rcHandler = new AbstractDescriptiveReadWrite<>() {
                Long value = 42L;

                @Override
                public Promise<TimedValue<Long>> doPullValue(PromiseFactory pf, String modelPackageUri, String model,
                        String provider, String service, String resource, TimedValue<Long> cachedValue) {
                    return pf.resolved(new TimedValueImpl<Long>(value));
                }

                @Override
                public Promise<TimedValue<Long>> doPushValue(PromiseFactory pf, String modelPackageUri, String model,
                        String provider, String service, String resource, TimedValue<Long> cachedValue,
                        TimedValue<Long> newValue) {
                    this.value = newValue.getValue();
                    return pf.resolved(new TimedValueImpl<Long>(value));
                }
            };

            assertEquals(Long.class, rcHandler.getResourceType());

            final String modelName = "wbHandlerRWTest";
            final String svcName = "svc";
            thread.addWhiteboardResourceHandler(rcHandler, makeProps(modelName, svcName, "rc"));

            createProviders(modelName, svcName);

            TimedValue<Long> result = getValue(PROVIDER_A, svcName, "rc", Long.class);
            assertNotNull(result.getValue(), "No value");
            assertNotNull(result.getTimestamp(), "No timestamp");
            assertEquals(42L, result.getValue());

            setValue(PROVIDER_A, svcName, "rc", (r) -> r.setValue(1234L, Instant.now()));
            result = getValue(PROVIDER_A, svcName, "rc", Long.class);
            assertNotNull(result.getValue(), "No value");
            assertNotNull(result.getTimestamp(), "No timestamp");
            assertEquals(1234L, result.getValue());

            setValue(PROVIDER_A, svcName, "rc", (r) -> r.setValue(null, Instant.now()));
            result = getValue(PROVIDER_A, svcName, "rc", Long.class);
            assertNull(result.getValue(), "Value still there");
            assertNotNull(result.getTimestamp(), "No timestamp");
        }

        @Test
        void testActEcho() throws Throwable {
            AbstractDescriptiveAct<String> providerEchoHandler = new AbstractDescriptiveAct<>() {
                @Override
                public List<Entry<String, Class<?>>> getNamedParameterTypes() {
                    return List.of();
                }

                @Override
                protected Promise<String> doAct(PromiseFactory promiseFactory, String modelPackageUri, String model,
                        String provider, String service, String resource, Map<String, Object> arguments) {
                    return promiseFactory.resolved(provider);
                }
            };

            assertEquals(String.class, providerEchoHandler.getReturnType());

            final String modelName = "wbHandlerActTest";
            final String svcName = "svc";
            thread.addWhiteboardResourceHandler(providerEchoHandler, makeProps(modelName, svcName, "rc"));

            createProviders(modelName, svcName);

            // Test action
            assertEquals(PROVIDER_A, act(PROVIDER_A, svcName, "rc", Map.of()));
            assertEquals(PROVIDER_B, act(PROVIDER_B, svcName, "rc", Map.of()));

            // Other kinds of access must fail
            assertThrows(IllegalArgumentException.class, () -> getValue(PROVIDER_A, svcName, "rc", String.class));
            assertThrows(IllegalArgumentException.class,
                    () -> setValue(PROVIDER_A, svcName, "rc", (r) -> r.setValue(1234, Instant.now())));
        }

        @Test
        void testActArgs() throws Throwable {
            AbstractDescriptiveAct<Integer> providerEchoHandler = new AbstractDescriptiveAct<>() {
                @Override
                public List<Entry<String, Class<?>>> getNamedParameterTypes() {
                    return List.of(Map.entry("value", String.class), Map.entry("radix", Integer.class));
                }

                @Override
                public Promise<Integer> doAct(PromiseFactory pf, String modelPackageUri, String model, String provider,
                        String service, String resource, Map<String, Object> arguments) {

                    String value = (String) arguments.get("value");
                    if (value == null) {
                        return pf.failed(new NullPointerException("No value given"));
                    }

                    Integer radix = (Integer) arguments.get("radix");
                    if (radix == null) {
                        radix = 10;
                    }
                    return pf.resolved(Integer.parseInt(value, radix));
                }
            };

            final String modelName = "wbHandlerActTest2";
            final String svcName = "svc";
            thread.addWhiteboardResourceHandler(providerEchoHandler, makeProps(modelName, svcName, "rc"));

            createProviders(modelName, svcName);

            // Test action
            assertEquals(10, act(PROVIDER_A, svcName, "rc", Map.of("value", "10")));
            assertEquals(10, act(PROVIDER_A, svcName, "rc", Map.of("value", "10", "radix", 10)));
            assertEquals(2, act(PROVIDER_A, svcName, "rc", Map.of("value", "10", "radix", 2)));
            assertEquals(255, act(PROVIDER_A, svcName, "rc", Map.of("value", "FF", "radix", 16)));

            // Test errors
            assertThrows(NullPointerException.class, () -> act(PROVIDER_A, svcName, "rc", Map.of()));
            assertThrows(NullPointerException.class, () -> act(PROVIDER_A, svcName, "rc", Map.of("value", null)));

            // Other kinds of access must fail
            assertThrows(IllegalArgumentException.class, () -> getValue(PROVIDER_A, svcName, "rc", String.class));
            assertThrows(IllegalArgumentException.class,
                    () -> setValue(PROVIDER_A, svcName, "rc", (r) -> r.setValue(1234, Instant.now())));
        }
    }

    @Nested
    class WhiteboardHandlerTest {

        Map<String, Object> makeProps(String model, String service, String resource) {
            return Map.of(Constants.SERVICE_ID, 42L, "sensiNact.whiteboard.model", model,
                    "sensiNact.whiteboard.service", service, "sensiNact.whiteboard.resource", resource);
        }

        void makeResource(final String modelName, final String serviceName, final String resourceName,
                final Consumer<ResourceBuilder<?, Object>> builderCaller) {
            thread.execute(new AbstractSensinactCommand<Void>() {
                @Override
                protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                        PromiseFactory promiseFactory) {
                    ResourceBuilder<?, Object> builder = null;
                    Resource resource = null;

                    Model model = modelMgr.getModel(modelName);
                    if (model == null) {
                        builder = modelMgr.createModel(null, modelName).withService(serviceName)
                                .withResource(resourceName);
                    } else {
                        Service service = model.getServices().get(serviceName);
                        if (service == null) {
                            builder = model.createService(serviceName).withResource(resourceName);
                        } else {
                            resource = service.getResources().get(resourceName);
                            if (resource == null) {
                                builder = service.createResource(resourceName);
                            }
                        }
                    }

                    if (builder != null) {
                        // Construct the resource
                        builderCaller.accept(builder);
                    }

                    return promiseFactory.resolved(null);
                }
            });
        }

        void makeValueResource(final String modelName, final String serviceName, final String resourceName,
                Class<?> type) {
            makeResource(modelName, serviceName, resourceName,
                    b -> b.withType(type).withGetter().withSetter().withGetterCache(Duration.ZERO).buildAll());
        }

        void makeActionResource(final String modelName, final String serviceName, final String resourceName,
                Class<?> resultType, List<Entry<String, Class<?>>> params) {
            makeResource(modelName, serviceName, resourceName,
                    b -> b.withAction(params).withType(resultType).buildAll());
        }

        @Test
        void testReadOnly() throws Throwable {
            WhiteboardGet<Integer> getHandler = (pf, modelPackageUri, model, provider, service, resource, resourceType,
                    cachedValue) -> pf.resolved(new TimedValueImpl<>(42));

            final String modelName = "wbHandlerROTest";
            final String svcName = "svc";
            final String rcName = "rc";

            // Register handler
            thread.addWhiteboardResourceHandler(getHandler, makeProps(modelName, svcName, rcName));

            // Create model
            makeValueResource(modelName, svcName, rcName, Integer.class);

            // Create providers
            createProviders(modelName, svcName);

            TimedValue<Integer> result = getValue(PROVIDER_A, svcName, rcName, Integer.class);
            assertNotNull(result.getValue(), "No value");
            assertNotNull(result.getTimestamp(), "No timestamp");
            assertEquals(42, result.getValue());

            // FIXME: Change exception type
            assertThrows(Exception.class,
                    () -> setValue(PROVIDER_A, svcName, rcName, (r) -> r.setValue(1234, Instant.now())));
        }

        @Test
        void testReadWrite() throws Throwable {
            WhiteboardSet<Long> rcHandler = new WhiteboardSet<>() {
                Long value = 42L;

                @Override
                public Promise<TimedValue<Long>> pullValue(PromiseFactory pf, String modelPackageUri, String model,
                        String provider, String service, String resource, Class<Long> resourceType,
                        TimedValue<Long> cachedValue) {
                    return pf.resolved(new TimedValueImpl<Long>(value));
                }

                @Override
                public Promise<TimedValue<Long>> pushValue(PromiseFactory pf, String modelPackageUri, String model,
                        String provider, String service, String resource, Class<Long> resourceType,
                        TimedValue<Long> cachedValue, TimedValue<Long> newValue) {
                    this.value = newValue.getValue();
                    return pf.resolved(new TimedValueImpl<Long>(value));
                }
            };

            final String modelName = "wbHandlerRWTest";
            final String svcName = "svc";
            final String rcName = "rc";

            // This time, prepare the provider before registering the handler
            makeValueResource(modelName, svcName, rcName, Long.class);
            createProviders(modelName, svcName);

            thread.addWhiteboardResourceHandler(rcHandler, makeProps(modelName, svcName, rcName));

            TimedValue<Long> result = getValue(PROVIDER_A, svcName, rcName, Long.class);
            assertNotNull(result.getValue(), "No value");
            assertNotNull(result.getTimestamp(), "No timestamp");
            assertEquals(42L, result.getValue());

            setValue(PROVIDER_A, svcName, rcName, (r) -> r.setValue(1234L, Instant.now()));
            result = getValue(PROVIDER_A, svcName, rcName, Long.class);
            assertNotNull(result.getValue(), "No value");
            assertNotNull(result.getTimestamp(), "No timestamp");
            assertEquals(1234L, result.getValue());

            setValue(PROVIDER_A, svcName, rcName, (r) -> r.setValue(null, Instant.now()));
            result = getValue(PROVIDER_A, svcName, rcName, Long.class);
            assertNull(result.getValue(), "Value still there");
            assertNotNull(result.getTimestamp(), "No timestamp");
        }

        @Test
        void testActEcho() throws Throwable {
            WhiteboardAct<String> providerEchoHandler = (promiseFactory, modelPackageUri, model, provider, service,
                    resource, arguments) -> promiseFactory.resolved(provider);

            final String modelName = "wbHandlerActTest";
            final String svcName = "svc";
            final String rcName = "rc";
            thread.addWhiteboardResourceHandler(providerEchoHandler, makeProps(modelName, svcName, rcName));

            makeActionResource(modelName, svcName, rcName, String.class, List.of());
            createProviders(modelName, svcName);

            // Test action
            assertEquals(PROVIDER_A, act(PROVIDER_A, svcName, rcName, Map.of()));
            assertEquals(PROVIDER_B, act(PROVIDER_B, svcName, rcName, Map.of()));

            // Other kinds of access must fail
            assertThrows(IllegalArgumentException.class, () -> getValue(PROVIDER_A, svcName, rcName, String.class));
            assertThrows(IllegalArgumentException.class,
                    () -> setValue(PROVIDER_A, svcName, rcName, (r) -> r.setValue(1234, Instant.now())));
        }

        @Test
        void testActArgs() throws Throwable {
            WhiteboardAct<Integer> providerEchoHandler = (pf, modelPackageUri, model, provider, service, resource,
                    arguments) -> {
                String value = (String) arguments.get("value");
                if (value == null) {
                    return pf.failed(new NullPointerException("No value given"));
                }

                Integer radix = (Integer) arguments.get("radix");
                if (radix == null) {
                    radix = 10;
                }
                return pf.resolved(Integer.parseInt(value, radix));
            };

            final String modelName = "wbHandlerActTest2";
            final String svcName = "svc";
            final String rcName = "rc";
            thread.addWhiteboardResourceHandler(providerEchoHandler, makeProps(modelName, svcName, rcName));

            makeActionResource(modelName, svcName, rcName, Integer.class,
                    List.of(Map.entry("value", String.class), Map.entry("radix", Integer.class)));
            createProviders(modelName, svcName);

            // Test action
            assertEquals(10, act(PROVIDER_A, svcName, rcName, Map.of("value", "10")));
            assertEquals(10, act(PROVIDER_A, svcName, rcName, Map.of("value", "10", "radix", 10)));
            assertEquals(2, act(PROVIDER_A, svcName, rcName, Map.of("value", "10", "radix", 2)));
            assertEquals(255, act(PROVIDER_A, svcName, rcName, Map.of("value", "FF", "radix", 16)));

            // Test errors
            assertThrows(NullPointerException.class, () -> act(PROVIDER_A, svcName, rcName, Map.of()));
            assertThrows(NullPointerException.class, () -> act(PROVIDER_A, svcName, rcName, Map.of("value", null)));

            // Other kinds of access must fail
            assertThrows(IllegalArgumentException.class, () -> getValue(PROVIDER_A, svcName, rcName, String.class));
            assertThrows(IllegalArgumentException.class,
                    () -> setValue(PROVIDER_A, svcName, rcName, (r) -> r.setValue(1234, Instant.now())));
        }
    }

    @Nested
    class HandlerSelectionTest {
        Map<String, Object> makeProps(long svcId, String model, String service, String resource, String... providers) {
            Map<String, Object> props = new HashMap<>();
            props.put(Constants.SERVICE_ID, svcId);
            props.put("sensiNact.whiteboard.model", model);
            props.put("sensiNact.whiteboard.service", service);
            props.put("sensiNact.whiteboard.resource", resource);
            props.put("sensiNact.whiteboard.providers", providers.length == 0 ? null : providers);
            return props;
        }

        void makeResource(final String modelName, final String serviceName, final String resourceName,
                final Consumer<ResourceBuilder<?, Object>> builderCaller) {
            thread.execute(new AbstractSensinactCommand<Void>() {
                @Override
                protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                        PromiseFactory promiseFactory) {
                    ResourceBuilder<?, Object> builder = null;
                    Resource resource = null;

                    Model model = modelMgr.getModel(modelName);
                    if (model == null) {
                        builder = modelMgr.createModel(null, modelName).withService(serviceName)
                                .withResource(resourceName);
                    } else {
                        Service service = model.getServices().get(serviceName);
                        if (service == null) {
                            builder = model.createService(serviceName).withResource(resourceName);
                        } else {
                            resource = service.getResources().get(resourceName);
                            if (resource == null) {
                                builder = service.createResource(resourceName);
                            }
                        }
                    }

                    if (builder != null) {
                        // Construct the resource
                        builderCaller.accept(builder);
                    }

                    return promiseFactory.resolved(null);
                }
            });
        }

        void makeValueResource(final String modelName, final String serviceName, final String resourceName,
                Class<?> type) {
            makeResource(modelName, serviceName, resourceName,
                    b -> b.withType(type).withGetter().withSetter().withGetterCache(Duration.ZERO).buildAll());
        }

        void makeActionResource(final String modelName, final String serviceName, final String resourceName,
                Class<?> resultType, List<Entry<String, Class<?>>> params) {
            makeResource(modelName, serviceName, resourceName,
                    b -> b.withAction(params).withType(resultType).buildAll());
        }

        @Test
        void testHandlersProviderFilter() throws Throwable {
            WhiteboardGet<Integer> h1 = (pf, modelPackageUri, model, provider, service, resource, resourceType,
                    cachedValue) -> pf.resolved(new TimedValueImpl<>(1));
            WhiteboardGet<Integer> h2 = (pf, modelPackageUri, model, provider, service, resource, resourceType,
                    cachedValue) -> pf.resolved(new TimedValueImpl<>(2));

            final String modelName = "wbHandlerPriority";
            final String svcName = "svc";
            final String rcName = "rc";

            // Register handlers
            Map<String, Object> props1 = makeProps(41, modelName, svcName, rcName, PROVIDER_A);
            thread.addWhiteboardResourceHandler(h1, props1);
            thread.addWhiteboardResourceHandler(h2, makeProps(42, modelName, svcName, rcName));

            // Create model
            makeValueResource(modelName, svcName, rcName, Integer.class);

            // Create providers
            createProviders(modelName, svcName);

            // Provider A should be handled by H1
            TimedValue<Integer> result = getValue(PROVIDER_A, svcName, rcName, Integer.class);
            assertNotNull(result.getValue(), "No value");
            assertNotNull(result.getTimestamp(), "No timestamp");
            assertEquals(1, result.getValue());

            // Provider B should be handled by H2
            result = getValue(PROVIDER_B, svcName, rcName, Integer.class);
            assertNotNull(result.getValue(), "No value");
            assertNotNull(result.getTimestamp(), "No timestamp");
            assertEquals(2, result.getValue());

            // Remove handler 1
            thread.removeWhiteboardResourceHandler(h1, props1);

            // Provider A should be handled by H2
            result = getValue(PROVIDER_A, svcName, rcName, Integer.class);
            assertNotNull(result.getValue(), "No value");
            assertNotNull(result.getTimestamp(), "No timestamp");
            assertEquals(2, result.getValue());

            // Provider B should be handled by H2
            result = getValue(PROVIDER_B, svcName, rcName, Integer.class);
            assertNotNull(result.getValue(), "No value");
            assertNotNull(result.getTimestamp(), "No timestamp");
            assertEquals(2, result.getValue());

            // Add back H1
            thread.addWhiteboardResourceHandler(h1, props1);

            // Provider A should be handled by H1
            result = getValue(PROVIDER_A, svcName, rcName, Integer.class);
            assertNotNull(result.getValue(), "No value");
            assertNotNull(result.getTimestamp(), "No timestamp");
            assertEquals(1, result.getValue());

            // Provider B should be handled by H2
            result = getValue(PROVIDER_B, svcName, rcName, Integer.class);
            assertNotNull(result.getValue(), "No value");
            assertNotNull(result.getTimestamp(), "No timestamp");
            assertEquals(2, result.getValue());
        }

        @Test
        void testHandlersWildcardFilter() throws Throwable {
            WhiteboardGet<Integer> h1 = (pf, modelPackageUri, model, provider, service, resource, resourceType,
                    cachedValue) -> pf.resolved(new TimedValueImpl<>(1));
            WhiteboardGet<Integer> h2 = (pf, modelPackageUri, model, provider, service, resource, resourceType,
                    cachedValue) -> pf.resolved(new TimedValueImpl<>(2));
            WhiteboardGet<Integer> h3 = (pf, modelPackageUri, model, provider, service, resource, resourceType,
                    cachedValue) -> pf.resolved(new TimedValueImpl<>(3));

            final String modelName = "wbHandlerPriority";
            final String svcName1 = "svc1";
            final String rcName1 = "rc";
            final String svcName2 = "svc2";
            final String rcName2 = "test";

            // Register handlers
            thread.addWhiteboardResourceHandler(h1, makeProps(41, modelName, svcName1, rcName1));
            thread.addWhiteboardResourceHandler(h2, makeProps(42, modelName, svcName1, null));
            thread.addWhiteboardResourceHandler(h3, makeProps(42, modelName, null, null));

            // Create model
            makeValueResource(modelName, svcName1, rcName1, Integer.class);
            makeValueResource(modelName, svcName1, rcName2, Integer.class);
            makeValueResource(modelName, svcName2, rcName1, Integer.class);
            makeValueResource(modelName, svcName2, rcName2, Integer.class);

            // Create providers
            createProviders(modelName, svcName1);

            // svc1/rc1 should be handled by H1
            TimedValue<Integer> result = getValue(PROVIDER_A, svcName1, rcName1, Integer.class);
            assertNotNull(result.getValue(), "No value");
            assertNotNull(result.getTimestamp(), "No timestamp");
            assertEquals(1, result.getValue());

            // svc1/rc2 should be handled by H2
            result = getValue(PROVIDER_B, svcName1, rcName2, Integer.class);
            assertNotNull(result.getValue(), "No value");
            assertNotNull(result.getTimestamp(), "No timestamp");
            assertEquals(2, result.getValue());

            // svc2/rc1 should be handled by H3
            result = getValue(PROVIDER_B, svcName2, rcName1, Integer.class);
            assertNotNull(result.getValue(), "No value");
            assertNotNull(result.getTimestamp(), "No timestamp");
            assertEquals(3, result.getValue());

            // svc2/rc2 should be handled by H3
            result = getValue(PROVIDER_B, svcName2, rcName2, Integer.class);
            assertNotNull(result.getValue(), "No value");
            assertNotNull(result.getTimestamp(), "No timestamp");
            assertEquals(3, result.getValue());
        }
    }
}

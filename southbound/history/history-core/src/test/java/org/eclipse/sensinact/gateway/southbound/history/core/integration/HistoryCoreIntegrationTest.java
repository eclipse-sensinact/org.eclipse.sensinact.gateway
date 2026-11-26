/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.history.core.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.command.ResourceCommand;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.southbound.history.inmemory.InMemoryHistoryStorage;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryProvider;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryIngestFilter;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryRecordingStep;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryRecordingStepFactory;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * End-to-end test of the history engine on the in-memory backend: resource
 * updates flow through the typed event bus into the storage, are queryable
 * through the {@link HistoryProvider} service and the legacy ACT facade, and
 * historization filters take effect and disappear at runtime without
 * restarting the backend.
 */
public class HistoryCoreIntegrationTest {

    private static final String PROVIDER_NAME = "inmemory-test";
    private static final String SECOND_PROVIDER_NAME = "inmemory-test-second";
    private static final Instant TS_1 = Instant.parse("2026-01-01T00:00:00Z");
    private ResourcePath path;

    @InjectService
    DataUpdate push;

    @InjectService
    GatewayThread gatewayThread;

    HistoryProvider provider;

    @BeforeEach
    void setUp(
            @InjectConfiguration(withConfig = @WithConfiguration(pid = "sensinact.history.inmemory", location = "?", properties = {
                    @Property(key = "provider", value = PROVIDER_NAME) })) Configuration storageConfig,
            @InjectService(filter = "(" + HistoryProvider.PROP_NAME + "=" + PROVIDER_NAME
                    + ")", cardinality = 0) ServiceAware<HistoryProvider> providerAware)
            throws Exception {
        provider = providerAware.waitForService(5000);
    }

    private void pushTemperature(Object value, Instant timestamp) throws Exception {
        GenericDto dto = new GenericDto();
        dto.model = "testModel";
        dto.provider = path.provider();
        dto.service = path.service();
        dto.resource = path.resource();
        dto.value = value;
        dto.type = value.getClass();
        dto.timestamp = timestamp;
        push.pushUpdate(dto).getValue();
    }

    private void awaitCount(long expected) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        long current = -1;
        while (System.currentTimeMillis() < deadline) {
            current = provider.getValueCount(path, TimeRange.ALL);
            if (current == expected) {
                return;
            }
            Thread.sleep(50);
        }
        throw new AssertionFailedError("Expected " + expected + " stored values but found " + current);
    }

    private static void unregisterQuietly(ServiceRegistration<?> registration) {
        try {
            registration.unregister();
        } catch (IllegalStateException e) {
            // already unregistered by the test
        }
    }

    /** Records of the resource of the current test, ignoring the other updates still in flight. */
    private long temperatureRecords(List<HistoricalRecord> records) {
        return records.stream().filter(record -> path.equals(record.path())).count();
    }

    private static void awaitTrue(String what, BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        assertTrue(condition.getAsBoolean(), "Timed out waiting for " + what);
    }

    @Test
    void updatesFlowIntoHistoryAndOutThroughServiceAndFacade() throws Exception {
        path = new ResourcePath("sensorFlow", "data", "temperature");
        pushTemperature(21.5d, TS_1);
        pushTemperature(22.0d, TS_1.plusSeconds(60));
        pushTemperature(23.5d, TS_1.plusSeconds(120));
        awaitCount(3);

        List<TimedValue<?>> values = provider.getValues(HistoryQuery.builder(path).build()).values();
        assertEquals(List.of(21.5d, 22.0d, 23.5d), values.stream().map(TimedValue::getValue).toList());

        Object actResult = gatewayThread.execute(new ResourceCommand<Object>(
                "https://eclipse.org/sensinact/sensiNactHistory", "sensiNactHistory", PROVIDER_NAME, "history",
                "single") {
            @Override
            protected Promise<Object> call(SensinactResource resource, PromiseFactory promiseFactory) {
                return resource.act(Map.of("provider", path.provider(), "service", path.service(), "resource",
                        path.resource())).map(Object.class::cast);
            }
        }).getValue();

        assertTrue(actResult instanceof TimedValue<?>, "ACT facade did not return a TimedValue: " + actResult);
        assertEquals(21.5d, ((TimedValue<?>) actResult).getValue());
    }

    @Test
    void deadbandFilterSuppressesAtRuntimeAndItsRemovalNeedsNoBackendRestart(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = "sensinact.history.filter", name = "deadband-it", location = "?", properties = {
                    @Property(key = "name", value = "deadband-it"),
                    @Property(key = "target", value = PROVIDER_NAME),
                    @Property(key = "change.mode", value = "deadband"),
                    @Property(key = "change.threshold", value = "0.5") })) Configuration filterConfig,
            @InjectService(cardinality = 0) ServiceAware<HistoryIngestFilter> filterAware) throws Exception {
        path = new ResourcePath("sensorDeadband", "data", "temperature");
        assertNotNull(filterAware.waitForService(5000), "historization filter service did not appear");

        pushTemperature(21.0d, TS_1);
        awaitCount(1);
        pushTemperature(21.2d, TS_1.plusSeconds(60));
        pushTemperature(22.0d, TS_1.plusSeconds(120));
        awaitCount(2);

        filterConfig.delete();
        long deadline = System.currentTimeMillis() + 5000;
        while (!filterAware.isEmpty() && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        assertTrue(filterAware.isEmpty(), "Filter service did not disappear after config deletion");

        pushTemperature(22.1d, TS_1.plusSeconds(180));
        awaitCount(3);

        assertEquals(List.of(21.0d, 22.0d, 22.1d),
                provider.getFirstValues(path, 10, 0).stream().map(TimedValue::getValue).toList());
    }

    @Test
    void eachOfSeveralHistoriesUsesTheStepOfItsBestFactory(@InjectBundleContext BundleContext context,
            @InjectService(filter = "(" + HistoryProvider.PROP_NAME + "=" + SECOND_PROVIDER_NAME
                    + ")", cardinality = 0) ServiceAware<HistoryProvider> secondAware)
            throws Exception {
        // a second history on the same gateway, with its own storage
        ServiceRegistration<HistoryStorage> secondStorage = context.registerService(HistoryStorage.class,
                new InMemoryHistoryStorage(100),
                new Hashtable<>(Map.of(HistoryStorage.PROP_NAME, SECOND_PROVIDER_NAME)));
        try {
            assertNotNull(secondAware.waitForService(5000), "second history provider did not appear");
            checkEachHistoryUsesTheStepOfItsBestFactory(context);
        } finally {
            secondStorage.unregister();
        }
    }

    private void checkEachHistoryUsesTheStepOfItsBestFactory(BundleContext context) throws Exception {
        path = new ResourcePath("sensorSeveral", "data", "temperature");
        Map<String, List<HistoricalRecord>> recorded = new ConcurrentHashMap<>();
        BiFunction<String, String, HistoryRecordingStep> stepOf = (factory, name) -> record -> recorded
                .computeIfAbsent(factory + ":" + name, key -> new CopyOnWriteArrayList<>()).add(record);
        HistoryRecordingStepFactory forAll = recording -> stepOf.apply("all", recording.providerName());
        HistoryRecordingStepFactory forSecond = recording -> stepOf.apply("second", recording.providerName());

        // the better ranked factory is registered first: the second history never uses the other
        // one, so events still in flight while factories are registered cannot reach it
        ServiceRegistration<HistoryRecordingStepFactory> secondRegistration = context.registerService(
                HistoryRecordingStepFactory.class, forSecond,
                new Hashtable<>(Map.of(HistoryRecordingStepFactory.PROP_NAME, SECOND_PROVIDER_NAME,
                        Constants.SERVICE_RANKING, 10)));
        ServiceRegistration<HistoryRecordingStepFactory> allRegistration = context.registerService(
                HistoryRecordingStepFactory.class, forAll, new Hashtable<>());
        try {
            pushTemperature(21.0d, TS_1);
            awaitTrue("both histories to receive the update",
                    () -> temperatureRecords(recorded.getOrDefault("all:" + PROVIDER_NAME, List.of())) == 1
                            && temperatureRecords(
                                    recorded.getOrDefault("second:" + SECOND_PROVIDER_NAME, List.of())) == 1);
            assertEquals(0, provider.getValueCount(path, TimeRange.ALL));

            // the better ranked factory took over the second history, the first one kept the other;
            // checked while both factories are registered, as later events reach whichever step
            // a history falls back to once a factory is removed
            assertEquals(Set.of("all:" + PROVIDER_NAME, "second:" + SECOND_PROVIDER_NAME), recorded.keySet());
        } finally {
            secondRegistration.unregister();
            allRegistration.unregister();
        }
    }

    @Test
    void bestRankedFactoryIsUsedAndTheNextOneTakesOverWhenItIsRemoved(@InjectBundleContext BundleContext context)
            throws Exception {
        path = new ResourcePath("sensorRanking", "data", "temperature");
        List<HistoricalRecord> low = new CopyOnWriteArrayList<>();
        List<HistoricalRecord> high = new CopyOnWriteArrayList<>();
        HistoryRecordingStepFactory lowFactory = recording -> low::add;
        HistoryRecordingStepFactory highFactory = recording -> high::add;

        ServiceRegistration<HistoryRecordingStepFactory> lowRegistration = context.registerService(
                HistoryRecordingStepFactory.class, lowFactory,
                new Hashtable<>(Map.of(Constants.SERVICE_RANKING, 1)));
        ServiceRegistration<HistoryRecordingStepFactory> highRegistration = context.registerService(
                HistoryRecordingStepFactory.class, highFactory,
                new Hashtable<>(Map.of(Constants.SERVICE_RANKING, 10)));
        try {
            pushTemperature(21.0d, TS_1);
            awaitTrue("the best ranked step to receive the update", () -> temperatureRecords(high) == 1);
            assertEquals(0, temperatureRecords(low));

            highRegistration.unregister();
            pushTemperature(22.0d, TS_1.plusSeconds(60));
            awaitTrue("the next factory to take over", () -> temperatureRecords(low) == 1);
            assertEquals(1, temperatureRecords(high));
            assertEquals(0, provider.getValueCount(path, TimeRange.ALL));

            lowRegistration.unregister();
            pushTemperature(23.0d, TS_1.plusSeconds(120));
            awaitCount(1);
            assertEquals(List.of(23.0d),
                    provider.getFirstValues(path, 10, 0).stream().map(TimedValue::getValue).toList());
            assertEquals(1, temperatureRecords(low));
            assertEquals(1, temperatureRecords(high));
        } finally {
            unregisterQuietly(highRegistration);
            unregisterQuietly(lowRegistration);
        }
    }

    @Test
    void recordingStepFactoryReplacesTheStepAtRuntimeAndItsRemovalRestoresTheDefault(
            @InjectBundleContext BundleContext context) throws Exception {
        path = new ResourcePath("sensorStep", "data", "temperature");
        List<HistoricalRecord> received = new CopyOnWriteArrayList<>();
        AtomicBoolean closed = new AtomicBoolean();
        HistoryRecordingStepFactory factory = recording -> new HistoryRecordingStep() {
            @Override
            public void record(HistoricalRecord record) {
                received.add(record);
            }

            @Override
            public void close() {
                closed.set(true);
            }
        };

        ServiceRegistration<HistoryRecordingStepFactory> registration = context.registerService(
                HistoryRecordingStepFactory.class, factory,
                new Hashtable<>(Map.of(HistoryRecordingStepFactory.PROP_NAME, PROVIDER_NAME)));
        try {
            pushTemperature(21.0d, TS_1);
            // a new provider also publishes its administrative resources
            awaitTrue("the replacement step to receive the update", () -> temperatureRecords(received) == 1);
            assertEquals(0, provider.getValueCount(path, TimeRange.ALL));
        } finally {
            registration.unregister();
        }
        awaitTrue("the replacement step to be closed", closed::get);

        pushTemperature(22.0d, TS_1.plusSeconds(60));
        awaitCount(1);

        assertEquals(1, temperatureRecords(received));
        assertEquals(List.of(22.0d),
                provider.getFirstValues(path, 10, 0).stream().map(TimedValue::getValue).toList());
    }

    @Test
    void rankingChangeOfAFactoryIsTakenIntoAccountWithoutReregistration(@InjectBundleContext BundleContext context)
            throws Exception {
        path = new ResourcePath("sensorRankingChange", "data", "temperature");
        List<HistoricalRecord> first = new CopyOnWriteArrayList<>();
        List<HistoricalRecord> second = new CopyOnWriteArrayList<>();
        ServiceRegistration<HistoryRecordingStepFactory> firstRegistration = context.registerService(
                HistoryRecordingStepFactory.class, (HistoryRecordingStepFactory) recording -> first::add,
                new Hashtable<>(Map.of(Constants.SERVICE_RANKING, 10)));
        ServiceRegistration<HistoryRecordingStepFactory> secondRegistration = context.registerService(
                HistoryRecordingStepFactory.class, (HistoryRecordingStepFactory) recording -> second::add,
                new Hashtable<>(Map.of(Constants.SERVICE_RANKING, 1)));
        try {
            pushTemperature(21.0d, TS_1);
            awaitTrue("the best ranked step to receive the update", () -> temperatureRecords(first) == 1);

            secondRegistration.setProperties(new Hashtable<>(Map.of(Constants.SERVICE_RANKING, 20)));
            pushTemperature(22.0d, TS_1.plusSeconds(60));

            awaitTrue("the newly best ranked step to receive the update", () -> temperatureRecords(second) == 1);
            assertEquals(1, temperatureRecords(first));
        } finally {
            unregisterQuietly(secondRegistration);
            unregisterQuietly(firstRegistration);
        }
    }

    @Test
    void failingFactoryLetsTheNextOneServe(@InjectBundleContext BundleContext context) throws Exception {
        path = new ResourcePath("sensorFailingFactory", "data", "temperature");
        List<HistoricalRecord> received = new CopyOnWriteArrayList<>();
        ServiceRegistration<HistoryRecordingStepFactory> failingRegistration = context.registerService(
                HistoryRecordingStepFactory.class, (HistoryRecordingStepFactory) recording -> {
                    throw new IllegalStateException("cannot create");
                }, new Hashtable<>(Map.of(Constants.SERVICE_RANKING, 10)));
        ServiceRegistration<HistoryRecordingStepFactory> workingRegistration = context.registerService(
                HistoryRecordingStepFactory.class, (HistoryRecordingStepFactory) recording -> received::add,
                new Hashtable<>(Map.of(Constants.SERVICE_RANKING, 1)));
        try {
            pushTemperature(21.0d, TS_1);

            awaitTrue("the next factory to receive the update", () -> temperatureRecords(received) == 1);
            assertEquals(0, provider.getValueCount(path, TimeRange.ALL));
            assertNull(failingRegistration.getReference().getUsingBundles(), "the failing factory is still in use");
        } finally {
            unregisterQuietly(failingRegistration);
            unregisterQuietly(workingRegistration);
        }
    }

    @Test
    void bufferingStepWritesWhatItHoldsWhenItsFactoryIsRemoved(@InjectBundleContext BundleContext context)
            throws Exception {
        path = new ResourcePath("sensorBuffering", "data", "temperature");
        List<HistoricalRecord> held = new CopyOnWriteArrayList<>();
        HistoryRecordingStepFactory factory = recording -> new HistoryRecordingStep() {
            @Override
            public void record(HistoricalRecord record) {
                held.add(record);
            }

            @Override
            public void close() {
                recording.storage().store(List.copyOf(held));
            }
        };
        ServiceRegistration<HistoryRecordingStepFactory> registration = context.registerService(
                HistoryRecordingStepFactory.class, factory,
                new Hashtable<>(Map.of(HistoryRecordingStepFactory.PROP_NAME, PROVIDER_NAME)));
        try {
            pushTemperature(21.0d, TS_1);
            awaitTrue("the buffering step to hold the update", () -> temperatureRecords(held) == 1);
            assertEquals(0, provider.getValueCount(path, TimeRange.ALL));
        } finally {
            registration.unregister();
        }

        awaitCount(1);
        assertEquals(List.of(21.0d), provider.getFirstValues(path, 10, 0).stream().map(TimedValue::getValue).toList());
    }

    @Test
    void bufferingStepWritesWhatItHoldsBeforeItsStorageIsReleased(@InjectBundleContext BundleContext context,
            @InjectService(filter = "(" + HistoryProvider.PROP_NAME + "=" + SECOND_PROVIDER_NAME
                    + ")", cardinality = 0) ServiceAware<HistoryProvider> secondAware)
            throws Exception {
        path = new ResourcePath("sensorBufferingStop", "data", "temperature");
        InMemoryHistoryStorage secondStorage = new InMemoryHistoryStorage(100);
        List<String> events = new CopyOnWriteArrayList<>();
        List<HistoricalRecord> held = new CopyOnWriteArrayList<>();
        HistoryRecordingStepFactory factory = recording -> new HistoryRecordingStep() {
            @Override
            public void record(HistoricalRecord record) {
                held.add(record);
            }

            @Override
            public void close() {
                recording.storage().store(List.copyOf(held));
                events.add("flush");
            }
        };
        // a service factory, so that the release of the storage is seen
        ServiceFactory<HistoryStorage> storageFactory = new ServiceFactory<>() {
            @Override
            public HistoryStorage getService(Bundle bundle, ServiceRegistration<HistoryStorage> registration) {
                return secondStorage;
            }

            @Override
            public void ungetService(Bundle bundle, ServiceRegistration<HistoryStorage> registration,
                    HistoryStorage service) {
                events.add("unget");
            }
        };
        ServiceRegistration<HistoryRecordingStepFactory> factoryRegistration = context.registerService(
                HistoryRecordingStepFactory.class, factory,
                new Hashtable<>(Map.of(HistoryRecordingStepFactory.PROP_NAME, SECOND_PROVIDER_NAME)));
        ServiceRegistration<?> storageRegistration = null;
        try {
            storageRegistration = context.registerService(HistoryStorage.class.getName(), storageFactory,
                    new Hashtable<>(Map.of(HistoryStorage.PROP_NAME, SECOND_PROVIDER_NAME)));
            assertNotNull(secondAware.waitForService(5000), "second history provider did not appear");
            pushTemperature(21.0d, TS_1);
            awaitTrue("the buffering step to hold the update", () -> temperatureRecords(held) == 1);
            assertEquals(0, secondStorage.count(path, TimeRange.ALL));

            storageRegistration.unregister();

            assertEquals(List.of("flush", "unget"), events);
            assertEquals(1, secondStorage.count(path, TimeRange.ALL));
        } finally {
            if (storageRegistration != null) {
                unregisterQuietly(storageRegistration);
            }
            unregisterQuietly(factoryRegistration);
        }
    }
}

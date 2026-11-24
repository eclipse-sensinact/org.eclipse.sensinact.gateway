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
package org.eclipse.sensinact.gateway.southbound.history.core.ingest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.southbound.history.core.filter.ChangeCondition;
import org.eclipse.sensinact.gateway.southbound.history.core.filter.ChangeCondition.Mode;
import org.eclipse.sensinact.gateway.southbound.history.core.filter.ConfiguredIngestFilter;
import org.eclipse.sensinact.gateway.southbound.history.core.ingest.IngestionPipeline.RetiredStep;
import org.eclipse.sensinact.gateway.southbound.history.inmemory.InMemoryHistoryStorage;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryRecordingStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IngestionPipelineTest {

    private static final Instant T0 = Instant.parse("2026-01-01T00:00:00Z");

    private InMemoryHistoryStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryHistoryStorage(10_000);
    }

    private static ResourceDataNotification update(String resource, Object value, Instant timestamp) {
        return new ResourceDataNotification("uri", "model", "prov", "svc", resource, null, value, timestamp,
                value == null ? String.class : value.getClass(), Map.of());
    }

    private static ICriterion criterion(List<String> topics, Predicate<ResourceDataNotification> predicate) {
        return new ICriterion() {
            @Override
            public List<String> dataTopics() {
                return topics;
            }

            @Override
            public Predicate<ResourceDataNotification> dataEventFilter() {
                return predicate;
            }

            @Override
            public BiPredicate<ProviderSnapshot, GeoJsonObject> getLocationFilter() {
                return null;
            }

            @Override
            public Predicate<ProviderSnapshot> getProviderFilter() {
                return null;
            }

            @Override
            public Predicate<ServiceSnapshot> getServiceFilter() {
                return null;
            }

            @Override
            public Predicate<ResourceSnapshot> getResourceFilter() {
                return null;
            }

            @Override
            public ResourceValueFilter getResourceValueFilter() {
                return null;
            }
        };
    }

    private long count(String resource) {
        return storage.count(new ResourcePath("prov", "svc", resource), TimeRange.ALL);
    }

    @Test
    void withoutFiltersAndPropertiesEverythingIsStored() {
        IngestionPipeline pipeline = new IngestionPipeline("test", storage, null, null);

        pipeline.handle(update("a", 1L, T0));
        pipeline.handle(update("b", "x", T0));

        assertEquals(1, count("a"));
        assertEquals(1, count("b"));
        assertEquals(Set.of("DATA/*"), pipeline.topics());
    }

    @Test
    void backendPropertiesApplyWhenNoFilterIsConfigured() {
        ICriterion include = criterion(List.of("DATA/model/prov/svc/*"),
                event -> "keep".equals(event.resource()));
        ICriterion exclude = criterion(null, event -> "drop".equals(event.resource()));
        IngestionPipeline pipeline = new IngestionPipeline("test", storage, include, exclude);

        pipeline.handle(update("keep", 1L, T0));
        pipeline.handle(update("other", 1L, T0));

        assertEquals(1, count("keep"));
        assertEquals(0, count("other"));
        assertEquals(Set.of("DATA/model/prov/svc/*"), pipeline.topics());
    }

    @Test
    void configuredFiltersWinOverBackendProperties() {
        ICriterion backendInclude = criterion(List.of("DATA/backend/*"), event -> false);
        IngestionPipeline pipeline = new IngestionPipeline("test", storage, backendInclude, null);
        pipeline.setFilters(List.of(new ConfiguredIngestFilter("f", List.of(),
                criterion(List.of("DATA/filter/*"), event -> "wanted".equals(event.resource())), null, null)));

        pipeline.handle(update("wanted", 1L, T0));
        pipeline.handle(update("unwanted", 1L, T0));

        assertEquals(1, count("wanted"));
        assertEquals(0, count("unwanted"));
        assertEquals(Set.of("DATA/filter/*"), pipeline.topics());
    }

    @Test
    void anyFilterExcludeVetoesEvenWhenAnotherIncludes() {
        ConfiguredIngestFilter includeAll = new ConfiguredIngestFilter("in", List.of(),
                criterion(List.of("DATA/*"), event -> true), null, null);
        ConfiguredIngestFilter excludeDrop = new ConfiguredIngestFilter("ex", List.of(),
                criterion(List.of("DATA/*"), event -> false),
                criterion(null, event -> "drop".equals(event.resource())), null);
        IngestionPipeline pipeline = new IngestionPipeline("test", storage, null, null);
        pipeline.setFilters(List.of(includeAll, excludeDrop));

        pipeline.handle(update("drop", 1L, T0));
        pipeline.handle(update("keep", 1L, T0));

        assertEquals(0, count("drop"));
        assertEquals(1, count("keep"));
    }

    @Test
    void deadbandFilterSuppressesAgainstLastStoredValue() {
        ConfiguredIngestFilter deadband = new ConfiguredIngestFilter("db", List.of(),
                criterion(List.of("DATA/*"), event -> true), null,
                new ChangeCondition(Mode.DEADBAND, new BigDecimal("0.5"), null, null));
        IngestionPipeline pipeline = new IngestionPipeline("test", storage, null, null);
        pipeline.setFilters(List.of(deadband));

        pipeline.handle(update("temp", 21.0d, T0));
        pipeline.handle(update("temp", 21.2d, T0.plusSeconds(10)));
        pipeline.handle(update("temp", 21.4d, T0.plusSeconds(20)));
        pipeline.handle(update("temp", 22.0d, T0.plusSeconds(30)));

        assertEquals(2, count("temp"));
        assertEquals(22.0d,
                storage.latestValue(new ResourcePath("prov", "svc", "temp")).orElseThrow().getValue());
    }

    @Test
    void topicsUnionOverAllFilters() {
        IngestionPipeline pipeline = new IngestionPipeline("test", storage, null, null);
        pipeline.setFilters(List.of(
                new ConfiguredIngestFilter("a", List.of(), criterion(List.of("DATA/m1/*"), event -> true), null,
                        null),
                new ConfiguredIngestFilter("b", List.of(), criterion(List.of("DATA/m2/*", "DATA/m1/*"),
                        event -> true), null, null)));

        assertEquals(Set.of("DATA/m1/*", "DATA/m2/*"), pipeline.topics());
    }

    @Test
    void storageFailureIsContainedAndDoesNotPoisonLastStored() {
        IngestionPipeline pipeline = new IngestionPipeline("test", new InMemoryHistoryStorage(10) {
            @Override
            public void store(List<HistoricalRecord> records) {
                throw new IllegalStateException("backend down");
            }
        }, null, null);

        pipeline.handle(update("a", 1L, T0));
        assertTrue(true);
    }

    /** Recording step keeping what it receives, and optionally failing. */
    private static class CapturingStep implements HistoryRecordingStep {
        final List<HistoricalRecord> records = new ArrayList<>();
        RuntimeException failure;

        @Override
        public void record(HistoricalRecord record) {
            records.add(record);
            if (failure != null) {
                RuntimeException toThrow = failure;
                failure = null;
                throw toThrow;
            }
        }
    }

    private static ConfiguredIngestFilter deadbandFilter() {
        return new ConfiguredIngestFilter("db", List.of(), criterion(List.of("DATA/*"), event -> true), null,
                new ChangeCondition(Mode.DEADBAND, new BigDecimal("0.5"), null, null));
    }

    @Test
    void givenReplacementStepWhenUpdatesAreHandledThenStepReceivesEveryRecordInsteadOfStorage() {
        CapturingStep step = new CapturingStep();
        IngestionPipeline pipeline = new IngestionPipeline("test", storage, null, null);
        pipeline.replaceRecordingStep(step);

        pipeline.handle(update("a", 1L, T0));
        pipeline.handle(update("b", "x", T0));

        assertEquals(List.of("a", "b"), step.records.stream().map(r -> r.path().resource()).toList());
        assertEquals(0, count("a"));
        assertEquals(0, count("b"));
    }

    @Test
    void givenReplacementStepWhenUpdateIsFilteredOutThenStepDoesNotReceiveIt() {
        CapturingStep step = new CapturingStep();
        ICriterion exclude = criterion(null, event -> "drop".equals(event.resource()));
        IngestionPipeline pipeline = new IngestionPipeline("test", storage, null, exclude);
        pipeline.replaceRecordingStep(step);

        pipeline.handle(update("drop", 1L, T0));
        pipeline.handle(update("keep", 1L, T0));

        assertEquals(List.of("keep"), step.records.stream().map(r -> r.path().resource()).toList());
    }

    @Test
    void givenStepReturnsNormallyWhenValueIsWithinDeadbandThenItIsSuppressed() {
        CapturingStep step = new CapturingStep();
        IngestionPipeline pipeline = new IngestionPipeline("test", storage, null, null);
        pipeline.setFilters(List.of(deadbandFilter()));
        pipeline.replaceRecordingStep(step);

        pipeline.handle(update("temp", 21.0d, T0));
        pipeline.handle(update("temp", 21.1d, T0.plusSeconds(10)));

        assertEquals(1, step.records.size());
    }

    @Test
    void givenStepThrowsWhenNextValueIsWithinDeadbandThenItIsStillHandedOver() {
        CapturingStep step = new CapturingStep();
        step.failure = new IllegalStateException("step failed");
        IngestionPipeline pipeline = new IngestionPipeline("test", storage, null, null);
        pipeline.setFilters(List.of(deadbandFilter()));
        pipeline.replaceRecordingStep(step);

        pipeline.handle(update("temp", 21.0d, T0));
        pipeline.handle(update("temp", 21.1d, T0.plusSeconds(10)));

        assertEquals(2, step.records.size());
    }

    @Test
    void givenNewStepWhenItReplacesTheCurrentOneThenThePreviousIsReturnedAndOnlyTheNewOneRecords() {
        CapturingStep first = new CapturingStep();
        CapturingStep second = new CapturingStep();
        IngestionPipeline pipeline = new IngestionPipeline("test", storage, null, null);
        pipeline.replaceRecordingStep(first);

        HistoryRecordingStep previous = pipeline.replaceRecordingStep(second).retire();
        pipeline.handle(update("a", 1L, T0));

        assertSame(first, previous);
        assertEquals(0, first.records.size());
        assertEquals(1, second.records.size());
    }

    @Test
    void givenNoReplacementWhenStepIsReplacedThenTheBuiltInStepIsReturned() {
        IngestionPipeline pipeline = new IngestionPipeline("test", storage, null, null);

        HistoryRecordingStep previous = pipeline.replaceRecordingStep(new CapturingStep()).retire();

        assertInstanceOf(DirectRecordingStep.class, previous);
    }

    @Test
    void givenReplacementStepWhenDefaultIsRestoredThenRecordsGoToStorageAgain() {
        CapturingStep step = new CapturingStep();
        IngestionPipeline pipeline = new IngestionPipeline("test", storage, null, null);
        pipeline.replaceRecordingStep(step);

        HistoryRecordingStep previous = pipeline.replaceRecordingStep(null).retire();
        pipeline.handle(update("a", 1L, T0));

        assertSame(step, previous);
        assertEquals(0, step.records.size());
        assertEquals(1, count("a"));
    }

    @Test
    void givenRecordInProgressWhenStepIsRetiredThenRetirementWaitsForTheRecord() throws Exception {
        CountDownLatch inRecord = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        List<String> events = new CopyOnWriteArrayList<>();
        CapturingStep step = new CapturingStep() {
            @Override
            public void record(HistoricalRecord record) {
                inRecord.countDown();
                try {
                    events.add(release.await(5, TimeUnit.SECONDS) ? "record-end" : "record-timeout");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                super.record(record);
            }
        };
        IngestionPipeline pipeline = new IngestionPipeline("test", storage, null, null);
        pipeline.replaceRecordingStep(step);
        Thread handler = new Thread(() -> pipeline.handle(update("a", 1L, T0)));
        handler.start();
        assertTrue(inRecord.await(5, TimeUnit.SECONDS));

        RetiredStep replaced = pipeline.replaceRecordingStep(null);
        Thread retirer = new Thread(() -> {
            replaced.retire();
            events.add("retired");
        });
        retirer.start();
        retirer.join(300);
        release.countDown();
        retirer.join(5_000);
        handler.join(5_000);

        assertEquals(List.of("record-end", "retired"), events);
    }

    @Test
    void givenRecordInProgressWhenStepIsReplacedThenTheNextRecordGoesToTheNewStepAtOnce() throws Exception {
        CountDownLatch inRecord = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CapturingStep blocked = new CapturingStep() {
            @Override
            public void record(HistoricalRecord record) {
                inRecord.countDown();
                try {
                    release.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                super.record(record);
            }
        };
        CapturingStep next = new CapturingStep();
        IngestionPipeline pipeline = new IngestionPipeline("test", storage, null, null);
        pipeline.replaceRecordingStep(blocked);
        Thread handler = new Thread(() -> pipeline.handle(update("a", 1L, T0)));
        handler.start();
        assertTrue(inRecord.await(5, TimeUnit.SECONDS));

        pipeline.replaceRecordingStep(next);
        pipeline.handle(update("b", 1L, T0));

        // recorded while the previous step is still busy with its record
        assertEquals(1, next.records.size());
        assertEquals(0, blocked.records.size());
        release.countDown();
        handler.join(5_000);
        assertEquals(1, blocked.records.size());
    }
}

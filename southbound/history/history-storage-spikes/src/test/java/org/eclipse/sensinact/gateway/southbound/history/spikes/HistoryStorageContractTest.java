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
package org.eclipse.sensinact.gateway.southbound.history.spikes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.Feature;
import org.eclipse.sensinact.gateway.geojson.FeatureCollection;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.geojson.Polygon;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.AggregationQuery;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.AggregationQuery.Function;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoryPage;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoryStorage;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoryStorage.Capability;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.PruneRequest;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.SortOrder;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.ValueFilter;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.ValueFilter.Op;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.ValueKind;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Shared contract every candidate backend must pass. T1 pins lossless
 * round-trip fidelity (value AND Java type), T2 the query semantics needed
 * for parity with the legacy HistoricalQueries contract, T3 aggregation, T4
 * prune semantics for housekeeping.
 */
public abstract class HistoryStorageContractTest {

    protected static final ResourcePath PATH = new ResourcePath("testProvider", "testService", "testResource");
    protected static final Instant T0 = Instant.parse("2020-01-01T00:00:00Z");

    protected HistoryStorage storage;

    protected abstract HistoryStorage createStorage() throws Exception;

    @BeforeEach
    void setUpStorage() throws Exception {
        storage = createStorage();
    }

    @AfterEach
    void tearDownStorage() {
        if (storage != null) {
            storage.close();
        }
    }

    protected static HistoricalRecord record(ResourcePath path, Instant timestamp, ValueKind kind, Object value) {
        return new HistoricalRecord("https://eclipse.org/sensinact/test", "testModel", path, timestamp, kind, value);
    }

    protected void storeAt(Instant timestamp, ValueKind kind, Object value) {
        storage.store(List.of(record(PATH, timestamp, kind, value)));
    }

    protected Object readBack(Instant timestamp) {
        Optional<TimedValue<?>> read = storage.valueAt(PATH, timestamp);
        assertTrue(read.isPresent(), "no value stored at " + timestamp);
        assertEquals(timestamp, read.get().getTimestamp());
        return read.get().getValue();
    }

    static List<Object> roundTripValues() {
        return List.of(
                42L, Integer.valueOf(7), Short.valueOf((short) 3), Byte.valueOf((byte) 1),
                3.14d, 2.5f,
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                new BigDecimal("1.000000000000000000000000000001"),
                new BigDecimal("-987654321987654321.123456789"),
                new BigInteger("123456789123456789123456789"));
    }

    @Nested
    class T1RoundTripFidelity {

        @ParameterizedTest
        @MethodSource("org.eclipse.sensinact.gateway.southbound.history.spikes.HistoryStorageContractTest#roundTripValues")
        void numericValueAndTypeSurvive(Object value) {
            storeAt(T0, ValueKind.NUMBER, value);

            Object read = readBack(T0);

            assertEquals(value.getClass(), read.getClass());
            assertEquals(value, read);
        }

        @Test
        void booleanSurvives() {
            storeAt(T0, ValueKind.BOOLEAN, Boolean.TRUE);
            assertEquals(Boolean.TRUE, readBack(T0));
        }

        @Test
        void stringSurvives() {
            storeAt(T0, ValueKind.STRING, "on/off & \"quoted\" ünïcode ☃");
            assertEquals("on/off & \"quoted\" ünïcode ☃", readBack(T0));
        }

        @Test
        void nullValueSurvives() {
            storeAt(T0, ValueKind.STRING, null);
            assertNull(readBack(T0));
        }

        @Test
        void geoJsonPointSurvives() {
            Point point = new Point(5.7d, 12.3d);
            storeAt(T0, ValueKind.GEOJSON, point);
            assertEquals(point, readBack(T0));
        }

        @Test
        void geoJsonPolygonSurvives() {
            Polygon polygon = new Polygon(List.of(List.of(new Coordinates(0, 0), new Coordinates(0, 1),
                    new Coordinates(1, 1), new Coordinates(0, 0))), null, Map.of());
            storeAt(T0, ValueKind.GEOJSON, polygon);
            assertEquals(polygon, readBack(T0));
        }

        @Test
        void geoJsonFeatureCollectionSurvives() {
            Feature feature = new Feature("f1", new Point(5.7d, 12.3d),
                    Map.of("name", "sensor-1", "height", 12), null, Map.of());
            FeatureCollection collection = new FeatureCollection(List.of(feature), null, Map.of());

            storeAt(T0, ValueKind.GEOJSON, collection);

            assertEquals(collection, readBack(T0));
        }

        @Test
        void nestedObjectSurvives() {
            Map<String, Object> nested = Map.of("a", List.of(1, 2, 3), "b", Map.of("c", "d"), "e", true);
            storeAt(T0, ValueKind.OBJECT, nested);
            assertEquals(nested, readBack(T0));
        }
    }

    @Nested
    class T2QuerySemantics {

        void storeSequence(int count) {
            storage.store(IntStream.range(0, count)
                    .mapToObj(i -> record(PATH, T0.plusSeconds(i * 60L), ValueKind.NUMBER, (long) i)).toList());
        }

        @Test
        void valueAtReturnsExactOrPredecessor() {
            storeSequence(3);

            assertEquals(1L, storage.valueAt(PATH, T0.plusSeconds(60)).orElseThrow().getValue());
            assertEquals(1L, storage.valueAt(PATH, T0.plusSeconds(90)).orElseThrow().getValue());
            assertTrue(storage.valueAt(PATH, T0.minusSeconds(1)).isEmpty());
        }

        @Test
        void firstAndLatestValue() {
            storeSequence(5);

            assertEquals(0L, storage.firstValue(PATH).orElseThrow().getValue());
            assertEquals(4L, storage.latestValue(PATH).orElseThrow().getValue());
            assertTrue(storage.firstValue(new ResourcePath("no", "such", "path")).isEmpty());
        }

        @Test
        void countRespectsBoundInclusivity() {
            storeSequence(10);

            assertEquals(10, storage.count(PATH, TimeRange.ALL));
            assertEquals(3, storage.count(PATH, TimeRange.closed(T0, T0.plusSeconds(120))));
            assertEquals(2, storage.count(PATH, new TimeRange(T0, false, T0.plusSeconds(120), true)));
            assertEquals(1, storage.count(PATH, new TimeRange(T0, false, T0.plusSeconds(120), false)));
        }

        @Test
        void ascendingPagingWithOffsetAndHasMore() {
            storeSequence(10);

            HistoryPage page = storage.values(HistoryQuery.of(PATH, 4).withOffset(2));

            assertEquals(List.of(2L, 3L, 4L, 5L), page.values().stream().map(TimedValue::getValue).toList());
            assertTrue(page.hasMore());

            HistoryPage lastPage = storage.values(HistoryQuery.of(PATH, 4).withOffset(6));
            assertEquals(List.of(6L, 7L, 8L, 9L), lastPage.values().stream().map(TimedValue::getValue).toList());
            assertFalse(lastPage.hasMore());
        }

        @Test
        void descendingPaging() {
            storeSequence(10);

            HistoryPage page = storage.values(HistoryQuery.of(PATH, 3).withOrder(SortOrder.DESCENDING).withOffset(1));

            assertEquals(List.of(8L, 7L, 6L), page.values().stream().map(TimedValue::getValue).toList());
            assertTrue(page.hasMore());
        }

        @Test
        void rangeQueryHonorsExclusiveBounds() {
            storeSequence(10);

            HistoryPage page = storage.values(HistoryQuery.of(PATH, 100)
                    .withRange(new TimeRange(T0.plusSeconds(60), false, T0.plusSeconds(240), false)));

            assertEquals(List.of(2L, 3L), page.values().stream().map(TimedValue::getValue).toList());
        }

        @Test
        void emptyResultHasNoMore() {
            HistoryPage page = storage.values(HistoryQuery.of(PATH, 10));
            assertTrue(page.values().isEmpty());
            assertFalse(page.hasMore());
        }

        @Test
        void valueFilterSelectsMatchingRecords() {
            if (!storage.capabilities().contains(Capability.VALUE_FILTERING)) {
                return;
            }
            storeSequence(10);

            HistoryPage page = storage.values(HistoryQuery.of(PATH, 100).withValueFilter(ValueFilter.of(Op.GT, 6L)));

            assertEquals(List.of(7L, 8L, 9L), page.values().stream().map(TimedValue::getValue).toList());
        }

        @Test
        void valueFilterComparesWithBigDecimalPrecision() {
            if (!storage.capabilities().contains(Capability.VALUE_FILTERING)) {
                return;
            }
            storeAt(T0, ValueKind.NUMBER, new BigDecimal("1.000000000000000000000000000001"));
            storeAt(T0.plusSeconds(60), ValueKind.NUMBER, new BigDecimal("1.000000000000000000000000000002"));

            HistoryPage page = storage.values(HistoryQuery.of(PATH, 100)
                    .withValueFilter(ValueFilter.of(Op.GT, new BigDecimal("1.000000000000000000000000000001"))));

            assertEquals(1, page.values().size());
        }
    }

    @Nested
    class T3Aggregation {

        @Test
        void numericBucketsAggregateAndNonNumericsAreCounted() {
            if (!storage.capabilities().contains(Capability.AGGREGATION)) {
                return;
            }
            storage.store(List.of(
                    record(PATH, T0.plusSeconds(10), ValueKind.NUMBER, 10L),
                    record(PATH, T0.plusSeconds(20), ValueKind.NUMBER, 20L),
                    record(PATH, T0.plusSeconds(30), ValueKind.STRING, "not a number"),
                    record(PATH, T0.plusSeconds(70), ValueKind.NUMBER, 40L)));

            List<AggregationQuery.Bucket> buckets = storage.aggregate(new AggregationQuery(PATH, TimeRange.ALL,
                    Duration.ofMinutes(1), Set.of(Function.MIN, Function.MAX, Function.AVG, Function.COUNT)));

            assertEquals(2, buckets.size());

            AggregationQuery.Bucket first = buckets.get(0);
            assertEquals(T0, first.bucketStart());
            assertEquals(3L, first.results().get(Function.COUNT));
            assertEquals(0, new BigDecimal("10").compareTo((BigDecimal) first.results().get(Function.MIN)));
            assertEquals(0, new BigDecimal("20").compareTo((BigDecimal) first.results().get(Function.MAX)));
            assertEquals(0, new BigDecimal("15").compareTo((BigDecimal) first.results().get(Function.AVG)));

            AggregationQuery.Bucket second = buckets.get(1);
            assertEquals(T0.plusSeconds(60), second.bucketStart());
            assertEquals(1L, second.results().get(Function.COUNT));
        }
    }

    @Nested
    class T4Prune {

        void storeSequence(ResourcePath path, int count) {
            storage.store(IntStream.range(0, count)
                    .mapToObj(i -> record(path, T0.plusSeconds(i * 60L), ValueKind.NUMBER, (long) i)).toList());
        }

        @Test
        void pruneByAge() {
            storeSequence(PATH, 10);

            long deleted = storage.prune(new PruneRequest(null, T0.plusSeconds(5 * 60), null));

            assertEquals(5, deleted);
            assertEquals(5, storage.count(PATH, TimeRange.ALL));
            assertEquals(5L, storage.firstValue(PATH).orElseThrow().getValue());
        }

        @Test
        void pruneByKeepCount() {
            storeSequence(PATH, 10);

            long deleted = storage.prune(new PruneRequest(null, null, 3L));

            assertEquals(7, deleted);
            assertEquals(List.of(7L, 8L, 9L),
                    storage.values(HistoryQuery.of(PATH, 10)).values().stream().map(TimedValue::getValue).toList());
        }

        @Test
        void pruneScopedToResourceLeavesOthersAlone() {
            ResourcePath other = new ResourcePath("otherProvider", "otherService", "otherResource");
            storeSequence(PATH, 4);
            storeSequence(other, 4);

            long deleted = storage.prune(new PruneRequest(PATH, T0.plusSeconds(1000), null));

            assertEquals(4, deleted);
            assertEquals(0, storage.count(PATH, TimeRange.ALL));
            assertEquals(4, storage.count(other, TimeRange.ALL));
        }

        @Test
        void pruneCombinedBoundsDeleteUnionOfBoth() {
            storeSequence(PATH, 10);

            long deleted = storage.prune(new PruneRequest(null, T0.plusSeconds(2 * 60), 5L));

            assertEquals(5, deleted);
            assertEquals(List.of(5L, 6L, 7L, 8L, 9L),
                    storage.values(HistoryQuery.of(PATH, 10)).values().stream().map(TimedValue::getValue).toList());
        }
    }
}

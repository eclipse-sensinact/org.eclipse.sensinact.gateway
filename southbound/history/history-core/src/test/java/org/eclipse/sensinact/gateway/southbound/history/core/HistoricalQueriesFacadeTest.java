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
package org.eclipse.sensinact.gateway.southbound.history.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.IntStream;

import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.southbound.history.inmemory.InMemoryHistoryStorage;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.storage.ValueKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pins the facade against the legacy ACT semantics captured in
 * TimescaleHistoryTest and the golden-response ITs: the 500-row page with its
 * asymmetric empty-marker convention, skip-from-the-end without a start time,
 * and the legacy numeric narrowing.
 */
class HistoricalQueriesFacadeTest {

    private static final ResourcePath PATH = new ResourcePath("testProvider", "testService", "testResource");
    private static final Instant T0 = Instant.parse("2020-01-01T00:00:00Z");

    private InMemoryHistoryStorage storage;
    private HistoricalQueriesFacade facade;

    @BeforeEach
    void setUp() {
        storage = new InMemoryHistoryStorage(10_000);
        facade = new HistoricalQueriesFacade(new HistoryProviderEngine("test", storage));
    }

    private void storeLongs(int count) {
        storage.store(IntStream.range(0, count)
                .mapToObj(i -> record(T0.plusSeconds(i * 60L), ValueKind.NUMBER, (long) i)).toList());
    }

    private HistoricalRecord record(Instant timestamp, ValueKind kind, Object value) {
        return new HistoricalRecord("https://eclipse.org/sensinact/test", "testModel", PATH, timestamp, kind, value);
    }

    private static ZonedDateTime zdt(Instant instant) {
        return instant.atZone(ZoneOffset.UTC);
    }

    @Test
    void rangeWithStartTimeAppendsEmptyMarkerWhenMoreDataExists() {
        storeLongs(600);

        List<TimedValue<?>> result = facade.getValueRange("testProvider", "testService", "testResource", zdt(T0),
                null, null);

        assertEquals(501, result.size());
        for (int i = 0; i < 500; i++) {
            assertEquals((long) i, result.get(i).getValue());
        }
        assertNull(result.get(500).getValue());
        assertNull(result.get(500).getTimestamp());
    }

    @Test
    void rangeWithStartTimeAndNoMoreDataHasNoMarker() {
        storeLongs(300);

        List<TimedValue<?>> result = facade.getValueRange("testProvider", "testService", "testResource", zdt(T0),
                null, null);

        assertEquals(300, result.size());
    }

    @Test
    void rangeWithoutStartTimeReturnsNewest500ChronologicallyWithoutMarker() {
        storeLongs(600);

        List<TimedValue<?>> result = facade.getValueRange("testProvider", "testService", "testResource", null, null,
                null);

        assertEquals(500, result.size());
        assertEquals(100L, result.get(0).getValue());
        assertEquals(599L, result.get(499).getValue());
    }

    @Test
    void rangeWithoutStartTimeSkipsFromTheEnd() {
        storeLongs(600);

        List<TimedValue<?>> result = facade.getValueRange("testProvider", "testService", "testResource", null, null,
                150);

        assertEquals(450, result.size());
        assertEquals(0L, result.get(0).getValue());
        assertEquals(449L, result.get(449).getValue());
    }

    @Test
    void rangeWithStartTimeSkipsFromTheStart() {
        storeLongs(600);

        List<TimedValue<?>> result = facade.getValueRange("testProvider", "testService", "testResource", zdt(T0),
                null, 550);

        assertEquals(50, result.size());
        assertEquals(550L, result.get(0).getValue());
    }

    @Test
    void rangeBoundsAreInclusive() {
        storeLongs(10);

        List<TimedValue<?>> result = facade.getValueRange("testProvider", "testService", "testResource", zdt(T0),
                zdt(T0.plusSeconds(120)), null);

        assertEquals(3, result.size());
    }

    @Test
    void singleReturnsValueAtOrBefore() {
        storeLongs(3);

        TimedValue<?> exact = facade.getSingleValue("testProvider", "testService", "testResource",
                zdt(T0.plusSeconds(60)));
        TimedValue<?> between = facade.getSingleValue("testProvider", "testService", "testResource",
                zdt(T0.plusSeconds(90)));

        assertEquals(1L, exact.getValue());
        assertEquals(1L, between.getValue());
    }

    @Test
    void singleWithoutTimeReturnsEarliestValue() {
        storeLongs(3);

        assertEquals(0L,
                facade.getSingleValue("testProvider", "testService", "testResource", null).getValue());
    }

    @Test
    void singleWithoutDataReturnsEmptyTimedValue() {
        TimedValue<?> result = facade.getSingleValue("testProvider", "testService", "testResource", zdt(T0));

        assertNull(result.getValue());
        assertNull(result.getTimestamp());
    }

    @Test
    void countUsesInclusiveBounds() {
        storeLongs(10);

        assertEquals(10, facade.getStoredValueCount("testProvider", "testService", "testResource", null, null));
        assertEquals(3, facade.getStoredValueCount("testProvider", "testService", "testResource", zdt(T0),
                zdt(T0.plusSeconds(120))));
    }

    @Test
    void numbersAreNarrowedLikeTheLegacyStore() {
        storage.store(List.of(
                record(T0, ValueKind.NUMBER, 42),
                record(T0.plusSeconds(60), ValueKind.NUMBER, new BigDecimal("4.2")),
                record(T0.plusSeconds(120), ValueKind.NUMBER, new BigDecimal("17")),
                record(T0.plusSeconds(180), ValueKind.NUMBER, Double.NaN),
                record(T0.plusSeconds(240), ValueKind.STRING, "text")));

        List<TimedValue<?>> result = facade.getValueRange("testProvider", "testService", "testResource", zdt(T0),
                null, null);

        assertEquals(42L, result.get(0).getValue());
        assertEquals(4.2d, result.get(1).getValue());
        assertEquals(17L, result.get(2).getValue());
        assertEquals(Double.NaN, result.get(3).getValue());
        assertEquals("text", result.get(4).getValue());
    }
}

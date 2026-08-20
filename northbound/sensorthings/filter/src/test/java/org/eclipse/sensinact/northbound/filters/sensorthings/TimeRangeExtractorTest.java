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
package org.eclipse.sensinact.northbound.filters.sensorthings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TimeRangeExtractorTest {

    private static final Set<String> OBSERVATION_TIME_FIELDS = Set.of("phenomenonTime", "resultTime");

    private static final Instant T1 = Instant.parse("2014-07-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2014-07-10T00:00:00Z");

    private TimeRange extract(String filter) {
        Optional<TimeRange> range = TimeRangeExtractor.extract(filter, OBSERVATION_TIME_FIELDS);
        assertTrue(range.isPresent(), "Expected a time range for: " + filter);
        return range.get();
    }

    private void assertNotExtractable(String filter) {
        assertTrue(TimeRangeExtractor.extract(filter, OBSERVATION_TIME_FIELDS).isEmpty(),
                "Expected no time range for: " + filter);
    }

    @Test
    void lowerBoundExclusive() {
        TimeRange range = extract("phenomenonTime gt 2014-07-01T00:00:00Z");
        assertEquals(new TimeRange(T1, false, null, true), range);
    }

    @Test
    void lowerBoundInclusive() {
        TimeRange range = extract("phenomenonTime ge 2014-07-01T00:00:00Z");
        assertEquals(new TimeRange(T1, true, null, true), range);
    }

    @Test
    void upperBoundExclusive() {
        TimeRange range = extract("resultTime lt 2014-07-10T00:00:00Z");
        assertEquals(new TimeRange(null, true, T2, false), range);
    }

    @Test
    void upperBoundInclusive() {
        TimeRange range = extract("resultTime le 2014-07-10T00:00:00Z");
        assertEquals(new TimeRange(null, true, T2, true), range);
    }

    @Test
    void equalityBecomesAPointRange() {
        TimeRange range = extract("phenomenonTime eq 2014-07-01T00:00:00Z");
        assertEquals(new TimeRange(T1, true, T1, true), range);
    }

    @Test
    void conjunctionIntersectsTheBounds() {
        TimeRange range = extract("phenomenonTime gt 2014-07-01T00:00:00Z and phenomenonTime lt 2014-07-10T00:00:00Z");
        assertEquals(new TimeRange(T1, false, T2, false), range);
    }

    @Test
    void mixedTimeFieldsIntersect() {
        TimeRange range = extract("phenomenonTime ge 2014-07-01T00:00:00Z and resultTime le 2014-07-10T00:00:00Z");
        assertEquals(new TimeRange(T1, true, T2, true), range);
    }

    @Test
    void tightestLowerBoundWins() {
        TimeRange range = extract("phenomenonTime gt 2014-07-01T00:00:00Z and phenomenonTime ge 2014-07-10T00:00:00Z");
        assertEquals(new TimeRange(T2, true, null, true), range);
    }

    @Test
    void exclusiveWinsOnEqualBounds() {
        TimeRange range = extract("phenomenonTime ge 2014-07-01T00:00:00Z and phenomenonTime gt 2014-07-01T00:00:00Z");
        assertEquals(new TimeRange(T1, false, null, true), range);
    }

    @Test
    void parenthesesAreTransparent() {
        TimeRange range = extract(
                "(phenomenonTime gt 2014-07-01T00:00:00Z) and (phenomenonTime lt 2014-07-10T00:00:00Z)");
        assertEquals(new TimeRange(T1, false, T2, false), range);
    }

    @Test
    void threeClauseConjunction() {
        TimeRange range = extract("phenomenonTime gt 2014-07-01T00:00:00Z"
                + " and phenomenonTime lt 2014-07-10T00:00:00Z and resultTime le 2014-07-10T00:00:00Z");
        assertEquals(new TimeRange(T1, false, T2, false), range);
    }

    @Test
    void offsetLiteralIsNormalizedToUtc() {
        TimeRange range = extract("phenomenonTime ge 2014-07-01T02:00:00+02:00");
        assertEquals(new TimeRange(T1, true, null, true), range);
    }

    @Test
    void contradictoryBoundsMatchNothing() {
        TimeRange range = extract("phenomenonTime gt 2014-07-10T00:00:00Z and phenomenonTime lt 2014-07-01T00:00:00Z");
        assertFalse(range.contains(T1));
        assertFalse(range.contains(T2));
        assertFalse(range.contains(Instant.parse("2014-07-05T00:00:00Z")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "result eq 'value1'",
            "phenomenonTime gt 2014-07-01T00:00:00Z or phenomenonTime lt 2014-07-10T00:00:00Z",
            "phenomenonTime gt 2014-07-01T00:00:00Z and result ge 10",
            "not phenomenonTime gt 2014-07-01T00:00:00Z",
            "phenomenonTime ne 2014-07-01T00:00:00Z",
            "validTime gt 2014-07-01T00:00:00Z",
            "2014-07-01T00:00:00Z lt phenomenonTime",
            "phenomenonTime gt 42",
            "year(phenomenonTime) eq 2014",
            "phenomenonTime",
            "phenomenonTime gt now()",
            "" })
    void nonReducibleFiltersYieldNothing(String filter) {
        assertNotExtractable(filter);
    }
}

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
package org.eclipse.sensinact.gateway.southbound.history.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class TimeRangeTest {

    private static final Instant T = Instant.parse("2020-06-15T10:30:00Z");

    @Test
    void allContainsEverything() {
        assertTrue(TimeRange.ALL.contains(Instant.MIN));
        assertTrue(TimeRange.ALL.contains(T));
        assertTrue(TimeRange.ALL.contains(Instant.MAX));
    }

    @Test
    void closedRangeIncludesBothBounds() {
        TimeRange range = TimeRange.closed(T, T.plusSeconds(10));

        assertTrue(range.contains(T));
        assertTrue(range.contains(T.plusSeconds(10)));
        assertFalse(range.contains(T.minusNanos(1)));
        assertFalse(range.contains(T.plusSeconds(10).plusNanos(1)));
    }

    @Test
    void exclusiveBoundsExcludeTheBoundInstant() {
        TimeRange range = new TimeRange(T, false, T.plusSeconds(10), false);

        assertFalse(range.contains(T));
        assertFalse(range.contains(T.plusSeconds(10)));
        assertTrue(range.contains(T.plusSeconds(5)));
    }

    @Test
    void invertedBoundsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> TimeRange.closed(T.plusSeconds(1), T));
    }

    @Test
    void millisecondOfMatchesEveryMicrosecondOfThatMillisecond() {
        Instant milliTruncated = Instant.parse("2020-06-15T10:30:00.123Z");
        TimeRange range = TimeRange.millisecondOf(milliTruncated.plusNanos(456_789));

        assertEquals(milliTruncated, range.from());
        assertTrue(range.contains(milliTruncated));
        assertTrue(range.contains(milliTruncated.plusNanos(999_999)));
        assertFalse(range.contains(milliTruncated.plusMillis(1)));
        assertFalse(range.contains(milliTruncated.minusNanos(1)));
    }
}

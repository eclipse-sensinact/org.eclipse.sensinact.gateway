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

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Time interval with explicit bound inclusivity. A {@code null} bound is
 * unbounded. Bounds carry the full precision of {@link Instant} — consumers
 * matching a millisecond-truncated timestamp against higher-precision storage
 * should use {@link #millisecondOf(Instant)} instead of arithmetic tricks.
 */
public record TimeRange(Instant from, boolean fromInclusive, Instant to, boolean toInclusive) {

    public static final TimeRange ALL = new TimeRange(null, true, null, true);

    public TimeRange {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must not be after to");
        }
    }

    /** Both bounds inclusive; either may be null (unbounded). */
    public static TimeRange closed(Instant from, Instant to) {
        return new TimeRange(from, true, to, true);
    }

    public static TimeRange atOrBefore(Instant to) {
        return new TimeRange(null, true, to, true);
    }

    public static TimeRange atOrAfter(Instant from) {
        return new TimeRange(from, true, null, true);
    }

    /**
     * The half-open range {@code [t truncated to milliseconds, +1ms)}: matches
     * every stored timestamp that falls into the same millisecond as
     * {@code t}, regardless of the storage's sub-millisecond precision.
     */
    public static TimeRange millisecondOf(Instant t) {
        Instant millisecond = t.truncatedTo(ChronoUnit.MILLIS);
        return new TimeRange(millisecond, true, millisecond.plusMillis(1), false);
    }

    public boolean contains(Instant t) {
        if (from != null && (fromInclusive ? t.isBefore(from) : !t.isAfter(from))) {
            return false;
        }
        if (to != null && (toInclusive ? t.isAfter(to) : !t.isBefore(to))) {
            return false;
        }
        return true;
    }
}

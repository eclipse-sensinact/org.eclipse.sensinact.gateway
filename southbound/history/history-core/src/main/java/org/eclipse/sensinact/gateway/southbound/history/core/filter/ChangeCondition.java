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
package org.eclipse.sensinact.gateway.southbound.history.core.filter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.storage.ValueKind;

/**
 * Change-based suppression for the historization filter: decides whether an
 * update changed enough compared to the last <em>stored</em> value. The first
 * update of a resource is always stored; an optional heartbeat interval
 * bounds the gap flat signals can produce. Deadband applies to numeric
 * values; all other kinds degrade to on-change equality.
 */
public class ChangeCondition {

    public enum Mode {
        ALL, ON_CHANGE, DEADBAND
    }

    private final Mode mode;
    private final BigDecimal threshold;
    private final BigDecimal thresholdPercent;
    private final Duration maxInterval;

    public ChangeCondition(Mode mode, BigDecimal threshold, BigDecimal thresholdPercent, Duration maxInterval) {
        this.mode = Objects.requireNonNull(mode, "mode must not be null");
        if (mode == Mode.DEADBAND && threshold == null && thresholdPercent == null) {
            throw new IllegalArgumentException("deadband requires change.threshold or change.threshold.percent");
        }
        if (threshold != null && thresholdPercent != null) {
            throw new IllegalArgumentException("only one of change.threshold/change.threshold.percent may be set");
        }
        this.threshold = threshold;
        this.thresholdPercent = thresholdPercent;
        this.maxInterval = maxInterval;
    }

    public static final ChangeCondition STORE_ALL = new ChangeCondition(Mode.ALL, null, null, null);

    public boolean shouldStore(HistoricalRecord record, Optional<TimedValue<?>> lastStored) {
        if (mode == Mode.ALL) {
            return true;
        }
        if (lastStored.isEmpty()) {
            return true;
        }
        TimedValue<?> last = lastStored.get();
        if (heartbeatExpired(record, last)) {
            return true;
        }
        if (mode == Mode.ON_CHANGE || record.kind() != ValueKind.NUMBER) {
            return !Objects.equals(record.value(), last.getValue());
        }
        return exceedsDeadband(record.value(), last.getValue());
    }

    private boolean heartbeatExpired(HistoricalRecord record, TimedValue<?> last) {
        return maxInterval != null && last.getTimestamp() != null
                && !record.timestamp().isBefore(last.getTimestamp().plus(maxInterval));
    }

    private boolean exceedsDeadband(Object newValue, Object lastValue) {
        BigDecimal current = toBigDecimal(newValue);
        BigDecimal previous = toBigDecimal(lastValue);
        if (current == null || previous == null) {
            return !Objects.equals(newValue, lastValue);
        }
        BigDecimal delta = current.subtract(previous).abs();
        BigDecimal limit = threshold != null ? threshold
                : previous.abs().multiply(thresholdPercent).divide(BigDecimal.valueOf(100));
        return delta.compareTo(limit) >= 0;
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Double d) {
            return d.isNaN() || d.isInfinite() ? null : BigDecimal.valueOf(d);
        }
        if (value instanceof Float f) {
            return f.isNaN() || f.isInfinite() ? null : new BigDecimal(f.toString());
        }
        if (value instanceof BigInteger bi) {
            return new BigDecimal(bi);
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.longValue());
        }
        return null;
    }
}

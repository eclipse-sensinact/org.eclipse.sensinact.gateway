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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.southbound.history.api.HistoricalQueries;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryPage;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryProvider;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.SortOrder;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;

/**
 * Bit-identical implementation of the legacy ACT contract on top of a
 * {@link HistoryProvider}. The quirks below are contract, pinned by
 * TimescaleHistoryTest and the golden-response ITs:
 *
 * - range with fromTime returns at most 500 values plus an empty 501st
 *   marker when more data exists;
 * - range without fromTime returns the newest 500 values in chronological
 *   order, skip counts from the end, and there is NO marker;
 * - numbers are narrowed the way the legacy store read them back:
 *   scale <= 0 becomes Long, everything else double, NaN/±Infinity stay
 *   Double.
 */
@SuppressWarnings("deprecation")
public class HistoricalQueriesFacade implements HistoricalQueries {

    private static final int PAGE_LIMIT = 500;

    private final HistoryProvider provider;

    public HistoricalQueriesFacade(HistoryProvider provider) {
        this.provider = Objects.requireNonNull(provider, "provider must not be null");
    }

    @Override
    public TimedValue<?> getSingleValue(String provider, String service, String resource, ZonedDateTime time) {
        ResourcePath path = new ResourcePath(provider, service, resource);
        var value = time == null ? this.provider.getFirstValue(path)
                : this.provider.getValueAt(path, time.toInstant());
        return value.map(HistoricalQueriesFacade::narrowed).orElseGet(DefaultTimedValue::new);
    }

    @Override
    public List<TimedValue<?>> getValueRange(String provider, String service, String resource, ZonedDateTime fromTime,
            ZonedDateTime toTime, Integer skip) {
        ResourcePath path = new ResourcePath(provider, service, resource);
        long offset = skip == null ? 0 : skip;

        if (fromTime != null) {
            TimeRange range = TimeRange.closed(fromTime.toInstant(), toTime == null ? null : toTime.toInstant());
            HistoryPage page = this.provider.getValues(HistoryQuery.builder(path).range(range)
                    .order(SortOrder.ASCENDING).offset(offset).limit(PAGE_LIMIT).build());

            List<TimedValue<?>> result = new ArrayList<>(narrowed(page.values()));
            if (page.hasMore()) {
                result.add(new DefaultTimedValue<>());
            }
            return result;
        }

        TimeRange range = toTime == null ? TimeRange.ALL : TimeRange.atOrBefore(toTime.toInstant());
        HistoryPage page = this.provider.getValues(HistoryQuery.builder(path).range(range)
                .order(SortOrder.DESCENDING).offset(offset).limit(PAGE_LIMIT).build());

        List<TimedValue<?>> result = new ArrayList<>(narrowed(page.values()));
        Collections.reverse(result);
        return result;
    }

    @Override
    public Long getStoredValueCount(String provider, String service, String resource, ZonedDateTime fromTime,
            ZonedDateTime toTime) {
        ResourcePath path = new ResourcePath(provider, service, resource);
        TimeRange range = TimeRange.closed(fromTime == null ? null : fromTime.toInstant(),
                toTime == null ? null : toTime.toInstant());
        return this.provider.getValueCount(path, range);
    }

    private static List<TimedValue<?>> narrowed(List<TimedValue<?>> values) {
        return values.stream().<TimedValue<?>>map(HistoricalQueriesFacade::narrowed).toList();
    }

    private static TimedValue<?> narrowed(TimedValue<?> value) {
        Object narrowed = narrowValue(value.getValue());
        return narrowed == value.getValue() ? value : new DefaultTimedValue<>(narrowed, value.getTimestamp());
    }

    /**
     * Legacy numeric read shape: the old store persisted every number as SQL
     * NUMERIC and read it back as Long (scale <= 0) or double, keeping
     * NaN/±Infinity as Double.
     */
    private static Object narrowValue(Object value) {
        if (!(value instanceof Number number)) {
            return value;
        }
        if (value instanceof Double d && (d.isNaN() || d.isInfinite())) {
            return d;
        }
        if (value instanceof Float f && (f.isNaN() || f.isInfinite())) {
            return f.doubleValue();
        }
        BigDecimal decimal;
        if (number instanceof BigDecimal bd) {
            decimal = bd;
        } else if (number instanceof Double || number instanceof Float) {
            decimal = BigDecimal.valueOf(number.doubleValue());
        } else if (number instanceof BigInteger bi) {
            decimal = new BigDecimal(bi);
        } else {
            decimal = BigDecimal.valueOf(number.longValue());
        }
        return decimal.scale() <= 0 ? (Object) decimal.longValueExact() : (Object) decimal.doubleValue();
    }
}

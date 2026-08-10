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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.junit.jupiter.api.Test;

class HistoryProviderStreamTest {

    private static final ResourcePath PATH = new ResourcePath("p", "s", "r");
    private static final Instant T0 = Instant.parse("2020-01-01T00:00:00Z");

    /** Minimal provider over a fixed list, paging with maxPageSize 4. */
    static class ListBackedProvider implements HistoryProvider {

        final List<TimedValue<?>> data = IntStream.range(0, 10)
                .<TimedValue<?>>mapToObj(i -> new DefaultTimedValue<>((long) i, T0.plusSeconds(i))).toList();
        int pagesServed = 0;

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public Set<HistoryCapability> getCapabilities() {
            return Set.of();
        }

        @Override
        public Optional<TimedValue<?>> getValueAt(ResourcePath path, Instant at) {
            return Optional.empty();
        }

        @Override
        public Optional<TimedValue<?>> getFirstValue(ResourcePath path) {
            return Optional.empty();
        }

        @Override
        public Optional<TimedValue<?>> getLatestValue(ResourcePath path) {
            return Optional.empty();
        }

        @Override
        public long getValueCount(ResourcePath path, TimeRange range) {
            return data.size();
        }

        @Override
        public HistoryPage getValues(HistoryQuery query) {
            pagesServed++;
            int from = (int) Math.min(query.offset(), data.size());
            int to = Math.min(from + getMaxPageSize(), data.size());
            return HistoryPage.of(data.subList(from, to), query.offset(), to < data.size());
        }

        @Override
        public int getMaxPageSize() {
            return 4;
        }

        @Override
        public List<AggregateBucket> aggregate(AggregationQuery query) {
            throw new UnsupportedHistoryOperationException(HistoryCapability.AGGREGATION);
        }
    }

    @Test
    void streamValuesPagesThroughAllResults() {
        ListBackedProvider provider = new ListBackedProvider();

        List<Object> values = provider.streamValues(HistoryQuery.builder(PATH).build(), Long.MAX_VALUE)
                .<Object>map(TimedValue::getValue).toList();

        assertEquals(LongStream.range(0, 10).boxed().toList(), values);
        assertEquals(3, provider.pagesServed);
    }

    @Test
    void streamValuesStopsAtMaxTotal() {
        ListBackedProvider provider = new ListBackedProvider();

        List<Object> values = provider.streamValues(HistoryQuery.builder(PATH).build(), 6)
                .<Object>map(TimedValue::getValue).toList();

        assertEquals(LongStream.range(0, 6).boxed().toList(), values);
        assertEquals(2, provider.pagesServed);
    }

    @Test
    void streamValuesStartsAtQueryOffset() {
        ListBackedProvider provider = new ListBackedProvider();

        List<Object> values = provider.streamValues(HistoryQuery.builder(PATH).offset(7).build(), Long.MAX_VALUE)
                .<Object>map(TimedValue::getValue).toList();

        assertEquals(LongStream.range(7, 10).boxed().toList(), values);
    }

    @Test
    void streamValuesOnEmptyHistoryIsEmpty() {
        ListBackedProvider provider = new ListBackedProvider() {
            @Override
            public HistoryPage getValues(HistoryQuery query) {
                pagesServed++;
                return HistoryPage.of(List.of(), query.offset(), false);
            }
        };

        assertEquals(0, provider.streamValues(HistoryQuery.builder(PATH).build(), Long.MAX_VALUE).count());
        assertEquals(1, provider.pagesServed);
    }
}

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
package org.eclipse.sensinact.gateway.southbound.history.spikes.inmemory;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.AggregationQuery;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.AggregationQuery.Bucket;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.AggregationQuery.Function;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoryPage;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoryStorage;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.PruneRequest;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.SortOrder;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.ValueFilter;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.ValueKind;

/**
 * Reference implementation: validates the contract test itself and serves as
 * the behavioral baseline for candidate adapters.
 */
public class InMemoryHistoryStorage implements HistoryStorage {

    private record Stored(ValueKind kind, Object value) {
    }

    private final Map<ResourcePath, NavigableMap<Instant, Stored>> data = new ConcurrentHashMap<>();

    @Override
    public Set<Capability> capabilities() {
        return Set.of(Capability.AGGREGATION, Capability.VALUE_FILTERING);
    }

    @Override
    public int maxPageSize() {
        return 10_000;
    }

    @Override
    public void store(List<HistoricalRecord> records) {
        for (HistoricalRecord record : records) {
            data.computeIfAbsent(record.path(), p -> new TreeMap<>()).put(record.timestamp(),
                    new Stored(record.kind(), record.value()));
        }
    }

    @Override
    public Optional<TimedValue<?>> valueAt(ResourcePath path, Instant at) {
        NavigableMap<Instant, Stored> series = data.getOrDefault(path, new TreeMap<>());
        Map.Entry<Instant, Stored> entry = series.floorEntry(at);
        return toTimedValue(entry);
    }

    @Override
    public Optional<TimedValue<?>> firstValue(ResourcePath path) {
        return toTimedValue(data.getOrDefault(path, new TreeMap<>()).firstEntry());
    }

    @Override
    public Optional<TimedValue<?>> latestValue(ResourcePath path) {
        return toTimedValue(data.getOrDefault(path, new TreeMap<>()).lastEntry());
    }

    @Override
    public long count(ResourcePath path, TimeRange range) {
        return matching(path, range, null).count();
    }

    @Override
    public HistoryPage values(HistoryQuery query) {
        int limit = Math.min(query.limit(), maxPageSize());
        Comparator<Map.Entry<Instant, Stored>> byTime = Map.Entry.comparingByKey();
        if (query.order() == SortOrder.DESCENDING) {
            byTime = byTime.reversed();
        }
        List<Map.Entry<Instant, Stored>> all = matching(query.path(), query.range(), query.valueFilter())
                .sorted(byTime).skip(query.offset()).limit(limit + 1L).toList();

        boolean hasMore = all.size() > limit;
        List<TimedValue<?>> values = all.stream().limit(limit)
                .<TimedValue<?>>map(e -> new DefaultTimedValue<>(e.getValue().value(), e.getKey())).toList();
        return new HistoryPage(values, query.offset(), hasMore);
    }

    @Override
    public List<Bucket> aggregate(AggregationQuery query) {
        Map<Instant, List<Map.Entry<Instant, Stored>>> buckets = new TreeMap<>();
        matching(query.path(), query.range(), null)
                .forEach(e -> buckets.computeIfAbsent(bucketStart(e.getKey(), query.bucketSize()),
                        b -> new ArrayList<>()).add(e));

        List<Bucket> result = new ArrayList<>();
        for (Map.Entry<Instant, List<Map.Entry<Instant, Stored>>> bucket : buckets.entrySet()) {
            List<BigDecimal> numbers = bucket.getValue().stream().map(e -> toBigDecimal(e.getValue().value()))
                    .filter(Objects::nonNull).toList();
            Map<Function, Object> results = new EnumMap<>(Function.class);
            for (Function fn : query.functions()) {
                results.put(fn, switch (fn) {
                case COUNT -> (long) bucket.getValue().size();
                case MIN -> numbers.stream().min(Comparator.naturalOrder()).orElse(null);
                case MAX -> numbers.stream().max(Comparator.naturalOrder()).orElse(null);
                case SUM -> numbers.isEmpty() ? null : numbers.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                case AVG -> numbers.isEmpty() ? null
                        : numbers.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                                .divide(BigDecimal.valueOf(numbers.size()), java.math.MathContext.DECIMAL64);
                });
            }
            result.add(new Bucket(bucket.getKey(), results));
        }
        return result;
    }

    @Override
    public long prune(PruneRequest request) {
        long deleted = 0;
        for (Map.Entry<ResourcePath, NavigableMap<Instant, Stored>> series : data.entrySet()) {
            if (request.scope() != null && !request.scope().equals(series.getKey())) {
                continue;
            }
            NavigableMap<Instant, Stored> values = series.getValue();
            List<Instant> ordered = new ArrayList<>(values.descendingKeySet());
            for (int i = 0; i < ordered.size(); i++) {
                Instant t = ordered.get(i);
                boolean tooOld = request.olderThan() != null && t.isBefore(request.olderThan());
                boolean beyondKeep = request.keepLatestPerResource() != null && i >= request.keepLatestPerResource();
                if (tooOld || beyondKeep) {
                    values.remove(t);
                    deleted++;
                }
            }
        }
        return deleted;
    }

    @Override
    public void close() {
        data.clear();
    }

    private java.util.stream.Stream<Map.Entry<Instant, Stored>> matching(ResourcePath path, TimeRange range,
            ValueFilter filter) {
        return data.getOrDefault(path, new TreeMap<>()).entrySet().stream()
                .filter(e -> range.contains(e.getKey()))
                .filter(e -> filter == null || matches(filter, e.getValue().value()));
    }

    private static boolean matches(ValueFilter filter, Object value) {
        return filter.conditions().stream().allMatch(c -> matches(c, value));
    }

    private static boolean matches(ValueFilter.Condition condition, Object value) {
        if (value == null) {
            return false;
        }
        BigDecimal left = toBigDecimal(value);
        BigDecimal right = toBigDecimal(condition.literal());
        int cmp;
        if (left != null && right != null) {
            cmp = left.compareTo(right);
        } else if (value instanceof Comparable && value.getClass() == condition.literal().getClass()) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            int c = ((Comparable) value).compareTo(condition.literal());
            cmp = c;
        } else {
            return switch (condition.op()) {
            case EQ -> value.equals(condition.literal());
            case NE -> !value.equals(condition.literal());
            default -> false;
            };
        }
        return switch (condition.op()) {
        case EQ -> cmp == 0;
        case NE -> cmp != 0;
        case LT -> cmp < 0;
        case LE -> cmp <= 0;
        case GT -> cmp > 0;
        case GE -> cmp >= 0;
        };
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Double d) {
            return (d.isNaN() || d.isInfinite()) ? null : BigDecimal.valueOf(d);
        }
        if (value instanceof Float f) {
            return (f.isNaN() || f.isInfinite()) ? null : new BigDecimal(f.toString());
        }
        if (value instanceof java.math.BigInteger bi) {
            return new BigDecimal(bi);
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.longValue());
        }
        return null;
    }

    private static Instant bucketStart(Instant t, Duration bucketSize) {
        long bucketMillis = bucketSize.toMillis();
        long start = Math.floorDiv(t.toEpochMilli(), bucketMillis) * bucketMillis;
        return Instant.ofEpochMilli(start);
    }

    private static Optional<TimedValue<?>> toTimedValue(Map.Entry<Instant, Stored> entry) {
        return entry == null ? Optional.empty()
                : Optional.of(new DefaultTimedValue<>(entry.getValue().value(), entry.getKey()));
    }
}

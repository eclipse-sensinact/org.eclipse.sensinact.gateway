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
package org.eclipse.sensinact.gateway.southbound.history.inmemory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
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
import java.util.stream.Stream;

import org.eclipse.sensinact.core.twin.DefaultTimedValue;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.southbound.history.provider.AggregateBucket;
import org.eclipse.sensinact.gateway.southbound.history.provider.AggregateFunction;
import org.eclipse.sensinact.gateway.southbound.history.provider.AggregationQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryCapability;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryPage;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.SortOrder;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.provider.ValueFilter;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryStorage;
import org.eclipse.sensinact.gateway.southbound.history.storage.PruneRequest;

/**
 * Heap-backed {@link HistoryStorage}: reference implementation of the SPI,
 * used for docker-free integration testing and demo deployments. Not intended
 * for large or durable histories.
 */
public class InMemoryHistoryStorage implements HistoryStorage {

    private final Map<ResourcePath, NavigableMap<Instant, HistoricalRecord>> data = new ConcurrentHashMap<>();
    private final int maxPageSize;

    public InMemoryHistoryStorage(int maxPageSize) {
        if (maxPageSize <= 0) {
            throw new IllegalArgumentException("maxPageSize must be positive");
        }
        this.maxPageSize = maxPageSize;
    }

    @Override
    public Set<HistoryCapability> capabilities() {
        return Set.of(HistoryCapability.AGGREGATION, HistoryCapability.VALUE_FILTERING);
    }

    @Override
    public int maxPageSize() {
        return maxPageSize;
    }

    @Override
    public void store(List<HistoricalRecord> records) {
        for (HistoricalRecord record : records) {
            data.computeIfAbsent(record.path(), p -> new TreeMap<>()).put(record.timestamp(), record);
        }
    }

    @Override
    public Optional<TimedValue<?>> valueAt(ResourcePath path, Instant at) {
        return toTimedValue(series(path).floorEntry(at));
    }

    @Override
    public Optional<TimedValue<?>> firstValue(ResourcePath path) {
        return toTimedValue(series(path).firstEntry());
    }

    @Override
    public Optional<TimedValue<?>> latestValue(ResourcePath path) {
        return toTimedValue(series(path).lastEntry());
    }

    @Override
    public long count(ResourcePath path, TimeRange range) {
        return matching(path, range, null).count();
    }

    @Override
    public HistoryPage values(HistoryQuery query) {
        int limit = query.limit() == HistoryQuery.PROVIDER_DEFAULT_LIMIT ? maxPageSize
                : Math.min(query.limit(), maxPageSize);
        Comparator<HistoricalRecord> byTime = Comparator.comparing(HistoricalRecord::timestamp);
        if (query.order() == SortOrder.DESCENDING) {
            byTime = byTime.reversed();
        }
        List<HistoricalRecord> page = matching(query.path(), query.range(), query.valueFilter()).sorted(byTime)
                .skip(query.offset()).limit(limit + 1L).toList();

        boolean hasMore = page.size() > limit;
        List<TimedValue<?>> values = page.stream().limit(limit)
                .<TimedValue<?>>map(r -> new DefaultTimedValue<>(r.value(), r.timestamp())).toList();
        return HistoryPage.of(values, query.offset(), hasMore);
    }

    @Override
    public List<AggregateBucket> aggregate(AggregationQuery query) {
        Map<Instant, List<HistoricalRecord>> buckets = new TreeMap<>();
        matching(query.path(), query.range(), null).forEach(
                r -> buckets.computeIfAbsent(bucketStart(r.timestamp(), query.bucketSize()), b -> new ArrayList<>())
                        .add(r));

        List<AggregateBucket> result = new ArrayList<>();
        for (Map.Entry<Instant, List<HistoricalRecord>> bucket : buckets.entrySet()) {
            List<HistoricalRecord> records = bucket.getValue();
            records.sort(Comparator.comparing(HistoricalRecord::timestamp));
            List<BigDecimal> numbers = records.stream().map(r -> toBigDecimal(r.value())).filter(Objects::nonNull)
                    .toList();

            Map<AggregateFunction, Object> results = new EnumMap<>(AggregateFunction.class);
            for (AggregateFunction function : query.functions()) {
                Object value = switch (function) {
                case COUNT -> (long) records.size();
                case MIN -> numbers.stream().min(Comparator.naturalOrder()).orElse(null);
                case MAX -> numbers.stream().max(Comparator.naturalOrder()).orElse(null);
                case SUM -> numbers.isEmpty() ? null : numbers.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                case AVG -> numbers.isEmpty() ? null
                        : numbers.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                                .divide(BigDecimal.valueOf(numbers.size()), MathContext.DECIMAL64);
                case FIRST -> records.get(0).value();
                case LAST -> records.get(records.size() - 1).value();
                };
                if (value != null) {
                    results.put(function, value);
                }
            }
            result.add(new AggregateBucket(bucket.getKey(), results));
        }
        return result;
    }

    @Override
    public long prune(PruneRequest request) {
        record Candidate(ResourcePath path, Instant timestamp) {
        }
        List<Candidate> candidates = new ArrayList<>();
        for (Map.Entry<ResourcePath, NavigableMap<Instant, HistoricalRecord>> series : data.entrySet()) {
            if (request.paths() != null && !request.paths().contains(series.getKey())) {
                continue;
            }
            List<Instant> newestFirst = new ArrayList<>(series.getValue().descendingKeySet());
            for (int i = 0; i < newestFirst.size(); i++) {
                Instant timestamp = newestFirst.get(i);
                boolean tooOld = request.olderThan() != null && timestamp.isBefore(request.olderThan());
                boolean beyondKeep = request.keepLatestPerResource() != null
                        && i >= request.keepLatestPerResource();
                if (tooOld || beyondKeep) {
                    candidates.add(new Candidate(series.getKey(), timestamp));
                }
            }
        }
        candidates.sort(Comparator.comparing(Candidate::timestamp));

        long cap = request.maxDelete() == null ? Long.MAX_VALUE : request.maxDelete();
        long deleted = 0;
        for (Candidate candidate : candidates) {
            if (deleted == cap) {
                break;
            }
            data.get(candidate.path()).remove(candidate.timestamp());
            deleted++;
        }
        return deleted;
    }

    private NavigableMap<Instant, HistoricalRecord> series(ResourcePath path) {
        return data.getOrDefault(path, new TreeMap<>());
    }

    private Stream<HistoricalRecord> matching(ResourcePath path, TimeRange range, ValueFilter filter) {
        return series(path).values().stream().filter(r -> range.contains(r.timestamp()))
                .filter(r -> filter == null || matches(filter, r.value()));
    }

    private static boolean matches(ValueFilter filter, Object value) {
        return filter.conditions().stream().allMatch(condition -> matches(condition, value));
    }

    private static boolean matches(ValueFilter.Condition condition, Object value) {
        if (value == null) {
            return false;
        }
        BigDecimal left = toBigDecimal(value);
        BigDecimal right = toBigDecimal(condition.literal());
        Integer comparison;
        if (left != null && right != null) {
            comparison = left.compareTo(right);
        } else if (value instanceof String s && condition.literal() instanceof String literal) {
            comparison = s.compareTo(literal);
        } else {
            comparison = null;
        }
        if (comparison == null) {
            return switch (condition.op()) {
            case EQ -> value.equals(condition.literal());
            case NE -> !value.equals(condition.literal());
            default -> false;
            };
        }
        return switch (condition.op()) {
        case EQ -> comparison == 0;
        case NE -> comparison != 0;
        case LT -> comparison < 0;
        case LE -> comparison <= 0;
        case GT -> comparison > 0;
        case GE -> comparison >= 0;
        };
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

    private static Instant bucketStart(Instant timestamp, Duration bucketSize) {
        long bucketMillis = bucketSize.toMillis();
        return Instant.ofEpochMilli(Math.floorDiv(timestamp.toEpochMilli(), bucketMillis) * bucketMillis);
    }

    private static Optional<TimedValue<?>> toTimedValue(Map.Entry<Instant, HistoricalRecord> entry) {
        return entry == null ? Optional.empty()
                : Optional.of(new DefaultTimedValue<>(entry.getValue().value(), entry.getKey()));
    }
}

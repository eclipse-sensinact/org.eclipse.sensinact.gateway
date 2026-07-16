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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.AggregationQuery;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.AggregationQuery.Function;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.HistoryStorage;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.SortOrder;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.spikes.spi.ValueKind;

/**
 * Spike-grade benchmark (timed JUnit, not JMH): seeds a fixed dataset and
 * measures the operations that matter for query pushdown. Results print as
 * "BENCH|" lines for easy extraction from the build output.
 */
public final class StorageBenchmark {

    private static final int HOT_ROWS = 1_000_000;
    private static final int SPREAD_RESOURCES = 1_000;
    private static final int SPREAD_ROWS_EACH = 100;
    private static final int BATCH_SIZE = 10_000;
    private static final int WARMUP = 5;
    private static final int ITERATIONS = 20;
    private static final long HOT_SPACING_SECONDS = 3;

    private static final Instant T0 = Instant.parse("2024-01-01T00:00:00Z");
    private static final ResourcePath HOT = new ResourcePath("benchProvider", "benchService", "hotResource");

    private StorageBenchmark() {
    }

    public static void run(HistoryStorage storage, String label) {
        double ingestRate = seed(storage);
        System.out.println(String.format(Locale.ROOT, "BENCH|%s|ingest|%.0f rows/s", label, ingestRate));

        measure(label, "range asc offset=500k limit=500",
                () -> storage.values(HistoryQuery.of(HOT, 500).withOffset(500_000)));
        measure(label, "range desc limit=500",
                () -> storage.values(HistoryQuery.of(HOT, 500).withOrder(SortOrder.DESCENDING)));
        measure(label, "latestValue", () -> storage.latestValue(HOT));
        measure(label, "valueAt (point-in-time)", () -> storage.valueAt(HOT, T0.plusSeconds(HOT_ROWS)));
        measure(label, "count all", () -> storage.count(HOT, TimeRange.ALL));
        measure(label, "aggregate 1h buckets / 30 days",
                () -> storage.aggregate(new AggregationQuery(HOT,
                        TimeRange.closed(T0, T0.plus(Duration.ofDays(30))), Duration.ofHours(1),
                        Set.of(Function.MIN, Function.MAX, Function.AVG, Function.COUNT))));
    }

    private static double seed(HistoryStorage storage, ResourcePath path, int rows, long spacingSeconds,
            Random random) {
        List<HistoricalRecord> batch = new ArrayList<>(BATCH_SIZE);
        for (int i = 0; i < rows; i++) {
            batch.add(new HistoricalRecord("https://eclipse.org/sensinact/bench", "benchModel", path,
                    T0.plusSeconds(i * spacingSeconds), ValueKind.NUMBER, random.nextDouble() * 100.0));
            if (batch.size() == BATCH_SIZE || i == rows - 1) {
                storage.store(batch);
                batch = new ArrayList<>(BATCH_SIZE);
            }
        }
        return rows;
    }

    private static double seed(HistoryStorage storage) {
        Random random = new Random(42);
        long start = System.nanoTime();
        double total = seed(storage, HOT, HOT_ROWS, HOT_SPACING_SECONDS, random);
        for (int r = 0; r < SPREAD_RESOURCES; r++) {
            total += seed(storage, new ResourcePath("spreadProvider", "spreadService", "resource-" + r),
                    SPREAD_ROWS_EACH, 60, random);
        }
        double seconds = (System.nanoTime() - start) / 1e9;
        return total / seconds;
    }

    private static void measure(String label, String operation, Supplier<?> query) {
        for (int i = 0; i < WARMUP; i++) {
            query.get();
        }
        long[] nanos = new long[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.nanoTime();
            query.get();
            nanos[i] = System.nanoTime() - start;
        }
        java.util.Arrays.sort(nanos);
        double p50 = nanos[ITERATIONS / 2] / 1e6;
        double p95 = nanos[(int) Math.ceil(ITERATIONS * 0.95) - 1] / 1e6;
        System.out.println(
                String.format(Locale.ROOT, "BENCH|%s|%s|p50=%.1f ms|p95=%.1f ms", label, operation, p50, p95));
    }
}

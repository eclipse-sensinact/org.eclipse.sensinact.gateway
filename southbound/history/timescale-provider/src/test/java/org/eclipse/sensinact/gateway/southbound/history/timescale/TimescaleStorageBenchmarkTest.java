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
package org.eclipse.sensinact.gateway.southbound.history.timescale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.eclipse.sensinact.gateway.southbound.history.provider.AggregateFunction;
import org.eclipse.sensinact.gateway.southbound.history.provider.AggregationQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryPage;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.SortOrder;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.provider.ValueFilter;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoricalRecord;
import org.eclipse.sensinact.gateway.southbound.history.storage.ValueKind;
import org.eclipse.sensinact.gateway.southbound.history.timescale.TimescaleHistoryStorage.TxRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Load test ("Lasttest") for the production {@link TimescaleHistoryStorage}:
 * seeds one million values on a hot resource plus 100k values spread over
 * 1000 resources, then measures the operations the SensorThings pushdown
 * relies on. Results print as {@code BENCH|} lines; the assertions are
 * deliberately generous sanity ceilings that only catch pathological
 * regressions such as a missing index turning an index lookup into a
 * sequential scan.
 *
 * Disabled by default — run with:
 *
 * <pre>
 * mvn test -pl southbound/history/timescale-provider \
 *     -Dtest=TimescaleStorageBenchmarkTest -Dhistory.benchmark=true
 * </pre>
 */
@EnabledIfSystemProperty(named = "history.benchmark", matches = "true")
@EnabledIf("dockerAvailable")
class TimescaleStorageBenchmarkTest {

    private static final int HOT_ROWS = 1_000_000;
    private static final int SPREAD_RESOURCES = 1_000;
    private static final int SPREAD_ROWS_EACH = 100;
    private static final int BATCH_SIZE = 10_000;
    private static final int WARMUP = 3;
    private static final int ITERATIONS = 15;
    private static final long HOT_SPACING_SECONDS = 3;

    private static final Instant T0 = Instant.parse("2024-01-01T00:00:00Z");
    private static final ResourcePath HOT = new ResourcePath("benchProvider", "benchService", "hotResource");

    static boolean dockerAvailable() {
        return DockerClientFactory.instance().isDockerAvailable();
    }

    @Test
    void timescaleBenchmark() throws Exception {
        try (PostgreSQLContainer container = new PostgreSQLContainer(
                DockerImageName.parse("timescale/timescaledb:latest-pg16").asCompatibleSubstituteFor("postgres"))) {
            container.start();
            runBenchmark(container, "timescale-pg16");
        }
    }

    @Test
    void plainPostgresBenchmark() throws Exception {
        try (PostgreSQLContainer container = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))) {
            container.start();
            runBenchmark(container, "postgres-16");
        }
    }

    private void runBenchmark(PostgreSQLContainer container, String label) throws Exception {
        String url = container.getJdbcUrl() + (container.getJdbcUrl().contains("?") ? "&" : "?")
                + "reWriteBatchedInserts=true";
        try (Connection connection = DriverManager.getConnection(url, container.getUsername(),
                container.getPassword())) {
            connection.setAutoCommit(false);
            TxRunner committing = new TxRunner() {
                @Override
                public <T> T inTransaction(Callable<T> operation) {
                    try {
                        T result = operation.call();
                        connection.commit();
                        return result;
                    } catch (Exception e) {
                        try {
                            connection.rollback();
                        } catch (SQLException rollbackFailure) {
                            e.addSuppressed(rollbackFailure);
                        }
                        throw e instanceof RuntimeException re ? re : new IllegalStateException(e);
                    }
                }
            };
            TimescaleHistoryStorage storage = new TimescaleHistoryStorage(committing, () -> connection, 10_000);
            storage.initialize();

            double ingestRate = seed(storage);
            System.out.println(String.format(Locale.ROOT, "BENCH|%s|ingest|%.0f rows/s", label, ingestRate));
            assertTrue(ingestRate > 1_000, "Pathological ingest rate: " + ingestRate + " rows/s");
            assertEquals(HOT_ROWS, storage.count(HOT, TimeRange.ALL));

            measure(label, "range asc offset=500k limit=500", 10_000, () -> {
                HistoryPage page = storage.values(HistoryQuery.builder(HOT).offset(500_000).limit(500).build());
                assertEquals(500, page.values().size());
                return page;
            });
            measure(label, "range desc limit=500", 2_000, () -> {
                HistoryPage page = storage
                        .values(HistoryQuery.builder(HOT).order(SortOrder.DESCENDING).limit(500).build());
                assertEquals(500, page.values().size());
                return page;
            });
            measure(label, "latestValue", 1_000, () -> storage.latestValue(HOT).orElseThrow());
            measure(label, "valueAt (point-in-time)", 1_000,
                    () -> storage.valueAt(HOT, T0.plusSeconds(HOT_ROWS)).orElseThrow());
            measure(label, "count 1M rows", 5_000, () -> storage.count(HOT, TimeRange.ALL));
            measure(label, "count with value filter", 10_000,
                    () -> storage.count(HOT, TimeRange.ALL, ValueFilter.of(ValueFilter.Op.GT, 90.0d)));
            measure(label, "range with value filter limit=500", 10_000, () -> {
                HistoryPage page = storage.values(HistoryQuery.builder(HOT).limit(500)
                        .valueFilter(ValueFilter.of(ValueFilter.Op.GT, 90.0d)).build());
                assertEquals(500, page.values().size());
                return page;
            });
            measure(label, "aggregate 1h buckets / 30 days", 30_000,
                    () -> storage.aggregate(new AggregationQuery(HOT,
                            TimeRange.closed(T0, T0.plus(Duration.ofDays(30))), Duration.ofHours(1),
                            Set.of(AggregateFunction.MIN, AggregateFunction.MAX, AggregateFunction.AVG,
                                    AggregateFunction.COUNT))));
        }
    }

    private double seed(TimescaleHistoryStorage storage) {
        Random random = new Random(42);
        long start = System.nanoTime();
        long total = seed(storage, HOT, HOT_ROWS, HOT_SPACING_SECONDS, random);
        for (int r = 0; r < SPREAD_RESOURCES; r++) {
            total += seed(storage, new ResourcePath("spreadProvider", "spreadService", "resource-" + r),
                    SPREAD_ROWS_EACH, 60, random);
        }
        double seconds = (System.nanoTime() - start) / 1e9;
        return total / seconds;
    }

    private long seed(TimescaleHistoryStorage storage, ResourcePath path, int rows, long spacingSeconds,
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

    private void measure(String label, String operation, long p50CeilingMillis, Supplier<?> query) {
        for (int i = 0; i < WARMUP; i++) {
            query.get();
        }
        long[] nanos = new long[ITERATIONS];
        for (int i = 0; i < ITERATIONS; i++) {
            long start = System.nanoTime();
            query.get();
            nanos[i] = System.nanoTime() - start;
        }
        Arrays.sort(nanos);
        double p50 = nanos[ITERATIONS / 2] / 1e6;
        double p95 = nanos[(int) Math.ceil(ITERATIONS * 0.95) - 1] / 1e6;
        System.out.println(
                String.format(Locale.ROOT, "BENCH|%s|%s|p50=%.1f ms|p95=%.1f ms", label, operation, p50, p95));
        assertTrue(p50 < p50CeilingMillis, String.format(Locale.ROOT,
                "Pathological latency for '%s': p50 %.1f ms exceeds the %d ms sanity ceiling", operation, p50,
                p50CeilingMillis));
    }
}

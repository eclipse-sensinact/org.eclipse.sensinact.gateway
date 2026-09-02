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
package org.eclipse.sensinact.sensorthings.sensing.rest.integration.sensinact;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.abort;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.gateway.test.testcontainers.postgres.RequirePostgresContainer;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.rest.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import tools.jackson.core.type.TypeReference;

/**
 * End-to-end load test ("Lasttest") for the query pushdown: one million
 * observations behind a single datastream, requested over the SensorThings
 * REST API. A page at $skip=500000 can only be served correctly when the
 * pagination reaches the database — the in-memory fallback sees just the
 * newest {@code history.results.max} values — so the content assertions prove
 * the pushdown at scale, while the timings print as {@code BENCH|} lines
 * with a generous sanity ceiling.
 *
 * Disabled by default — run with:
 *
 * <pre>
 * HISTORY_BENCHMARK=true mvn verify -pl northbound/sensorthings/rest.gateway \
 *     -Dmaven.test.skip.exec=true \
 *     -Dtest=org.eclipse.sensinact.sensorthings.sensing.rest.integration.sensinact.ObservationHistoryLoadTest
 * </pre>
 */
@RequirePostgresContainer
public class ObservationHistoryLoadTest extends AbstractIntegrationTest {

    private static final TypeReference<ResultList<Observation>> RESULT_OBSERVATIONS = new TypeReference<>() {
    };

    private static final Instant T0 = Instant.parse("2024-01-01T00:00:00Z");
    private static final int TOTAL_ROWS = 1_000_000;
    private static final int BATCH_SIZE = 10_000;
    private static final long REQUEST_CEILING_MILLIS = 15_000;
    private static final String OBSERVATIONS_PATH = "/Datastreams(loadSensor~data~temperature)/Observations";

    private static JdbcDatabaseContainer<?> container;

    @BeforeAll
    static void startContainer() throws Exception {
        if (!"true".equalsIgnoreCase(System.getenv("HISTORY_BENCHMARK"))) {
            abort("HISTORY_BENCHMARK is not set, skipping the load test");
        }

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(DockerClientFactory.class.getClassLoader());
        try {
            try {
                DockerClientFactory.lazyClient().versionCmd().exec();
            } catch (Throwable t) {
                abort("No docker executable on the path, so tests will be skipped");
            }

            // the same image as the storage-level TimescaleStorageBenchmarkTest,
            // so the two load test halves measure on the same Postgres version
            // (the functional ITs deliberately stay on the PG14 minimum)
            container = new PostgreSQLContainer(DockerImageName.parse("timescale/timescaledb:latest-pg16")
                    .asCompatibleSubstituteFor("postgres"));
            container.withDatabaseName("sensinactHistory");
            container.start();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    private Configuration historyProviderConfig;

    @BeforeEach
    void setupTest(
            @InjectConfiguration(withConfig = @WithConfiguration(pid = "sensinact.history.timescale", location = "?")) Configuration historyConfig,
            @InjectConfiguration(withConfig = @WithConfiguration(pid = "sensinact.sensorthings.northbound.rest", location = "?")) Configuration sensorthingsConfig)
            throws Exception {

        assertNotNull(container);

        historyProviderConfig = historyConfig;

        historyConfig.update(new Hashtable<>(Map.of("url", container.getJdbcUrl(), "user", container.getUsername(),
                ".password", container.getPassword())));

        Hashtable<String, Object> newProps = new Hashtable<String, Object>();
        newProps.put("history.provider", "timescale-history");

        Dictionary<String, Object> properties = sensorthingsConfig.getProperties();
        Enumeration<String> keys = properties.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            newProps.put(key, properties.get(key));
        }

        sensorthingsConfig.update(newProps);

        waitForHistoryTables();
        waitForSensorthingsAPI();
    }

    @AfterEach
    void cleanupTest() throws Exception {
        if (historyProviderConfig != null) {
            historyProviderConfig.delete();
            historyProviderConfig = null;
        }

        try (Connection connection = getDataSource().getConnection()) {
            final Statement stmt = connection.createStatement();
            stmt.execute("DROP TABLE IF EXISTS sensinact.history");
        }
    }

    @AfterAll
    static void stopContainer() {
        if (container != null) {
            container.stop();
            container = null;
        }
    }

    private PGSimpleDataSource getDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setURL(container.getJdbcUrl());
        ds.setUser(container.getUsername());
        ds.setPassword(container.getPassword());
        ds.setReWriteBatchedInserts(true);
        return ds;
    }

    private void waitForHistoryTables() {
        boolean ready = false;
        final long timeout = System.currentTimeMillis() + 5000;
        Object lastError = null;
        do {
            try {
                waitForRowCount("sensinact.history", "", 0, true);
                // prove the ingestion chain is wired before tests push data:
                // events sent before the history listener is up would be lost
                GenericDto probe = new GenericDto();
                probe.modelPackageUri = "sensinact";
                probe.provider = "startupProbe";
                probe.service = "probe";
                probe.resource = "probe" + System.nanoTime();
                probe.type = Boolean.class;
                probe.value = Boolean.TRUE;
                probe.timestamp = Instant.now();
                push.pushUpdate(probe).getValue();
                waitForRowCount("sensinact.history", "WHERE provider = 'startupProbe'", 1, true);

                ready = true;
                lastError = null;
                break;
            } catch (Exception e) {
                lastError = e;
            } catch (AssertionFailedError e) {
                lastError = e;
            }
        } while (!ready && System.currentTimeMillis() < timeout);

        assertTrue(ready, "History provider setup timed out: " + lastError);
    }

    private void waitForSensorthingsAPI() {
        boolean ready = false;
        final long timeout = System.currentTimeMillis() + 5000;
        Exception lastError = null;
        do {
            try {
                utils.queryJson("/Datastreams", RESULT_OBSERVATIONS);
                ready = true;
                lastError = null;
                break;
            } catch (Exception e) {
                lastError = e;
            }
        } while (!ready && System.currentTimeMillis() < timeout);

        assertTrue(ready, "SensorThings API setup timed out: " + lastError);
    }

    private void waitForRowCount(final String table, final String where, final int count, final boolean allowMore) {
        int current = -1;
        int currentUnchangedCount = 0;
        try (Connection conn = getDataSource().getConnection()) {
            for (int i = 0; i < 60; i++) {
                try (ResultSet rs = conn.createStatement()
                        .executeQuery("SELECT COUNT(*) FROM " + table + " " + where)) {
                    assertTrue(rs.next());
                    int updated = rs.getInt(1);
                    currentUnchangedCount = updated == current ? currentUnchangedCount + 1 : 0;
                    if (currentUnchangedCount > 9) {
                        throw new AssertionFailedError("The count for table " + table + " has stabilised at " + current
                                + " which is less than the expected " + count);
                    }
                    current = updated;
                    if (current == count || (current > count && allowMore)) {
                        return;
                    }
                }
                Thread.sleep(200);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new AssertionFailedError("Did not reach the required count " + count + " only " + current);
    }

    /**
     * Row {@code i} has value {@code i} and timestamp {@code T0 + i seconds}.
     * Row 0 goes through the live ingestion chain to materialize the twin
     * resource; the rest is written directly to the database — one million
     * rows would take far too long through the event bus.
     */
    private void seedMillionRows() throws Exception {
        createResource("loadSensor", "data", "temperature", 0, T0);
        waitForRowCount("sensinact.history", "WHERE provider = 'loadSensor' AND service = 'data'", 1, false);

        long start = System.nanoTime();
        try (Connection conn = getDataSource().getConnection();
                PreparedStatement ps = conn.prepareStatement("INSERT INTO sensinact.history"
                        + " (time, modelpackageuri, model, provider, service, resource, value_kind, java_type, value_num)"
                        + " VALUES (?, 'sensinact', 'loadSensor', 'loadSensor', 'data', 'temperature', 0,"
                        + " 'java.lang.Long', ?::numeric)")) {
            for (int i = 1; i < TOTAL_ROWS; i++) {
                ps.setTimestamp(1, Timestamp.from(T0.plusSeconds(i)));
                ps.setString(2, Integer.toString(i));
                ps.addBatch();
                if (i % BATCH_SIZE == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
        }
        double seconds = (System.nanoTime() - start) / 1e9;
        System.out.println(String.format(Locale.ROOT, "BENCH|rest-e2e|seed 1M rows via JDBC|%.1f s", seconds));

        waitForRowCount("sensinact.history", "WHERE provider = 'loadSensor' AND service = 'data'", TOTAL_ROWS,
                false);
    }

    private ResultList<Observation> timedQuery(String operation, String query) throws Exception {
        Callable<ResultList<Observation>> request = () -> utils.queryJson(OBSERVATIONS_PATH + query,
                RESULT_OBSERVATIONS);
        request.call(); // warm-up
        long start = System.nanoTime();
        ResultList<Observation> result = request.call();
        double millis = (System.nanoTime() - start) / 1e6;
        System.out.println(String.format(Locale.ROOT, "BENCH|rest-e2e|%s|%.1f ms", operation, millis));
        assertTrue(millis < REQUEST_CEILING_MILLIS, String.format(Locale.ROOT,
                "Pathological latency for '%s': %.1f ms exceeds the %d ms sanity ceiling", operation, millis,
                REQUEST_CEILING_MILLIS));
        return result;
    }

    private static List<Integer> results(ResultList<Observation> page) {
        return page.value().stream().map(o -> ((Number) o.result()).intValue()).toList();
    }

    private static List<Integer> ascending(int from, int count) {
        return java.util.stream.IntStream.range(from, from + count).boxed().toList();
    }

    @Test
    void pushdownServesOneMillionObservations() throws Exception {
        seedMillionRows();

        // a correct page at skip=500000 is only reachable database-side: the
        // in-memory fallback paginates a window of the newest 3000 values
        ResultList<Observation> deepNoCount = timedQuery("page at $skip=500000 without $count",
                "?$top=50&$skip=500000");
        assertEquals(ascending(500_000, 50), results(deepNoCount));
        assertNull(deepNoCount.count());

        ResultList<Observation> deep = timedQuery("page at $skip=500000",
                "?$top=50&$skip=500000&$count=true");
        assertEquals(TOTAL_ROWS, deep.count());
        assertEquals(ascending(500_000, 50), results(deep));
        assertNotNull(deep.nextLink());
        assertTrue(deep.nextLink().contains("skip=500050"), "Unexpected nextLink: " + deep.nextLink());

        ResultList<Observation> newest = timedQuery("newest page via $orderby desc",
                "?$orderby=phenomenonTime%20desc&$top=50&$count=true");
        assertEquals(TOTAL_ROWS, newest.count());
        assertEquals(TOTAL_ROWS - 1, results(newest).get(0));
        assertEquals(TOTAL_ROWS - 50, results(newest).get(49));

        String window = URLEncoder.encode(String.format("phenomenonTime ge %s and phenomenonTime lt %s",
                T0.plusSeconds(600_000), T0.plusSeconds(600_100)), StandardCharsets.UTF_8);
        ResultList<Observation> filtered = timedQuery("time window $filter mid-dataset",
                "?$filter=" + window + "&$top=50&$count=true");
        assertEquals(100, filtered.count());
        assertEquals(ascending(600_000, 50), results(filtered));
        assertNotNull(filtered.nextLink());

        ResultList<Observation> lastPage = timedQuery("last page at $skip=999950", "?$top=50&$skip=999950");
        assertEquals(ascending(999_950, 50), results(lastPage));
        assertNull(lastPage.nextLink());
    }
}

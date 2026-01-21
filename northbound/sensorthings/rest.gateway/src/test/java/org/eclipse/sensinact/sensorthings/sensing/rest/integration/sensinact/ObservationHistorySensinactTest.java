/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.sensorthings.sensing.rest.integration.sensinact;

import static java.time.Duration.ofDays;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.abort;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.core.type.TypeReference;

public class ObservationHistorySensinactTest extends AbstractIntegrationTest {
    /** 2012-01-01T01:23:45.123456Z */
    private static final Instant TS_2012 = Instant.parse("2012-01-01T01:23:45.123456Z");

    private static final TypeReference<ResultList<Observation>> RESULT_OBSERVATIONS = new TypeReference<ResultList<Observation>>() {
    };

    private static JdbcDatabaseContainer<?> container;

    @BeforeAll
    static void startContainer() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ObservationHistorySensinactTest.class.getClassLoader());
        try {
            try {
                DockerClientFactory.lazyClient().versionCmd().exec();
            } catch (Throwable t) {
                abort("No docker executable on the path, so tests will be skipped");
            }

            container = new PostgreSQLContainer<>(DockerImageName.parse("timescale/timescaledb-ha")
                    .asCompatibleSubstituteFor("postgres").withTag("pg14-latest"));
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

        // Wait for the tables to be ready
        waitForHistoryTables();

        // Wait for the servlet to be valid
        waitForSensorthingsAPI();
    }

    private void waitForHistoryTables() {
        boolean ready = false;
        final long timeout = System.currentTimeMillis() + 5000;
        Exception lastError = null;
        do {
            try {
                for (final String table : List.of("numeric_data", "text_data", "geo_data")) {
                    waitForRowCount("sensinact." + table, 0, true);
                }
                // Got a valid count
                ready = true;
                lastError = null;
                break;
            } catch (Exception e) {
                // Ignore
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
                // Will throw an error if not ready
                utils.queryJson("/Datastreams", new TypeReference<ResultList<Datastream>>() {
                });
                // Got a valid count
                ready = true;
                lastError = null;
                break;
            } catch (Exception e) {
                // Ignore
                lastError = e;
            }
        } while (!ready && System.currentTimeMillis() < timeout);

        assertTrue(ready, "SensorThings API setup timed out: " + lastError);
    }

    @AfterEach
    void cleanupTest() throws Exception {
        if (historyProviderConfig != null) {
            historyProviderConfig.delete();
            historyProviderConfig = null;
        }

        try (Connection connection = getDataSource().getConnection()) {
            final Statement stmt = connection.createStatement();
            for (final String table : List.of("numeric_data", "text_data", "geo_data")) {
                stmt.execute("DROP TABLE IF EXISTS sensinact." + table);
            }
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
        return ds;
    }

    private void waitForRowCount(String table, int count) {
        waitForRowCount(table, count, false);
    }

    private void waitForRowCount(String table, int count, boolean allowMore) {
        int current = -1;
        try (Connection conn = getDataSource().getConnection()) {
            for (int i = 0; i < 60; i++) {
                try (ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + table)) {
                    assertTrue(rs.next());
                    current = rs.getInt(1);
                    if (current == count) {
                        return;
                    } else if (current > count) {
                        if (allowMore) {
                            return;
                        }

                        try (ResultSet rs2 = conn.createStatement().executeQuery("SELECT * FROM " + table)) {
                            int j = 0;
                            ResultSetMetaData metaData = rs2.getMetaData();
                            final int nbCols = metaData.getColumnCount();
                            final Map<Integer, String> names = new HashMap<>();
                            for (int col = 1; col <= nbCols; col++) {
                                names.put(col, metaData.getColumnName(col));
                            }
                            final List<String> row = new ArrayList<>(nbCols);
                            while (rs2.next()) {
                                for (int col = 1; col <= nbCols; col++) {
                                    row.add(names.get(col) + "=" + rs2.getObject(col));
                                }
                                System.out.println("* " + j++ + " / " + current + " => " + String.join(", ", row));
                                row.clear();
                            }

                            System.out.flush();
                        }
                        throw new AssertionFailedError("The count for table " + table + " was " + current
                                + " which is larger than the expected " + count);
                    }
                }
                Thread.sleep(200);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new AssertionFailedError("Did not reach the required count " + count + " only " + current);
    }

    @Test
    void getDataStreamObservations() throws Exception {
        for (int i = 0; i < 1000; i++) {
            createResource("foo", "bar", "baz", String.valueOf(i), TS_2012.plus(ofDays(i)));
        }
        for (int i = 0; i < 4000; i++) {
            createResource("foo", "bar", "foobar", Integer.valueOf(i), TS_2012.plus(ofDays(i)));
        }
        // 1008: 1000 updates + history provider name & description & model &
        // modelPackageUri + foo
        // provider name & description & modelUri
        waitForRowCount("sensinact.text_data", 1008);
        waitForRowCount("sensinact.numeric_data", 4000);

        ResultList<Observation> observations = utils.queryJson("/Datastreams(foo~bar~baz)/Observations?$count=true",
                RESULT_OBSERVATIONS);

        assertEquals(1000, observations.count());
        assertEquals(500, observations.value().size()); // Is this 500 because of
                                                        // https://eclipse-sensinact.readthedocs.io/en/latest/southbound/history/history.html??
        assertNotNull(observations.nextLink());

        for (int i = 0; i < 500; i++) {
            Instant ts = TS_2012.plus(ofDays(i));
            assertEquals(ts, observations.value().get(i).resultTime());
            assertEquals(String.valueOf(i), observations.value().get(i).result());
        }

        observations = utils.queryJson(observations.nextLink(), RESULT_OBSERVATIONS);

        assertEquals(1000, observations.count());
        assertEquals(500, observations.value().size());
        assertNull(observations.nextLink());

        for (int i = 0; i < 500; i++) {
            Instant ts = TS_2012.plus(ofDays(i + 500));
            assertEquals(ts, observations.value().get(i).resultTime());
            assertEquals(String.valueOf(i + 500), observations.value().get(i).result());
        }

        observations = utils.queryJson("/Datastreams(foo~bar~foobar)/Observations?$count=true", RESULT_OBSERVATIONS);
        assertEquals(4000, observations.count());
        assertEquals(500, observations.value().size());
        assertNotNull(observations.nextLink());

        for (int i = 0; i < 500; i++) {
            Instant ts = TS_2012.plus(ofDays(i + 1000));
            assertEquals(ts, observations.value().get(i).resultTime());
            assertEquals(i + 1000, observations.value().get(i).result());
        }
    }

    @Test
    void getHistoricObservationTest() throws Exception {
        for (int i = 0; i < 10; i++) {
            createResource("fizz", "buzz", "fizzbuzz", String.valueOf(i), TS_2012.plus(ofDays(i)));
        }
        // 16: 10 updates + history provider name & model & modelPackageUri + fizz
        // provider name & modelUri
        waitForRowCount("sensinact.text_data", 18);

        String id = String.format("%s~%s~%s~%s", "fizz", "buzz", "fizzbuzz",
                Long.toString(TS_2012.plus(ofDays(3)).toEpochMilli(), 16));

        Observation o = utils.queryJson("/Observations(" + id + ")", new TypeReference<Observation>() {
        });
        assertEquals(id, o.id());
        assertEquals(TS_2012.plus(ofDays(3)), o.resultTime());
        assertEquals("3", o.result());
    }

    @Test
    void navigateToObservationTest() throws Exception {
        for (int i = 0; i < 10; i++) {
            createResource("ding", "dong", "bell", String.valueOf(i), TS_2012.plus(ofDays(i)));
        }
        waitForRowCount("sensinact.text_data", 18);

        ResultList<Datastream> streams = utils.queryJson("/Datastreams", new TypeReference<ResultList<Datastream>>() {
        });

        assertFalse(streams.value().isEmpty());
        Datastream datastream = streams.value().stream().filter(d -> "ding~dong~bell".equals(d.id())).findFirst().get();

        ResultList<Observation> observations = utils.queryJson(datastream.observationsLink(),
                new TypeReference<ResultList<Observation>>() {
                });

        assertEquals(10, observations.value().size());
        Observation observation = observations.value().get(1);
        String id = String.format("%s~%s~%s~%s", "ding", "dong", "bell",
                Long.toString(TS_2012.plus(ofDays(1)).toEpochMilli(), 16));
        assertEquals(id, observation.id());
        assertEquals(TS_2012.plus(ofDays(1)), observation.resultTime());
        assertEquals("1", observation.result());
    }

    @Test
    void testFilterPhenomenonTime() throws Exception {
        final TypeReference<ResultList<Observation>> RESULT_OBSERVATIONS = new TypeReference<>() {
        };

        // Create two unique providers with different phenomenonTimes
        String testProvider = "phenomenonTimeTestProvider";
        String svc = "sensor";
        String rc = "temperature";

        // Create timestamps - one in 2010, one in 2020
        Instant earlierTime = ZonedDateTime.of(2010, 6, 15, 12, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Instant laterTime = ZonedDateTime.of(2020, 6, 15, 12, 0, 0, 0, ZoneOffset.UTC).toInstant();

        // Create resources with different timestamps
        createResource(testProvider, svc, rc, 25.5, earlierTime);
        createResource(testProvider, "admin", "location", new Point(Coordinates.EMPTY, null, null));
        createResource(testProvider, svc, rc, 30.2, laterTime);

        // Test phenomenonTime lt filter - should return only the earlier observation
        ResultList<Observation> observations = utils.queryJson(
                String.format("/Datastreams(phenomenonTimeTestProvider~sensor~temperature)/Observations?$filter=%s",
                        URLEncoder.encode("phenomenonTime lt 2015-01-01T00:00:00Z", StandardCharsets.UTF_8)),
                RESULT_OBSERVATIONS);

        assertEquals(1, observations.value().size(), "Should find exactly one observation for earlier timestamp");
        Observation obs = observations.value().get(0);
        assertTrue(obs.id().toString().startsWith(testProvider + "~"), "Should be from testProvider: " + obs.id());
        assertEquals(earlierTime, obs.phenomenonTime());
        assertEquals(25.5, obs.result());

        // Test phenomenonTime gt filter - should return only the later observation
        observations = utils.queryJson(
                String.format("/Datastreams(phenomenonTimeTestProvider~sensor~temperature)/Observations?$filter=%s",
                        URLEncoder.encode("phenomenonTime gt 2015-01-01T00:00:00Z", StandardCharsets.UTF_8)),
                RESULT_OBSERVATIONS);

        assertEquals(1, observations.value().size(), "Should find exactly one observation for later timestamp");
        obs = observations.value().get(0);
        assertTrue(obs.id().toString().startsWith(testProvider + "~"), "Should be from testProvider: " + obs.id());
        assertEquals(laterTime, obs.phenomenonTime());
        assertEquals(30.2, obs.result());

    }

    @Test
    void testFilterLargerThanBlocksize() throws Exception {
        for (int i = 0; i < 1000; i++) {
            createResource("foo", "bar", "foobar", Integer.valueOf(i), TS_2012.plus(ofDays(i)));
        }
        waitForRowCount("sensinact.numeric_data", 1000);
        // Test phenomenonTime lt filter - should return only the earlier observation
        ResultList<Observation> observations = utils
                .queryJson(String.format("/Datastreams(foo~bar~foobar)/Observations?$filter=%s",
                        URLEncoder.encode(
                                "phenomenonTime gt 2014-07-01T00:00:00Z and phenomenonTime lt 2014-07-10T00:00:00Z ",
                                StandardCharsets.UTF_8)),
                        new TypeReference<>() {
                        });
        assertEquals(9, observations.value().size(), "Should find 9 observations between 1.7.2014 and 10.7.2014");
        Instant start = ZonedDateTime.of(2014, 7, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Instant end = ZonedDateTime.of(2014, 7, 10, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        for (Observation obs : observations.value()) {
            assertTrue(obs.phenomenonTime().isAfter(start));
            assertTrue(obs.phenomenonTime().isBefore(end));
        }
    }
}

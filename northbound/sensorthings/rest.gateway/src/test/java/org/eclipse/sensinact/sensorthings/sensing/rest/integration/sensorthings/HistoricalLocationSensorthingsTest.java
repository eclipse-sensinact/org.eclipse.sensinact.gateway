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
package org.eclipse.sensinact.sensorthings.sensing.rest.integration.sensorthings;

import static java.time.Duration.ofDays;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.abort;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
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
import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.eNS_URI;

public class HistoricalLocationSensorthingsTest extends AbstractIntegrationTest {

    private static final Instant TS_2012 = Instant.parse("2012-01-01T01:23:45.123456Z");

    private static JdbcDatabaseContainer<?> container;

    @BeforeAll
    static void startContainer() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(HistoricalLocationSensorthingsTest.class.getClassLoader());
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
    void getThingHistoricalLocationsTest() throws Exception {
        for (int i = 0; i < 10; i++) {
            createThing("fizz", List.of(), List.of(), new Point(i, i), TS_2012.plus(ofDays(i)));

        }
        // 10 updates
        waitForRowCount("sensinact.geo_data", 10);

        ResultList<HistoricalLocation> o = utils.queryJson("/Things(fizz)/HistoricalLocations?$count=true",
                new TypeReference<ResultList<HistoricalLocation>>() {
                });
        assertEquals(o.count(), 10);
    }

    @Test
    void getThingLocationHistoricalLocationsTest() throws Exception {
        createThing("thing", List.of("fizz"), List.of(), TS_2012);
        createLocation("fizz");

        for (int i = 0; i < 10; i++) {
            // simulate also the update of location due to update of location
            createResourceWithPackageUri("thing", eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "location", new Point(i, i),
                    TS_2012.plus(ofDays(i)));
        }
        // 10 updates
        waitForRowCount("sensinact.geo_data", 12);

        ResultList<HistoricalLocation> o = utils.queryJson("/Locations(fizz)/HistoricalLocations?$count=true",
                new TypeReference<ResultList<HistoricalLocation>>() {
                });
        assertEquals(11, o.count());
    }

    @Test
    void getDataStreamHistoricalLocationsTest() throws Exception {
        for (int i = 0; i < 10; i++) {
            createThing("fizz", List.of(), List.of(), new Point(i, i), TS_2012.plus(ofDays(i)));
            createDatastream("fizzbuzz", "fizz", "test" + i, TS_2012.plus(ofDays(i)));
        }
        // 10 updates
        waitForRowCount("sensinact.geo_data", 10);

        String id = "fizzbuzz";

        ResultList<HistoricalLocation> o = utils.queryJson(
                "/Datastreams(" + id + ")/Thing/HistoricalLocations?$count=true",
                new TypeReference<ResultList<HistoricalLocation>>() {
                });
        assertEquals(o.count(), 10);
    }

}

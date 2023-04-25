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
package org.eclipse.sensinact.sensorthings.sensing.rest.integration;

import static java.time.Duration.ofDays;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.abort;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opentest4j.AssertionFailedError;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.core.type.TypeReference;

@ExtendWith({ BundleContextExtension.class, ConfigurationExtension.class, MockitoExtension.class })
public class ObservationHistoryTest extends AbstractIntegrationTest {

    private static final Instant TS_2012 = Instant.parse("2012-01-01T00:00:00.00Z");

    private static final TypeReference<ResultList<Observation>> RESULT_OBSERVATIONS = new TypeReference<ResultList<Observation>>() {
    };

    @BeforeAll
    static void check() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ObservationHistoryTest.class.getClassLoader());
        try {
            DockerClientFactory.lazyClient().versionCmd().exec();
        } catch (Throwable t) {
            abort("No docker executable on the path, so tests will be skipped");
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    @BeforeEach
    void startContainer(
            @InjectConfiguration(withConfig = @WithConfiguration(pid = "sensinact.history.timescale", location = "?")) Configuration historyConfig,
            @InjectConfiguration(withConfig = @WithConfiguration(pid = "sensinact.sensorthings.northbound.rest", location = "?")) Configuration sensorthingsConfig)
            throws Exception {
        container = new PostgreSQLContainer<>(DockerImageName.parse("timescale/timescaledb-ha")
                .asCompatibleSubstituteFor("postgres").withTag("pg14-latest"));

        container.withDatabaseName("sensinactHistory");
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            container.start();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
        historyConfig.update(new Hashtable<>(Map.of("url", container.getJdbcUrl(), "user", container.getUsername(),
                ".password", container.getPassword())));
        sensorthingsConfig.update(new Hashtable<>(Map.of("history.provider", "timescale-history")));
        Thread.sleep(1000);
    }

    @AfterEach
    void stopContainer() {
        container.stop();
    }

    private JdbcDatabaseContainer<?> container;

    private PGSimpleDataSource getDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setURL(container.getJdbcUrl());
        ds.setUser(container.getUsername());
        ds.setPassword(container.getPassword());
        return ds;
    }

    private void waitForRowCount(String table, int count) {
        int current = -1;
        try (Connection conn = getDataSource().getConnection()) {
            for (int i = 0; i < 60; i++) {
                try (ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + table)) {
                    assertTrue(rs.next());
                    current = rs.getInt(1);
                    if (current == count) {
                        return;
                    } else if (current > count) {
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
        // 1004: 1000 updates + history provider name & modelUri + foo provider name & modelUri
        waitForRowCount("sensinact.text_data", 1004);
        waitForRowCount("sensinact.numeric_data", 4000);

        ResultList<Observation> observations = utils.queryJson("/Datastreams(foo~bar~baz)/Observations?$count=true",
                RESULT_OBSERVATIONS);

        assertEquals(1000, observations.count);
        assertEquals(500, observations.value.size());
        assertNotNull(observations.nextLink);

        for (int i = 0; i < 500; i++) {
            Instant ts = TS_2012.plus(ofDays(i));
            assertEquals(ts, observations.value.get(i).resultTime);
            assertEquals(String.valueOf(i), observations.value.get(i).result);
        }

        observations = utils.queryJson(observations.nextLink, RESULT_OBSERVATIONS);

        assertEquals(1000, observations.count);
        assertEquals(500, observations.value.size());
        assertNull(observations.nextLink);

        for (int i = 0; i < 500; i++) {
            Instant ts = TS_2012.plus(ofDays(i + 500));
            assertEquals(ts, observations.value.get(i).resultTime);
            assertEquals(String.valueOf(i + 500), observations.value.get(i).result);
        }

        observations = utils.queryJson("/Datastreams(foo~bar~foobar)/Observations?$count=true", RESULT_OBSERVATIONS);
        // Note 3000, not 4000 as we limit the amount we retrieve
        assertEquals(3000, observations.count);
        assertEquals(500, observations.value.size());
        assertNotNull(observations.nextLink);

        for (int i = 0; i < 500; i++) {
            Instant ts = TS_2012.plus(ofDays(i + 1000));
            assertEquals(ts, observations.value.get(i).resultTime);
            assertEquals(i + 1000, observations.value.get(i).result);
        }
    }

    @Test
    void getHistoricObservationTest() throws Exception {
        for (int i = 0; i < 10; i++) {
            createResource("fizz", "buzz", "fizzbuzz", String.valueOf(i), TS_2012.plus(ofDays(i)));
        }
        // 14: 10 updates + history provider name & modelUri + fizz provider name & modelUri
        waitForRowCount("sensinact.text_data", 14);

        String id = String.format("%s~%s~%s~%s", "fizz", "buzz", "fizzbuzz",
                Long.toString(TS_2012.plus(ofDays(3)).toEpochMilli(), 16));

        Observation o = utils.queryJson("/Observations(" + id + ")", new TypeReference<Observation>() {
        });
        assertEquals(id, o.id);
        assertEquals(TS_2012.plus(ofDays(3)), o.resultTime);
        assertEquals("3", o.result);
    }

    @Test
    void navigateToObservationTest() throws Exception {
        for (int i = 0; i < 10; i++) {
            createResource("ding", "dong", "bell", String.valueOf(i), TS_2012.plus(ofDays(i)));
        }
        waitForRowCount("sensinact.text_data", 14);

        ResultList<Datastream> streams = utils.queryJson("/Datastreams", new TypeReference<ResultList<Datastream>>() {
        });

        assertFalse(streams.value.isEmpty());
        Datastream datastream = streams.value.stream().filter(d -> "ding~dong~bell".equals(d.id)).findFirst().get();

        ResultList<Observation> observations = utils.queryJson(datastream.observationsLink,
                new TypeReference<ResultList<Observation>>() {
                });

        assertEquals(10, observations.value.size());
        Observation observation = observations.value.get(1);
        String id = String.format("%s~%s~%s~%s", "ding", "dong", "bell",
                Long.toString(TS_2012.plus(ofDays(1)).toEpochMilli(), 16));
        assertEquals(id, observation.id);
        assertEquals(TS_2012.plus(ofDays(1)), observation.resultTime);
        assertEquals("1", observation.result);
    }
}

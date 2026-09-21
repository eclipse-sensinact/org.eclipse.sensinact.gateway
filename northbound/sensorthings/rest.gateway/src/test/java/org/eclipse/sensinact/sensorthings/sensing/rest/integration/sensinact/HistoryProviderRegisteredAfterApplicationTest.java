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
*   Data In Motion - initial implementation
**********************************************************************/
package org.eclipse.sensinact.sensorthings.sensing.rest.integration.sensinact;

import static java.time.Duration.ofDays;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.abort;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.gateway.test.testcontainers.postgres.RequirePostgresContainer;
import org.eclipse.sensinact.northbound.session.ProviderDescription;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.rest.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
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
 * Production startup order: the SensorThings application registers with the
 * whiteboard first, the history storage connects to its database later. The
 * other history tests update the application configuration <em>after</em> the
 * history provider exists, which re-registers the application and hides
 * whether a provider that appears later is ever picked up.
 */
@RequirePostgresContainer
public class HistoryProviderRegisteredAfterApplicationTest extends AbstractIntegrationTest {

    private static final String HISTORY_PROVIDER_NAME = "timescale-history";
    private static final Instant FIRST_OBSERVATION = Instant.parse("2012-01-01T01:23:45.123456Z");
    private static final int OBSERVATIONS_PER_DATASTREAM = 25;
    private static final TypeReference<ResultList<Observation>> OBSERVATIONS = new TypeReference<>() {
    };

    private static JdbcDatabaseContainer<?> container;

    private TestInfo testInfo;
    private Configuration historyStorageConfiguration;

    @BeforeAll
    static void startContainer() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(DockerClientFactory.class.getClassLoader());
        try {
            try {
                DockerClientFactory.lazyClient().versionCmd().exec();
            } catch (Throwable t) {
                abort("No docker executable on the path, so tests will be skipped");
            }
            container = new PostgreSQLContainer(DockerImageName.parse("timescale/timescaledb-ha")
                    .asCompatibleSubstituteFor("postgres").withTag("pg14-latest"));
            container.withDatabaseName("sensinactHistory");
            container.start();
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

    @AfterAll
    static void stopContainer() {
        if (container != null) {
            container.stop();
            container = null;
        }
    }

    @BeforeEach
    void registerTheApplicationBeforeAnyHistoryProviderExists(TestInfo testInfo,
            @InjectConfiguration(withConfig = @WithConfiguration(pid = "sensinact.history.timescale", location = "?")) Configuration historyConfig,
            @InjectConfiguration(withConfig = @WithConfiguration(pid = "sensinact.sensorthings.northbound.rest", location = "?")) Configuration sensorthingsConfig)
            throws Exception {
        assertNotNull(container);
        this.testInfo = testInfo;
        this.historyStorageConfiguration = historyConfig;

        selectHistoryProvider(sensorthingsConfig, HISTORY_PROVIDER_NAME);
        waitForSensorthingsAPI();
        waitForHistoryProvider(false);

        historyConfig.update(new Hashtable<>(Map.of("url", container.getJdbcUrl(), "user", container.getUsername(),
                ".password", container.getPassword())));
        waitForHistoryTables();
        waitForHistoryProvider(true);
    }

    @AfterEach
    void removeTheHistoryStorage() throws Exception {
        if (historyStorageConfiguration != null) {
            historyStorageConfiguration.delete();
            historyStorageConfiguration = null;
        }
        waitForHistoryProvider(false);

        try (Connection connection = getDataSource().getConnection()) {
            connection.createStatement().execute("DROP TABLE IF EXISTS sensinact.history");
        }
    }

    @Test
    void observationsComeFromAHistoryProviderThatAppearedAfterTheApplicationRegistered() throws Exception {
        for (int i = 0; i < OBSERVATIONS_PER_DATASTREAM; i++) {
            createResource("lateHistory", "sensor", "temperature", Integer.valueOf(i), FIRST_OBSERVATION.plus(ofDays(i)));
        }
        waitForRowCount("sensinact.history", "WHERE provider = 'lateHistory' AND resource = 'temperature'",
                OBSERVATIONS_PER_DATASTREAM);

        ResultList<Observation> observations = utils.queryJson(
                "/Datastreams(lateHistory~sensor~temperature)/Observations?$count=true", OBSERVATIONS);

        assertEquals(OBSERVATIONS_PER_DATASTREAM, observations.count(),
                "the northbound answered with the current value only: it never saw the history provider that registered after the application did");
        assertEquals(OBSERVATIONS_PER_DATASTREAM, observations.value().size());
        for (int i = 0; i < OBSERVATIONS_PER_DATASTREAM; i++) {
            assertEquals(FIRST_OBSERVATION.plus(ofDays(i)), observations.value().get(i).resultTime());
        }
    }

    private void selectHistoryProvider(Configuration sensorthingsConfig, String providerName) throws Exception {
        Hashtable<String, Object> properties = new Hashtable<>();
        Dictionary<String, Object> existing = sensorthingsConfig.getProperties();
        if (existing != null) {
            Enumeration<String> keys = existing.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                properties.put(key, existing.get(key));
            }
        }
        properties.put("history.provider", providerName);
        sensorthingsConfig.update(properties);
    }

    private void waitForSensorthingsAPI() {
        long timeout = System.currentTimeMillis() + 5000;
        Exception lastError = null;
        do {
            try {
                utils.queryJson("/Datastreams", new TypeReference<ResultList<Datastream>>() {
                });
                return;
            } catch (Exception e) {
                lastError = e;
            }
        } while (System.currentTimeMillis() < timeout);
        fail("SensorThings API setup timed out in " + testInfo.getDisplayName() + ": " + lastError);
    }

    private void waitForHistoryProvider(boolean exists) throws InterruptedException {
        for (int i = 0; i < 20; i++) {
            try {
                ProviderDescription description = session.describeProvider(HISTORY_PROVIDER_NAME);
                if (exists) {
                    if (description != null && HISTORY_PROVIDER_NAME.equals(description.provider)) {
                        return;
                    }
                } else if (description == null) {
                    return;
                }
            } catch (Exception e) {
                if (!exists) {
                    return;
                }
            }
            Thread.sleep(250);
        }
        fail((exists ? "No history provider exists in " : "History provider still exists in ")
                + testInfo.getDisplayName());
    }

    private void waitForHistoryTables() {
        long timeout = System.currentTimeMillis() + 5000;
        Exception lastError = null;
        do {
            try {
                waitForRowCount("sensinact.history", "", 0, true);
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
                return;
            } catch (Exception | AssertionFailedError e) {
                lastError = e instanceof Exception ex ? ex : new RuntimeException(e);
            }
        } while (System.currentTimeMillis() < timeout);
        fail("History tables setup timed out in " + testInfo.getDisplayName() + ": " + lastError);
    }

    private void waitForRowCount(String table, String where, int count) {
        waitForRowCount(table, where, count, false);
    }

    private void waitForRowCount(String table, String where, int count, boolean allowMore) {
        int current = -1;
        int unchangedPolls = 0;
        try (Connection connection = getDataSource().getConnection()) {
            for (int i = 0; i < 200; i++) {
                try (ResultSet rows = connection.createStatement()
                        .executeQuery("SELECT COUNT(*) FROM " + table + " " + where)) {
                    assertTrue(rows.next());
                    int updated = rows.getInt(1);
                    unchangedPolls = updated == current ? unchangedPolls + 1 : 0;
                    if (unchangedPolls > 9) {
                        throw new AssertionFailedError("The count for table " + table + " has stabilised at "
                                + current + " which is less than the expected " + count);
                    }
                    current = updated;
                    if (current == count || (allowMore && current > count)) {
                        return;
                    }
                }
                Thread.sleep(50);
            }
        } catch (Exception e) {
            throw new AssertionFailedError("Failed waiting for " + count + " rows in " + table, e);
        }
        throw new AssertionFailedError("Timed out waiting for " + count + " rows in " + table + ", found " + current);
    }

    private PGSimpleDataSource getDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL(container.getJdbcUrl());
        dataSource.setUser(container.getUsername());
        dataSource.setPassword(container.getPassword());
        return dataSource;
    }
}

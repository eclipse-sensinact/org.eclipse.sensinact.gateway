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
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.sensinact.core.twin.TimedValue;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.southbound.history.provider.HistoryQuery;
import org.eclipse.sensinact.gateway.southbound.history.provider.ResourcePath;
import org.eclipse.sensinact.gateway.southbound.history.provider.TimeRange;
import org.eclipse.sensinact.gateway.southbound.history.timescale.TimescaleHistoryStorage.TxRunner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Pins the one-time migration from the pre-rework three-table schema: rows
 * become readable through the unified schema with their historical value
 * shapes, and the legacy tables are renamed rather than dropped. Uses the
 * timescaledb-ha image because the legacy geo table needs PostGIS.
 */
@Testcontainers
@EnabledIf("dockerAvailable")
class LegacyMigrationTest {

    static boolean dockerAvailable() {
        return DockerClientFactory.instance().isDockerAvailable();
    }

    @Container
    static final PostgreSQLContainer container = new PostgreSQLContainer(
            DockerImageName.parse("timescale/timescaledb-ha:pg14-latest").asCompatibleSubstituteFor("postgres"));

    private static final Instant TS = Instant.parse("2020-01-01T00:00:00Z");

    private Connection connection;

    @AfterEach
    void closeConnection() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    void legacyRowsSurviveTheMigrationWithTheirHistoricalShapes() throws Exception {
        connection = DriverManager.getConnection(container.getJdbcUrl(), container.getUsername(),
                container.getPassword());
        connection.setAutoCommit(true);

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS sensinact");
            stmt.execute("CREATE EXTENSION IF NOT EXISTS Postgis");
            stmt.execute("CREATE TABLE sensinact.numeric_data (time TIMESTAMPTZ NOT NULL,"
                    + " modelpackageuri VARCHAR(128), model VARCHAR(128), provider VARCHAR(128),"
                    + " service VARCHAR(128), resource VARCHAR(128), data NUMERIC)");
            stmt.execute("CREATE TABLE sensinact.text_data (time TIMESTAMPTZ NOT NULL,"
                    + " modelpackageuri VARCHAR(128), model VARCHAR(128), provider VARCHAR(128),"
                    + " service VARCHAR(128), resource VARCHAR(128), data text)");
            stmt.execute("CREATE TABLE sensinact.geo_data (time TIMESTAMPTZ NOT NULL,"
                    + " modelpackageuri VARCHAR(128), model VARCHAR(128), provider VARCHAR(128),"
                    + " service VARCHAR(128), resource VARCHAR(128), data geography(POINT,4326))");

            stmt.execute("INSERT INTO sensinact.numeric_data VALUES"
                    + " ('2020-01-01T00:00:00Z', 'uri', 'm', 'p', 's', 'count', 42),"
                    + " ('2020-01-01T00:01:00Z', 'uri', 'm', 'p', 's', 'count', 4.2)");
            stmt.execute("INSERT INTO sensinact.text_data VALUES"
                    + " ('2020-01-01T00:00:00Z', 'uri', 'm', 'p', 's', 'state', 'on')");
            stmt.execute("INSERT INTO sensinact.geo_data VALUES"
                    + " ('2020-01-01T00:00:00Z', 'uri', 'm', 'p', 's', 'location',"
                    + " ST_GeomFromGeoJSON('{\"type\":\"Point\",\"coordinates\":[5.7,12.3]}')::geography)");
        }

        TxRunner plain = new TxRunner() {
            @Override
            public <T> T inTransaction(Callable<T> operation) {
                try {
                    return operation.call();
                } catch (Exception e) {
                    throw e instanceof RuntimeException re ? re : new IllegalStateException(e);
                }
            }
        };
        TimescaleHistoryStorage storage = new TimescaleHistoryStorage(plain, () -> connection, 100);
        storage.initialize();

        List<TimedValue<?>> numbers = storage
                .values(HistoryQuery.builder(new ResourcePath("p", "s", "count")).build()).values();
        assertEquals(42L, numbers.get(0).getValue());
        assertEquals(TS, numbers.get(0).getTimestamp());
        assertEquals(4.2d, numbers.get(1).getValue());
        assertEquals(TS.plusSeconds(60), numbers.get(1).getTimestamp());

        TimedValue<?> state = storage.latestValue(new ResourcePath("p", "s", "state")).orElseThrow();
        assertEquals("on", state.getValue());
        assertEquals(TS, state.getTimestamp());

        Object location = storage.latestValue(new ResourcePath("p", "s", "location")).orElseThrow().getValue();
        assertTrue(location instanceof Point, "migrated geo row is not a Point: " + location);
        assertEquals(5.7d, ((Point) location).coordinates().longitude(), 0.000001);

        assertEquals(1, storage.count(new ResourcePath("p", "s", "state"), TimeRange.ALL));
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM sensinact.text_data_migrated")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }
}

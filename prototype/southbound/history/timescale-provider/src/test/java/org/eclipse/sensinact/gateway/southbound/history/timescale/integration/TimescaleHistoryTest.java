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
package org.eclipse.sensinact.gateway.southbound.history.timescale.integration;

import static java.time.Duration.ofDays;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.abort;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.sensinact.prototype.PrototypePush;
import org.eclipse.sensinact.prototype.generic.dto.GenericDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@ExtendWith({ BundleContextExtension.class, ConfigurationExtension.class, MockitoExtension.class })
public class TimescaleHistoryTest {

    private static final Instant BASE_TIMESTAMP = Instant.parse("2012-01-01T00:00:00.00Z");

    @BeforeAll
    static void check() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(TimescaleHistoryTest.class.getClassLoader());
        try {
            DockerClientFactory.lazyClient().versionCmd().exec();
        } catch (Throwable t) {
            abort("No docker executable on the path, so tests will be skipped");
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    @BeforeEach
    void startContainer() {
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
    }

    @AfterEach
    void stopContainer() {
        container.stop();
    }

    @InjectService
    PrototypePush push;

    private JdbcDatabaseContainer<?> container;

    @Test
    void basicStringData(
            @InjectConfiguration(withConfig = @WithConfiguration(pid = "sensinact.history.timescale", location = "?")) Configuration cm)
            throws Exception {
        cm.update(new Hashtable<>(Map.of("url", container.getJdbcUrl(), "user", container.getUsername(), ".password",
                container.getPassword())));

        Thread.sleep(1000);

        push.pushUpdate(getDto("fizz", BASE_TIMESTAMP)).getValue();
        push.pushUpdate(getDto("buzz", BASE_TIMESTAMP.plus(ofDays(366)))).getValue();
        push.pushUpdate(getDto("fizzbuzz", BASE_TIMESTAMP.plus(ofDays(366 + 365)))).getValue();

        Thread.sleep(1000);

        try (Connection connection = getDataSource().getConnection();
                ResultSet result = connection.createStatement()
                        .executeQuery("SELECT * FROM sensinact.text_data WHERE provider = 'bar' ORDER BY time;")) {

            assertTrue(result.next());
            checkResult(result, "fizz", BASE_TIMESTAMP);
            assertTrue(result.next());
            checkResult(result, "buzz", BASE_TIMESTAMP.plus(ofDays(366)));
            assertTrue(result.next());
            checkResult(result, "fizzbuzz", BASE_TIMESTAMP.plus(ofDays(366 + 365)));
            assertFalse(result.next());
        }
    }

    private PGSimpleDataSource getDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setURL(container.getJdbcUrl());
        ds.setUser(container.getUsername());
        ds.setPassword(container.getPassword());
        return ds;
    }

    private void checkResult(ResultSet result, String data, Instant timestamp) throws SQLException {
        assertEquals("foo", result.getString("model"));
        assertEquals("bar", result.getString("provider"));
        assertEquals("foobar", result.getString("service"));
        assertEquals("foofoobarbar", result.getString("resource"));
        assertEquals(data, result.getString("data"));
        assertEquals(Timestamp.from(timestamp), result.getTimestamp("time"));
    }

    private GenericDto getDto(String value, Instant timestamp) {
        GenericDto dto = new GenericDto();
        dto.model = "foo";
        dto.provider = "bar";
        dto.service = "foobar";
        dto.resource = "foofoobarbar";
        dto.value = value;
        dto.type = String.class;
        dto.timestamp = timestamp;
        return dto;
    }

    @Test
    void basicNumberData(
            @InjectConfiguration(withConfig = @WithConfiguration(pid = "sensinact.history.timescale", location = "?")) Configuration cm)
            throws Exception {
        cm.update(new Hashtable<>(Map.of("url", container.getJdbcUrl(), "user", container.getUsername(), ".password",
                container.getPassword())));

        Thread.sleep(1000);

        push.pushUpdate(getDto(3, BASE_TIMESTAMP)).getValue();
        push.pushUpdate(getDto(5, BASE_TIMESTAMP.plus(ofDays(366)))).getValue();
        push.pushUpdate(getDto(7, BASE_TIMESTAMP.plus(ofDays(366 + 365)))).getValue();

        Thread.sleep(1000);

        try (Connection connection = getDataSource().getConnection();
                ResultSet result = connection.createStatement()
                        .executeQuery("SELECT * FROM sensinact.numeric_data WHERE provider = 'buzz' ORDER BY time;")) {

            assertTrue(result.next());
            checkResult(result, 3, BASE_TIMESTAMP);
            assertTrue(result.next());
            checkResult(result, 5, BASE_TIMESTAMP.plus(ofDays(366)));
            assertTrue(result.next());
            checkResult(result, 7, BASE_TIMESTAMP.plus(ofDays(366 + 365)));
            assertFalse(result.next());
        }
    }

    private void checkResult(ResultSet result, Integer data, Instant timestamp) throws SQLException {
        assertEquals("fizz", result.getString("model"));
        assertEquals("buzz", result.getString("provider"));
        assertEquals("fizzbuzz", result.getString("service"));
        assertEquals("fizzfizzbuzzbuzz", result.getString("resource"));
        assertEquals(data, result.getInt("data"));
        assertEquals(Timestamp.from(timestamp), result.getTimestamp("time"));
    }

    private GenericDto getDto(Integer value, Instant timestamp) {
        GenericDto dto = new GenericDto();
        dto.model = "fizz";
        dto.provider = "buzz";
        dto.service = "fizzbuzz";
        dto.resource = "fizzfizzbuzzbuzz";
        dto.value = value;
        dto.type = Integer.class;
        dto.timestamp = timestamp;
        return dto;
    }

    @Test
    void basicDecimalData(
            @InjectConfiguration(withConfig = @WithConfiguration(pid = "sensinact.history.timescale", location = "?")) Configuration cm)
            throws Exception {
        cm.update(new Hashtable<>(Map.of("url", container.getJdbcUrl(), "user", container.getUsername(), ".password",
                container.getPassword())));

        Thread.sleep(1000);

        push.pushUpdate(getDto(1.2d, BASE_TIMESTAMP)).getValue();
        push.pushUpdate(getDto(3.4d, BASE_TIMESTAMP.plus(ofDays(366)))).getValue();
        push.pushUpdate(getDto(5.6d, BASE_TIMESTAMP.plus(ofDays(366 + 365)))).getValue();

        Thread.sleep(1000);

        try (Connection connection = getDataSource().getConnection();
                ResultSet result = connection.createStatement().executeQuery(
                        "SELECT * FROM sensinact.numeric_data WHERE provider = 'Bobbidi' ORDER BY time;")) {

            assertTrue(result.next());
            checkResult(result, 1.2d, BASE_TIMESTAMP);
            assertTrue(result.next());
            checkResult(result, 3.4d, BASE_TIMESTAMP.plus(ofDays(366)));
            assertTrue(result.next());
            checkResult(result, 5.6d, BASE_TIMESTAMP.plus(ofDays(366 + 365)));
            assertFalse(result.next());
        }
    }

    private void checkResult(ResultSet result, Double data, Instant timestamp) throws SQLException {
        assertEquals("Bibbidi", result.getString("model"));
        assertEquals("Bobbidi", result.getString("provider"));
        assertEquals("Boo", result.getString("service"));
        assertEquals("Magic", result.getString("resource"));
        assertEquals(data, result.getBigDecimal("data").doubleValue());
        assertEquals(Timestamp.from(timestamp), result.getTimestamp("time"));
    }

    private GenericDto getDto(Double value, Instant timestamp) {
        GenericDto dto = new GenericDto();
        dto.model = "Bibbidi";
        dto.provider = "Bobbidi";
        dto.service = "Boo";
        dto.resource = "Magic";
        dto.value = BigDecimal.valueOf(value);
        dto.type = BigDecimal.class;
        dto.timestamp = timestamp;
        return dto;
    }
}

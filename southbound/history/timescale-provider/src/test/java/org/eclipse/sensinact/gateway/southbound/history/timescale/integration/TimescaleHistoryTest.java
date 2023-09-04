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
import static org.junit.Assert.assertNull;
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
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.command.ResourceCommand;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opentest4j.AssertionFailedError;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@ExtendWith({ BundleContextExtension.class, ConfigurationExtension.class, MockitoExtension.class })
public class TimescaleHistoryTest {

    private static final Instant TS_2012 = Instant.parse("2012-01-01T00:00:00.00Z");
    private static final Instant TS_2013 = Instant.parse("2013-01-01T00:00:00.00Z");
    private static final Instant TS_2014 = Instant.parse("2014-01-01T00:00:00.00Z");

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
    void startContainer(
            @InjectConfiguration(withConfig = @WithConfiguration(pid = "sensinact.history.timescale", location = "?")) Configuration cm)
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
        cm.update(new Hashtable<>(Map.of("url", container.getJdbcUrl(), "user", container.getUsername(), ".password",
                container.getPassword())));
        Thread.sleep(1000);
    }

    @AfterEach
    void stopContainer() {
        container.stop();
        thread.execute(new AbstractSensinactCommand<Void>() {
            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                    PromiseFactory promiseFactory) {
                twin.getProviders().forEach(SensinactProvider::delete);
                return null;
            }
        });
    }

    @InjectService
    DataUpdate push;
    @InjectService
    GatewayThread thread;

    private JdbcDatabaseContainer<?> container;

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

    private PGSimpleDataSource getDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setURL(container.getJdbcUrl());
        ds.setUser(container.getUsername());
        ds.setPassword(container.getPassword());
        return ds;
    }

    private void waitForRowCount(String table, int count) {
        try (Connection conn = getDataSource().getConnection()) {
            for (int i = 0; i < 50; i++) {
                try (ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + table)) {
                    assertTrue(rs.next());
                    int current = rs.getInt(1);
                    if (current == count) {
                        return;
                    } else if (current > count) {
                        throw new AssertionFailedError("The count for table " + table + " was " + current
                                + " which is larger than the expected " + count);
                    }
                }
                Thread.sleep(100);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    class InsertTests {
        @Test
        void basicStringData() throws Exception {
            push.pushUpdate(getDto("fizz", TS_2012)).getValue();
            push.pushUpdate(getDto("buzz", TS_2013)).getValue();
            push.pushUpdate(getDto("fizzbuzz", TS_2014)).getValue();

            waitForRowCount("sensinact.text_data", 7);

            try (Connection connection = getDataSource().getConnection();
                    ResultSet result = connection.createStatement()
                            .executeQuery("SELECT * FROM sensinact.text_data WHERE provider = 'bar' ORDER BY time;")) {

                assertTrue(result.next());
                checkResult(result, "admin", "friendlyName", "bar", TS_2012);
                // Just skip the modelURI
                assertTrue(result.next());
                assertTrue(result.next());
                checkResult(result, "fizz", TS_2012);
                assertTrue(result.next());
                checkResult(result, "buzz", TS_2013);
                assertTrue(result.next());
                checkResult(result, "fizzbuzz", TS_2014);
                assertFalse(result.next());
            }
        }

        private void checkResult(ResultSet result, String data, Instant timestamp) throws SQLException {
            checkResult(result, "foobar", "foofoobarbar", data, timestamp);
        }

        private void checkResult(ResultSet result, String service, String resource, String data, Instant timestamp)
                throws SQLException {
            assertEquals("foo", result.getString("model"));
            assertEquals("bar", result.getString("provider"));
            assertEquals(service, result.getString("service"));
            assertEquals(resource, result.getString("resource"));
            assertEquals(data, result.getString("data"));
            assertEquals(Timestamp.from(timestamp), result.getTimestamp("time"));
        }

        @Test
        void basicNumberData() throws Exception {
            push.pushUpdate(getDto(3, TS_2012)).getValue();
            push.pushUpdate(getDto(5, TS_2013)).getValue();
            push.pushUpdate(getDto(7, TS_2014)).getValue();

            waitForRowCount("sensinact.numeric_data", 3);

            try (Connection connection = getDataSource().getConnection();
                    ResultSet result = connection.createStatement().executeQuery(
                            "SELECT * FROM sensinact.numeric_data WHERE provider = 'buzz' ORDER BY time;")) {

                assertTrue(result.next());
                checkResult(result, 3, TS_2012);
                assertTrue(result.next());
                checkResult(result, 5, TS_2013);
                assertTrue(result.next());
                checkResult(result, 7, TS_2014);
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

        @Test
        void basicDecimalData() throws Exception {
            push.pushUpdate(getDto(1.2d, TS_2012)).getValue();
            push.pushUpdate(getDto(3.4d, TS_2013)).getValue();
            push.pushUpdate(getDto(5.6d, TS_2014)).getValue();

            waitForRowCount("sensinact.numeric_data", 3);

            try (Connection connection = getDataSource().getConnection();
                    ResultSet result = connection.createStatement().executeQuery(
                            "SELECT * FROM sensinact.numeric_data WHERE provider = 'Bobbidi' ORDER BY time;")) {

                assertTrue(result.next());
                checkResult(result, 1.2d, TS_2012);
                assertTrue(result.next());
                checkResult(result, 3.4d, TS_2013);
                assertTrue(result.next());
                checkResult(result, 5.6d, TS_2014);
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
    }

    @Nested
    class getSingleValueTests {
        @Test
        void basicStringData() throws Exception {
            push.pushUpdate(getDto("fizz", TS_2012)).getValue();
            push.pushUpdate(getDto("buzz", TS_2013)).getValue();
            push.pushUpdate(getDto("fizzbuzz", TS_2014)).getValue();

            waitForRowCount("sensinact.text_data", 7);

            thread.execute(new ResourceCommand<Void>("sensiNactHistory", "timescale-history", "history", "single") {

                @Override
                protected Promise<Void> call(SensinactResource resource, PromiseFactory pf) {
                    // If equal, return the value
                    TimedValue<?> result = safeGet(
                            resource.act(Map.of("provider", "bar", "service", "foobar", "resource", "foofoobarbar",
                                    "time", TS_2012.atOffset(ZoneOffset.UTC))).map(TimedValue.class::cast));
                    assertEquals("fizz", result.getValue());
                    assertEquals(TS_2012, result.getTimestamp());

                    // If in between, return the newest that is older
                    result = safeGet(resource.act(Map.of("provider", "bar", "service", "foobar", "resource",
                            "foofoobarbar", "time", TS_2012.plus(ofDays(500)).atOffset(ZoneOffset.UTC)))
                            .map(TimedValue.class::cast));
                    assertEquals("buzz", result.getValue());
                    assertEquals(TS_2013, result.getTimestamp());

                    // If null, return the oldest
                    result = safeGet(
                            resource.act(Map.of("provider", "bar", "service", "foobar", "resource", "foofoobarbar"))
                                    .map(TimedValue.class::cast));
                    assertEquals("fizz", result.getValue());
                    assertEquals(TS_2012, result.getTimestamp());
                    return pf.resolved(null);
                }
            }).getValue();
        }

        private <T> T safeGet(Promise<T> p) {
            try {
                return p.getValue();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        void basicNumberData() throws Exception {
            push.pushUpdate(getDto(1, TS_2012)).getValue();
            push.pushUpdate(getDto(2, TS_2013)).getValue();
            push.pushUpdate(getDto(3, TS_2014)).getValue();

            waitForRowCount("sensinact.numeric_data", 3);

            thread.execute(new ResourceCommand<Void>("sensiNactHistory", "timescale-history", "history", "single") {

                @Override
                protected Promise<Void> call(SensinactResource resource, PromiseFactory pf) {
                    // If equal, return the value
                    TimedValue<?> result = safeGet(resource.act(Map.of("provider", "buzz", "service", "fizzbuzz",
                            "resource", "fizzfizzbuzzbuzz", "time", TS_2012.atOffset(ZoneOffset.UTC)))
                            .map(TimedValue.class::cast));
                    assertEquals(1L, result.getValue());
                    assertEquals(TS_2012, result.getTimestamp());

                    // If in between, return the newest that is older
                    result = safeGet(resource
                            .act(Map.of("provider", "buzz", "service", "fizzbuzz", "resource", "fizzfizzbuzzbuzz",
                                    "time", TS_2012.plus(ofDays(500)).atOffset(ZoneOffset.UTC)))
                            .map(TimedValue.class::cast));
                    assertEquals(2L, result.getValue());
                    assertEquals(TS_2013, result.getTimestamp());

                    // If null, return the oldest
                    result = safeGet(resource
                            .act(Map.of("provider", "buzz", "service", "fizzbuzz", "resource", "fizzfizzbuzzbuzz"))
                            .map(TimedValue.class::cast));
                    assertEquals(1L, result.getValue());
                    assertEquals(TS_2012, result.getTimestamp());
                    return pf.resolved(null);
                }
            }).getValue();
        }

        @Test
        void basicDecimalData() throws Exception {
            push.pushUpdate(getDto(1.2d, TS_2012)).getValue();
            push.pushUpdate(getDto(3.4d, TS_2013)).getValue();
            push.pushUpdate(getDto(5.6d, TS_2014)).getValue();

            waitForRowCount("sensinact.numeric_data", 3);

            thread.execute(new ResourceCommand<Void>("sensiNactHistory", "timescale-history", "history", "single") {

                @Override
                protected Promise<Void> call(SensinactResource resource, PromiseFactory pf) {
                    // If equal, return the value
                    TimedValue<?> result = safeGet(
                            resource.act(Map.of("provider", "Bobbidi", "service", "Boo", "resource", "Magic", "time",
                                    TS_2012.atOffset(ZoneOffset.UTC))).map(TimedValue.class::cast));
                    assertEquals(1.2d, result.getValue());
                    assertEquals(TS_2012, result.getTimestamp());

                    // If in between, return the newest that is older
                    result = safeGet(resource.act(Map.of("provider", "Bobbidi", "service", "Boo", "resource", "Magic",
                            "time", TS_2012.plus(ofDays(500)).atOffset(ZoneOffset.UTC))).map(TimedValue.class::cast));
                    assertEquals(3.4d, result.getValue());
                    assertEquals(TS_2013, result.getTimestamp());

                    // If null, return the oldest
                    result = safeGet(resource.act(Map.of("provider", "Bobbidi", "service", "Boo", "resource", "Magic"))
                            .map(TimedValue.class::cast));
                    assertEquals(1.2d, result.getValue());
                    assertEquals(TS_2012, result.getTimestamp());
                    return pf.resolved(null);
                }
            }).getValue();
        }
    }

    @Nested
    class getMultiValueTests {
        @Test
        void basicStringData() throws Exception {
            push.pushUpdate(getDto("fizz", TS_2012)).getValue();
            push.pushUpdate(getDto("buzz", TS_2013)).getValue();
            push.pushUpdate(getDto("fizzbuzz", TS_2014)).getValue();

            waitForRowCount("sensinact.text_data", 7);

            thread.execute(new ResourceCommand<Void>("sensiNactHistory", "timescale-history", "history", "range") {

                @SuppressWarnings("unchecked")
                @Override
                protected Promise<Void> call(SensinactResource resource, PromiseFactory pf) {
                    // If equal, return the value
                    List<TimedValue<?>> result = safeGet(resource
                            .act(Map.of("provider", "bar", "service", "foobar", "resource", "foofoobarbar", "fromTime",
                                    TS_2012.atOffset(ZoneOffset.UTC), "toTime", TS_2013.atOffset(ZoneOffset.UTC)))
                            .map(List.class::cast));
                    assertEquals(2, result.size());
                    assertEquals("fizz", result.get(0).getValue());
                    assertEquals(TS_2012, result.get(0).getTimestamp());
                    assertEquals("buzz", result.get(1).getValue());
                    assertEquals(TS_2013, result.get(1).getTimestamp());

                    // No Limit
                    result = safeGet(resource.act(Map.of("provider", "bar", "service", "foobar", "resource",
                            "foofoobarbar", "fromTime", TS_2012.plus(ofDays(1)).atOffset(ZoneOffset.UTC)))
                            .map(List.class::cast));
                    assertEquals(2, result.size());
                    assertEquals("buzz", result.get(0).getValue());
                    assertEquals(TS_2013, result.get(0).getTimestamp());
                    assertEquals("fizzbuzz", result.get(1).getValue());
                    assertEquals(TS_2014, result.get(1).getTimestamp());

                    return pf.resolved(null);
                }
            }).getValue();
        }

        private <T> T safeGet(Promise<T> p) {
            try {
                return p.getValue();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Test
        void basicNumberData() throws Exception {
            push.pushUpdate(getDto(1, TS_2012)).getValue();
            push.pushUpdate(getDto(2, TS_2013)).getValue();
            push.pushUpdate(getDto(3, TS_2014)).getValue();

            waitForRowCount("sensinact.numeric_data", 3);

            thread.execute(new ResourceCommand<Void>("sensiNactHistory", "timescale-history", "history", "range") {

                @SuppressWarnings("unchecked")
                @Override
                protected Promise<Void> call(SensinactResource resource, PromiseFactory pf) {
                    // If equal, return the value
                    List<TimedValue<?>> result = safeGet(resource.act(Map.of("provider", "buzz", "service", "fizzbuzz",
                            "resource", "fizzfizzbuzzbuzz", "fromTime", TS_2012.atOffset(ZoneOffset.UTC), "toTime",
                            TS_2013.atOffset(ZoneOffset.UTC))).map(List.class::cast));
                    assertEquals(2, result.size());
                    assertEquals(1L, result.get(0).getValue());
                    assertEquals(TS_2012, result.get(0).getTimestamp());
                    assertEquals(2L, result.get(1).getValue());
                    assertEquals(TS_2013, result.get(1).getTimestamp());

                    // No Limit
                    result = safeGet(resource
                            .act(Map.of("provider", "buzz", "service", "fizzbuzz", "resource", "fizzfizzbuzzbuzz",
                                    "fromTime", TS_2012.plus(ofDays(1)).atOffset(ZoneOffset.UTC)))
                            .map(List.class::cast));
                    assertEquals(2, result.size());
                    assertEquals(2L, result.get(0).getValue());
                    assertEquals(TS_2013, result.get(0).getTimestamp());
                    assertEquals(3L, result.get(1).getValue());
                    assertEquals(TS_2014, result.get(1).getTimestamp());

                    return pf.resolved(null);
                }
            }).getValue();
        }

        @Test
        void basicDecimalData() throws Exception {
            push.pushUpdate(getDto(1.2d, TS_2012)).getValue();
            push.pushUpdate(getDto(3.4d, TS_2013)).getValue();
            push.pushUpdate(getDto(5.6d, TS_2014)).getValue();

            waitForRowCount("sensinact.numeric_data", 3);

            thread.execute(new ResourceCommand<Void>("sensiNactHistory", "timescale-history", "history", "range") {

                @SuppressWarnings("unchecked")
                @Override
                protected Promise<Void> call(SensinactResource resource, PromiseFactory pf) {
                    // If equal, return the value
                    List<TimedValue<?>> result = safeGet(resource
                            .act(Map.of("provider", "Bobbidi", "service", "Boo", "resource", "Magic", "fromTime",
                                    TS_2012.atOffset(ZoneOffset.UTC), "toTime", TS_2013.atOffset(ZoneOffset.UTC)))
                            .map(List.class::cast));
                    assertEquals(2, result.size());
                    assertEquals(1.2d, result.get(0).getValue());
                    assertEquals(TS_2012, result.get(0).getTimestamp());
                    assertEquals(3.4d, result.get(1).getValue());
                    assertEquals(TS_2013, result.get(1).getTimestamp());

                    // No Limit
                    result = safeGet(resource.act(Map.of("provider", "Bobbidi", "service", "Boo", "resource", "Magic",
                            "fromTime", TS_2012.plus(ofDays(1)).atOffset(ZoneOffset.UTC))).map(List.class::cast));
                    assertEquals(2, result.size());
                    assertEquals(3.4d, result.get(0).getValue());
                    assertEquals(TS_2013, result.get(0).getTimestamp());
                    assertEquals(5.6d, result.get(1).getValue());
                    assertEquals(TS_2014, result.get(1).getTimestamp());

                    return pf.resolved(null);
                }
            }).getValue();
        }

        @Test
        void manyStringData() throws Exception {
            for (int i = 0; i < 1000; i++) {
                push.pushUpdate(getDto(String.valueOf(i), TS_2012.plus(ofDays(i)))).getValue();
            }

            waitForRowCount("sensinact.text_data", 1004);

            thread.execute(new ResourceCommand<Void>("sensiNactHistory", "timescale-history", "history", "range") {

                @SuppressWarnings("unchecked")
                @Override
                protected Promise<Void> call(SensinactResource resource, PromiseFactory pf) {
                    // If equal, return the value
                    List<TimedValue<?>> result = safeGet(resource
                            .act(Map.of("provider", "bar", "service", "foobar", "resource", "foofoobarbar", "fromTime",
                                    TS_2012.atOffset(ZoneOffset.UTC), "toTime", TS_2013.atOffset(ZoneOffset.UTC)))
                            .map(List.class::cast));
                    assertEquals(367, result.size());
                    for (int i = 0; i < 367; i++) {
                        assertEquals(String.valueOf(i), result.get(i).getValue());
                        assertEquals(TS_2012.plus(ofDays(i)), result.get(i).getTimestamp());
                    }

                    // same query, skip 50
                    result = safeGet(resource.act(Map.of("provider", "bar", "service", "foobar", "resource",
                            "foofoobarbar", "fromTime", TS_2012.atOffset(ZoneOffset.UTC), "toTime",
                            TS_2013.atOffset(ZoneOffset.UTC), "skip", 50)).map(List.class::cast));
                    assertEquals(317, result.size());
                    for (int i = 0; i < 317; i++) {
                        assertEquals(String.valueOf(i + 50), result.get(i).getValue());
                        assertEquals(TS_2012.plus(ofDays(i + 50)), result.get(i).getTimestamp());
                    }

                    // No Limit
                    result = safeGet(resource.act(Map.of("provider", "bar", "service", "foobar", "resource",
                            "foofoobarbar", "fromTime", TS_2012.plus(ofDays(1)).atOffset(ZoneOffset.UTC)))
                            .map(List.class::cast));
                    assertEquals(501, result.size());
                    for (int i = 0; i < 500; i++) {
                        assertEquals(String.valueOf(i + 1), result.get(i).getValue());
                        assertEquals(TS_2012.plus(ofDays(i + 1)), result.get(i).getTimestamp());
                    }
                    assertNull(result.get(500).getTimestamp());
                    assertNull(result.get(500).getValue());

                    // No start - get the latest 500 before the to time
                    result = safeGet(resource.act(Map.of("provider", "bar", "service", "foobar", "resource",
                            "foofoobarbar", "toTime", TS_2014.atOffset(ZoneOffset.UTC))).map(List.class::cast));
                    assertEquals(500, result.size());
                    long valueAt2014 = TS_2012.until(TS_2014, ChronoUnit.DAYS);
                    for (int i = 0; i < 500; i++) {
                        assertEquals(String.valueOf(valueAt2014 - 499 + i), result.get(i).getValue());
                        assertEquals(TS_2014.minus(ofDays(499 - i)), result.get(i).getTimestamp());
                    }

                    // No start or end - get the latest 500
                    result = safeGet(
                            resource.act(Map.of("provider", "bar", "service", "foobar", "resource", "foofoobarbar"))
                                    .map(List.class::cast));
                    assertEquals(500, result.size());

                    for (int i = 0; i < 500; i++) {
                        assertEquals(String.valueOf(500 + i), result.get(i).getValue());
                        assertEquals(TS_2012.plus(ofDays(500 + i)), result.get(i).getTimestamp());
                    }

                    return pf.resolved(null);
                }
            }).getValue();
        }

        @Test
        void manyStringCount() throws Exception {
            for (int i = 0; i < 1000; i++) {
                push.pushUpdate(getDto(String.valueOf(i), TS_2012.plus(ofDays(i)))).getValue();
            }

            waitForRowCount("sensinact.text_data", 1004);

            thread.execute(new ResourceCommand<Void>("sensiNactHistory", "timescale-history", "history", "count") {

                @Override
                protected Promise<Void> call(SensinactResource resource, PromiseFactory pf) {
                    // If equal, return the value
                    Long result = safeGet(resource
                            .act(Map.of("provider", "bar", "service", "foobar", "resource", "foofoobarbar", "fromTime",
                                    TS_2012.atOffset(ZoneOffset.UTC), "toTime", TS_2013.atOffset(ZoneOffset.UTC)))
                            .map(Long.class::cast));
                    assertEquals(367, result);

                    // No Limit
                    result = safeGet(resource.act(Map.of("provider", "bar", "service", "foobar", "resource",
                            "foofoobarbar", "fromTime", TS_2012.plus(ofDays(1)).atOffset(ZoneOffset.UTC)))
                            .map(Long.class::cast));
                    assertEquals(999, result);

                    // No start - get the latest 500 before the to time
                    result = safeGet(resource.act(Map.of("provider", "bar", "service", "foobar", "resource",
                            "foofoobarbar", "toTime", TS_2014.atOffset(ZoneOffset.UTC))).map(Long.class::cast));
                    assertEquals(366 + 365 + 1, result);

                    // No start or end - get the latest 500
                    result = safeGet(
                            resource.act(Map.of("provider", "bar", "service", "foobar", "resource", "foofoobarbar"))
                                    .map(Long.class::cast));
                    assertEquals(1000, result);

                    return pf.resolved(null);
                }
            }).getValue();
        }

        @Test
        void manyNumberData() throws Exception {
            for (int i = 0; i < 1000; i++) {
                push.pushUpdate(getDto(i, TS_2012.plus(ofDays(i)))).getValue();
            }

            waitForRowCount("sensinact.text_data", 1002);

            thread.execute(new ResourceCommand<Void>("sensiNactHistory", "timescale-history", "history", "range") {

                @SuppressWarnings("unchecked")
                @Override
                protected Promise<Void> call(SensinactResource resource, PromiseFactory pf) {
                    // If equal, return the value
                    List<TimedValue<?>> result = safeGet(resource.act(Map.of("provider", "buzz", "service", "fizzbuzz",
                            "resource", "fizzfizzbuzzbuzz", "fromTime", TS_2012.atOffset(ZoneOffset.UTC), "toTime",
                            TS_2013.atOffset(ZoneOffset.UTC))).map(List.class::cast));
                    assertEquals(367, result.size());
                    for (int i = 0; i < 367; i++) {
                        assertEquals(Long.valueOf(i), result.get(i).getValue());
                        assertEquals(TS_2012.plus(ofDays(i)), result.get(i).getTimestamp());
                    }

                    // same query, skip 50
                    result = safeGet(resource.act(Map.of("provider", "buzz", "service", "fizzbuzz", "resource",
                            "fizzfizzbuzzbuzz", "fromTime", TS_2012.atOffset(ZoneOffset.UTC), "toTime",
                            TS_2013.atOffset(ZoneOffset.UTC), "skip", 50)).map(List.class::cast));
                    assertEquals(317, result.size());
                    for (int i = 0; i < 317; i++) {
                        assertEquals(Long.valueOf(i + 50), result.get(i).getValue());
                        assertEquals(TS_2012.plus(ofDays(i + 50)), result.get(i).getTimestamp());
                    }

                    // No Limit
                    result = safeGet(resource
                            .act(Map.of("provider", "buzz", "service", "fizzbuzz", "resource", "fizzfizzbuzzbuzz",
                                    "fromTime", TS_2012.plus(ofDays(1)).atOffset(ZoneOffset.UTC)))
                            .map(List.class::cast));
                    assertEquals(501, result.size());
                    for (int i = 0; i < 500; i++) {
                        assertEquals(Long.valueOf(i + 1), result.get(i).getValue());
                        assertEquals(TS_2012.plus(ofDays(i + 1)), result.get(i).getTimestamp());
                    }
                    assertNull(result.get(500).getTimestamp());
                    assertNull(result.get(500).getValue());

                    // No start - get the latest 500 before the to time
                    result = safeGet(resource.act(Map.of("provider", "buzz", "service", "fizzbuzz", "resource",
                            "fizzfizzbuzzbuzz", "toTime", TS_2014.atOffset(ZoneOffset.UTC))).map(List.class::cast));
                    assertEquals(500, result.size());
                    long valueAt2014 = TS_2012.until(TS_2014, ChronoUnit.DAYS);
                    for (int i = 0; i < 500; i++) {
                        assertEquals(Long.valueOf(valueAt2014 - 499 + i), result.get(i).getValue());
                        assertEquals(TS_2014.minus(ofDays(499 - i)), result.get(i).getTimestamp());
                    }

                    // No start or end - get the latest 500
                    result = safeGet(resource
                            .act(Map.of("provider", "buzz", "service", "fizzbuzz", "resource", "fizzfizzbuzzbuzz"))
                            .map(List.class::cast));
                    assertEquals(500, result.size());

                    for (int i = 0; i < 500; i++) {
                        assertEquals(Long.valueOf(500 + i), result.get(i).getValue());
                        assertEquals(TS_2012.plus(ofDays(500 + i)), result.get(i).getTimestamp());
                    }

                    return pf.resolved(null);
                }
            }).getValue();
        }

        @Test
        void manyNumberCount() throws Exception {
            for (int i = 0; i < 1000; i++) {
                push.pushUpdate(getDto(i, TS_2012.plus(ofDays(i)))).getValue();
            }

            waitForRowCount("sensinact.text_data", 1002);

            thread.execute(new ResourceCommand<Void>("sensiNactHistory", "timescale-history", "history", "count") {

                @Override
                protected Promise<Void> call(SensinactResource resource, PromiseFactory pf) {
                    // If equal, return the value
                    Long result = safeGet(resource.act(Map.of("provider", "buzz", "service", "fizzbuzz", "resource",
                            "fizzfizzbuzzbuzz", "fromTime", TS_2012.atOffset(ZoneOffset.UTC), "toTime",
                            TS_2013.atOffset(ZoneOffset.UTC))).map(Long.class::cast));
                    assertEquals(367, result);

                    // No Limit
                    result = safeGet(resource
                            .act(Map.of("provider", "buzz", "service", "fizzbuzz", "resource", "fizzfizzbuzzbuzz",
                                    "fromTime", TS_2012.plus(ofDays(1)).atOffset(ZoneOffset.UTC)))
                            .map(Long.class::cast));
                    assertEquals(999, result);

                    // No start
                    result = safeGet(resource.act(Map.of("provider", "buzz", "service", "fizzbuzz", "resource",
                            "fizzfizzbuzzbuzz", "toTime", TS_2014.atOffset(ZoneOffset.UTC))).map(Long.class::cast));
                    assertEquals(366 + 365 + 1, result);

                    // No start or end
                    result = safeGet(resource
                            .act(Map.of("provider", "buzz", "service", "fizzbuzz", "resource", "fizzfizzbuzzbuzz"))
                            .map(Long.class::cast));
                    assertEquals(1000, result);

                    return pf.resolved(null);
                }
            }).getValue();
        }

        @Test
        void manyDecimalData() throws Exception {
            for (int i = 0; i < 1000; i++) {
                push.pushUpdate(getDto(1.0001d * i, TS_2012.plus(ofDays(i)))).getValue();
            }

            waitForRowCount("sensinact.text_data", 1002);

            thread.execute(new ResourceCommand<Void>("sensiNactHistory", "timescale-history", "history", "range") {

                @SuppressWarnings("unchecked")
                @Override
                protected Promise<Void> call(SensinactResource resource, PromiseFactory pf) {
                    // If equal, return the value
                    List<TimedValue<?>> result = safeGet(resource
                            .act(Map.of("provider", "Bobbidi", "service", "Boo", "resource", "Magic", "fromTime",
                                    TS_2012.atOffset(ZoneOffset.UTC), "toTime", TS_2013.atOffset(ZoneOffset.UTC)))
                            .map(List.class::cast));
                    assertEquals(367, result.size());
                    for (int i = 0; i < 367; i++) {
                        assertEquals(Double.valueOf(1.0001d * i), (double) result.get(i).getValue(), 0.0001d);
                        assertEquals(TS_2012.plus(ofDays(i)), result.get(i).getTimestamp());
                    }

                    // same query, skip 50
                    result = safeGet(resource.act(Map.of("provider", "Bobbidi", "service", "Boo", "resource", "Magic",
                            "fromTime", TS_2012.atOffset(ZoneOffset.UTC), "toTime", TS_2013.atOffset(ZoneOffset.UTC),
                            "skip", 50)).map(List.class::cast));
                    assertEquals(317, result.size());
                    for (int i = 0; i < 317; i++) {
                        assertEquals(Double.valueOf(1.0001d * (i + 50)), (double) result.get(i).getValue(), 0.0001d);
                        assertEquals(TS_2012.plus(ofDays(i + 50)), result.get(i).getTimestamp());
                    }

                    // No Limit
                    result = safeGet(resource.act(Map.of("provider", "Bobbidi", "service", "Boo", "resource", "Magic",
                            "fromTime", TS_2012.plus(ofDays(1)).atOffset(ZoneOffset.UTC))).map(List.class::cast));
                    assertEquals(501, result.size());
                    for (int i = 0; i < 500; i++) {
                        assertEquals(Double.valueOf(1.0001d * (i + 1)), (double) result.get(i).getValue(), 0.0001d);
                        assertEquals(TS_2012.plus(ofDays(i + 1)), result.get(i).getTimestamp());
                    }
                    assertNull(result.get(500).getTimestamp());
                    assertNull(result.get(500).getValue());

                    // No start - get the latest 500 before the to time
                    result = safeGet(resource.act(Map.of("provider", "Bobbidi", "service", "Boo", "resource", "Magic",
                            "toTime", TS_2014.atOffset(ZoneOffset.UTC))).map(List.class::cast));
                    assertEquals(500, result.size());
                    double valueAt2014 = 1.0001d * (TS_2012.until(TS_2014, ChronoUnit.DAYS));
                    for (int i = 0; i < 500; i++) {
                        assertEquals(Double.valueOf(valueAt2014 - ((499 - i) * 1.0001d)),
                                (double) result.get(i).getValue(), 0.0001d);
                        assertEquals(TS_2014.minus(ofDays(499 - i)), result.get(i).getTimestamp());
                    }

                    // No start or end - get the latest 500
                    result = safeGet(resource.act(Map.of("provider", "Bobbidi", "service", "Boo", "resource", "Magic"))
                            .map(List.class::cast));
                    assertEquals(500, result.size());

                    for (int i = 0; i < 500; i++) {
                        assertEquals(Double.valueOf(1.0001d * (500 + i)), (double) result.get(i).getValue(), 0.0001d);
                        assertEquals(TS_2012.plus(ofDays(500 + i)), result.get(i).getTimestamp());
                    }

                    return pf.resolved(null);
                }
            }).getValue();
        }

        @Test
        void manyDecimalCount() throws Exception {
            for (int i = 0; i < 1000; i++) {
                push.pushUpdate(getDto(1.0001d * i, TS_2012.plus(ofDays(i)))).getValue();
            }

            waitForRowCount("sensinact.text_data", 1002);

            thread.execute(new ResourceCommand<Void>("sensiNactHistory", "timescale-history", "history", "count") {

                @Override
                protected Promise<Void> call(SensinactResource resource, PromiseFactory pf) {
                    // If equal, return the value
                    Long result = safeGet(resource
                            .act(Map.of("provider", "Bobbidi", "service", "Boo", "resource", "Magic", "fromTime",
                                    TS_2012.atOffset(ZoneOffset.UTC), "toTime", TS_2013.atOffset(ZoneOffset.UTC)))
                            .map(Long.class::cast));
                    assertEquals(367, result);

                    // No Limit
                    result = safeGet(resource.act(Map.of("provider", "Bobbidi", "service", "Boo", "resource", "Magic",
                            "fromTime", TS_2012.plus(ofDays(1)).atOffset(ZoneOffset.UTC))).map(Long.class::cast));
                    assertEquals(999, result);

                    // No start
                    result = safeGet(resource.act(Map.of("provider", "Bobbidi", "service", "Boo", "resource", "Magic",
                            "toTime", TS_2014.atOffset(ZoneOffset.UTC))).map(Long.class::cast));
                    assertEquals(366 + 365 + 1, result);

                    // No start or end
                    result = safeGet(resource.act(Map.of("provider", "Bobbidi", "service", "Boo", "resource", "Magic"))
                            .map(Long.class::cast));
                    assertEquals(1000, result);

                    return pf.resolved(null);
                }
            }).getValue();
        }
    }
}

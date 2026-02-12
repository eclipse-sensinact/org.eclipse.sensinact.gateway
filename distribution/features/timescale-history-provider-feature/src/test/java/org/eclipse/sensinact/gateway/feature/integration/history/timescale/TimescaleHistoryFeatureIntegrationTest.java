/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.gateway.feature.integration.history.timescale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.abort;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Hashtable;
import java.util.Map;

import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import jakarta.json.spi.JsonProvider;

import org.apache.felix.cm.json.io.ConfigurationReader;
import org.apache.felix.cm.json.io.ConfigurationResource;
import org.apache.felix.cm.json.io.Configurations;
import org.eclipse.sensinact.gateway.feature.utilities.test.ServerProcessHandler;
import org.eclipse.sensinact.gateway.test.testcontainers.postgres.RequirePostgresContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@RequirePostgresContainer
class TimescaleHistoryFeatureIntegrationTest {

    private static ServerProcessHandler server = new ServerProcessHandler();

    private static JdbcDatabaseContainer<?> container;

    @BeforeAll
    static void check() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(DockerClientFactory.class.getClassLoader());
        try {
            DockerClientFactory.lazyClient().versionCmd().exec();
        } catch (Throwable t) {
            abort("No docker executable on the path, so tests will be skipped");
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }

        container = new PostgreSQLContainer(DockerImageName.parse("timescale/timescaledb-ha")
                .asCompatibleSubstituteFor("postgres").withTag("pg14-latest"));

        container.withDatabaseName("sensinactHistory");
        cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(DockerClientFactory.class.getClassLoader());
            container.start();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
        Thread.sleep(1000);

        Path configFolder = Paths.get("target/it/config");
        Files.createDirectories(configFolder);

        try (BufferedReader br = Files.newBufferedReader(Paths.get("src/it/resources/config/configuration.json"));
                BufferedWriter bw = Files.newBufferedWriter(configFolder.resolve("configuration.json"),
                        StandardOpenOption.CREATE)) {
            ConfigurationReader configReader = Configurations.buildReader()
                    .withConfiguratorPropertyHandler((a, b, c) -> {
                    }).build(br);
            ConfigurationResource cr = configReader.readConfigurationResource();

            cr.getConfigurations().put("sensinact.history.timescale", new Hashtable<>(Map.of("url",
                    container.getJdbcUrl(), "user", container.getUsername(), ".password", container.getPassword())));

            Configurations.buildWriter().build(bw).writeConfigurationResource(cr);
        }

        server.setConfigFolder(configFolder.toString());
        server.startSensinact();
    }

    @AfterAll
    static void stop() throws Exception {
        server.stopSensinact();
        if (container != null) {
            container.stop();
        }
    }

    @Test
    void testHttp() throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8083/sensinact")).GET().build();

        // Wait for an Http endpoint
        waitFor(client, request);

        request = HttpRequest
                .newBuilder(URI.create(
                        "http://localhost:8083/sensinact/providers/temp_1/services/sensor/resources/temperature/GET"))
                .GET().build();
        waitFor(client, request);

        // Now wait for at least one historic value to be recorded
        Thread.sleep(3000);

        request = HttpRequest.newBuilder(URI.create(
                "http://localhost:8083/sensinact/providers/timescale-history/services/history/resources/single/ACT"))
                .POST(BodyPublishers.ofString(
                        "[{\"name\": \"provider\", \"value\": \"temp_1\"}, {\"name\": \"service\", \"value\": \"sensor\"}, {\"name\": \"resource\", \"value\": \"temperature\"}]"))
                .header("Content-Type", "application/json").build();

        JsonObject result = waitFor(client, request);

        JsonObject timedValue = result.getJsonObject("response");

        assertTrue(timedValue.containsKey("timestamp"));
        assertTrue(timedValue.containsKey("value"));

        Instant time = Instant.parse(timedValue.getString("timestamp"));
        Instant now = Instant.now();
        assertTrue(time.isBefore(now));
        assertTrue(time.isAfter(now.minus(Duration.ofSeconds(30))));

        JsonValue value = timedValue.get("value");
        assertTrue(value.getValueType() == ValueType.NUMBER);
        assertEquals(15d, ((JsonNumber) value).doubleValue(), 15d);

    }

    private JsonObject waitFor(HttpClient client, HttpRequest request) throws IOException, InterruptedException {
        JsonObject result = null;
        boolean found = false;
        for (int i = 0; i < 10; i++) {
            if (!server.isAlive()) {
                fail("Server process lost");
            }

            try {
                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
                if (200 == response.statusCode()) {
                    try (JsonReader jr = JsonProvider.provider().createReader(new StringReader(response.body()))) {
                        result = jr.readObject();
                    }
                    if (result.getInt("statusCode") == 200) {
                        found = true;
                        break;
                    } else {
                        result = null;
                    }
                }
            } catch (ConnectException | HttpConnectTimeoutException timeout) {
                // Just try again in a little while
            }
            Thread.sleep(1000);
        }
        assertTrue(found, "Did not contact the server");
        return result;
    }
}

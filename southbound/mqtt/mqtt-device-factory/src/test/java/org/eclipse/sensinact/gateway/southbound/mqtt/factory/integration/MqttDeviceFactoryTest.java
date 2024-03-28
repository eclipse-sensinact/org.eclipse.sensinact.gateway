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
package org.eclipse.sensinact.gateway.southbound.mqtt.factory.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.security.UserInfo;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.northbound.session.ResourceDescription;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.namespace.service.ServiceNamespace;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;

import io.moquette.BrokerConstants;
import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;

/**
 * Tests for the MQTT device factory
 */
@WithFactoryConfiguration(factoryPid = "sensinact.southbound.mqtt", name = "h1", location = "?", properties = {
        @Property(key = "id", value = "handler1"), @Property(key = "host", value = "127.0.0.1"),
        @Property(key = "port", value = "2183"), @Property(key = "topics", value = "sensinact/mqtt/test1/+"), })
@WithFactoryConfiguration(factoryPid = "sensinact.southbound.mqtt", name = "h2", location = "?", properties = {
        @Property(key = "id", value = "handler2"), @Property(key = "host", value = "127.0.0.1"),
        @Property(key = "port", value = "2183"), @Property(key = "topics", value = "sensinact/mqtt/test2/+"), })
@WithConfiguration(pid = "sensinact.mqtt.device.factory", location = "?", properties = {
        @Property(key = "mqtt.handler.id", value = "handler1"),
        @Property(key = "mapping", value = "{\n" + "  \"parser\": \"csv\",\n"
                + "  \"parser.options\": { \"header\": true },\n" + "  \"mapping\": {\n"
                + "    \"@provider\": \"Name\",\n"
                + "    \"@latitude\": { \"path\": \"Latitude\", \"type\": \"float\" },\n"
                + "    \"@longitude\": { \"path\": \"Longitude\", \"type\": \"float\" },\n"
                + "    \"@date\": \"Date\", \"@time\": \"Time\",\n"
                + "    \"data/testName\": { \"literal\": \"${context.topic-2}\" },\n"
                + "    \"data/value\": { \"path\": \"Value\", \"type\": \"int\" }\n" + "  },\n"
                + "  \"mapping.options\": { \"format.date\": \"d.M.y\", \"format.time\": \"H:m\" }\n" + "}"), })
@Requirement(namespace = ServiceNamespace.SERVICE_NAMESPACE, filter = "(objectClass=org.eclipse.sensinact.northbound.session.SensiNactSessionManager)")
public class MqttDeviceFactoryTest {

    private static final UserInfo USER = UserInfo.ANONYMOUS;

    private static Server server;

    @InjectService
    SensiNactSessionManager sessionManager;
    SensiNactSession session;

    BlockingQueue<ResourceDataNotification> queue;

    @BeforeAll
    static void startBroker() throws IOException {
        server = new Server();
        IConfig config = new MemoryConfig(new Properties());
        config.setProperty(BrokerConstants.HOST_PROPERTY_NAME, "127.0.0.1");
        config.setProperty(BrokerConstants.PORT_PROPERTY_NAME, "2183");
        server.startServer(config);
    }

    @AfterAll
    static void stopBroker() throws IOException {
        server.stopServer();
    }

    @BeforeEach
    void start() throws InterruptedException {
        session = sessionManager.getDefaultSession(USER);
        queue = new ArrayBlockingQueue<>(32);
    }

    @AfterEach
    void stop() {
        session.activeListeners().keySet().forEach(session::removeListener);
        session = null;
    }

    /**
     * Opens the given file from resources
     */
    byte[] readFile(final String filename) throws IOException {
        try (InputStream inStream = getClass().getClassLoader().getResourceAsStream("/" + filename)) {
            return inStream.readAllBytes();
        }
    }

    /**
     * Tests device registration
     */
    @Test
    void testWorking() throws Exception {
        // Excepted providers
        final String provider1 = "typed-provider1";
        final String provider2 = "typed-provider2";

        // Register listener
        session.addListener(List.of(provider1 + "/*"), (t, e) -> queue.offer(e), null, null, null);

        // Providers shouldn't exist yet
        assertNull(session.describeProvider(provider1));
        assertNull(session.describeProvider(provider2));

        // Read the file
        byte[] csvContent = readFile("csv-header-typed.csv");

        // Send MQTT message on handler 1
        final MqttClient client = new MqttClient("tcp://127.0.0.1:2183", MqttClient.generateClientId());
        client.connect();
        try {
            client.publish("sensinact/mqtt/test1/handler", csvContent, 1, false);
        } finally {
            client.disconnect();
            client.close();
        }

        // Wait for the provider to appear
        assertNotNull(queue.poll(1, TimeUnit.SECONDS));

        // Ensure resource type
        assertEquals(42, session.getResourceValue(provider1, "data", "value", Integer.class));
        assertEquals(84, session.getResourceValue(provider2, "data", "value", Integer.class));

        // Ensure timestamp
        Instant timestamp1 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 14, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp1, session.describeResource(provider1, "data", "value").timestamp);

        Instant timestamp2 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 17, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp2, session.describeResource(provider2, "data", "value").timestamp);

        // Ensure location update (and its timestamp)
        ResourceDescription location1 = session.describeResource(provider1, "admin", "location");
        assertEquals(timestamp1, location1.timestamp);
        assertNotNull(location1.value);
        Point geoPoint = (Point) location1.value;
        assertEquals(1.2, geoPoint.coordinates.latitude, 0.001);
        assertEquals(3.4, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));

        ResourceDescription location2 = session.describeResource(provider2, "admin", "location");
        assertNotNull(location2.value);
        assertEquals(timestamp2, location2.timestamp);
        geoPoint = (Point) location2.value;
        assertEquals(5.6, geoPoint.coordinates.latitude, 0.001);
        assertEquals(7.8, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));

        // Ensure resources from context
        assertEquals("test1", session.getResourceValue(provider1, "data", "testName", String.class));
        assertEquals("test1", session.getResourceValue(provider2, "data", "testName", String.class));
    }

    /**
     * Tests device registration
     */
    @Test
    void testHandlerFilter() throws Exception {
        // Excepted providers
        final String provider1 = "handler-provider1";

        // Register listener
        session.addListener(List.of(provider1 + "/*"), (t, e) -> queue.offer(e), null, null, null);

        // Providers shouldn't exist yet
        assertNull(session.describeProvider(provider1));

        // Read the file and change the provider IDs
        byte[] csvContent = new String(readFile("csv-header-typed.csv"), StandardCharsets.UTF_8)
                .replace("typed-provider", "handler-provider").getBytes(StandardCharsets.UTF_8);

        // Send MQTT message on handler 2
        final MqttClient client = new MqttClient("tcp://broker.hivemq.com:1883", MqttClient.generateClientId());
        client.connect();
        try {
            client.publish("sensinact/mqtt/test2/handler", csvContent, 1, false);
        } finally {
            client.disconnect();
            client.close();
        }

        // Wait for the provider to NOT appear
        assertNull(queue.poll(1, TimeUnit.SECONDS));
        assertNull(session.describeProvider(provider1));
    }
}

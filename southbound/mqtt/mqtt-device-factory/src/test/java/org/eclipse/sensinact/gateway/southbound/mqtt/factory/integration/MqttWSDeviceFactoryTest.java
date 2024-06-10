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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
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
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.osgi.util.promise.Promises;

import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;

/**
 * Tests for the MQTT device factory using WebSockets
 */
@WithFactoryConfiguration(factoryPid = "sensinact.southbound.mqtt", name = "h1", location = "?", properties = {
        @Property(key = "id", value = "handlerWS"), @Property(key = "protocol", value = "ws"),
        @Property(key = "host", value = "127.0.0.1"), @Property(key = "port", value = "2184"),
        @Property(key = "path", value = "/ws"), @Property(key = "topics", value = "sensinact/mqtt/testWS/+"), })
@WithConfiguration(pid = "sensinact.mqtt.device.factory", location = "?", properties = {
        @Property(key = "mqtt.handler.id", value = "handlerWS"),
        @Property(key = "mapping", value = "{\n" + "  \"parser\": \"csv\",\n"
                + "  \"parser.options\": { \"header\": true },\n" + "  \"mapping\": {\n"
                + "    \"@provider\": \"Name\",\n"
                + "    \"@latitude\": { \"path\": \"Latitude\", \"type\": \"float\" },\n"
                + "    \"@longitude\": { \"path\": \"Longitude\", \"type\": \"float\" },\n"
                + "    \"@date\": \"Date\", \"@time\": \"Time\",\n"
                + "    \"data/testName\": { \"literal\": \"${context.topic-2}\" },\n"
                + "    \"data/value\": { \"path\": \"Value\", \"type\": \"int\" }\n" + "  },\n"
                + "  \"mapping.options\": { \"format.date\": \"d.M.y\", \"format.time\": \"H:m\" }\n" + "}"), })
@WithConfiguration(pid = "sensinact.session.manager", properties = @Property(key = "auth.policy", value = "ALLOW_ALL"))
@Requirement(namespace = ServiceNamespace.SERVICE_NAMESPACE, filter = "(objectClass=org.eclipse.sensinact.northbound.session.SensiNactSessionManager)")
public class MqttWSDeviceFactoryTest {

    // Moquette server
    private static Server server;

    // Target topic
    private static String TOPIC = "sensinact/mqtt/testWS/handler";

    @InjectService
    GatewayThread thread;

    @InjectService
    SensiNactSessionManager sessionManager;
    SensiNactSession session;

    BlockingQueue<ResourceDataNotification> queue;

    // Excepted providers
    private final String typedProvider1 = "typed-provider1";
    private final String typedProvider2 = "typed-provider2";

    // MQTT client
    private MqttClient client;

    @BeforeAll
    static void startBroker() throws IOException {
        server = new Server();
        IConfig config = new MemoryConfig(new Properties());
        config.setProperty(IConfig.HOST_PROPERTY_NAME, "127.0.0.1");
        config.setProperty(IConfig.PORT_PROPERTY_NAME, "2183");
        config.setProperty(IConfig.WEB_SOCKET_PORT_PROPERTY_NAME, "2184");
        config.setProperty(IConfig.WEB_SOCKET_PATH_PROPERTY_NAME, "/ws");
        server.startServer(config);
    }

    @AfterAll
    static void stopBroker() throws IOException {
        server.stopServer();
    }

    @BeforeEach
    void start() throws Exception {
        session = sessionManager.getDefaultAnonymousSession();
        queue = new ArrayBlockingQueue<>(32);

        client = new MqttClient("tcp://127.0.0.1:2183", MqttClient.generateClientId());
        client.connect();
    }

    @AfterEach
    void stop() throws Exception {
        session.activeListeners().keySet().forEach(session::removeListener);
        session = null;

        // Clear retained & Disconnect
        if (client.isConnected()) {
            client.publish(TOPIC, new byte[0], 1, true);
            client.disconnect();
        }
        client.close();

        thread.execute(new AbstractSensinactCommand<Void>() {
            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                    PromiseFactory promiseFactory) {
                for (String provider : Arrays.asList(typedProvider1, typedProvider2)) {
                    Optional.ofNullable(twin.getProvider(provider)).ifPresent(p -> p.delete());
                }
                return Promises.resolved(null);
            }
        }).getValue();
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
        // Register listener
        session.addListener(List.of(typedProvider1 + "/*"), (t, e) -> queue.offer(e), null, null, null);

        // Providers shouldn't exist yet
        assertNull(session.describeProvider(typedProvider1));
        assertNull(session.describeProvider(typedProvider2));

        // Read the file
        byte[] csvContent = readFile("csv-header-typed.csv");

        // Send MQTT message on handler and retain it (sometimes this client publishes
        // the message before the configured client has subscribed)
        client.publish(TOPIC, csvContent, 1, true);

        // Wait for the provider to appear
        assertNotNull(queue.poll(1, TimeUnit.SECONDS));

        // Ensure resource type
        assertEquals(42, session.getResourceValue(typedProvider1, "data", "value", Integer.class));
        assertEquals(84, session.getResourceValue(typedProvider2, "data", "value", Integer.class));

        // Ensure timestamp
        Instant timestamp1 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 14, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp1, session.describeResource(typedProvider1, "data", "value").timestamp);

        Instant timestamp2 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 17, 0).atOffset(ZoneOffset.UTC));
        assertEquals(timestamp2, session.describeResource(typedProvider2, "data", "value").timestamp);

        // Ensure location update (and its timestamp)
        ResourceDescription location1 = session.describeResource(typedProvider1, "admin", "location");
        assertEquals(timestamp1, location1.timestamp);
        assertNotNull(location1.value);
        Point geoPoint = (Point) location1.value;
        assertEquals(1.2, geoPoint.coordinates.latitude, 0.001);
        assertEquals(3.4, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));

        ResourceDescription location2 = session.describeResource(typedProvider2, "admin", "location");
        assertNotNull(location2.value);
        assertEquals(timestamp2, location2.timestamp);
        geoPoint = (Point) location2.value;
        assertEquals(5.6, geoPoint.coordinates.latitude, 0.001);
        assertEquals(7.8, geoPoint.coordinates.longitude, 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates.elevation));

        // Ensure resources from context
        assertEquals("testWS", session.getResourceValue(typedProvider1, "data", "testName", String.class));
        assertEquals("testWS", session.getResourceValue(typedProvider2, "data", "testName", String.class));
    }
}

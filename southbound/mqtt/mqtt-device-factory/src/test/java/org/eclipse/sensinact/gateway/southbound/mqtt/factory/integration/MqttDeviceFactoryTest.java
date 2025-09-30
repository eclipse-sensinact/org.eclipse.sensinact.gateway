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

import static io.netty.handler.codec.mqtt.MqttMessageType.PUBLISH;
import static io.netty.handler.codec.mqtt.MqttQoS.EXACTLY_ONCE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessageListener;
import org.eclipse.sensinact.northbound.session.ResourceDescription;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.namespace.service.ServiceNamespace;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.osgi.util.promise.Promises;

import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;

/**
 * Tests for the MQTT device factory
 */

@WithConfiguration(pid = "sensinact.session.manager", properties = @Property(key = "auth.policy", value = "ALLOW_ALL"))
@Requirement(namespace = ServiceNamespace.SERVICE_NAMESPACE, filter = "(objectClass=org.eclipse.sensinact.northbound.session.SensiNactSessionManager)")
public class MqttDeviceFactoryTest {

    private static Server server;

    @InjectService
    GatewayThread thread;

    @InjectService
    SensiNactSessionManager sessionManager;
    SensiNactSession session;

    BlockingQueue<ResourceDataNotification> queue;

    // Excepted providers
    final String typedProvider1 = "typed-provider1";
    final String typedProvider2 = "typed-provider2";
    final String handlerProvider = "handler-provider1";

    @BeforeAll
    static void startBroker() throws IOException {
        server = new Server();
        IConfig config = new MemoryConfig(new Properties());
        config.setProperty(IConfig.HOST_PROPERTY_NAME, "127.0.0.1");
        config.setProperty(IConfig.PORT_PROPERTY_NAME, "21830");
        config.setProperty(IConfig.PERSISTENCE_ENABLED_PROPERTY_NAME, "false");
        config.setProperty(IConfig.ENABLE_TELEMETRY_NAME, "false");
        server.startServer(config);
    }

    @AfterAll
    static void stopBroker() throws IOException {
        server.stopServer();
    }

    @BeforeEach
    void start() throws InterruptedException {
        session = sessionManager.getDefaultAnonymousSession();
        queue = new ArrayBlockingQueue<>(32);
    }

    @BeforeEach
    void ensureClientsReady(@InjectBundleContext BundleContext ctx,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = "sensinact.southbound.mqtt", name = "h1", location = "?",
            properties = {
                    @Property(key = "id", value = "handler1"), @Property(key = "host", value = "127.0.0.1"),
                    @Property(key = "port", value = "21830"), @Property(key = "topics", value = "sensinact/mqtt/test1/+"), }))
            Configuration handler1Config,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = "sensinact.southbound.mqtt", name = "h2", location = "?",
            properties = {
                    @Property(key = "id", value = "handler2"), @Property(key = "host", value = "127.0.0.1"),
                    @Property(key = "port", value = "21830"), @Property(key = "topics", value = "sensinact/mqtt/test2/+"), }))
            Configuration handler2Config) {
        String id = UUID.randomUUID().toString();
        testClient(id, "sensinact/mqtt/test1/blah", "handler1", ctx);
        testClient(id, "sensinact/mqtt/test2/blah", "handler2", ctx);
    }

    void testClient(String clientId, String topic, String handler, BundleContext ctx) {
        Semaphore sem = new Semaphore(0);
        ServiceRegistration<?> reg = ctx.registerService(IMqttMessageListener.class, (h,t,m) -> {
            if(t.equals(topic) && h.equals(handler) && "test".equals(new String(m.getPayload(), UTF_8))) {
                sem.release();
            } else {
                System.out.println("Ignoring message " + m);
            }
        }, new Hashtable<>(Map.of(IMqttMessageListener.MQTT_TOPICS_FILTERS, topic)));

        try {
            for(int i = 0; i < 10; i++) {
                MqttPublishMessage mpm = createMessage(topic, "test".getBytes(UTF_8));
                server.internalPublish(mpm, clientId);
                if(sem.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
                    return;
                }
            }
            fail("Client not ready");
        } catch (InterruptedException e) {
            fail("Interrupted");
        } finally {
            reg.unregister();
        }
    }

    private static final AtomicInteger counter = new AtomicInteger(42);

    private MqttPublishMessage createMessage(String topic, byte[] payload) {
        MqttFixedHeader header = new MqttFixedHeader(PUBLISH, false, EXACTLY_ONCE, false, 0);
        ByteBuf buf = Unpooled.wrappedBuffer(payload);
        MqttPublishVariableHeader topicHeader = new MqttPublishVariableHeader(topic, counter.getAndIncrement());
        MqttPublishMessage mpm = new MqttPublishMessage(header, topicHeader, buf);
        return mpm;
    }

    @AfterEach
    void stop() throws Exception {
        session.activeListeners().keySet().forEach(session::removeListener);
        session = null;

        thread.execute(new AbstractSensinactCommand<Void>() {
            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                    PromiseFactory promiseFactory) {
                for (String provider : Arrays.asList(typedProvider1, typedProvider2, handlerProvider)) {
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
    @WithConfiguration(pid = "sensinact.mqtt.device.factory", location = "?", properties = {
            @Property(key = "name", value = "testHandler1"),
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
    @Test
    void testWorking(@InjectService(filter = "(name=testHandler1)") IMqttMessageListener listener) throws Exception {
        // Register listener
        session.addListener(List.of(typedProvider1 + "/*"), (t, e) -> queue.offer(e), null, null, null);

        // Providers shouldn't exist yet
        assertNull(session.describeProvider(typedProvider1));
        assertNull(session.describeProvider(typedProvider2));

        // Send MQTT message on handler 1
        server.internalPublish(createMessage("sensinact/mqtt/test1/handler", readFile("csv-header-typed.csv")),
                MqttClient.generateClientId());

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
        assertEquals(1.2, geoPoint.coordinates().latitude(), 0.001);
        assertEquals(3.4, geoPoint.coordinates().longitude(), 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates().elevation()));

        ResourceDescription location2 = session.describeResource(typedProvider2, "admin", "location");
        assertNotNull(location2.value);
        assertEquals(timestamp2, location2.timestamp);
        geoPoint = (Point) location2.value;
        assertEquals(5.6, geoPoint.coordinates().latitude(), 0.001);
        assertEquals(7.8, geoPoint.coordinates().longitude(), 0.001);
        assertTrue(Double.isNaN(geoPoint.coordinates().elevation()));

        // Ensure resources from context
        assertEquals("test1", session.getResourceValue(typedProvider1, "data", "testName", String.class));
        assertEquals("test1", session.getResourceValue(typedProvider2, "data", "testName", String.class));
    }

    @WithConfiguration(pid = "sensinact.mqtt.device.factory", location = "?", properties = {
            @Property(key = "name", value = "testHandler2"),
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
    /**
     * Tests device registration
     */
    @Test
    void testHandlerFilter(@InjectService(filter = "(name=testHandler2)") IMqttMessageListener listener) throws Exception {
        // Register listener
        session.addListener(List.of(handlerProvider + "/*"), (t, e) -> queue.offer(e), null, null, null);

        // Providers shouldn't exist yet
        assertNull(session.describeProvider(handlerProvider));

        // Read the file and change the provider IDs
        byte[] csvContent = new String(readFile("csv-header-typed.csv"), StandardCharsets.UTF_8)
                .replace("typed-provider", "handler-provider").getBytes(StandardCharsets.UTF_8);

        // Send MQTT message on handler 2
        server.internalPublish(createMessage("sensinact/mqtt/test2/handler", csvContent),
                MqttClient.generateClientId());

        // Wait for the provider to NOT appear
        assertNull(queue.poll(1, TimeUnit.SECONDS));
        assertNull(session.describeProvider(handlerProvider));
    }
}

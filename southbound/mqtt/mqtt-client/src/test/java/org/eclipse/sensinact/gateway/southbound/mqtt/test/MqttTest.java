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
package org.eclipse.sensinact.gateway.southbound.mqtt.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessage;
import org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessageListener;
import org.eclipse.sensinact.gateway.southbound.mqtt.impl.MqttClientConfiguration;
import org.eclipse.sensinact.gateway.southbound.mqtt.impl.MqttClientHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;

/**
 * Tests of the MQTT southbound
 */
public class MqttTest {

    /**
     * Client to publish messages
     */
    private MqttClient client;

    /**
     * Active handlers
     */
    private final List<MqttClientHandler> handlers = new ArrayList<>();

    private static  Server server;

    @BeforeAll
    static void startServer() throws Exception {
        server = new Server();
        IConfig config = new MemoryConfig(new Properties());
        config.setProperty(IConfig.HOST_PROPERTY_NAME, "127.0.0.1");
        config.setProperty(IConfig.PORT_PROPERTY_NAME, "2183");
        server.startServer(config);
    }

    @BeforeEach
    void start() throws Exception {

        client = new MqttClient("tcp://127.0.0.1:2183", MqttClient.generateClientId());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        client.connect(options);

        handlers.add(setupHandler("id1", "sensinact/mqtt/test1/+"));
        handlers.add(setupHandler(null, "sensinact/mqtt/test2/+"));
    }

    @AfterEach
    void stop() throws Exception {
        client.disconnect();
        client.close();

        for (MqttClientHandler handler : handlers) {
            handler.deactivate();
        }
    }

    @AfterAll
    static void stopServer() throws Exception {
        server.stopServer();
    }

    MqttClientHandler setupHandler(final String handlerId, final String... topics) throws Exception {
        MqttClientHandler handler = new MqttClientHandler();
        MqttClientConfiguration mock = Mockito.mock(MqttClientConfiguration.class);
        Mockito.when(mock.id()).thenReturn(handlerId);
        Mockito.when(mock.host()).thenReturn("127.0.0.1");
        Mockito.when(mock.port()).thenReturn(2183);
        Mockito.when(mock.topics()).thenReturn(topics);
        handler.activate(mock);
        return handler;
    }

    @Test
    void testMqttMultipleListeners() throws Exception {
        // Register a listener as a service
        final BlockingQueue<IMqttMessage> messages1 = new ArrayBlockingQueue<>(32);
        final IMqttMessageListener listener1 = (handler, topic, msg) -> messages1.add(msg);

        final BlockingQueue<IMqttMessage> messages2 = new ArrayBlockingQueue<>(32);
        final IMqttMessageListener listener2 = (handler, topic, msg) -> messages2.add(msg);

        for (MqttClientHandler handler : handlers) {
            handler.addListener(listener1, Map.of(IMqttMessageListener.MQTT_TOPICS_FILTERS,
                    new String[] { "sensinact/mqtt/test1/foo", "sensinact/mqtt/test1/bar" }));
            handler.addListener(listener2,
                    Map.of(IMqttMessageListener.MQTT_TOPICS_FILTERS, new String[] { "sensinact/mqtt/test1/foo" }));
        }

        IMqttMessage msg1, msg2;
        String topic;
        String content = "Hello";

        // Test valid message for both listeners
        topic = "sensinact/mqtt/test1/foo";
        client.publish(topic, content.getBytes(StandardCharsets.UTF_8), 1, false);
        // Wait a bit
        msg1 = messages1.poll(1, TimeUnit.SECONDS);
        assertNotNull(msg1);
        assertEquals(topic, msg1.getTopic());
        assertEquals(content, new String(msg1.getPayload(), StandardCharsets.UTF_8));

        msg2 = messages2.poll(1, TimeUnit.SECONDS);
        assertNotNull(msg2);
        assertEquals(topic, msg2.getTopic());
        assertEquals(content, new String(msg2.getPayload(), StandardCharsets.UTF_8));
        assertTrue(msg1.getPayload() != msg2.getPayload(), "Payload is not a new one each time");

        // Test valid message for just 1 listener
        topic = "sensinact/mqtt/test1/bar";
        client.publish(topic, content.getBytes(StandardCharsets.UTF_8), 1, false);
        msg1 = messages1.poll(1, TimeUnit.SECONDS);
        assertNotNull(msg1);
        assertEquals(topic, msg1.getTopic());
        assertEquals(content, new String(msg1.getPayload(), StandardCharsets.UTF_8));

        assertEquals(0, messages2.size(), "Listener got an unexpected message");
    }

    @Test
    void testMqttMultipleConfigurations() throws Exception {
        // Register a listener as a service
        final BlockingQueue<IMqttMessage> messages = new ArrayBlockingQueue<>(32);
        final IMqttMessageListener listener = (handler, topic, msg) -> messages.add(msg);

        for (MqttClientHandler handler : handlers) {
            handler.addListener(listener, Map.of(IMqttMessageListener.MQTT_TOPICS_FILTERS,
                    new String[] { "sensinact/mqtt/test1/foo", "sensinact/mqtt/test2/bar" }));
        }

        IMqttMessage msg;
        String topic;
        String content = "Hello";

        // Test valid message for listener on 1st configuration
        topic = "sensinact/mqtt/test1/foo";
        client.publish(topic, content.getBytes(StandardCharsets.UTF_8), 1, false);
        // Wait a bit
        msg = messages.poll(1, TimeUnit.SECONDS);
        assertNotNull(msg);
        assertEquals(topic, msg.getTopic());
        assertEquals(content, new String(msg.getPayload(), StandardCharsets.UTF_8));

        // Test invalid message for listener on 1st configuration
        topic = "sensinact/mqtt/test1/bar";
        client.publish(topic, content.getBytes(StandardCharsets.UTF_8), 1, false);
        // Wait a bit
        msg = messages.poll(1, TimeUnit.SECONDS);
        assertNull(msg);

        // Test invalid message for listener on 2nd configuration
        topic = "sensinact/mqtt/test2/foo";
        client.publish(topic, content.getBytes(StandardCharsets.UTF_8), 1, false);
        // Wait a bit
        msg = messages.poll(1, TimeUnit.SECONDS);
        assertNull(msg);

        // Test valid message on 2nd configuration
        topic = "sensinact/mqtt/test2/bar";
        client.publish(topic, content.getBytes(StandardCharsets.UTF_8), 1, false);
        msg = messages.poll(1, TimeUnit.SECONDS);
        assertNotNull(msg);
        assertEquals(topic, msg.getTopic());
        assertEquals(content, new String(msg.getPayload(), StandardCharsets.UTF_8));
    }

    @Test
    void testMqttHandlerId() throws Exception {
        // Register a listener as a service
        final BlockingQueue<IMqttMessage> messages = new ArrayBlockingQueue<>(32);
        final IMqttMessageListener listener = (handler, topic, msg) -> {
            assertEquals(handler, msg.getHandlerId());
            messages.add(msg);
        };

        for (MqttClientHandler handler : handlers) {
            handler.addListener(listener, Map.of(IMqttMessageListener.MQTT_TOPICS_FILTERS,
                    new String[] { "sensinact/mqtt/test1/foo", "sensinact/mqtt/test2/bar" }));
        }

        IMqttMessage msg1, msg2;
        String topic;
        String content = "HandlerID";

        // Test valid message for listener on 1st configuration
        topic = "sensinact/mqtt/test1/foo";
        client.publish(topic, content.getBytes(StandardCharsets.UTF_8), 1, false);
        // Wait a bit
        msg1 = messages.poll(1, TimeUnit.SECONDS);
        assertNotNull(msg1);
        assertEquals("id1", msg1.getHandlerId());
        assertEquals(topic, msg1.getTopic());
        assertEquals(content, new String(msg1.getPayload(), StandardCharsets.UTF_8));

        // Test valid message on 2nd configuration
        topic = "sensinact/mqtt/test2/bar";
        client.publish(topic, content.getBytes(StandardCharsets.UTF_8), 1, false);
        msg2 = messages.poll(1, TimeUnit.SECONDS);
        assertNotNull(msg2);
        // Raises an exception if invalid
        UUID id2 = UUID.fromString(msg2.getHandlerId());
        assertNotEquals(msg1.getHandlerId(), id2.toString());
        assertEquals(topic, msg2.getTopic());
        assertEquals(content, new String(msg2.getPayload(), StandardCharsets.UTF_8));
    }
}

/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessage;
import org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessageListener;
import org.eclipse.sensinact.gateway.southbound.mqtt.impl.MqttClientConfiguration;
import org.eclipse.sensinact.gateway.southbound.mqtt.impl.MqttClientHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.moquette.BrokerConstants;
import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;

/**
 * Tests of the MQTT southbound
 */
public class MqttDelayedStartTest {

    /**
     * Client to publish messages
     */
    private MqttClient client;

    /**
     * Active handlers
     */
    private MqttClientHandler handler;

    private Server server;

    @BeforeEach
    void setupHandlers() throws Exception {
        handler = new MqttClientHandler();
    }

    private MqttClientConfiguration getConfig() {
        MqttClientConfiguration mock = Mockito.mock(MqttClientConfiguration.class);
        Mockito.when(mock.id()).thenReturn("id1");
        Mockito.when(mock.host()).thenReturn("127.0.0.1");
        Mockito.when(mock.port()).thenReturn(2183);
        Mockito.when(mock.topics()).thenReturn(new String[] { "sensinact/mqtt/test1/+" });
        Mockito.when(mock.client_reconnect_delay()).thenReturn(1000);
        return mock;
    }

    void startServerAndLocalClient() throws Exception {
        server = new Server();
        IConfig config = new MemoryConfig(new Properties());
        config.setProperty(BrokerConstants.HOST_PROPERTY_NAME, "127.0.0.1");
        config.setProperty(BrokerConstants.PORT_PROPERTY_NAME, "2183");
        server.startServer(config);

        client = new MqttClient("tcp://127.0.0.1:2183", MqttClient.generateClientId());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        client.connect(options);
    }

    @AfterEach
    void stop() throws Exception {
        try {
            MqttClient c = client;
            client = null;
            if (c != null) {
                c.disconnect();
                c.close();
            }

            MqttClientHandler h = handler;
            handler = null;
            if (h != null) {
                h.deactivate();
            }
        } finally {
            Server s = server;
            server = null;
            if (s != null) {
                s.stopServer();
            }
        }
    }

    private void doTest(final BlockingQueue<IMqttMessage> receiveQueue)
            throws MqttException, MqttPersistenceException, InterruptedException {
        String topic;
        String content = "Hello";

        // Test valid message for listener
        topic = "sensinact/mqtt/test1/foo";

        IMqttMessage msg = null;
        // Try a few times waiting for a reconnect
        for (int i = 0; i < 5 && msg == null; i++) {
            client.publish(topic, content.getBytes(StandardCharsets.UTF_8), 1, false);
            // Wait a bit
            msg = receiveQueue.poll(1, TimeUnit.SECONDS);
        }
        assertNotNull(msg);
        assertEquals(topic, msg.getTopic());
        assertEquals(content, new String(msg.getPayload(), StandardCharsets.UTF_8));
    }

    @Test
    void testMqttDelayedServerStart() throws Exception {
        // Activate the handler first

        handler.activate(getConfig());

        // Register a listener as a service
        final BlockingQueue<IMqttMessage> messages1 = new ArrayBlockingQueue<>(32);
        final IMqttMessageListener listener1 = (handler, topic, msg) -> messages1.add(msg);

        handler.addListener(listener1, Map.of(IMqttMessageListener.MQTT_TOPICS_FILTERS,
                new String[] { "sensinact/mqtt/test1/foo", "sensinact/mqtt/test1/bar" }));

        // Now start the server
        startServerAndLocalClient();

        doTest(messages1);
    }

    @Test
    void testMqttEarlyHandlerRegDelayedServerStart() throws Exception {
        // Register a listener as a service first
        final BlockingQueue<IMqttMessage> messages1 = new ArrayBlockingQueue<>(32);
        final IMqttMessageListener listener1 = (handler, topic, msg) -> messages1.add(msg);

        handler.addListener(listener1, Map.of(IMqttMessageListener.MQTT_TOPICS_FILTERS,
                new String[] { "sensinact/mqtt/test1/foo", "sensinact/mqtt/test1/bar" }));

        // Activate the handler

        handler.activate(getConfig());

        // Now start the server
        startServerAndLocalClient();

        doTest(messages1);
    }

    @Test
    void testMqttEarlyHandlerRegEarlyServerStart() throws Exception {

        // Start the server
        startServerAndLocalClient();

        // Register a listener as a service
        final BlockingQueue<IMqttMessage> messages1 = new ArrayBlockingQueue<>(32);
        final IMqttMessageListener listener1 = (handler, topic, msg) -> messages1.add(msg);

        handler.addListener(listener1, Map.of(IMqttMessageListener.MQTT_TOPICS_FILTERS,
                new String[] { "sensinact/mqtt/test1/foo", "sensinact/mqtt/test1/bar" }));

        // Activate the handler

        handler.activate(getConfig());

        doTest(messages1);
    }

    @Test
    void testMqttEarlyServerStart() throws Exception {

        // Start the server
        startServerAndLocalClient();

        // Activate the handler

        handler.activate(getConfig());

        // Register a listener as a service
        final BlockingQueue<IMqttMessage> messages1 = new ArrayBlockingQueue<>(32);
        final IMqttMessageListener listener1 = (handler, topic, msg) -> messages1.add(msg);

        handler.addListener(listener1, Map.of(IMqttMessageListener.MQTT_TOPICS_FILTERS,
                new String[] { "sensinact/mqtt/test1/foo", "sensinact/mqtt/test1/bar" }));

        doTest(messages1);
    }
}

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessage;
import org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessageListener;
import org.eclipse.sensinact.gateway.southbound.mqtt.impl.MqttClientConfiguration;
import org.eclipse.sensinact.gateway.southbound.mqtt.impl.MqttClientHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.MemoryConfig;
import io.moquette.broker.security.IAuthenticator;

/**
 * Tests of the MQTT southbound
 */
public class MqttAuthTest {

    private static final String USER = "foo";
    private static final String PASSWORD = "foobar";

    public static class TestAuth implements IAuthenticator {
        @Override
        public boolean checkValid(String clientId, String username, byte[] password) {
            return USER.equals(username) && PASSWORD.equals(new String(password));
        }
    }

    /**
     * Client to publish messages
     */
    private MqttClient client;

    /**
     * Active handlers
     */
    private final List<MqttClientHandler> handlers = new ArrayList<>();

    private Server server;

    @BeforeEach
    void start() throws Exception {
        server = new Server();
        IConfig config = new MemoryConfig(new Properties());
        config.setProperty(IConfig.HOST_PROPERTY_NAME, "127.0.0.1");
        config.setProperty(IConfig.PORT_PROPERTY_NAME, "2183");
        config.setProperty(IConfig.AUTHENTICATOR_CLASS_NAME, TestAuth.class.getName());
        config.setProperty(IConfig.ALLOW_ANONYMOUS_PROPERTY_NAME, "false");
        server.startServer(config);

        client = new MqttClient("tcp://127.0.0.1:2183", MqttClient.generateClientId());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(USER);
        options.setPassword(PASSWORD.toCharArray());
        client.connect(options);
    }

    @AfterEach
    void stop() throws Exception {
        try {
            client.disconnect();
            client.close();

            for (MqttClientHandler handler : handlers) {
                handler.deactivate();
            }
        } finally {
            server.stopServer();
        }
    }

    MqttClientHandler setupHandler(final String handlerId, final String user, final String password,
            final String... topics) throws Exception {
        MqttClientHandler handler = new MqttClientHandler();
        MqttClientConfiguration mock = Mockito.mock(MqttClientConfiguration.class);
        Mockito.when(mock.id()).thenReturn(handlerId);
        Mockito.when(mock.host()).thenReturn("127.0.0.1");
        Mockito.when(mock.port()).thenReturn(2183);
        Mockito.when(mock.topics()).thenReturn(topics);
        Mockito.when(mock.user()).thenReturn(user);
        Mockito.when(mock._password()).thenReturn(password != null ? password : null);
        handler.activate(mock);
        return handler;
    }

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void testMqttConnect() throws Exception {
        // Register a listener as a service
        final BlockingQueue<IMqttMessage> messages = new ArrayBlockingQueue<>(32);
        final IMqttMessageListener listener = (handler, topic, msg) -> {
            assertEquals(handler, msg.getHandlerId());
            messages.add(msg);
        };

        final String topicNoAuth = "sensinact/mqtt/test1/noauth";
        final String topicAuth = "sensinact/mqtt/test1/auth";
        final String topicAuthBad = "sensinact/mqtt/test1/auth-bad";

        assertThrows(MqttSecurityException.class,
                () -> handlers.add(setupHandler("id-noauth", null, null, topicNoAuth)));

        assertThrows(MqttSecurityException.class,
                () -> handlers.add(setupHandler("id-auth-bad", USER, PASSWORD + "toto", topicAuthBad)));

        handlers.add(setupHandler("id-auth", USER, PASSWORD, topicAuth));

        for (MqttClientHandler handler : handlers) {
            handler.addListener(listener,
                    Map.of(IMqttMessageListener.MQTT_TOPICS_FILTERS, new String[] { "sensinact/mqtt/test1/+" }));
        }

        IMqttMessage msg1;
        String content = "HandlerID";

        // Send all messages, the good one last
        for (String topic : List.of(topicNoAuth, topicAuthBad, topicAuth)) {
            client.publish(topic, content.getBytes(StandardCharsets.UTF_8), 1, false);
        }

        // Wait a bit (we should get only 1 message)
        msg1 = messages.poll(1, TimeUnit.SECONDS);
        assertNotNull(msg1);
        assertEquals(0, messages.size());
        assertEquals("id-auth", msg1.getHandlerId());
        assertEquals(topicAuth, msg1.getTopic());
        assertEquals(content, new String(msg1.getPayload(), StandardCharsets.UTF_8));

        // Ensure we still didn't get other messages
        assertEquals(0, messages.size());
    }

    @Test
    void testRefusedCredentialsLogNameTheHandler() throws Exception {
        final List<String> messages = new CopyOnWriteArrayList<>();
        final AppenderBase<ILoggingEvent> appender = new AppenderBase<>() {
            @Override
            protected void append(final ILoggingEvent event) {
                messages.add(event.getFormattedMessage());
            }
        };
        appender.start();
        final ch.qos.logback.classic.Logger handlerLogger = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger(MqttClientHandler.class);
        final Level previousLevel = handlerLogger.getLevel();
        handlerLogger.setLevel(Level.ERROR);
        handlerLogger.addAppender(appender);
        try {
            assertThrows(MqttSecurityException.class,
                    () -> setupHandler("id-refused", USER, PASSWORD + "toto", "sensinact/mqtt/test2/+"));

            final String source = "MQTT handler 'id-refused' (tcp://127.0.0.1:2183): ";
            assertTrue(messages.stream().anyMatch(m -> m.startsWith(source) && m.contains("failed to connect")),
                    messages::toString);
            // The credentials must never be logged
            assertFalse(messages.stream().anyMatch(m -> m.contains(PASSWORD)), messages::toString);
        } finally {
            handlerLogger.detachAppender(appender);
            handlerLogger.setLevel(previousLevel);
            appender.stop();
        }
    }

    @Test
    void testMissingTrustStoreLogsTheHandler() throws Exception {
        final String trustStorePassword = "trust-store-secret";
        final String missingPath = "/does/not/exist/truststore.p12";
        final List<ILoggingEvent> events = new CopyOnWriteArrayList<>();
        final AppenderBase<ILoggingEvent> appender = new AppenderBase<>() {
            @Override
            protected void append(final ILoggingEvent event) {
                events.add(event);
            }
        };
        appender.start();
        final ch.qos.logback.classic.Logger handlerLogger = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger(MqttClientHandler.class);
        final Level previousLevel = handlerLogger.getLevel();
        handlerLogger.setLevel(Level.ERROR);
        handlerLogger.addAppender(appender);
        try {
            final MqttClientConfiguration config = Mockito.mock(MqttClientConfiguration.class);
            Mockito.when(config.id()).thenReturn("id-no-truststore");
            Mockito.when(config.host()).thenReturn("127.0.0.1");
            Mockito.when(config.port()).thenReturn(2183);
            Mockito.when(config.topics()).thenReturn(new String[] { "sensinact/mqtt/test2/+" });
            Mockito.when(config.auth_truststore_path()).thenReturn(missingPath);
            Mockito.when(config._auth_truststore_password()).thenReturn(trustStorePassword);

            // Activation must still fail
            assertThrows(FileNotFoundException.class, () -> new MqttClientHandler().activate(config));

            final List<String> messages = events.stream().map(ILoggingEvent::getFormattedMessage).toList();
            // Logged once, naming the handler, the broker and the file
            assertEquals(1, messages.size(), messages::toString);
            final String message = messages.get(0);
            assertTrue(message.startsWith("MQTT handler 'id-no-truststore' (ssl://127.0.0.1:2183): "), message);
            assertTrue(message.contains("the TLS configuration cannot be loaded"), message);
            assertTrue(message.contains(missingPath), message);
            assertEquals(Level.ERROR, events.get(0).getLevel());
            // The password must never be logged
            assertFalse(message.contains(trustStorePassword), message);
        } finally {
            handlerLogger.detachAppender(appender);
            handlerLogger.setLevel(previousLevel);
            appender.stop();
        }
    }
}

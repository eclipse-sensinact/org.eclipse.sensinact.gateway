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
package org.eclipse.sensinact.gateway.feature.integration.sensorthingsmqtt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.sensinact.gateway.feature.utilities.test.ServerProcessHandler;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class SensorthingsMqttIntegrationTest {

    private static ServerProcessHandler server = new ServerProcessHandler();

    @BeforeAll
    static void startServer() throws Exception {
        server.startSensinact();
    }

    @AfterAll
    static void stopServer() throws Exception {
        server.stopSensinact();
    }

    private IMqttAsyncClient client;

    private BlockingQueue<String> messages = new ArrayBlockingQueue<>(64);

    private IMqttMessageListener listener;

    private ObjectMapper mapper;

    @BeforeEach
    void start() throws Exception {
        client = new MqttAsyncClient("tcp://127.0.0.1:18883", "test-client");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(2000);
        for (int i = 0; i < 5; i++) {
            try {
                client.connect(options).waitForCompletion(1000);
            } catch (MqttException e) {
                if (e.getCause() instanceof ConnectException) {
                    Thread.sleep(2000);
                    continue;
                } else {
                    throw e;
                }
            }
            break;
        }

        listener = (t, m) -> messages.put(new String(m.getPayload(), StandardCharsets.UTF_8));

        mapper = JsonMapper.builder().addModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true).build();
    }

    @AfterEach
    void stop() throws Exception {
        try {
            client.disconnect(500).waitForCompletion(1000);
        } catch (MqttException e) {
            // Swallow it
        }
        messages.clear();
    }

    private <T> List<T> pollMessages(int expected, Class<T> type)
            throws InterruptedException, JsonProcessingException, JsonMappingException {
        List<T> streams = new ArrayList<>();

        String message = messages.poll(3000, TimeUnit.MILLISECONDS);
        for (int i = 0; i < expected; i++) {
            assertNotNull(message, () -> "Received " + streams.size() + " messages");
            streams.add(mapper.readValue(message, type));
            message = messages.poll(3000, TimeUnit.MILLISECONDS);
        }
        return streams;
    }

    @Test
    void testMqttNotifications() throws Exception {

        final Instant start = Instant.now();
        Thread.sleep(2000);

        // Test datastream
        final String thingId = "temp1~sensor~temperature";
        String topic = "v1.1/Datastreams(" + thingId + ")";
        client.subscribe(topic, 0, listener).waitForCompletion(5000);
        final List<Datastream> datastreamUpdates = pollMessages(4, Datastream.class);
        client.unsubscribe(topic).waitForCompletion(2000);
        messages.clear();

        for (final Datastream ds : datastreamUpdates) {
            assertEquals(thingId, ds.id());
            assertEquals("No description", ds.description());
            assertEquals("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation", ds.observationType());
            assertNotNull(ds.properties(), "No properties given");
            assertTrue(ds.properties().containsKey("timestamp"), "No timestamp property");
            assertTrue(Instant.parse((String) ds.properties().get("timestamp")).isAfter(start));

            assertNull(ds.observationsLink(), "Got a link");
            assertNull(ds.observedPropertyLink(), "Got a link");
            assertNull(ds.selfLink(), "Got a link");
            assertNull(ds.sensorLink(), "Got a link");
            assertNull(ds.thingLink(), "Got a link");
        }

        // Test observation
        topic = "v1.1/Observations(" + thingId + ")";
        client.subscribe(topic, 0, listener).waitForCompletion(2000);
        final List<Observation> observationUpdates = pollMessages(4, Observation.class);
        client.unsubscribe(topic).waitForCompletion(2000);
        messages.clear();

        for (final Observation obs : observationUpdates) {
            assertTrue(String.valueOf(obs.id()).startsWith(thingId + "~"), "Invalid observation ID: " + obs.id());
            assertNotNull(obs.result(), "No result in observation");
            assertInstanceOf(Number.class, obs.result());
            assertTrue(obs.resultTime().isAfter(start));
            assertEquals(obs.resultTime(), obs.phenomenonTime());

            assertNull(obs.datastreamLink(), "Got a link");
            assertNull(obs.featureOfInterestLink(), "Got a link");
            assertNull(obs.selfLink(), "Got a link");
        }
    }
}

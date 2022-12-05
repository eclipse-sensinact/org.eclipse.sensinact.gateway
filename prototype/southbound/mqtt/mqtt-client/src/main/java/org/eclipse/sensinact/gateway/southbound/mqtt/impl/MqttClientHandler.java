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
package org.eclipse.sensinact.gateway.southbound.mqtt.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.function.Predicate;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessageListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles MQTT client instances
 */
@Component(service = {}, configurationPid = "sensinact.southbound.mqtt", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class MqttClientHandler implements MqttCallback {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(MqttClientHandler.class);

    /**
     * Configured internal ID
     */
    private String handlerId;

    /**
     * MQTT client
     */
    private MqttClient client;

    /**
     * Reconnection delay in milliseconds
     */
    private int reconnectDelayMs;

    /**
     * Reconnection task
     */
    private Timer reconnectTimer;

    /**
     * Listener -&gt; Topic handling predicate
     */
    private Map<IMqttMessageListener, Predicate<String>> listeners = Collections
            .synchronizedMap(new IdentityHashMap<>());

    /**
     * MQTT listener registered
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addListener(IMqttMessageListener listener, Map<String, Object> svcProps) {
        final String[] filters = getArrayProperty(svcProps.get(IMqttMessageListener.MQTT_TOPICS_FILTERS));
        listeners.put(listener, (str) -> serviceMatchesTopic(str, filters));
    }

    /**
     * MQTT listener unregistered
     */
    public void removeListener(IMqttMessageListener listener) {
        listeners.remove(listener);
    }

    /**
     * Configuration available
     */
    @Activate
    public void activate(final MqttClientConfiguration config) throws Exception {
        // Validate configuration
        final String broker = makeBrokerUri(config);
        final String clientId = makeClientId(config);
        final String[] topics = config.topics();
        if (topics == null || topics.length == 0) {
            logger.error("No topic to subscribe to");
            throw new IllegalArgumentException("No MQTT topic given");
        }

        final String configId = config.id();
        if (configId == null || configId.isBlank()) {
            handlerId = UUID.randomUUID().toString();
        } else {
            handlerId = configId;
        }

        // Setup options: always start with a clean session
        final MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        // Start client (blocking)
        logger.debug("Connecting MQTT client with ID {}", clientId);
        client = new MqttClient(broker, clientId);
        client.setCallback(this);
        client.setManualAcks(true);
        client.connect(options);

        // Register to topics (we're now connected)
        for (String topic : topics) {
            logger.debug("Subscribing MQTT client {} to topic: {}", clientId, topic);
            client.subscribe(topic);
        }

        // All done
        logger.info("MQTT client {} started", clientId);
    }

    @Deactivate
    public void deactivate() throws Exception {
        if (client != null) {
            client.disconnect();
            client.close();
            logger.info("MQTT client {} stopped", client.getClientId());
            client = null;
        }

        handlerId = null;
    }

    /**
     * Generates a broker URI based on configuration
     *
     * @throws URISyntaxException Invalid syntax in generated URI
     */
    private String makeBrokerUri(final MqttClientConfiguration config) throws URISyntaxException {
        final String mqttHost = config.host();
        if (mqttHost == null || mqttHost.isEmpty()) {
            logger.error("No MQTT host given");
            throw new IllegalArgumentException("No MQTT host given");
        }

        return new URI("tcp", null, mqttHost, config.port(), null, null, null).toString();
    }

    /**
     * Generates a client ID
     */
    private String makeClientId(final MqttClientConfiguration config) {
        final String givenId = config.client_id();
        if (givenId == null || givenId.isEmpty()) {
            return MqttClient.generateClientId();
        }

        return givenId;
    }

    /**
     * Connection to MQTT broker has been lost
     */
    @Override
    public void connectionLost(final Throwable cause) {
        logger.warn("Connection to MQTT broker lost: {}. Waiting before reconnecting.", cause.getMessage());

        if (reconnectTimer != null) {
            reconnectTimer.cancel();
            reconnectTimer = null;
        }

        reconnectTimer = new Timer();
        reconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    client.reconnect();
                } catch (MqttException e) {
                    logger.error("Error trying to reconnect to MQTT broker: {}", e.getMessage(), e);
                    connectionLost(e);
                }
            }
        }, reconnectDelayMs);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // Prepare the message
        if (message.isDuplicate()) {
            logger.warn("Ignoring duplicate MQTT message on topic=[{}]: {}", topic, message);
            client.messageArrivedComplete(message.getId(), message.getQos());
            return;
        }
        final SensiNactMqttMessage snMessage = new SensiNactMqttMessage(handlerId, topic, message);
        client.messageArrivedComplete(message.getId(), message.getQos());

        // Notify matching listeners
        Map<IMqttMessageListener, Predicate<String>> workListeners;
        synchronized (listeners) {
            workListeners = new IdentityHashMap<>(listeners);
        }

        for (Entry<IMqttMessageListener, Predicate<String>> entry : workListeners.entrySet()) {
            if (entry.getValue().test(topic)) {
                entry.getKey().onMqttMessage(handlerId, topic, snMessage);
            }
        }
    }

    /**
     * Checks if the given service is configured to handle the given topic
     *
     * @param topic  Message topic
     * @param svcRef Potential listener
     * @return True if the listener can handle the topic
     */
    private boolean serviceMatchesTopic(String topic, String[] filters) {
        if (filters == null || filters.length == 0) {
            // No filter: no message
            return false;
        }

        for (String filter : filters) {
            if (MqttTopic.isMatched(filter, topic)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Extracts a string array from the given property value, if possible
     */
    private String[] getArrayProperty(final Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String[]) {
            return (String[]) value;
        } else if (value instanceof String) {
            return ((String) value).split("[,;]");
        }

        return null;
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Ignore
    }
}

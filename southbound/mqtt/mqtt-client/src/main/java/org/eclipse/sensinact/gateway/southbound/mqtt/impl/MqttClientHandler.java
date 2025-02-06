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

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
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
import org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessage;
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

    private Object lock = new Object();

    /**
     * Listener -&gt; Topic handling predicate
     */
    private Map<IMqttMessageListener, Predicate<String>> listeners = new IdentityHashMap<>();

    private Map<String, IMqttMessage> topic2last = new HashMap<>();

    private String[] topics;

    private String clientId;

    private MqttConnectOptions connectOptions;

    /**
     * MQTT listener registered
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addListener(IMqttMessageListener listener, Map<String, Object> svcProps) {
        final String[] filters = getArrayProperty(svcProps.get(IMqttMessageListener.MQTT_TOPICS_FILTERS));
        Predicate<String> predicate = (str) -> serviceMatchesTopic(str, filters);
        Map<String, IMqttMessage> topic2lastCopy;
        synchronized(lock) {
            listeners.put(listener, predicate);
            topic2lastCopy = new HashMap<>(topic2last);
        }
        for (var lastEntry: topic2lastCopy.entrySet()) {
            String topic = lastEntry.getKey();
            IMqttMessage message = lastEntry.getValue();
            if (predicate.test(topic) && message.getPayload().length > 0) {
                listener.onMqttMessage(message.getHandlerId(), topic, message);
            }
        }
    }

    /**
     * MQTT listener unregistered
     */
    public void removeListener(IMqttMessageListener listener) {
        synchronized(lock) {
            listeners.remove(listener);
        }
    }

    /**
     * Configuration available
     */
    @Activate
    public void activate(final MqttClientConfiguration config) throws Exception {
        // Validate configuration
        reconnectDelayMs = config.client_reconnect_delay();
        if (reconnectDelayMs < 100) {
            reconnectDelayMs = 100;
        } else if (reconnectDelayMs > Duration.ofHours(1).toMillis()) {
            reconnectDelayMs = (int) Duration.ofHours(1).toMillis();
        }
        final String broker = makeBrokerUri(config);
        clientId = makeClientId(config);
        topics = config.topics();
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

        connectOptions = setupOptions(config);

        // Start client (blocking)
        logger.debug("Connecting MQTT client with ID {}", clientId);
        client = new MqttClient(broker, clientId);
        client.setCallback(this);
        client.setManualAcks(true);
        try {
            client.connect(connectOptions);
        } catch (MqttException e) {
            if (e.getCause() instanceof ConnectException) {
                connectionLost(e);
                logger.warn("MQTT client {} started, but currently unconnected", clientId);
                return;
            } else {
                // We fail to start due to the misconfiguration
                throw e;
            }
        }

        subscribe();

        // All done
        logger.info("MQTT client {} started", clientId);
    }

    private void subscribe() {
        // Register to topics (we're now connected)
        for (String topic : topics) {
            logger.debug("Subscribing MQTT client {} to topic: {}", clientId, topic);
            try {
                client.subscribe(topic);
            } catch (MqttException e) {
                logger.error("MQTT Client {} is unable to subscribe to topic {}", clientId, topic);
            }
        }
    }

    @Deactivate
    public void deactivate() throws Exception {
        if (client != null) {
            if (client.isConnected()) {
                client.disconnect();
            }
            client.close();
            logger.info("MQTT client {} stopped", client.getClientId());
            client = null;
        }

        if (reconnectTimer != null) {
            reconnectTimer.cancel();
            reconnectTimer = null;
        }

        handlerId = null;
    }

    private MqttConnectOptions setupOptions(final MqttClientConfiguration config) throws Exception {
        // Always start with a clean session
        final MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setConnectionTimeout(config.client_connection_timeout());

        // Setup password-based authentication
        final String userName = config.user();
        if (userName != null && !userName.isBlank()) {
            logger.debug("Connecting MQTT with authentication");
            options.setUserName(userName);
        }

        final String userPass = config._password();
        if (userPass != null) {
            options.setPassword(userPass.toCharArray());
        }

        // Setup certificate-based authentication
        if (config.auth_keystore_path() != null || config.auth_clientcert_path() != null) {
            options.setSocketFactory(SSLUtils.setupSSLSocketFactory(config));
        }

        return options;
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

        final boolean clientAuth = config.auth_keystore_path() != null || config.auth_clientcert_path() != null;
        String protocol = config.protocol();
        if (protocol == null || protocol.isBlank()) {
            protocol = "tcp";
        } else {
            protocol = protocol.strip().toLowerCase();
        }

        if (clientAuth) {
            if ("tcp".equals(protocol)) {
                protocol = "ssl";
            } else if ("ws".equals(protocol)) {
                protocol = "wss";
            } else {
                logger.warn("Trying to use client authentication on an unsecure connection.");
            }
        }

        final String path = protocol.startsWith("ws") ? config.path() : null;
        return new URI(protocol, null, mqttHost, config.port(), path, null, null).toString();
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
                    if (client == null) {
                        logger.error("Trying to reconnect a null client.");
                        return;
                    }
                    client.connect(connectOptions);
                } catch (MqttException e) {
                    if (e.getCause() instanceof ConnectException) {
                        logger.error("Error trying to reconnect to MQTT broker: {}", e.getMessage(), e);
                        connectionLost(e);
                    } else {
                        logger.error(
                                "Fatal error trying to reconnect to MQTT broker: {}. No further reconnection will be attempted",
                                e.getMessage(), e);
                    }
                    return;
                }
                subscribe();
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

        Map<IMqttMessageListener, Predicate<String>> listenersCopy;
        synchronized(lock) {
            listenersCopy = new IdentityHashMap<>(listeners);
            topic2last.put(snMessage.getTopic(), snMessage);
        }

        // Notify matching listeners
        for (Entry<IMqttMessageListener, Predicate<String>> entry : listenersCopy.entrySet()) {
            if (entry.getValue().test(topic)) {
                try {
                    entry.getKey().onMqttMessage(handlerId, topic, snMessage);
                } catch (Throwable t) {
                    logger.error("Error handling MQTT message. Client={}, topic={}, error={}", handlerId, topic,
                            t.getMessage(), t);
                }
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

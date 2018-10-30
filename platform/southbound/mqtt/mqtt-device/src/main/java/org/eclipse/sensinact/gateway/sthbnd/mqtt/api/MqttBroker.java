/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.mqtt.api;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.listener.MqttConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MqttBroker {
    private static final String DEFAULT_HOST = "127.0.0.1";
    private static final int DEFAULT_PORT = 1883;
    private static final Protocol DEFAULT_PROTOCOL = Protocol.TCP;
    private static final Logger LOG = LoggerFactory.getLogger(MqttBroker.class);

    public enum Protocol {
        TCP, SSL;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private final String clientId;
    private String host;
    private int port;
    private Protocol protocol;
    private MqttSession session;
    private MqttAuthentication authentication;
    private List<MqttTopic> topics;
    private MqttConnectionHandler handler;
    private MqttClient client;

    private MqttBroker(Builder builder) {
        this.clientId = UUID.randomUUID().toString();
        this.host = builder.host;
        this.port = builder.port;
        this.protocol = builder.protocol;
        this.session = builder.session;
        this.authentication = builder.authentication;
        this.topics = builder.topics;
        this.handler = builder.handler;
    }

    public String getClientId() {
        return clientId;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public MqttSession getSession() {
        return session;
    }

    public MqttAuthentication getAuthentication() {
        return authentication;
    }

    public void subscribeToTopic(MqttTopic topic) {
        try {
            client.subscribe(topic.getTopic(), topic.getListener());
            topics.add(topic);
            LOG.info("Subscription to the topic {} done", topic.getTopic());
        } catch (MqttException e) {
            LOG.error("Unable to subscribe to the topic {}", topic.getTopic());
        }
    }

    public void unsubscribeFromTopic(MqttTopic topic) {
        try {
            client.unsubscribe(topic.getTopic());
            topics.remove(topic);
            LOG.info("Unsubscription to the topic {} done", topic.getTopic());
        } catch (MqttException e) {
            LOG.error("Unable to unsubscribe from the topic {}", topic.getTopic());
        }
    }

    public List<MqttTopic> getTopics() {
        return topics;
    }

    public MqttConnectionHandler getConnectionHandler() {
        return handler;
    }

    public void connect() throws MqttException {
        final String brokerUrl = String.format("%s://%s:%d", protocol, host, port);
        client = new MqttClient(brokerUrl, UUID.randomUUID().toString(), new MemoryPersistence());
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        if (session != null) {
            if (session.getCleanSession() != null) {
                connectOptions.setCleanSession(session.getCleanSession());
            }
            if (session.getAutoReconnect() != null) {
                connectOptions.setAutomaticReconnect(session.getAutoReconnect());
            }
            if (session.getMaxInFlight() != null) {
                connectOptions.setMaxInflight(session.getMaxInFlight());
            }
        }
        if (authentication != null) {
            if (authentication.getUsername() != null) {
                connectOptions.setUserName(authentication.getUsername());
            }
            if (authentication.getPassword() != null) {
                connectOptions.setPassword(authentication.getPassword().toCharArray());
            }
            if (authentication.getSslProperties() != null) {
                connectOptions.setSSLProperties(authentication.getSslProperties());
            }
        }
        LOG.info("Connecting to broker: {}", brokerUrl);
        if (handler != null) {
            client.setCallback(handler);
        }
        try {
            if (!client.isConnected()) {
                client.connect(connectOptions);
                if (handler != null) {
                    handler.connectionEstablished(this);
                }
            } else {
                LOG.error("Already connected to the MQTT broker: {}", brokerUrl);
            }
        } catch (MqttException e) {
            if (handler != null) {
                handler.connectionFailed(this);
            }
            client = null;
            LOG.error("Failed to connect to MQTT broker", e);
        }
        LOG.info("Connected to broker: {}:{}", this.host, this.port);
        for (MqttTopic topic : topics) {
            try {
                client.subscribe(topic.getTopic(), topic.getListener());
                LOG.info("Subscription to the topic {} done", topic.getTopic());
            } catch (MqttException e) {
                LOG.error("Unable to subscribe to the topic {}", topic.getTopic());
            }
        }
    }

    public synchronized void disconnect() throws MqttException {
        if (client.isConnected()) {
            for (java.util.Iterator it = topics.iterator(); it.hasNext();) {
                MqttTopic topic=null;
                try {
                    topic=(MqttTopic) it.next();
                    client.unsubscribe(topic.getTopic());
                    it.remove();
                    LOG.info("Unsubscription to the topic {} done", topic.getTopic());
                } catch (MqttException e) {
                    LOG.error("Unable to unsubscribe from the topic {}", topic.getTopic());
                }
            }
            client.disconnect();
            client = null;
            LOG.info("Disconnected from MQTT broker: {}", this.host);
        } else {
            LOG.error("Unable to disconnect from MQTT broker: {}", host);
        }
    }

    public MqttClient getClient() {
        return client;
    }

    public void setSession(MqttSession session) {
        this.session = session;
    }

    /**
     * The builder abstraction.
     */
    public static class Builder {
        private String host = DEFAULT_HOST;
        private int port = DEFAULT_PORT;
        private Protocol protocol = DEFAULT_PROTOCOL;
        private MqttSession session;
        private MqttAuthentication authentication;
        private List<MqttTopic> topics;
        private MqttConnectionHandler handler;

        public Builder() {
            this.session = (new MqttSession.Builder()).build();
            this.authentication = (new MqttAuthentication.Builder()).build();
            this.topics = Collections.synchronizedList(new ArrayList<MqttTopic>());
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder protocol(Protocol protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder session(MqttSession session) {
            this.session = session;
            return this;
        }

        public Builder authentication(MqttAuthentication authentication) {
            this.authentication = authentication;
            return this;
        }

        public Builder topics(List<MqttTopic> topics) {
            this.topics = topics;
            return this;
        }

        public Builder handler(MqttConnectionHandler handler) {
            this.handler = handler;
            return this;
        }

        public MqttBroker build() {
            return new MqttBroker(this);
        }
    }
}

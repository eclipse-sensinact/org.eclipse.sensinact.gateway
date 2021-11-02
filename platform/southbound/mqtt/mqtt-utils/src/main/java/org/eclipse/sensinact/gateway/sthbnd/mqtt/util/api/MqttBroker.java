/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.mqtt.util.api;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.listener.MqttConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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

    private MqttBroker() {
        this.clientId = UUID.randomUUID().toString();
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

    public void publish(String topic, String message){
        try {
            LOG.info("Publishing message {} on the topic {}", message,topic);
            client.publish(topic, message.getBytes(), 1, false);
        } catch (Exception e) {
            LOG.error("Unable to publishing message {} on the topic {}", message,topic);
        }
    }

    public void subscribeToTopic(MqttTopic topic) {
    	synchronized(this.topics) {
    		if(this.topics.indexOf(topic) < 0) {
		        topics.add(topic);
		        LOG.info("Subscription to the topic {} added to the list", topic.getTopic());
		        try {
			        if(client == null || !client.isConnected()) {
			        	this.connect();
			        }
		            client.subscribe(topic.getTopic(), topic.getListener());
		            LOG.info("Subscription to the topic {} done", topic.getTopic());
		        } catch (MqttException e) {
		            LOG.error("Error when subscribing to the topic {}", topic.getTopic(),e);
		        }
    		}
    	}
    }

    public void unsubscribeFromTopic(MqttTopic topic) {
        try {
            client.unsubscribe(topic.getTopic());
            LOG.info("Unsubscription to the topic {} done", topic.getTopic());
            synchronized(this.topics) {
            	topics.remove(topic);
    	    	if(topics.isEmpty()) {
    	    		this.disconnect();
    	    	}
            }
        } catch (MqttException e) {
            LOG.error("Error when unsubscribing from the topic {}", topic.getTopic(), e);
        }
    }

    public List<MqttTopic> getTopics() {
        return topics;
    }

    public MqttConnectionHandler getConnectionHandler() {
        return handler;
    }

    public void connect() throws MqttException {
    	if (client!=null && client.isConnected()) {
            client.disconnect();
        }
    	client = null;
        final String brokerUrl = String.format("%s://%s:%d", protocol, host, port);
        client = new MqttClient(brokerUrl, UUID.randomUUID().toString(), new MemoryPersistence());        
        if (handler == null) {
            LOG.info("Custom Connection Handler not defined, using default reconnection for {}", brokerUrl);
            this.handler=new MqttConnectionHandlerImpl(this);
        }
        client.setCallback(handler);
        doConnect();
    }

    public void doConnect() throws MqttException {
        if(client == null) {
        	throw new MqttException(new NullPointerException("Valid client is required"));
        }
        final String brokerUrl = String.format("%s://%s:%d", protocol, host, port);
        
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
        try {
            if (!client.isConnected()) {
                client.connect(connectOptions);
                handler.connectionEstablished(this);
            } else {
                LOG.error("Already connected to the MQTT broker: {}", brokerUrl);
            }
        } catch (MqttException e) {
            handler.connectionFailed(this);
            LOG.error("Failed to connect to MQTT broker", e);
        }
        LOG.info("Connected to broker: {}:{}", this.host, this.port);
    }

    public void disconnect() throws MqttException {
        synchronized(this.topics) {
	        if(!topics.isEmpty()) {
		        for (Iterator<MqttTopic> it = topics.iterator(); it.hasNext();) {
		            MqttTopic topic=null;
		            try {
		                topic=(MqttTopic) it.next();
		                LOG.info("Unsubscribing from topic {} done", topic.getTopic());
		                client.unsubscribe(topic.getTopic());
		                it.remove();
		                LOG.info("Unsubscription to the topic {} done", topic.getTopic());
		            } catch (MqttException e) {
		                LOG.error("Unable to unsubscribe from the topic {}", topic.getTopic());
		            }
		        }
	        }
        }
        if (client!=null && client.isConnected()) {
            client.disconnect();
            LOG.info("Disconnected from MQTT broker: {}", this.host);
        }
        client = null;
    }

    public MqttClient getClient() {
        return client;
    }

    public void setSession(MqttSession session) {
        this.session = session;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    public void setAuthentication(MqttAuthentication authentication) {
        this.authentication = authentication;
    }

    public void setHandler(MqttConnectionHandler handler) {
        this.handler = handler;
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
            MqttBroker broker=new MqttBroker();
            broker.handler=handler;
            broker.topics=topics;
            broker.authentication=authentication;
            broker.host=this.host;
            broker.port=this.port;
            broker.protocol=this.protocol;
            broker.session=this.session;
            broker.authentication=this.authentication;
            broker.topics=this.topics;
            broker.handler=this.handler;
            return broker;
        }
    }

    private class MqttConnectionHandlerImpl extends MqttConnectionHandler {
    	private Timer timer = new Timer();
    	
        public MqttConnectionHandlerImpl(MqttBroker broker) {
            super(broker);
        }

        @Override
        public void connectionFailed(final MqttBroker broker) {
        	this.timer.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						broker.doConnect();
					} catch (MqttException e) {
						LOG.error("Connection Failed with {}://{}:{}",broker.getProtocol().name(),broker.getHost(),broker.getPort());
					}
				}        		
        	}, 5000);            
        }

        @Override
        public void connectionEstablished(MqttBroker broker) {
            LOG.debug("Connection with {}://{}:{} established",broker.getProtocol().name(),broker.getHost(),broker.getPort());
            for(MqttTopic topic:topics){
                try {
                    LOG.info("Subscription to the topic {} done", topic.getTopic());
                    broker.getClient().subscribe(topic.getTopic(),topic.getListener());
                } catch (Exception e) {
                    LOG.error("Unable to subscribe to the topic {}", topic.getTopic());
                }

            }

        }

        @Override
        public void connectionLost(MqttBroker broker) {
            try {
                broker.connect();
            } catch (MqttException e) {
                LOG.debug("Connection Lost with {}://{}:{}",broker.getProtocol().name(),broker.getHost(),broker.getPort());
            }
        }
    }
}

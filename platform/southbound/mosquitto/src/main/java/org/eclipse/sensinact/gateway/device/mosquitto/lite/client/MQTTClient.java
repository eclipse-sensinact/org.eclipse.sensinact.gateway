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
package org.eclipse.sensinact.gateway.device.mosquitto.lite.client;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.device.exception.MQTTConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * MQTT clients enabled connection sharing among different devices pointing for the same topic
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class MQTTClient {

    private static final Logger LOG = LoggerFactory.getLogger(MQTTClient.class);

    private String broker;
    private MemoryPersistence persistence = new MemoryPersistence();
    private MqttConnectOptions connOpts = new MqttConnectOptions();
    private MqttClient connection = null;

    public MQTTClient(String broker){
        this.broker=broker;
    }

    public void connect() throws MQTTConnectionException{
        String clientId= UUID.randomUUID().toString();
        try {
            connection = new MqttClient(broker, clientId, persistence);
            connOpts.setCleanSession(true);
            connOpts.setAutomaticReconnect(true);
            LOG.info("Connecting to broker: {} ", broker);
            connection.connect(connOpts);
            LOG.info("Connected to broker: {} ",broker);

        } catch (MqttException e) {
            LOG.error("Failed to connected to broker: {} ", e);
            throw new MQTTConnectionException(e);
        }

    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public synchronized MqttClient getConnection() throws MQTTConnectionException {

        if(connection==null || !connection.isConnected()){
            connect();
        }

        while(!connection.isConnected()){
            try {
                LOG.info("Waiting to connect.");
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                LOG.error("Failed waiting for MQTT connection.");
            }
        }

        return connection;
    }
}

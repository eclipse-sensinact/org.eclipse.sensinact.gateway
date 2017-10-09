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
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.mqtt.MQTTBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * MQTT clients enabled connection sharing among different devices pointing for the same topic
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class MQTTClient {

    private static final Logger LOG = LoggerFactory.getLogger(MQTTClient.class);

    private MQTTBroker broker;
    private MemoryPersistence persistence = new MemoryPersistence();
    private MqttConnectOptions connOpts = new MqttConnectOptions();
    private MQTTConnection connection = null;


    public MQTTClient(MQTTBroker broker){
        this.broker=broker;
    }

    public void connect() throws MQTTConnectionException{
        String clientId= UUID.randomUUID().toString();
        try {
            final String brokerURLConnection=String.format("%s://%s:%d",broker.getProtocol(),broker.getHost(),broker.getPort());
            connection = new MQTTConnection(new MqttClient(brokerURLConnection, clientId, persistence));
            if(broker.getSession()!=null){
                if(broker.getSession().getCleanSession()!=null) connOpts.setCleanSession(broker.getSession().getCleanSession());
                if(broker.getSession().getAutoReconnect()!=null) connOpts.setAutomaticReconnect(broker.getSession().getAutoReconnect());
                if(broker.getSession().getMaxInFlight()!=null) connOpts.setMaxInflight( broker.getSession().getMaxInFlight());
            }

            if(broker.getAuth()!=null){
                if(broker.getAuth().getUsername()!=null) connOpts.setUserName(broker.getAuth().getUsername());
                if(broker.getAuth().getPassword()!=null) connOpts.setPassword(broker.getAuth().getPassword().toCharArray());
                if(broker.getAuth().getSSLProperties()!=null) {
                    connOpts.setSSLProperties(broker.getAuth().getSSLProperties());
                }
            }

            LOG.info("Connecting to broker: {} ", broker);
            connection.getConnection().connect(connOpts);
            LOG.info("Connected to broker: {} ",broker);

        } catch (MqttException e) {
            LOG.error("Failed to connected to broker: {} ", e);
            throw new MQTTConnectionException(e);
        }

    }

    public synchronized MQTTConnection getConnection() throws MQTTConnectionException {

        if(connection==null||connection.getConnection()==null || !connection.getConnection().isConnected()){
            connect();
        }

        while(!connection.getConnection().isConnected()){
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

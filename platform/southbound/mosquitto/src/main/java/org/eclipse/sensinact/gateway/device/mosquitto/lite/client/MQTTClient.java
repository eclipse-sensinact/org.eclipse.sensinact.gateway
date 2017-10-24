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

import org.eclipse.paho.client.mqttv3.*;
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
    private final Long  RECONNECT_DELAY_MS=1000l;
    private MQTTBroker broker;
    private MemoryPersistence persistence = new MemoryPersistence();
    private MqttConnectOptions connOpts = new MqttConnectOptions();
    private MQTTConnection connection = null;
    private MQTTConnectionHandler handler;


    public MQTTClient(MQTTBroker broker){
        this.broker=broker;
    }

    public MQTTClient(MQTTBroker broker,MQTTConnectionHandler handler){
        this(broker);
        this.handler=handler;
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
            if(handler!=null) {
                connection.getConnection().setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                        MQTTClient.this.handler.connectionLost(null);
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        //Nothing to do here
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        //Nothing to do here
                    }
                });
            }

            try {
                connection.getConnection().connect(connOpts);
                if(handler!=null) handler.connectionEstablished(null);
            }catch(MqttException exception){
                if(handler!=null) handler.connectionFailed(null);
                connection=null;
                LOG.error("Failed to connect to MQTT broker",exception);
            }


            LOG.info("Connected to broker: {} ",broker);

        } catch (MqttException e) {
            LOG.error("Failed to connected to broker: {} ", e);
            throw new MQTTConnectionException(e);
        }

    }

    private Boolean isNotConnected(){
        return connection==null||connection.getConnection()==null || !connection.getConnection().isConnected();
    }

    public void disconnect() throws MQTTConnectionException {
        getConnection().disconnect();
    }

    public synchronized MQTTConnection getConnection() throws MQTTConnectionException {

        while(isNotConnected()){
            try {
                LOG.info("Waiting to connect.");
                connect();
                Thread.sleep(RECONNECT_DELAY_MS);
            } catch (InterruptedException e) {
                LOG.error("Failed waiting for MQTT connection.");
            }
        }

        return connection;
    }
}

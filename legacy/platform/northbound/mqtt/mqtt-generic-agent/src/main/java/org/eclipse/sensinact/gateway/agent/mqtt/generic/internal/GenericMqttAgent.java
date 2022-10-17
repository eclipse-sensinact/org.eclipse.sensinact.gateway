/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.agent.mqtt.generic.internal;

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic MQTT Agent
 */
public class GenericMqttAgent {
	
    private static final Logger LOG = LoggerFactory.getLogger(GenericMqttAgent.class);
    private final String broker;
    private final int qos;
    private final String username;
    private final String password;
    private MqttClient client;

    /**
     * Constructor
     *
     * @param broker URL of the broker
     * @param qos    QoS for the session
     * @throws IOException
     */
    public GenericMqttAgent(String broker, int qos,String prefix) throws IOException {
        this(broker,qos,prefix,null,null);
    }

    public GenericMqttAgent(String broker, int qos,String prefix,String username,String password) throws IOException {
        super();
        LOG.debug("Connecting to broker {} with QoS {} and prefix {}",broker,qos,prefix);
        this.broker = broker;
        this.qos = qos;
        this.username=username;
        this.password=password;
    }

    private void connect(){
        MqttConnectOptions connOpts = new MqttConnectOptions();
        if (username != null && password!=null) {
            connOpts.setUserName(username);
            connOpts.setPassword(password.toCharArray());
        }
        connOpts.setCleanSession(true);
        try {
            this.client = new MqttClient(this.broker, MqttClient.generateClientId(), new MemoryPersistence());
            this.client.connect(connOpts);
        } catch (MqttException me) {
            LOG.error("reason " + me.getReasonCode());
            LOG.error("msg " + me.getMessage());
            LOG.error("loc " + me.getLocalizedMessage());
            LOG.error("cause " + me.getCause());
            LOG.error("except " + me);
            me.printStackTrace();
        }
    }

    public void publish(String topic, String message) {
        MqttMessage mqMessage = new MqttMessage(message.getBytes());
        mqMessage.setQos(this.qos);
        this.publish(topic, mqMessage);
    }

    public void publish(String topic, MqttMessage message) {
        if(this.client==null){
            connect();
        }
        try {
            this.client.publish(topic, message);
        } catch (MqttPersistenceException me) {
            LOG.error("reason " + me.getReasonCode());
            LOG.error("msg " + me.getMessage());
            LOG.error("loc " + me.getLocalizedMessage());
            LOG.error("cause " + me.getCause());
            LOG.error("except " + me);
            me.printStackTrace();
        } catch (MqttException me) {
            this.client=null;
            LOG.error("reason " + me.getReasonCode());
            LOG.error("msg " + me.getMessage());
            LOG.error("loc " + me.getLocalizedMessage());
            LOG.error("cause " + me.getCause());
            LOG.error("except " + me);
            me.printStackTrace();
        }
    }

    /**
     * Unsubscribe MQTT broker
     */
    public void close() {
        try {
            this.client.disconnect();
        } catch (MqttException me) {
            LOG.error("reason " + me.getReasonCode());
            LOG.error("msg " + me.getMessage());
            LOG.error("loc " + me.getLocalizedMessage());
            LOG.error("cause " + me.getCause());
            LOG.error("except " + me);
            me.printStackTrace();
        }
    }

    public String getBroker() {
        return broker;
    }

    public int getQos() {
        return qos;
    }
}

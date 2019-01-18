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
package org.eclipse.sensinact.gateway.agent.mqtt.generic.internal;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Generic MQTT Agent
 */
public class GenericMqttAgent extends AbstractMidAgentCallback {
    private static final Logger LOG = LoggerFactory.getLogger(GenericMqttAgent.class);
    private final String broker;
    private final int qos;
    private MqttClient client;

    /**
     * Constructor
     *
     * @param broker URL of the broker
     * @param qos    QoS for the session
     * @throws IOException
     */
    public GenericMqttAgent(String broker, int qos,String prefix) throws IOException {
        super();
        LOG.debug("Connecting to broker {} with QoS {} and prefix {}",broker,qos,prefix);
        this.broker = broker;
        this.qos = qos;

    }

    private void connect(){
        MqttConnectOptions connOpts = new MqttConnectOptions();
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

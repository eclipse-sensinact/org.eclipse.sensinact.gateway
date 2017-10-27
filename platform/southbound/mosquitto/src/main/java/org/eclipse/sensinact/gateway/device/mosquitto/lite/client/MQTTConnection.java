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

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.subscriber.MQTTResourceMessage;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.subscriber.MQTTTopicMessage;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.device.exception.MQTTConnectionException;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQTTConnection {

    private static final Logger LOG = LoggerFactory.getLogger(MQTTConnection.class);
    private final MqttClient client;

    public MQTTConnection(MqttClient client){
        this.client=client;
    }

    public void connect() throws MQTTConnectionException {
        try {
            client.connect();
        } catch (Exception e) {
            throw new MQTTConnectionException(e);
        }
    }

    public void disconnect() throws MQTTConnectionException {
        try {
            client.disconnect();
        } catch (Exception e) {
            throw new MQTTConnectionException(e);
        }
    }

    public void subscribe(String topic, final MQTTTopicMessage listener) throws MqttException {
        client.subscribe(topic, new IMqttMessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                try {
                    String stringMessage=new String(message.getPayload());
                    LOG.debug("Notifying listener from topic [%s] with the message [%s]",topic,stringMessage);
                    listener.messageReceived(MQTTConnection.this,topic,stringMessage);
                }catch(Exception e){
                    LOG.error("Listener %s failed to process notification with Error.", listener.getClass().getCanonicalName(), e);
                }
            }
        });
    }

    public void subscribeResource(final Resource resource, final MQTTResourceMessage listener) throws MqttException {
        client.subscribe(resource.getTopic(), new IMqttMessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                try {
                    String stringMessage=new String(message.getPayload());
                    LOG.debug("Notifying resource listener from topic [%s] with the message [%s]",topic,stringMessage);
                    listener.messageReceived(MQTTConnection.this,resource,new String(message.getPayload()));
                }catch(Exception e){
                    LOG.error("Listener %s failed to process resource notification with Error.",listener.getClass().getCanonicalName(),e);
                }

            }
        });
    }

    public MqttClient getConnection(){
        return this.client;
    }

}

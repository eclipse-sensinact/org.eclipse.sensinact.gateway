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
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Resource;

public class MQTTConnection {


    private final MqttClient client;

    public MQTTConnection(MqttClient client){
        this.client=client;
    }

    public void connect() throws MqttException {
        client.connect();
    }

    public void disconnect() throws MqttException {
        client.disconnect();
    }

    public void subscribe(String topic, final MQTTTopicMessage listener) throws MqttException {
        client.subscribe(topic, new IMqttMessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                listener.messageReceived(MQTTConnection.this,topic,new String(message.getPayload()));
            }
        });
    }

    public void subscribeResource(final Resource resource, final MQTTResourceMessage listener) throws MqttException {
        client.subscribe(resource.getTopic(), new IMqttMessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                listener.messageReceived(MQTTConnection.this,resource,new String(message.getPayload()));
            }
        });
    }

    public MqttClient getConnection(){
        return this.client;
    }

}

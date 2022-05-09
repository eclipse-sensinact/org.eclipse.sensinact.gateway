/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.mqtt.util.listener;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public abstract class MqttTopicMessage implements IMqttMessageListener {
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        this.messageReceived(topic, new String(message.getPayload()));
    }

    protected abstract void messageReceived(String topic, String message);
}

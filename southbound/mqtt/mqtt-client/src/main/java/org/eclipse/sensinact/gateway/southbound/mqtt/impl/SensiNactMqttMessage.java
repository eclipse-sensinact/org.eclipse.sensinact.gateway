/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.mqtt.impl;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.sensinact.gateway.southbound.mqtt.api.IMqttMessage;

/**
 * Describes the content of a message
 */
public class SensiNactMqttMessage implements IMqttMessage {

    /**
     * ID of the handler that received the message
     */
    private final String handler;

    /**
     * Message topic
     */
    private final String topic;

    /**
     * Related Paho MQTT message
     */
    private final MqttMessage message;

    /**
     * Sets up the message wrapper
     */
    public SensiNactMqttMessage(final String handlerId, final String topic, final MqttMessage message) {
        this.handler = handlerId;
        this.topic = topic;
        this.message = message;
    }

    @Override
    public String getHandlerId() {
        return handler;
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public byte[] getPayload() {
        // Return a copy of the payload
        return message.getPayload().clone();
    }

    @Override
    public int getQos() {
        return message.getQos();
    }

    @Override
    public boolean isRetained() {
        return message.isRetained();
    }
}

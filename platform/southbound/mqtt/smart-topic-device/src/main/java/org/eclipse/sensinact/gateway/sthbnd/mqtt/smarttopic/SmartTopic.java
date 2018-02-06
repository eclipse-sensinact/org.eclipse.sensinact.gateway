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
package org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.api.MqttBroker;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.api.MqttTopic;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.listener.MqttTopicMessage;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.api.MqttPacket;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.MqttProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.exception.MessageInvalidSmartTopicException;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model.SmartTopicInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartTopic extends MqttTopicMessage {

    private static final Logger LOG = LoggerFactory.getLogger(SmartTopic.class);

    private final MqttBroker broker;
    private final MqttProtocolStackEndpoint endpoint;
    private final SmartTopicInterpolator smartTopicInterpolator;
    private String processor;

    public SmartTopic(MqttProtocolStackEndpoint endpoint, MqttBroker broker, String topic) {
        this.endpoint = endpoint;
        this.broker = broker;
        this.smartTopicInterpolator = new SmartTopicInterpolator(topic);

        try {
            broker.connect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        this.messageReceived(topic, new String(message.getPayload()));
    }

    @Override
    public void messageReceived(String topic, String message) {
        LOG.info("Message received by smarttopic {} topic {} message {}", smartTopicInterpolator.getSmartTopic(), topic, message);

        String service;
        String resource;
        String value;

        try {
            String provider = smartTopicInterpolator.getGroup(topic, "provider");

            try {
                service = smartTopicInterpolator.getGroup(topic, "service");
            } catch(MessageInvalidSmartTopicException e1) {
                service = "info";
            }

            try {
                resource = smartTopicInterpolator.getGroup(topic, "resource");
            } catch(MessageInvalidSmartTopicException e1) {
                resource = "value";
            }

            try {
                value = smartTopicInterpolator.getGroup(topic, "value");
            } catch(MessageInvalidSmartTopicException e1) {
                value = message;
            }

            MqttPacket packet = new MqttPacket(provider, service, resource, value);

            endpoint.process(packet);

            LOG.debug("Creating/Updating device {}/{}/{} with value {}", provider, service, resource, value);
        } catch (Exception e) {
            LOG.error("Failed to process SmartTopic message {}", message, e);
        }
    }

    public void activate() throws MqttException {
        LOG.info("Subscribing smarttopic {} from topic {}", smartTopicInterpolator.getSmartTopic(), smartTopicInterpolator.getTopic());

        MqttTopic topic = new MqttTopic(smartTopicInterpolator.getTopic(), this);

        broker.subscribeToTopic(topic);
    }

    public void desactivate() {
        try {
            broker.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }
}

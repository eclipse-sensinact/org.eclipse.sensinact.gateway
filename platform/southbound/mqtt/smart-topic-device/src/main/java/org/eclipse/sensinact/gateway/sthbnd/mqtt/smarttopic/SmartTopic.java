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
import org.eclipse.sensinact.gateway.sthbnd.mqtt.MqttProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.api.MqttBroker;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.api.MqttPacket;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.api.MqttTopic;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.listener.MqttTopicMessage;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.exception.MessageInvalidSmartTopicException;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model.SmartTopicInterpolator;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.ProcessorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class SmartTopic extends MqttTopicMessage {
    private static final Logger LOG = LoggerFactory.getLogger(SmartTopic.class);
    private final MqttBroker broker;
    private final MqttProtocolStackEndpoint endpoint;
    private final SmartTopicInterpolator smartTopicInterpolator;
    private String processor;
    private final Set<String> providers = new HashSet<String>();

    public SmartTopic(MqttProtocolStackEndpoint endpoint, MqttBroker broker, String topic) {
        this.endpoint = endpoint;
        this.broker = broker;
        this.smartTopicInterpolator = new SmartTopicInterpolator(topic);
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
            providers.add(provider);
            try {
                service = smartTopicInterpolator.getGroup(topic, "service");
            } catch (MessageInvalidSmartTopicException e1) {
                service = "info";
            }
            try {
                resource = smartTopicInterpolator.getGroup(topic, "resource");
            } catch (MessageInvalidSmartTopicException e1) {
                resource = "value";
            }
            try {
                value = smartTopicInterpolator.getGroup(topic, "value");
            } catch (MessageInvalidSmartTopicException e1) {
                value = message;
            }
            String valueProcessed = MqttPojoConfigTracker.processorExecutor.execute(value, ProcessorUtil.transformProcessorListInSelector(processor == null ? "" : processor));
            MqttPacket packet = new MqttPacket(provider, service, resource, valueProcessed);
            endpoint.process(packet);
            LOG.debug("Creating/Updating device {}/{}/{} with value {}", provider, service, resource, value);
        } catch (Exception e) {
            LOG.error("Failed to process SmartTopic message {}", message, e);
        }
    }

    public void activate() throws MqttException {
        LOG.info("Subscribing smarttopic {} from topic {}", smartTopicInterpolator.getSmartTopic(), smartTopicInterpolator.getTopic());
        try {
            this.broker.connect();
        } catch (MqttException e) {
            LOG.error("Failed to connect broker {}", broker.toString(), e);
            e.printStackTrace();
        }
        MqttTopic topic = new MqttTopic(smartTopicInterpolator.getTopic(), this);
        this.broker.subscribeToTopic(topic);
    }

    public void desactivate() {
        for (String provider : providers) {
            try {
                MqttPacket packet = new MqttPacket(provider);
                packet.setGoodbyeMessage(true);
                endpoint.process(packet);
            } catch (Exception e) {
                LOG.error("Unable to remove provider {}", provider, e);
            }
        }
        try {
            broker.disconnect();
        } catch (MqttException e) {
            LOG.error("Failed to disconnect broker {}", broker.toString(), e);
        }
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }
}

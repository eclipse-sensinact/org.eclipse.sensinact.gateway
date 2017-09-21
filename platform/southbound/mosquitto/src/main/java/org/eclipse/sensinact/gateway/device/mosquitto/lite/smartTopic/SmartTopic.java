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
package org.eclipse.sensinact.gateway.device.mosquitto.lite.smartTopic;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.MQTTConnection;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.subscriber.MQTTTopicMessage;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Provider;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Resource;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Service;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.runtime.MQTTManagerRuntime;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.smartTopic.exception.MessageInvalidSmartTopicException;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.smartTopic.model.SmartTopicInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartTopic implements MQTTTopicMessage {
    private static final Logger LOG = LoggerFactory.getLogger(SmartTopic.class);
    private final MQTTConnection connection;
    private final MQTTManagerRuntime runtime;
    private final SmartTopicInterpolator smartTopicInterpolator;
    private String processor;

    public SmartTopic(String smartTopic, MQTTConnection connection,MQTTManagerRuntime runtime){
        this.smartTopicInterpolator=new SmartTopicInterpolator(smartTopic);
        this.connection=connection;
        this.runtime=runtime;
    }

    @Override
    public void messageReceived(MQTTConnection connection, String topic, String message) {

        LOG.info("Message received by smarttopic {} topic {} message {}", smartTopicInterpolator.getSmartTopic(),topic,message);

        String provider=null;
        String service=null;
        String resource=null;
        String value=null;

        try {

            provider=smartTopicInterpolator.getGroup(topic,"provider");

            try{
                service=smartTopicInterpolator.getGroup(topic,"service");
            }catch(MessageInvalidSmartTopicException e1){
                service="info";
            }

            try{
                resource=smartTopicInterpolator.getGroup(topic,"resource");
            }catch(MessageInvalidSmartTopicException e1){
                resource="value";
            }

            try{
                value=smartTopicInterpolator.getGroup(topic,"value");
            }catch(MessageInvalidSmartTopicException e1){
                value=message;
            }

            LOG.info("Creating/Updating device {}/{}/{} with value {}",provider,service,resource,value);

            Provider providerType=new Provider();
            providerType.setName(provider);
            Service serviceType=new Service(providerType);
            serviceType.setName(service);
            Resource resourceType=new Resource(serviceType);
            resourceType.setProcessor(getProcessor());
            resourceType.setName(resource);
            //runtime.updateValue(provider,service,resource,value);
            runtime.messageReceived(connection,resourceType,value);
        } catch (Exception e) {
            LOG.error("Failed to process SmartTopic message {}",message,e);
        }

    }

    public void activate() throws MqttException {

        LOG.info("subscribing smarttopic {} from topic {}",smartTopicInterpolator.getSmartTopic(),smartTopicInterpolator.getTopic());

        connection.subscribe(smartTopicInterpolator.getTopic(), this);

    }

    public void desactivate(){

    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }
}

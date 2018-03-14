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
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.MqttActivator;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.MqttProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.api.MqttBroker;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.api.MqttPacket;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.api.MqttTopic;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.listener.MqttTopicMessage;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model.Provider;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model.Resource;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model.Service;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.ProcessorExecutor;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.ProcessorUtil;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.exception.ProcessorException;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.*;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.processor.formats.iface.ProcessorFormatIface;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Tracker responsible for detecting when new OSGi Service instance that configures an MQTT topic monitoring
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class MqttPojoConfigTracker implements ServiceTrackerCustomizer {

    private static final Logger LOG = LoggerFactory.getLogger(MqttActivator.class);

    private final BundleContext bundleContext;

    private MqttProtocolStackEndpoint endpoint;

    public MqttPojoConfigTracker(MqttProtocolStackEndpoint endpoint, BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.endpoint = endpoint;
    }

    public static final ProcessorExecutor processorExecutor=new ProcessorExecutor(new ArrayList<ProcessorFormatIface>(){{
        add(new ProcessorFormatArray());
        add(new ProcessorFormatBase64());
        add(new ProcessorFormatJSON());
        add(new ProcessorFormatString());
        add(new ProcessorFormatURLEncode());
        add(new ProcessorFormatPlus());
    }});

    @Override
    public Object addingService(ServiceReference serviceReference) {
        LOG.info("Loading POJO device configuration");

        final Provider provider = (Provider) bundleContext.getService(serviceReference);

        LOG.debug("Loading POJO device configuration {}", provider.getName());

        try {
            final MqttBroker broker = provider.getBroker();
            broker.connect();
            for(final Service service : provider.getServices()) {
                for(final Resource resource : service.getResources()) {

                    MqttTopicMessage listener = new MqttTopicMessage() {
                        @Override
                        public void messageReceived(String s, String s1) {

                            try {
                                String value=processorExecutor.execute(s1, ProcessorUtil.transformProcessorListInSelector(resource.getProcessor()==null?"":resource.getProcessor()));
                                MqttPacket packet = new MqttPacket(provider.getName(), service.getName(), resource.getName(), value);
                                endpoint.process(packet);
                            } catch (Exception e){
                                LOG.error("Failed to process MQTT package",e);
                            }

                        }
                    };

                    LOG.info("Subscribing to topic: {}", resource.getTopic());

                    if(resource.getTopic() != null) {
                        MqttTopic topic = new MqttTopic(resource.getTopic(), listener);

                        broker.subscribeToTopic(topic);
                    } else {
                        LOG.warn("Failed to register device {}, topic assigned cannot be null", provider.getName());
                    }

                    if(!provider.isDiscoveryOnFirstMessage()) {
                        LOG.info("Initiating {}/{}/{} with empty value", provider.getName(), service.getName(), resource.getName());
                        MqttPacket packet = new MqttPacket(provider.getName(), service.getName(),
                                resource.getName(), resource.getValue()==null?"":resource.getValue());
                        packet.setHelloMessage(true);
                        endpoint.process(packet);
                        //runtime.updateValue(provider.getName(), service.getName(), resource.getName(), "");
                    } else {
                        LOG.warn("Device {}/{}/{} is hidden until the first message is received",
                                provider.getName(), service.getName(), resource.getName());
                    }

                    LOG.info("Subscribed {}/{}/{} to the topic {}", provider.getName(), service.getName(),
                            resource.getName(), resource.getTopic());
                }
            }

            /*
            if(!provider.isDiscoveryOnFirstMessage()){
                runtime.updateValue(provider.getName(), null,null,null);
            }else {
                LOG.info("Device {} will appear as soon as one of the topic associated received the first message",provider.getName());
            }*/

            LOG.info("sensiNact Device created with the id {}", provider.getName());
        } catch (Exception e) {
            LOG.warn("Failed to create device {}, ignoring device", provider.getName(), e);
        }

        return bundleContext.getService(serviceReference);
    }

    @Override
    public void modifiedService(ServiceReference serviceReference, Object o) {
        //Not used; this generates a lot of duplicated message dispatching.
    }

    @Override
    public void removedService(ServiceReference serviceReference, Object o) {
        LOG.info("Detaching devices MQTT Bus service");

        try {
            Provider provider = (Provider) o;

            MqttPacket packet = new MqttPacket(provider.getName());
            packet.setGoodbyeMessage(true);

            endpoint.process(packet);
            LOG.info("sensiNact device {} removed", provider.getName());
            LOG.info("Detaching devices {} MQTT Bus service", provider.getName());
            provider.getBroker().disconnect();
        } catch (InvalidPacketException e) {
            LOG.error("Failed to read internal package", e);
        } catch (MqttException e) {
            LOG.error("Failed to disconnect", e);
        }
    }
}

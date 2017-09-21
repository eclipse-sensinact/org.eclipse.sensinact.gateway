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
package org.eclipse.sensinact.gateway.device.mosquitto.lite.sensinact;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.Activator;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.MQTTClient;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.MQTTConnection;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.ServerConnectionCache;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.subscriber.MQTTResourceMessage;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Provider;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Resource;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Service;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.ProcessorExecutor;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.ProcessorUtil;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats.ProcessorFormatArray;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats.ProcessorFormatBase64;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats.ProcessorFormatJSON;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats.ProcessorFormatString;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats.iface.ProcessorFormatIface;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.selector.SelectorIface;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.runtime.MQTTManagerRuntime;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracker responsible for detecting when new OSGi Service instance that configures an MQTT topic monitoring
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class MQTTPojoConfigTracker implements ServiceTrackerCustomizer {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);
    private final BundleContext bundleContext;
    private ProtocolStackEndpoint<MQTTPacket> connector;

    MQTTManagerRuntime runtime;

    public MQTTPojoConfigTracker(MQTTManagerRuntime runtime,BundleContext bundleContext){//ProtocolStackEndpoint<MQTTPacket> connector) {
        this.bundleContext=bundleContext;
        this.runtime=runtime;
    }

    @Override
    public Object addingService(ServiceReference serviceReference) {
        LOG.info("Loading POJO device configuration");
        final Provider provider=(Provider)bundleContext.getService(serviceReference);
        MQTTPacket packet = new MQTTPacket(provider.getName());
        packet.setCurrentState(provider);
        LOG.debug("Loading POJO device configuration {}", provider.getName());

        try {

            final MQTTClient client=ServerConnectionCache.getInstance(provider.getName(), provider.getBroker());

            for(final Service service:provider.getServices()){
                for(final Resource resource:service.getResources()){

                    LOG.info("Subscribing to topic: {}", resource.getTopic());

                    if(resource.getTopic()!=null){
                        client.getConnection().subscribeResource(resource, runtime);
                    }else {
                        LOG.warn("Failed to register device {}, topic assigned cannot be null", provider.getName());
                    }

                    if(!provider.isDiscoveryOnFirstMessage()){
                        LOG.info("Initiating {}/{}/{} with empty value",provider.getName(), service.getName(), resource.getName());
                        runtime.messageReceived(client.getConnection(),resource,"");
                        //runtime.updateValue(provider.getName(), service.getName(), resource.getName(), "");
                    } else {
                        LOG.warn("Device {}/{}/{} is hidden until the first message is received", provider.getName(), service.getName(), resource.getName());
                    }

                    LOG.info("Subscribed {}/{}/{} to the topic {}", provider.getName(),service.getName(),resource.getName(),resource.getTopic());

                }
            }

            /*
            if(!provider.isDiscoveryOnFirstMessage()){
                runtime.updateValue(provider.getName(), null,null,null);
            }else {
                LOG.info("Device {} will appear as soon as one of the topic associated received the first message",provider.getName());
            }*/

            LOG.info("Sensinact Device created with the id {}", provider.getName());

        } catch (Exception e) {
            LOG.warn("Failed to create device {}, ignoring device",provider.getName(),e);
        }
        return bundleContext.getService(serviceReference);
    }

    @Override
    public void modifiedService(ServiceReference serviceReference, Object o) {
        //Not used; this generates a lot of duplicated message dispatching.
    }

    @Override
    public void removedService(ServiceReference serviceReference, Object o) {
        LOG.info("Dettaching devices MQTT Bus service");
        try{
            Provider provider=(Provider)o;
            //processRemoval
            runtime.processRemoval(provider.getName());
            LOG.info("Sensinact Device {} removed", provider.getName());
            LOG.info("Dettaching devices {} MQTT Bus service", provider.getName());
            ServerConnectionCache.disconnectInstance(provider.getName());
        }catch (InvalidPacketException e){
            LOG.error("Failed to read sensinact package", e);
        }

    }
}

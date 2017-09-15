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
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.ServerConnectionCache;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Provider;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Resource;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Service;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.ProcessorImpl;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.ProcessorUtil;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats.ProcessorFormatArray;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats.ProcessorFormatJSON;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats.ProcessorFormatString;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats.iface.ProcessorFormatIface;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.selector.SelectorIface;
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
    /**
     * This is the list that will contains the processor formats supported by the mosquitto bridge.
     */
    private final List<ProcessorFormatIface> supportedProcessorFormat=new ArrayList<ProcessorFormatIface>();
    private ProtocolStackEndpoint<MQTTPacket> connector;

    public MQTTPojoConfigTracker(ProtocolStackEndpoint<MQTTPacket> connector, BundleContext bundleContext) {
        this.connector=connector;
        this.bundleContext=bundleContext;
        supportedProcessorFormat.add(new ProcessorFormatArray());
        supportedProcessorFormat.add(new ProcessorFormatString());
        supportedProcessorFormat.add(new ProcessorFormatJSON());
    }

    private void processRemoval(String processorId) throws InvalidPacketException {
        MQTTPacket packet=new MQTTPacket(processorId);
        packet.isGoodbye(true);
        connector.process(packet);
    }

    private void processData(String provider,String service,String resource, String value) throws InvalidPacketException {

        MQTTPacket packet=new MQTTPacket(provider);
        packet.isHello(true);

        try {
            packet.setInfo(service, resource, value);
            connector.process(packet);
        }catch(Exception e){
            LOG.info("Failed to process {}/{}/{} value {}",provider,service,resource,value);
        }

        /*
        packet.setCurrentState(busclient);
        if(busclient!=null&&busclient.getLatitude()!=null&&busclient.getLongitude()!=null)
            try {
                packet.setInfo("admin","location",String.format("%s:%s",packet.getCurrentState().getLatitude(),packet.getCurrentState().getLongitude()));
                connector.process(packet);
            }catch(Exception e){
                LOG.error("Failed to load location from device {}",packet.getServiceProviderIdentifier(),e);
            }
        else {
            LOG.info("Latitude or longitude are null for {}",packet.getServiceProviderIdentifier());
        }


        try {
            packet.setInfo("info", "other", payload);
            connector.process(packet);
        }catch(Exception e){
            LOG.info("Failed to process info/other received for device {}",packet.getServiceProviderIdentifier());
        }

        try {
            packet.setInfo("me", "meother", payload);
            connector.process(packet);
        }catch(Exception e){
            LOG.info("Failed to process me/meother received for device {}",packet.getServiceProviderIdentifier());
        }

        try {
            packet.setInfo("info", "value", payload);
            connector.process(packet);
        }catch(Exception e){
            LOG.info("Failed to process info/value received for device {}",packet.getServiceProviderIdentifier());
        }
        */
    }

    @Override
    public Object addingService(ServiceReference serviceReference) {
        LOG.info("Loading POJO device configuration");
        final Provider provider=(Provider)bundleContext.getService(serviceReference);
        MQTTPacket packet = new MQTTPacket(provider.getName());
        packet.setCurrentState(provider);
        LOG.debug("Loading POJO device configuration {}", provider.getName());

        try {
            final MQTTClient client=ServerConnectionCache.getInstance(provider.getName(), provider.getHost(), provider.getPort());

            for(final Service service:provider.getServices()){
                for(final Resource resource:service.getResources()){

                    LOG.info("**** Subscribing to topic: {}", resource.getTopic());

                    if(resource.getTopic()!=null)
                    client.getConnection().subscribe(resource.getTopic(),new IMqttMessageListener(){
                        @Override
                        public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                            LOG.info("message received: {}", new String(mqttMessage.getPayload()));
                            try {
                                String payload=new String(mqttMessage.getPayload());
                                MQTTPacket packet = null;
                                if(resource.getProcessor()!=null){
                                    LOG.debug("processor defined {}", resource.getProcessor());
                                    List<SelectorIface> selectors= ProcessorUtil.transformProcessorListInSelector(resource.getProcessor());
                                    ProcessorImpl processor=new ProcessorImpl(supportedProcessorFormat);
                                    payload=processor.execute(payload, selectors);
                                }else {
                                    LOG.debug("processor NOT defined");
                                }
                                processData(provider.getName(),service.getName(),resource.getName(),payload);

                            }catch (Exception e){
                                LOG.error("Failed to process MQTT message",e);
                            }

                        }
                    });
                    if(!provider.isDiscoveryOnFirstMessage()){
                        LOG.info("Initiating {}/{}/{} with empty value",provider.getName(), service.getName(), resource.getName());
                        processData(provider.getName(), service.getName(), resource.getName(),"");
                    }

                    LOG.info("**** Subscribed {}/{}/{} to the topic {}", provider.getName(),service.getName(),resource.getName(),resource.getTopic());

                }
            }

            if(!provider.isDiscoveryOnFirstMessage()){
                processData(provider.getName(), null,null,null);
            }else {
                LOG.info("Device {} will appear as soon as one of the topic associated received the first message",provider.getName());
            }
            LOG.info("Sensinact Device created with the id {}", provider.getName());

        } catch (Exception e) {
            LOG.warn("Failed to create device {}, ignoring device",provider.getName(),e);
        }
        return bundleContext.getService(serviceReference);
    }

    @Override
    public void modifiedService(ServiceReference serviceReference, Object o) {
        //Not used
    }

    @Override
    public void removedService(ServiceReference serviceReference, Object o) {
        LOG.info("Dettaching devices MQTT Bus service");
        try{
            Provider provider=(Provider)o;
            processRemoval(provider.getName());
            LOG.info("Sensinact Device {} removed", provider.getName());
            LOG.info("Dettaching devices {} MQTT Bus service", provider.getName());
            ServerConnectionCache.disconnectInstance(provider.getName());
        }catch (InvalidPacketException e){
            LOG.error("Failed to read sensinact package", e);
        }

    }
}

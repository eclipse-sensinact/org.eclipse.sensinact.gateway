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
import org.eclipse.sensinact.gateway.device.mosquitto.lite.device.MQTTBusClient;
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
public class MQTTBusClientTracker implements ServiceTrackerCustomizer {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);
    private final BundleContext bundleContext;
    /**
     * This is the list that will contains the processor formats supported by the mosquitto bridge.
     */
    private final List<ProcessorFormatIface> supportedProcessorFormat=new ArrayList<ProcessorFormatIface>();
    ProtocolStackEndpoint<MQTTPacket> connector;

    public MQTTBusClientTracker(ProtocolStackEndpoint<MQTTPacket> connector, BundleContext bundleContext) {
        this.connector=connector;
        this.bundleContext=bundleContext;
        supportedProcessorFormat.add(new ProcessorFormatArray());
        supportedProcessorFormat.add(new ProcessorFormatString());
        supportedProcessorFormat.add(new ProcessorFormatJSON());
    }

    private String generateID(MQTTBusClient busClient){
        return busClient.getId();
    }

    private void processRemoval(String processorId) throws InvalidPacketException {
        MQTTPacket packet=new MQTTPacket(processorId);
        packet.isGoodbye(true);
        connector.process(packet);
    }

    private void processData(MQTTBusClient busclient,String processorId, String payload) throws InvalidPacketException {

        MQTTPacket packet=new MQTTPacket(processorId);
        packet.isHello(true);
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
            packet.setInfo("info", "value", payload);
            connector.process(packet);
        }catch(Exception e){
            LOG.info("Failed to process info/value received for device {}",packet.getServiceProviderIdentifier());
        }
    }

    @Override
    public Object addingService(ServiceReference serviceReference) {
        LOG.info("Attaching MQTT Bus service");
        final MQTTBusClient busClient=(MQTTBusClient)bundleContext.getService(serviceReference);
        MQTTPacket packet = new MQTTPacket(generateID(busClient));
        packet.setCurrentState(busClient);
        LOG.debug("Updating MQTT Bus service {}",busClient.getId());

        try {

            String instanceFileName=serviceReference.getProperty("service.pid").toString();
            final MQTTClient client=ServerConnectionCache.getInstance(instanceFileName,busClient.getHost(),busClient.getPort());
            LOG.info("Subscribing to topic: {}", busClient.getTopic());
            client.getConnection().subscribe(busClient.getTopic(),new IMqttMessageListener(){
                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    LOG.info("message received: {}", new String(mqttMessage.getPayload()));
                    try {
                        String payload=new String(mqttMessage.getPayload());
                        MQTTPacket packet = null;
                        if(busClient.getProcessor()!=null){
                            List<SelectorIface> selectors= ProcessorUtil.transformProcessorListInSelector(busClient.getProcessor());
                            ProcessorImpl processor=new ProcessorImpl(supportedProcessorFormat);
                            payload=processor.execute(payload, selectors);
                            processData(busClient, busClient.getId(), payload);
                        }else {
                            processData(busClient, busClient.getId(), payload);
                        }
                    }catch (Exception e){
                        LOG.error("Failed to process MQTT message",e);
                    }

                }
            });
            LOG.info("Subscribed to topic: {}", busClient.getTopic());

            if(!busClient.getDiscoveryOnFirstMessage()){
                processData(busClient, busClient.getId(),null);
            }else {
                LOG.info("Device {} ({}) will appear as soon as the topic associated received the first message",busClient.getId(),busClient.getTopic());
            }

            LOG.info("Sensinact Device created with the id {}",generateID(busClient));
        } catch (Exception e) {
            LOG.warn("Failed to create device {}, ignoring device",generateID(busClient),e);
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
            MQTTBusClient busClient=(MQTTBusClient)o;
            processRemoval(busClient.getId());
            LOG.info("Sensinact Device {} removed", busClient.getId());
        }catch (InvalidPacketException e){
            LOG.error("Failed to read sensinact package", e);
        }
        try{
            final String id=serviceReference.getProperty("service.pid").toString();
            LOG.info("Dettaching devices {} MQTT Bus service",id);
            ServerConnectionCache.disconnectInstance(id);
        }catch(Exception e){
            LOG.error("Failed to disconnect from the topic associated with the if {}",serviceReference.getProperty("service.pid"));
        }
    }
}

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

import org.eclipse.sensinact.gateway.device.mosquitto.lite.Activator;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.device.MQTTPropertyFileConfig;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Provider;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Resource;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Service;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Tracker responsible for detecting when new OSGi Service instance that configures an MQTT topic monitoring
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class MQTTPropertyFileConfigTracker implements ServiceTrackerCustomizer {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);
    private final BundleContext bundleContext;
    private Map<String,ServiceRegistration> registration=new HashMap<>();
    /**
     * This is the list that will contains the processor formats supported by the mosquitto bridge.
     */
    public MQTTPropertyFileConfigTracker(ProtocolStackEndpoint<MQTTPacket> connector, BundleContext bundleContext) {
        this.bundleContext=bundleContext;
    }

    @Override
    public Object addingService(ServiceReference serviceReference) {
        LOG.info("Attaching MQTT Bus service");
        final MQTTPropertyFileConfig busClient=(MQTTPropertyFileConfig)bundleContext.getService(serviceReference);
        LOG.debug("Updating MQTT Bus service {}",busClient.getId());

        Provider provider=new Provider();
        provider.setName(busClient.getId());
        provider.setHost(busClient.getHost());
        provider.setPort(busClient.getPort());
        provider.setIsDiscoveryOnFirstMessage(busClient.getDiscoveryOnFirstMessage());


        Service serviceAdmin=new Service();
        serviceAdmin.setName("admin");
        Service serviceInfo=new Service();
        serviceInfo.setName("info");
        provider.getServices().add(serviceAdmin);
        provider.getServices().add(serviceInfo);

        if(busClient!=null&&busClient.getLatitude()!=null&&busClient.getLongitude()!=null)
            try {
                Resource locationResource=new Resource();
                locationResource.setName("location");
                locationResource.setValue(String.format("%s:%s",busClient.getLatitude(),busClient.getLongitude()));
                serviceAdmin.getResources().add(locationResource);
            }catch(Exception e){
                LOG.error("Failed to load location from device {}",busClient.getId(),e);
            }
        else {
            LOG.info("Latitude or longitude are null for {}",busClient.getId());
        }

        try {
            Resource valueResource=new Resource();
            valueResource.setName("value");
            valueResource.setTopic(busClient.getTopic());
            valueResource.setProcessor(busClient.getProcessor());
            serviceInfo.getResources().add(valueResource);
        }catch(Exception e){
            LOG.info("Failed to process info/value received for device {}",busClient.getId());
        }

        Dictionary<String,String> properties=new Hashtable<String,String>();

        registration.put(serviceReference.getProperty("service.pid").toString(),bundleContext.registerService(Provider.class,provider,properties));

        /***
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
         */

        return bundleContext.getService(serviceReference);
    }

    @Override
    public void modifiedService(ServiceReference serviceReference, Object o) {
        //Not used
    }

    @Override
    public void removedService(ServiceReference serviceReference, Object o) {


        ServiceRegistration record=registration.get(serviceReference.getProperty("service.pid").toString());

        LOG.info("Detaching devices MQTT Bus service");

        if(record!=null){

            try{
                record.unregister();
                LOG.info("Sensinact Device {} removed", ((MQTTPropertyFileConfig)o).getId());
            }catch (Exception e){
                LOG.error("Failed to read sensinact package", e);
            }

        }

    }
}

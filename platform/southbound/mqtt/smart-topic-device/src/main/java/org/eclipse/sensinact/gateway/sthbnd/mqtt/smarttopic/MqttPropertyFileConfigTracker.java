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

import org.eclipse.sensinact.gateway.sthbnd.mqtt.device.MqttProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.device.MqttPropertyFileConfig;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model.Provider;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model.Resource;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model.Service;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.api.MqttAuthentication;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.api.MqttBroker;
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
 *
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class MqttPropertyFileConfigTracker implements ServiceTrackerCustomizer {
    private static final Logger LOG = LoggerFactory.getLogger(MqttPropertyFileConfigTracker.class);
    private final BundleContext bundleContext;
    private final MqttProtocolStackEndpoint endpoint;
    private Map<String, ServiceRegistration> registration = new HashMap<>();
    private Map<String, SmartTopic> smartTopicService = new HashMap<String, SmartTopic>();

    /**
     * This is the list that will contains the processor formats supported by the MQTT bridge.
     */
    public MqttPropertyFileConfigTracker(BundleContext bundleContext, MqttProtocolStackEndpoint endpoint) {
        this.bundleContext = bundleContext;
        this.endpoint = endpoint;
    }

    private Provider buildProvider(MqttPropertyFileConfig configFile) throws Exception {
        final Provider provider = new Provider();
        provider.setName(configFile.getId());
        Service serviceAdmin = new Service(provider);
        serviceAdmin.setName("admin");
        Service serviceInfo = new Service(provider);
        serviceInfo.setName("info");
        provider.getServices().add(serviceAdmin);
        provider.getServices().add(serviceInfo);
        MqttAuthentication authentication = null;
        if(configFile.getUsername()!=null && !configFile.getUsername().trim().equals("")&&
                configFile.getPassword()!=null && !configFile.getPassword().trim().equals("") ){
            authentication=new MqttAuthentication.Builder().username(configFile.getUsername()).password(configFile.getPassword()).build();
        }
        MqttBroker broker = new MqttBroker.Builder().host(configFile.getHost()).port(configFile.getPort()).protocol(MqttBroker.Protocol.valueOf(configFile.getProtocol())).authentication(authentication).build();

        provider.setBroker(broker);

        provider.setIsDiscoveryOnFirstMessage(configFile.getDiscoveryOnFirstMessage());
        if (configFile != null && configFile.getLatitude() != null && configFile.getLongitude() != null) {
            try {
                Resource locationResource = new Resource(serviceAdmin);
                locationResource.setName("location");
                locationResource.setValue(String.format("%s:%s", configFile.getLatitude(), configFile.getLongitude()));
                serviceAdmin.getResources().add(locationResource);
            } catch (Exception e) {
                LOG.error("Failed to load location from device {}", configFile.getId(), e);
            }
        } else {
            LOG.info("Latitude or longitude are null for {}", configFile.getId());
        }

        if (configFile.getTopicType().equals("smarttopic")) {
            LOG.info("This topic config {} is a SmartTopic", configFile.getId());
            SmartTopic smartTopic = new SmartTopic(this.endpoint, broker, configFile.getTopic());
            smartTopicService.put(configFile.getId(), smartTopic);
            if (configFile.getProcessor() != null) {
                smartTopic.setProcessor(configFile.getProcessor());
            }
            smartTopic.activate();
            LOG.info("SmartTopic service started.");
        }else {
            try {
                Resource valueResource = new Resource(serviceInfo);
                valueResource.setName("value");
                valueResource.setTopic(configFile.getTopic());
                valueResource.setProcessor(configFile.getProcessor());
                serviceInfo.getResources().add(valueResource);
            } catch (Exception e) {
                LOG.info("Failed to process info/value received for device {}", configFile.getId());
            }
        }
        return provider;
    }

    @Override
    public Object addingService(ServiceReference serviceReference) {
        LOG.info("Attaching MQTT Bus service");
        final MqttPropertyFileConfig configFile = (MqttPropertyFileConfig) bundleContext.getService(serviceReference);
        LOG.debug("Updating MQTT Bus service {}", configFile.getId());
        try {
            Provider provider = buildProvider(configFile);
            Dictionary<String, String> properties = new Hashtable<String, String>();
            if (provider != null) {
                registration.put(serviceReference.getProperty("service.pid").toString(), bundleContext.registerService(Provider.class.getName(), provider, properties));
            }
        } catch (Exception e) {
            LOG.error("Failed to create MQTT device for file {}", configFile.getId(), e);
        }
        return bundleContext.getService(serviceReference);
    }

    @Override
    public void modifiedService(ServiceReference serviceReference, Object o) {
        //Not used
    }

    @Override
    public void removedService(ServiceReference serviceReference, Object o) {
        LOG.info("Detaching devices MQTT Bus service");
        final String servicePid = serviceReference.getProperty("service.pid").toString();
        ServiceRegistration record = registration.remove(servicePid);
        LOG.debug("Removing service pid {} which correspond to record {} from the list", servicePid, record);
        if (record != null) {
            try {
                record.unregister();
                LOG.info("sensiNact device {} removed", ((MqttPropertyFileConfig) o).getId());
            } catch (Exception e) {
                LOG.error("Failed to read internal package", e);
            }
        }
        SmartTopic smartTopicSer = smartTopicService.get(((MqttPropertyFileConfig) o).getId());
        if (smartTopicSer != null) {
            smartTopicSer.desactivate();
        }
    }
}

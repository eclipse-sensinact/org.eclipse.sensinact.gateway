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
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.MQTTClient;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.ServerConnectionCache;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.device.MQTTBusClient;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracker responsible for detecting when new EchoNet lamps were instantiated
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Nascimento</a>
 */
public class MQTTBusClientTracker implements ServiceTrackerCustomizer {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    private final BundleContext bundleContext;
    ProtocolStackEndpoint<MQTTPacket> connector;

    public MQTTBusClientTracker(ProtocolStackEndpoint<MQTTPacket> connector, 
    		BundleContext bundleContext)
    {
        this.connector=connector;
        this.bundleContext=bundleContext;
    }

    private String generateID(MQTTBusClient busClient){
        return busClient.getId();
    }

    @Override
    public Object addingService(ServiceReference serviceReference) {
        LOG.info("Attaching MQTT Bus service");
        final MQTTBusClient busClient=(MQTTBusClient)bundleContext.getService(serviceReference);
        //lamp.connect();
        MQTTPacket packet = new MQTTPacket(generateID(busClient), false);

        try {
            LOG.info("Subscribing to topic: {}", busClient.getTopic());
            MQTTClient client= ServerConnectionCache.getInstance(busClient.getHost(),busClient.getPort());
            client.getConnection().subscribe(busClient.getTopic(),new IMqttMessageListener(){
                @Override
                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                    LOG.debug("message received: {}",new String(mqttMessage.getPayload()));
                    MQTTPacket packet = new MQTTPacket(busClient.getId(), false,new String(mqttMessage.getPayload()));
                    connector.process(packet);
                }
            });
            LOG.info("Subscribed to topic: {}", busClient.getTopic());
            connector.process(packet);
            LOG.info("Sensinact Device created with the id {}",generateID(busClient));
        } catch (Exception e) {
            LOG.warn("Failed to create device {}, ignoring device",generateID(busClient),e);
        }

        return bundleContext.getService(serviceReference);
    }

    @Override
    public void modifiedService(ServiceReference serviceReference, Object o) {
        LOG.debug("Updating MQTT Bus service");
        removedService(serviceReference,o);
        addingService(serviceReference);
    }

    @Override
    public void removedService(ServiceReference serviceReference, Object o) {
        LOG.info("Dettaching MQTT Bus service");
        try{
            MQTTBusClient busClient=(MQTTBusClient)o;
            MQTTPacket packet = new MQTTPacket(generateID(busClient), false,true);
            connector.process(packet);
            LOG.info("Sensinact Device {} removed",generateID(busClient));
        }catch (InvalidPacketException e){
            LOG.error("Failed to read sensinact package", e);
        }
    }
}

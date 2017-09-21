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
package org.eclipse.sensinact.gateway.device.mosquitto.lite.runtime;

import org.eclipse.sensinact.gateway.device.mosquitto.lite.Activator;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.MQTTConnection;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.subscriber.MQTTResourceMessage;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Provider;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Resource;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.Service;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.ProcessorExecutor;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.ProcessorUtil;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats.*;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.formats.iface.ProcessorFormatIface;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.processor.selector.SelectorIface;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.sensinact.MQTTPacket;
import org.eclipse.sensinact.gateway.generic.ProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MQTTManagerRuntime implements MQTTResourceMessage{

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);
    private BundleContext bundleContext;
    private ProtocolStackEndpoint<MQTTPacket> connector;
    /**
     * This is the list that will contains the processor formats supported by the mosquitto bridge.
     */
    private final List<ProcessorFormatIface> supportedProcessorFormat=new ArrayList<ProcessorFormatIface>(){{
        add(new ProcessorFormatArray());
        add(new ProcessorFormatString());
        add(new ProcessorFormatJSON());
        add(new ProcessorFormatBase64());
        add(new ProcessorFormatURLEncode());
    }};

    public MQTTManagerRuntime(ProtocolStackEndpoint<MQTTPacket> connector, BundleContext bundleContext){
        this.connector=connector;
        this.bundleContext=bundleContext;
    }

    private void updateValue(String provider,String service,String resource,String value){

        MQTTPacket packet=new MQTTPacket(provider);
        packet.isHello(true);

        try {
            packet.setInfo(service, resource, value);
            connector.process(packet);
        }catch(Exception e){
            LOG.info("Failed to process {}/{}/{} value {}",provider,service,resource,value,e);
        }

    }

    public void processRemoval(String processorId) throws InvalidPacketException {
        MQTTPacket packet=new MQTTPacket(processorId);
        packet.isGoodbye(true);
        connector.process(packet);
    }

    @Override
    public void messageReceived(MQTTConnection connection, Resource resource, String value) {

        Provider provider=resource.getService().getProvider();
        Service service=resource.getService();

        LOG.info("device {} message received: {}", provider.getName(),value);
        try {
            String payload=value;
            if(resource.getProcessor()!=null){
                LOG.debug("processor defined {}", resource.getProcessor());
                List<SelectorIface> selectors= ProcessorUtil.transformProcessorListInSelector(resource.getProcessor());
                ProcessorExecutor processor=new ProcessorExecutor(supportedProcessorFormat);
                payload=processor.execute(payload, selectors);
            }else {
                LOG.debug("processor NOT defined");
            }
            updateValue(provider.getName(), service.getName(), resource.getName(), payload);

        }catch (Exception e){
            LOG.error("Failed to process MQTT message",e);
        }

    }

}

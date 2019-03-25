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
package org.eclipse.sensinact.gateway.agent.mqtt.inst.internal;

import org.eclipse.sensinact.gateway.agent.mqtt.generic.internal.AbstractMqttHandler;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.message.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

/**
 * AE = sNa Provider
 * Container = sNa Service
 * Container = sNa Resource
 * Instance = sNa Attribute
 */
public class SnaEventEventHandler extends AbstractMqttHandler {
    private final String payloadType;
    private final ConfigurationAdmin conf;
    Logger LOG= LoggerFactory.getLogger(SnaEventEventHandler.class.getName());
    private final String broker;
    private final Integer qos;
    private final String prefix;

    public SnaEventEventHandler(String broker,Integer qos,String prefix,String payloadType,ConfigurationAdmin conf) throws IOException {
        super();
        this.broker=broker;
        this.qos=qos;
        this.prefix=prefix;
        this.payloadType=payloadType;
        this.conf=conf;
    }

    /**
     * Treats the RegisteredUpdatedSnaEvent passed as parameter
     *
     * @param event the RegisteredUpdatedSnaEvent to process
     */
    public void doHandle(SnaUpdateMessageImpl event) {
        if(payloadType.equals("humanreadable")){
            try {
                LOG.debug("Event received update:"+event.getJSON().toString());
                JSONObject eventJson = new JSONObject(event.getJSON()).getJSONObject("notification");
                String provider = event.getPath().split("/")[1];
                String service = event.getPath().split("/")[2];
                String resource = event.getPath().split("/")[3];
                String valueProperty = event.getPath().split("/")[4];
                String value=eventJson.getString(valueProperty);
                switch (event.getType()) {
                    // Create contentInstance
                    case ATTRIBUTE_VALUE_UPDATED:
                        this.agent.publish(String.format("%s%s/%s/%s",prefix,provider,service,resource),value);
                        break;
                    default:
                        return;
                }

            }catch (Exception e){
                LOG.error("Failed",e);
            }
        }else {
            try {
                publicRawMessage(event);
            } catch (Exception e) {
                LOG.error("Failed",e);
            }

        }


    }

    /**
     * Treats the ServiceRegisteredSnaEvent passed as parameter
     *
     * @param event the ServiceRegisteredSnaEvent to process
     */
    public void doHandle(SnaLifecycleMessageImpl event) {
        if(!payloadType.equals("humanreadable")){
            publicRawMessage(event);
        }
    }

    private void publicRawMessage(SnaMessage event){
        try {
            Configuration configuration=conf.getConfiguration("sensinact");
            String namespace=configuration.getProperties().get("namespace").toString();
            if(!new JSONObject(event.getJSON()).getString("uri").contains(":")){
                LOG.debug("Using as namespace {}",namespace);
                this.agent.publish(String.format("%s%s",prefix,namespace),event.getJSON().toString());
                LOG.debug("Sending from namespace {} the message {}",namespace,event.getJSON().toString());
            }else {
                LOG.debug("Not propagating message to remote Sensinact instance {} ",namespace,event.getJSON().toString());
            }

        } catch (Exception e) {
            LOG.error("Failed",e);
        }
    }

    /**
     * @see MidAgentCallback#stop()
     */
    public void stop() {
    }

    /**
     * @see AbstractMidAgentCallback#doHandle(SnaErrorMessageImpl)
     */
    public void doHandle(SnaErrorMessageImpl event) {
    }

    /**
     * @see AbstractMidAgentCallback#doHandle(SnaResponseMessage)
     */
    public void doHandle(SnaResponseMessage event) {
    }
}

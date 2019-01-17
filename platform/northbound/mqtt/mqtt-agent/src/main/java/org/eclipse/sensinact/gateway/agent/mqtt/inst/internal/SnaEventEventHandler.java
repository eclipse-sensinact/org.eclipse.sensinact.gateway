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
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaResponseMessage;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;
import org.json.JSONArray;
import org.json.JSONObject;
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
    Logger LOG= LoggerFactory.getLogger(SnaEventEventHandler.class.getName());
    private final String broker;
    private final Integer qos;
    private final String prefix;

    public SnaEventEventHandler(String broker,Integer qos,String prefix) throws IOException {
        super();
        this.broker=broker;
        this.qos=qos;
        this.prefix=prefix;
    }

    /**
     * Treats the RegisteredUpdatedSnaEvent passed as parameter
     *
     * @param event the RegisteredUpdatedSnaEvent to process
     */
    public void doHandle(SnaUpdateMessageImpl event) {
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

    }

    /**
     * Treats the ServiceRegisteredSnaEvent passed as parameter
     *
     * @param event the ServiceRegisteredSnaEvent to process
     */
    public void doHandle(SnaLifecycleMessageImpl event) {
        /*
        String aeName = event.getPath().split("/")[1];
        JSONObject request = new JSONObject().put("fr", aeName).put("rqi", UUID.randomUUID().toString());
        JSONObject content = new JSONObject();
        switch (event.getType()) {
            // Create AE
            case PROVIDER_APPEARING:
                request.put("op", 1);
                request.put("ty", 2);
                request.put("to", "/" + cseBase);
                content.put("rn", aeName);
                content.put("api", "0.2.481.2.0001.001.000111");
                content.put("lbl", new JSONArray().put("key1").put("key2"));
                content.put("rr", true);
                request.put("pc", new JSONObject().put("m2m:ae", content));
                break;
            // Delete AE
            case PROVIDER_DISAPPEARING:
                request.put("op", 4);
                request.put("ty", 2);
                request.put("to", "/" + cseBase);
                content.put("rn", aeName);
                request.put("pc", new JSONObject().put("m2m:ae", content));
                break;
            // Create container
            case SERVICE_APPEARING:
                request.put("op", 1);
                request.put("ty", 3);
                request.put("to", "/" + cseBase + "/" + aeName);
                content.put("rn", event.getPath().split("/")[2]);
                content.put("lbl", new JSONArray().put(aeName));
                request.put("pc", new JSONObject().put("m2m:cnt", content));
                break;
            // Delete container
            case SERVICE_DISAPPEARING:
                request.put("op", 4);
                request.put("ty", 3);
                request.put("to", "/" + cseBase + "/" + aeName);
                content.put("rn", event.getPath().split("/")[2]);
                request.put("pc", new JSONObject().put("m2m:cnt", content));
                break;
            // Create container
            case RESOURCE_APPEARING:
                request.put("op", 1);
                request.put("ty", 3);
                request.put("to", "/" + cseBase + "/" + aeName + "/" + event.getPath().split("/")[2]);
                content.put("rn", event.getPath().split("/")[3]);
                content.put("lbl", new JSONArray().put(aeName));
                request.put("pc", new JSONObject().put("m2m:cnt", content));
                break;
            // Delete container
            case RESOURCE_DISAPPEARING:
                request.put("op", 4);
                request.put("ty", 3);
                request.put("to", "/" + cseBase + "/" + aeName + "/" + event.getPath().split("/")[2]);
                content.put("rn", event.getPath().split("/")[3]);
                request.put("pc", new JSONObject().put("m2m:cnt", content));
                break;
            default:
                return;
        }
        this.agent.publish(REQUEST_TOPIC + aeName + "/" + cseBase + "/json", new JSONObject().put("m2m:rqp", request).toString());
        */
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

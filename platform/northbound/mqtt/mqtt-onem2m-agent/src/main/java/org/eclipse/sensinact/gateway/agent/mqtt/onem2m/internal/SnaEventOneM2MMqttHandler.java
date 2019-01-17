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
package org.eclipse.sensinact.gateway.agent.mqtt.onem2m.internal;

import org.eclipse.sensinact.gateway.agent.mqtt.generic.internal.AbstractMqttHandler;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaResponseMessage;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.UUID;

/**
 * AE = sNa Provider
 * Container = sNa Service
 * Container = sNa Resource
 * Instance = sNa Attribute
 */
public class SnaEventOneM2MMqttHandler extends AbstractMqttHandler {
    private final String cseBase;
    // Request topic: /oneM2M/req/<originator>/<target-id>/<serialization-format>
    private static final String REQUEST_TOPIC = "/oneM2M/req/";

    public SnaEventOneM2MMqttHandler(String cseBase) throws IOException {
        super();
        this.cseBase = cseBase;
    }

    /**
     * Treats the RegisteredUpdatedSnaEvent passed as parameter
     *
     * @param event the RegisteredUpdatedSnaEvent to process
     */
    public void doHandle(SnaUpdateMessageImpl event) {
        JSONObject eventJson = new JSONObject(event.getJSON()).getJSONObject("notification");
        String aeName = event.getPath().split("/")[1];
        String containerServiceName = event.getPath().split("/")[2];
        String containerResourceName = event.getPath().split("/")[2];
        JSONObject request = new JSONObject().put("fr", aeName).put("to", "/" + cseBase + "/" + aeName + "/" + containerServiceName + "/" + containerResourceName).put("rqi", UUID.randomUUID().toString());
        JSONObject content = new JSONObject();
        switch (event.getType()) {
            // Create contentInstance
            case ATTRIBUTE_VALUE_UPDATED:
                request.put("op", 1);
                request.put("ty", 4);
                content.put("con", eventJson.get(DataResource.VALUE));
                request.put("pc", new JSONObject().put("m2m:cin", content));
                break;
            default:
                return;
        }
        this.agent.publish(REQUEST_TOPIC + aeName + "/" + cseBase + "/json", new JSONObject().put("m2m:rqp", request).toString());
    }

    /**
     * Treats the ServiceRegisteredSnaEvent passed as parameter
     *
     * @param event the ServiceRegisteredSnaEvent to process
     */
    public void doHandle(SnaLifecycleMessageImpl event) {
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

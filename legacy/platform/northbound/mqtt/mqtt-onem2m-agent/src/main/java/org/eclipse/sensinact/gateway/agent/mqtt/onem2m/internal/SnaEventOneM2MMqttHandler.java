/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.agent.mqtt.onem2m.internal;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.sensinact.gateway.agent.mqtt.generic.internal.AbstractMqttHandler;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.MidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaResponseMessage;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;

import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.spi.JsonProvider;

/**
 * AE = sNa Provider
 * Container = sNa Service
 * Container = sNa Resource
 * Instance = sNa Attribute
 */
public class SnaEventOneM2MMqttHandler extends AbstractMqttHandler {
	private static final Logger LOG = LoggerFactory.getLogger(SnaEventOneM2MMqttHandler.class);
	
	private final JsonProvider provider = JsonProviderFactory.getProvider();
	private final ObjectMapper mapper = JsonMapper.builder()
			.addModule(new JSONPModule(provider))
			.build();
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
        JsonObject eventJson;
		try {
			eventJson = mapper.readValue(event.getJSON(), JsonObject.class).getJsonObject("notification");
		} catch (Exception e) {
			LOG.error("Failed to read the Sna Update Message", e);
			return;
		}
        String aeName = event.getPath().split("/")[1];
        String containerServiceName = event.getPath().split("/")[2];
        String containerResourceName = event.getPath().split("/")[2];
        JsonObjectBuilder request = provider.createObjectBuilder()
        		.add("fr", aeName)
        		.add("to", "/" + cseBase + "/" + aeName + "/" + containerServiceName + "/" + containerResourceName)
        		.add("rqi", UUID.randomUUID().toString());
        JsonObjectBuilder content = provider.createObjectBuilder();
        switch (event.getType()) {
            // Create contentInstance
            case ATTRIBUTE_VALUE_UPDATED:
                request.add("op", 1);
                request.add("ty", 4);
                content.add("con", eventJson.get(DataResource.VALUE));
                request.add("pc", provider.createObjectBuilder().add("m2m:cin", content));
                break;
            default:
                return;
        }
        this.agent.publish(REQUEST_TOPIC + aeName + "/" + cseBase + "/json", 
        		provider.createObjectBuilder().add("m2m:rqp", request).build().toString());
    }

    /**
     * Treats the ServiceRegisteredSnaEvent passed as parameter
     *
     * @param event the ServiceRegisteredSnaEvent to process
     */
    public void doHandle(SnaLifecycleMessageImpl event) {
        String aeName = event.getPath().split("/")[1];
        JsonObjectBuilder request = provider.createObjectBuilder()
        		.add("fr", aeName)
        		.add("rqi", UUID.randomUUID().toString());
        JsonObjectBuilder content = provider.createObjectBuilder();
        switch (event.getType()) {
            // Create AE
            case PROVIDER_APPEARING:
                request.add("op", 1);
                request.add("ty", 2);
                request.add("to", "/" + cseBase);
                content.add("rn", aeName);
                content.add("api", "0.2.481.2.0001.001.000111");
                content.add("lbl", provider.createArrayBuilder().add("key1").add("key2"));
                content.add("rr", true);
                request.add("pc", provider.createObjectBuilder().add("m2m:ae", content));
                break;
            // Delete AE
            case PROVIDER_DISAPPEARING:
                request.add("op", 4);
                request.add("ty", 2);
                request.add("to", "/" + cseBase);
                content.add("rn", aeName);
                request.add("pc", provider.createObjectBuilder().add("m2m:ae", content));
                break;
            // Create container
            case SERVICE_APPEARING:
                request.add("op", 1);
                request.add("ty", 3);
                request.add("to", "/" + cseBase + "/" + aeName);
                content.add("rn", event.getPath().split("/")[2]);
                content.add("lbl", provider.createArrayBuilder().add(aeName));
                request.add("pc", provider.createObjectBuilder().add("m2m:cnt", content));
                break;
            // Delete container
            case SERVICE_DISAPPEARING:
                request.add("op", 4);
                request.add("ty", 3);
                request.add("to", "/" + cseBase + "/" + aeName);
                content.add("rn", event.getPath().split("/")[2]);
                request.add("pc", provider.createObjectBuilder().add("m2m:cnt", content));
                break;
            // Create container
            case RESOURCE_APPEARING:
                request.add("op", 1);
                request.add("ty", 3);
                request.add("to", "/" + cseBase + "/" + aeName + "/" + event.getPath().split("/")[2]);
                content.add("rn", event.getPath().split("/")[3]);
                content.add("lbl", provider.createArrayBuilder().add(aeName));
                request.add("pc", provider.createObjectBuilder().add("m2m:cnt", content));
                break;
            // Delete container
            case RESOURCE_DISAPPEARING:
                request.add("op", 4);
                request.add("ty", 3);
                request.add("to", "/" + cseBase + "/" + aeName + "/" + event.getPath().split("/")[2]);
                content.add("rn", event.getPath().split("/")[3]);
                request.add("pc", provider.createObjectBuilder().add("m2m:cnt", content));
                break;
            default:
                return;
        }
        this.agent.publish(REQUEST_TOPIC + aeName + "/" + cseBase + "/json", 
        		provider.createObjectBuilder().add("m2m:rqp", request).toString());
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
    public void doHandle(SnaResponseMessage<?, ?> event) {
    }
}

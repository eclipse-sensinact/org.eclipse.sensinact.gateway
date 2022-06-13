/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.agent.http.onem2m.internal;

import java.io.IOException;

import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.MidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaResponseMessage;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;
import org.eclipse.sensinact.gateway.util.CastUtils;
import org.eclipse.sensinact.gateway.util.GeoJsonUtils;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.eclipse.sensinact.gateway.util.location.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;

import jakarta.json.JsonObject;

/**
 * AE = sNa Provider
 * Container = sNa Service
 * Container = sNa Resource
 * Instance = sNa Attribute
 */
public class SnaEventOneM2MHttpHandler extends AbstractMidAgentCallback {
    private static Logger LOG = LoggerFactory.getLogger(SnaEventOneM2MHttpHandler.class.getCanonicalName());
    private final String cseBase;
    private final ObjectMapper mapper = JsonMapper.builder()
    		.addModule(new JSONPModule(JsonProviderFactory.getProvider()))
    		.build();
    
    public SnaEventOneM2MHttpHandler(String cseBase) throws IOException {
        this.cseBase = cseBase;
    }

    /**
     * Treats the RegisteredUpdatedSnaEvent passed as parameter
     *
     * @param event the RegisteredUpdatedSnaEvent to process
     */
    public void doHandle(SnaUpdateMessageImpl event) {

        LOG.debug("Received event {}", event.getJSON().toString());

        OneM2MModel model = OneM2MModel.getInstance(cseBase);

        JsonObject eventJson;
		try {
			eventJson = mapper.readValue(event.getJSON(), JsonObject.class).getJsonObject("notification");
		} catch (Exception e1) {
			LOG.error("Unable to read event {}", event.getJSON(), e1);
			return;
		}
		
        final String eventPathSplit[] = event.getPath().split("/");
        final String provider = eventPathSplit[1];
        final String service = eventPathSplit[2];
        final String resource = eventPathSplit[3];

        LOG.debug("Extracted provider '{}' service '{}' and resource '{}' from message", provider, service, resource);

        switch (event.getType()) {
            // Create contentInstance
            case ATTRIBUTE_VALUE_UPDATED:

                Object value = eventJson.get(DataResource.VALUE);
                LOG.debug("Extracted value '{}' from message", value.toString());
                if (event.getPath().endsWith("/admin/location/value")) {
                    LOG.debug("Location update message");
                    Point p = GeoJsonUtils.getFirstPointFromLocationString(CastUtils.cast(String.class, value));
                    if(p == null){
                        return;
                    }
                    try {
                        LOG.debug("Extracted latitude '{}'", p.latitude);
                        LOG.debug("Extracted longitude '{}'", p.longitude);
                        model.integrateReading(provider, "location", "latitude", String.valueOf(p.latitude));
                        model.integrateReading(provider, "location", "longitude", String.valueOf(p.longitude));
                    } catch (NumberFormatException e) {
                        LOG.error("Failed to integrate value", e);
                    }
                } else {
                    LOG.debug("Value update message to value '{}'", value.toString());
                    try {
                        model.integrateReading(provider, service, resource, value.toString());
                    } catch (NumberFormatException e) {
                        LOG.error("Failed to integrate value '{}'", value.toString(), e);
                    }
                }
                break;
            default:
                return;
        }
    }

    /**
     * Treats the ServiceRegisteredSnaEvent passed as parameter
     *
     * @param event the ServiceRegisteredSnaEvent to process
     */
    public void doHandle(SnaLifecycleMessageImpl event) {
        LOG.debug("Received Lifecycle event {}", event.getJSON().toString());

        LOG.debug("Evaluating event of the type {}", event.getType());

        switch (event.getType()) {
            case RESOURCE_DISAPPEARING:
                LOG.debug("Provider disappearing{}", event.getJSON().toString());
                String provider = event.getPath().split("/")[1];
                String service = event.getPath().split("/")[2];
                String resource = event.getPath().split("/")[3];
                OneM2MModel.getInstance(cseBase).removeProvider(provider);
                break;
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
    public void doHandle(SnaResponseMessage<?, ?> event) {
    }

}

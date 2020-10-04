/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.agent.mqtt.inst.internal;

import java.io.IOException;

import org.eclipse.sensinact.gateway.agent.mqtt.generic.internal.AbstractMqttHandler;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AE = sNa Provider
 * Container = sNa Service
 * Container = sNa Resource
 * Instance = sNa Attribute
 */
public class SnaEventEventHandler extends AbstractMqttHandler {
	
    Logger LOG= LoggerFactory.getLogger(SnaEventEventHandler.class.getName());
    
    private final String prefix;

    public SnaEventEventHandler(String prefix) throws IOException {
        super();
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
}

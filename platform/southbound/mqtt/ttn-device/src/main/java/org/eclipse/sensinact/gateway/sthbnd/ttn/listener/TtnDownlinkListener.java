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
package org.eclipse.sensinact.gateway.sthbnd.ttn.listener;

import java.util.Base64;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.api.MqttBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TtnDownlinkListener {
	
	private static final Logger LOG = LoggerFactory.getLogger(TtnDownlinkListener.class);

    private final Mediator mediator;
    private MqttBroker broker;

    public TtnDownlinkListener(Mediator mediator) {
        this.mediator = mediator;
    }

    public void setBroker(MqttBroker broker) {
    	this.broker = broker;
    }
    
    public void messageReceived(String applicationId, String deviceId, Object value) {
    	byte[] bytes = null;
    	String topic = String.format("%s/devices/%s/down",applicationId,deviceId);
    	if(value != null) {
    		if(value.getClass().isArray() && value.getClass().getComponentType() == byte.class) {
    			bytes = (byte[])value;
    		} else {
    			if(value.getClass()==String.class)
    				bytes = ((String)value).getBytes();
    			else 
    				bytes = String.valueOf(value).getBytes();	        			
    		}
		}
    	if(bytes == null) {
            if(LOG.isErrorEnabled()) 
                LOG.error("Null downlink value ");
    		return;
    	}
    	String message = String.format("{\"port\":1,\"confirmed\":true,\"payload_raw\":\"%s\"}", 
				Base64.getEncoder().encodeToString(bytes));
    	this.broker.publish(topic, message);
        if(LOG.isDebugEnabled()) 
            LOG.debug("Sent downlink message: " + message);
    }
}

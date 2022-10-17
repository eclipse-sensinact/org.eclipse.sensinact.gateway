/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.mqtt.util.api;

import org.eclipse.sensinact.gateway.sthbnd.mqtt.util.listener.MqttTopicMessage;

public class MqttTopic {
    private final String topic;
    private MqttTopicMessage listener;

    public MqttTopic(String topic, MqttTopicMessage listener) {
        this.topic = topic;
        this.listener = listener;
    }

    public String getTopic() {
        return topic;
    }

    public MqttTopicMessage getListener() {
        return listener;
    }

    public void setListener(MqttTopicMessage listener) {
        this.listener = listener;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
    	if(o == null) {
    		return false;
    	}
    	if(o.getClass() == String.class) {
    		return o.equals(this.topic);
    	}
    	if(MqttTopic.class.isAssignableFrom(o.getClass())) {
    		return ((MqttTopic)o).getTopic().equals(this.topic);
    	}
    	return false;
    }
}

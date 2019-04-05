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
}

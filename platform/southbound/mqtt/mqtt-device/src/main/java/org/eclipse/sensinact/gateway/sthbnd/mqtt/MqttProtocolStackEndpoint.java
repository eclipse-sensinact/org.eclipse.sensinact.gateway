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
package org.eclipse.sensinact.gateway.sthbnd.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.api.MqttBroker;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.api.MqttPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MqttProtocolStackEndpoint extends LocalProtocolStackEndpoint<MqttPacket> {
    private static final Logger LOG = LoggerFactory.getLogger(MqttProtocolStackEndpoint.class);
    private Map<String, MqttBroker> brokers;

    public MqttProtocolStackEndpoint(Mediator mediator) {
        super(mediator);
        this.brokers = new HashMap<String, MqttBroker>();
    }

    public void addBroker(MqttBroker broker) {
        brokers.put(broker.getClientId(), broker);
    }

    public void removeBroker(String clientId) {
        MqttBroker broker = brokers.remove(clientId);
        try {
            broker.disconnect();
        } catch (MqttException e) {
            LOG.error("Unable to disconnect from MQTT broker: {}, {}", broker.getHost(), e);
        }
    }

    public void connectBrokers() {
        for (Map.Entry<String, MqttBroker> map : brokers.entrySet()) {
            try {
                map.getValue().connect();
            } catch (Exception e) {
                LOG.error("Unable to connect from MQTT broker: {}, {}", map.getValue().getHost(), e);
            }
        }
    }

    public void disconnectBrokers() {
        for (Map.Entry<String, MqttBroker> map : brokers.entrySet()) {
            try {
                map.getValue().disconnect();
            } catch (MqttException e) {
                LOG.error("Unable to disconnect from MQTT broker: {}, {}", map.getValue().getHost(), e);
            }
        }
    }

    public Map<String, MqttBroker> getBrokers() {
        return brokers;
    }
}

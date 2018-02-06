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
package org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.model;

import org.eclipse.sensinact.gateway.sthbnd.mqtt.smarttopic.device.MqttProvider;

import java.util.ArrayList;
import java.util.List;

public class Provider implements MqttProvider {

    private String name;
    private MqttBroker broker;
    private List<Service> services=new ArrayList<>();
    private Boolean discoveryOnFirstMessage=Boolean.FALSE;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public Boolean isDiscoveryOnFirstMessage() {
        return discoveryOnFirstMessage;
    }

    public void setIsDiscoveryOnFirstMessage(Boolean discoveryOnFirstMessage) {
        this.discoveryOnFirstMessage = discoveryOnFirstMessage;
    }

    public MqttBroker getBroker() {
        return broker;
    }

    public void setBroker(MqttBroker broker) {
        this.broker = broker;
    }
}

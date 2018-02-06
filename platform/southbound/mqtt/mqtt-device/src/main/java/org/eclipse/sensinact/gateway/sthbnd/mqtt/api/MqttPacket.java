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
package org.eclipse.sensinact.gateway.sthbnd.mqtt.api;

import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.annotation.*;

/**
 * sensiNact Packet
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class MqttPacket implements Packet {

    @ServiceProviderID
    protected String providerId;

    @ServiceID
    protected String serviceId;

    @ResourceID
    protected String resourceId;

    @Data
    protected String payload;

    public MqttPacket(String providerId) {
        this.providerId = providerId;
    }

    public MqttPacket(String providerId, String serviceId, String resourceId, String data) {
        this.providerId = providerId;
        this.serviceId = serviceId;
        this.resourceId = resourceId;
        this.payload = data;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getPayload() {
        return payload;
    }

    @Override
    public byte[] getBytes() {
        //return payload.getBytes();

        return null;
    }
}

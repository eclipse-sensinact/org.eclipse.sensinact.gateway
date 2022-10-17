/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.ttn.packet;

import java.util.List;

import org.eclipse.sensinact.gateway.generic.packet.annotation.AttributeID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Data;
import org.eclipse.sensinact.gateway.generic.packet.annotation.HelloMessage;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Iteration;
import org.eclipse.sensinact.gateway.generic.packet.annotation.MetadataID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ResourceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceProviderID;
import org.eclipse.sensinact.gateway.sthbnd.mqtt.device.MqttPacket;
import org.eclipse.sensinact.gateway.sthbnd.ttn.model.TtnSubPacket;

public class TtnActivationPacket extends MqttPacket {

    @ServiceProviderID
    private String device;

    @HelloMessage
    private static final boolean HELLO_MESSAGE = true;

    private final List<TtnSubPacket<?>> subPackets;
    private int index = -1;

    public TtnActivationPacket(String device, List<TtnSubPacket<?>> subPackets) {
        super(device);

        this.device = device;
        this.subPackets = subPackets;
    }

    @Iteration
    public boolean isLast() {
        return ((++index) == (subPackets.size() - 1));
    }

    @ServiceID
    public String getService() {
        return subPackets.get(index).getService();
    }

    @ResourceID
    public String getResource() {
        return subPackets.get(index).getResource();
    }

    @AttributeID
    public String getAttribute() {
        return subPackets.get(index).getAttribute();
    }

    @MetadataID
    public String getMetadata() {
        return subPackets.get(index).getMetadata();
    }
    
    @Data
    public Object getData() {
        return subPackets.get(index).getValue();
    }
}

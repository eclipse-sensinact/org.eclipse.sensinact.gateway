/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.simulated.slider.internal;

import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Data;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ResourceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceProviderID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Timestamp;

/**
 *
 */
public class SliderPacket implements Packet {
    @ServiceProviderID
    public final String serviceProviderIdentifier;

    @ServiceID
    public final String serviceId;

    @ResourceID
    public final String resourceId;

    @Data
    public final Object value;

    @Timestamp
    public final long timestamp;

    /**
     * @param id
     * @param value
     */
    public SliderPacket(String id, int value) {
        this(id, "cursor", "position", value);
    }

    /**
     * @param id
     * @param serviceId
     * @param resourceId
     * @param value
     */
    public SliderPacket(String id, String serviceId, String resourceId, Object value) {
        this.serviceProviderIdentifier = id;
        this.serviceId = serviceId;
        this.resourceId = resourceId;
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * @InheritedDoc
     * @see Packet#getBytes()
     */
    @Override
    public byte[] getBytes() {
        return null;
    }
}

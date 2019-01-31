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
package org.eclipse.sensinact.gateway.sthbnd.inovallee;

import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Data;
import org.eclipse.sensinact.gateway.generic.packet.annotation.GoodbyeMessage;
import org.eclipse.sensinact.gateway.generic.packet.annotation.HelloMessage;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ResourceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceProviderID;

public class InovalleePacket implements Packet {

    @ServiceProviderID
    private String providerId;
    @ServiceID
    private String serviceId;
    @ResourceID
    private String resourceId;
    @Data
    private String payload;
    @HelloMessage
    private boolean helloMessage;
    @GoodbyeMessage
    private boolean goodbyeMessage;

    public InovalleePacket(String providerId) {
        this.providerId = providerId;
    }

    public InovalleePacket(String providerId, String serviceId, String resourceId, Object data) {
    	this(providerId, serviceId, resourceId, data.toString());
    }
    
    public InovalleePacket(String providerId, String serviceId, String resourceId, String data) {
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
        // legacy. No need.
        return null;
    }

    public void setHelloMessage(boolean helloMessage) {
        this.helloMessage = helloMessage;
    }

    public void setGoodbyeMessage(boolean goodbyeMessage) {
        this.goodbyeMessage = goodbyeMessage;
    }   
}

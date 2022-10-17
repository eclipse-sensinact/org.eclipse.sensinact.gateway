/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.mqtt.device;

import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Data;
import org.eclipse.sensinact.gateway.generic.packet.annotation.GoodbyeMessage;
import org.eclipse.sensinact.gateway.generic.packet.annotation.HelloMessage;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ResourceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceProviderID;

/**
 * sensiNact Packet
 *
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class MqttPacket implements Packet {
    protected String providerId;
    protected String serviceId;
    protected String resourceId;
    protected String payload;
    
    @HelloMessage
    private boolean helloMessage;
    @GoodbyeMessage
    private boolean goodbyeMessage;

    public MqttPacket(String providerId) {
        this.providerId = providerId;
    }

    public MqttPacket(String providerId, String serviceId, String resourceId, String data) {
        this.providerId = providerId;
        this.serviceId = serviceId;
        this.resourceId = resourceId;
        this.payload = data;
    }

    @ServiceProviderID
    public String getProviderId() {
        return providerId;
    }

    @ServiceID
    public String getServiceId() {
        return serviceId;
    }

    @ResourceID
    public String getResourceId() {
        return resourceId;
    }

    @Data
    public String getPayload() {
        return payload;
    }

    @Override
    public byte[] getBytes() {
        return null;
    }

    public void setHelloMessage(boolean helloMessage) {
        this.helloMessage = helloMessage;
    }

    public void setGoodbyeMessage(boolean goodbyeMessage) {
        this.goodbyeMessage = goodbyeMessage;
    }
}

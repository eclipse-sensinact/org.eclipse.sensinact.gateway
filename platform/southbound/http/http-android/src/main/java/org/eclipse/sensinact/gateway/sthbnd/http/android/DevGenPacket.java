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
package org.eclipse.sensinact.gateway.sthbnd.http.android;

import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.annotation.*;

public class DevGenPacket implements Packet {
    private String processorId;
    private String serviceId;
    private String resourceId;
    private boolean isHelloMessage;
    private boolean isGoodByeMessage;
    private String value;

    public DevGenPacket(String processorId, String serviceId, String resourceId) {
        this.processorId = processorId;
        this.serviceId = serviceId;
        this.resourceId = resourceId;
    }

    public DevGenPacket(String processorId, String serviceId, String resourceId, String value) {
        this.processorId = processorId;
        this.serviceId = serviceId;
        this.resourceId = resourceId;
        this.value = value;
    }

    public DevGenPacket(String processorId) {
        this.processorId = processorId;
    }

    private boolean learn = false;

    @Override
    public byte[] getBytes() {
        return new byte[0];
    }

    public boolean isLearn() {
        return learn;
    }

    @CommandID
    public Task.CommandType getCommand() {
        return (getCurrentState() != null)
                ? Task.CommandType.GET : Task.CommandType.SET;
    }

    @ServiceProviderID
    public String getServiceProviderIdentifier() {
        return processorId;
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
    public Object getData() {
        return value;
    }

    @HelloMessage
    public boolean isHello() {
        return this.isHelloMessage;
    }

    @GoodbyeMessage
    public boolean isGoodbye() {
        return this.isGoodByeMessage;
    }

    public String getCurrentState() {
        return value;
    }

    public void setCurrentState(String currentState) {
        this.value = currentState;
    }

    public void isHello(boolean isHelloMessage) {
        this.isHelloMessage = isHelloMessage;
    }

    public void isGoodbye(boolean isGoodByeMessage) {
        this.isGoodByeMessage = isGoodByeMessage;
    }
}

/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.generic.test.moke4;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.TaskManager;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.annotation.CommandID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Data;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ResourceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceProviderID;

public class MokePacket implements Packet {
    private Mediator mediator;

    private String serviceId;
    private String resourceId;
    private String processorId = null;
    private String taskId = null;
    private Object data;

    private CommandType command = null;

    /**
     * Constructor
     *
     * @param mediator    the associated {@link Mediator}
     * @param processorId the targeted {@link PacketProcessor} string
     *                    identifier
     * @param taskId      the associated {@link Task} string identifier
     * @param data        the embedded data array
     */
    public MokePacket(Mediator mediator, String processorId, String taskId, String serviceId, String resourceId, Object data) {
        this.mediator = mediator;
        this.processorId = processorId;
        this.taskId = taskId;
        this.resourceId = resourceId;
        this.data = data;
        this.serviceId = serviceId;
    }

    /**
     * @param taskId
     * @param mediator2
     * @param string
     * @param strings
     */
    public MokePacket(Mediator mediator, String processorId, String taskId, String[] serviceIds) {
        this.mediator = mediator;
        this.processorId = processorId;
        this.data = serviceIds;
        this.taskId = taskId;
    }

    /**
     * Returns the string identifier of the targeted
     * {@link PacketProcessor}
     *
     * @return the string identifier of the targeted
     * {@link PacketProcessor}
     */
    @ServiceProviderID
    public String getServiceProviderIdentifier() {
        return this.processorId;
    }

    /**
     * Returns the string identifier of the
     * associated {@link Task} if any
     *
     * @return the string identifier of the
     * associated {@link Task} if any
     */
    public String getTaskId() {
        return this.taskId;
    }

    /**
     * Returns the data array
     *
     * @return the data array
     */
    @Data
    public Object getData() {
        return this.data;
    }

    /**
     * @return
     */
    @ResourceID
    public String getResourceId() {
        return this.resourceId;
    }

    /**
     * @return
     */
    @ServiceID
    public String getServiceId() {
        return this.serviceId;
    }

    @Override
    public byte[] getBytes() {
        return null;
    }

    @CommandID
    CommandType getCommand() {
        if (this.command == null && this.taskId != null) {
            if (taskId.endsWith("SERVICES_ENUMERATION")) {
                return CommandType.SERVICES_ENUMERATION;
            }
            String[] taskIdElements = this.taskId.split(new String(new char[]{TaskManager.IDENTIFIER_SEP_CHAR}));
            this.command = CommandType.valueOf(taskIdElements[1]);
        }
        return this.command;
    }
}

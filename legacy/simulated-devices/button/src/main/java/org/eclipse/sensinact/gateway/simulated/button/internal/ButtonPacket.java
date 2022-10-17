/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.simulated.button.internal;

import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Data;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ResourceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceProviderID;

/**
 *
 */
public class ButtonPacket implements Packet {
    @ServiceProviderID
    public final String SERVICE_PROVIDER_IDENTIFIER = "button";
    @ServiceID
    public final String SERVICE_ID = "switch";
    @ResourceID
    public final String RESOURCE_ID = "state";
    private final boolean value;

    /**
     * @param value hte value of the button
     */
    public ButtonPacket(boolean value) {
        this.value = value;
    }

    /**
     * @return the current value
     */
    @Data
    public boolean getValue() {
        return this.value;
    }

    /**
     * @see Packet#getBytes()
     */
    @Override
    public byte[] getBytes() {
        return null;
    }
}

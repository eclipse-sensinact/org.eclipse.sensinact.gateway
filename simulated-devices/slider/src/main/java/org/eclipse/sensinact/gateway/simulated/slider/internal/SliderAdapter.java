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

import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;

/**
 *
 */
public class SliderAdapter {
    private final LocalProtocolStackEndpoint<SliderPacket> connector;
    private final String id;

    /**
     * @param connector
     */
    public SliderAdapter(String id, LocalProtocolStackEndpoint<SliderPacket> connector) {
        this.connector = connector;
        this.id = id;
    }

    /**
     * @param value
     */
    public void mouseReleased(int value) {
        try {
            connector.process(new SliderPacket(id, value));
        } catch (InvalidPacketException e) {
            e.printStackTrace();
        }
    }
}

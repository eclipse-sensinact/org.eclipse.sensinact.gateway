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
import org.eclipse.sensinact.gateway.generic.Connector;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.test.ProcessorService;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class MokeProcessor implements ProcessorService {
    private Connector<MokePacket> connector;
    private Mediator mediator;

    MokeProcessor(Mediator mediator, Connector<MokePacket> connector) {
        this.mediator = mediator;
        this.connector = connector;
    }

    /**
     * @throws InvalidPacketException
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.test.ProcessorService#process(java.lang.String)
     */
    @Override
    public void process(String packet) {
        String[] packetElements = packet.split(",");
        try {
            String argument0 = packetElements[0];
            String argument1 = packetElements.length > 1 ? packetElements[1] : "null";
            String argument2 = packetElements.length > 2 ? packetElements[2] : "null";
            String argument3 = packetElements.length > 3 ? packetElements[3] : "null";
            String argument4 = packetElements.length > 4 ? packetElements[4] : "null";

            this.connector.process(new MokePacket(mediator, argument0, argument1, argument2, argument3, argument4));
        } catch (InvalidPacketException e) {
            e.printStackTrace();
        }
    }
}

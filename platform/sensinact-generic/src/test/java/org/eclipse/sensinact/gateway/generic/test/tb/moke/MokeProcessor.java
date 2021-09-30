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
package org.eclipse.sensinact.gateway.generic.test.tb.moke;

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
     * @throws org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException
     * @inheritDoc
     * @see ProcessorService#process(java.lang.String)
     */
    @Override
    public void process(String packet) {
        try {
            this.connector.process(new MokePacket(mediator, packet, null, null, null, null));
        } catch (InvalidPacketException e) {
            e.printStackTrace();
        }
    }
}

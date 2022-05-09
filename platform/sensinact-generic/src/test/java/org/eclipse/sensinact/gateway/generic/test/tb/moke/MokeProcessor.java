/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.test.tb.moke;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.Connector;
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

    @Override
    public void process(String packet) {
        try {
            this.connector.process(new MokePacket(packet, null, null, null, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

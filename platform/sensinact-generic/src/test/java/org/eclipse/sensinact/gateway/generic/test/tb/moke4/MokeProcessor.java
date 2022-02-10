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
package org.eclipse.sensinact.gateway.generic.test.tb.moke4;

import java.util.Arrays;

import org.eclipse.sensinact.gateway.generic.Connector;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.test.ProcessorService;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class MokeProcessor implements ProcessorService {
    private Connector<MokePacket> connector;

    MokeProcessor(Connector<MokePacket> connector) {
        this.connector = connector;
    }

    /**
     * @throws InvalidPacketException
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.test.ProcessorService#process(java.lang.String)
     */
    @Override
    public void process(String packet) {
    	int n=0;
    	int pos = 0;
    	String packetElements[] = new String[5];
    	while(n < 4 && pos < packet.length()) {
    		int index = packet.indexOf(",", pos);
    		if(index > -1) {
    			packetElements[n++]=packet.substring(pos,index);
    			pos = index+1;
    		} else {
    			break;
    		}
    	}
    	packetElements[n] = packet.substring(pos);
        try {
            String argument0 = packetElements[0];
            String argument1 = n > 0 ? packetElements[1] : "null";
            String argument2 = n > 1 ? packetElements[2] : "null";
            String argument3 = n > 2 ? packetElements[3] : "null";
            String argument4 = n > 3 ? packetElements[4] : "null";
            this.connector.process(new MokePacket(argument0, argument1, argument2, argument3, argument4));
        } catch (InvalidPacketException e) {
            e.printStackTrace();
        }
    }
}

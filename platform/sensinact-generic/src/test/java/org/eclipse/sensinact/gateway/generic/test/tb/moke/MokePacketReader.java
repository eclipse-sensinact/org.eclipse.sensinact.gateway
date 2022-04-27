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

import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.PayloadServiceFragment;
import org.eclipse.sensinact.gateway.generic.packet.SimplePacketReader;

/**
 * Extended {@link PayloadServiceFragment} for tests
 */
public class MokePacketReader extends SimplePacketReader<MokePacket> {
	
	private MokePacket packet;
	/**
     * Constructor
     *
     */
    public MokePacketReader() {
        super();
    }

    @Override
    public void load(MokePacket packet) throws InvalidPacketException {
    	this.packet = packet;
    }
    
    @Override
    public void parse() throws InvalidPacketException {
    	if(this.packet == null )
    		super.configureEOF();
    	else {
	        super.setServiceProviderId(packet.getServiceProviderIdentifier());
	        super.setServiceId(packet.getServiceId());
	        super.setResourceId(packet.getResourceId());
	        super.setData(packet.getData());
	        super.setCommand(packet.getCommand());
	        this.packet = null;
	        super.configure();
    	}
    }
}

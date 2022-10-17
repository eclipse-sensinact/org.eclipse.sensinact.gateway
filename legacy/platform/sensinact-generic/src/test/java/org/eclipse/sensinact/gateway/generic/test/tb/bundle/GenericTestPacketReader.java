/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.test.tb.bundle;

import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.SimplePacketReader;

/**
 *
 */
public class GenericTestPacketReader extends SimplePacketReader<GenericTestPacket> {
	
	private GenericTestPacket packet;
	
	/**
     */
    protected GenericTestPacketReader() {
        super();
    }

    @Override
    public void load(GenericTestPacket packet) throws InvalidPacketException {
    	this.packet = packet;
    }
    
    @Override
    public void parse() throws InvalidPacketException{
        super.configureEOF();
    }
}

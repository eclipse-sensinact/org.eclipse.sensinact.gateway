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

package org.eclipse.sensinact.gateway.system.internal;

import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.PacketReader;
import org.eclipse.sensinact.gateway.generic.packet.SimplePacketReader;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * Extended {@link PacketReader} dedicated to {@link SystemPacket} 
 * treatment
 */
public class SystemPacketReader extends SimplePacketReader<SystemPacket> {

	private static final String SENSINACT_SERVICE_PROVIDER = "sensiNact";
    private static final String SENSINACT_SERVICE = "system";
    private static final String SENSINACT_RESOURCE = "event";
	
	/**
	 * @param mediator the mediator
	 */
    public SystemPacketReader(Mediator mediator) {
    	super(mediator);    	
    }
    
	/**
	 * @inheritDoc
	 *
	 * @see PacketReader#parse(Packet)
	 */
    @Override
    public void parse(SystemPacket packet) throws InvalidPacketException {
    	super.setCommand(CommandType.GET);
    	super.setServiceProviderId(SENSINACT_SERVICE_PROVIDER);
    	super.setServiceId(SENSINACT_SERVICE);
    	super.setResourceId(SENSINACT_RESOURCE);
    	super.setData(packet.getMessage());
    	super.configure();
    }
}

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

import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.PacketReader;
import org.eclipse.sensinact.gateway.generic.packet.PacketReaderFactory;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * Extended {@link PacketReaderFactory} dedicated to {@link SystemPacket} readers
 * instantiation
 */
public class SystemPacketReaderFactory implements PacketReaderFactory
{
	/**
	 * @inheritDoc
	 *
	 * @see PacketReaderFactory#handle(java.lang.Class)
	 */
    @Override
    public boolean handle(Class<? extends Packet> packetType) {
	    return SystemPacket.class.isAssignableFrom(packetType);
    }

	/**
	 * @throws InvalidPacketException 
	 * @inheritDoc
	 *
	 * @see PacketReaderFactory#newInstance(Mediator, ExtModelConfiguration, Packet)
	 */
    @Override
    public <P extends Packet> PacketReader<P> newInstance(
    	Mediator mediator, ExtModelConfiguration manager, P packet)
            throws InvalidPacketException 
    {
    	SystemPacket spacket = (SystemPacket) packet;
    	SystemPacketReader packetReader = new SystemPacketReader(mediator);
    	packetReader.parse(spacket);
    	return (PacketReader<P>) packetReader;
    }
}

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

package org.eclipse.sensinact.gateway.simulated.temperature.generator.internal;

import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.PacketReader;
import org.eclipse.sensinact.gateway.generic.packet.PacketReaderFactory;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.discovery.TemperaturesGeneratorDiscoveryPacket;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.discovery.TemperaturesGeneratorDiscoveryPacketReader;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.reader.TemperaturesGeneratorPacketReader;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;

public class TemperaturesGeneratorPacketReaderFactory 
implements PacketReaderFactory
{

	/**
	 * @InheritedDoc
	 *
	 * @see PacketReaderFactory#handle(Class)
	 */
    @Override
    public boolean handle(Class<? extends Packet> packetType)
    {
	    return TemperaturesGeneratorAbstractPacket.class.isAssignableFrom(
	    		packetType);
    }

    /**
	 * @InheritedDoc
	 *
	 * @see PacketReaderFactory#newInstance(Mediator, ExtModelConfiguration, Packet)
	 */
    @Override
    public <P extends Packet> PacketReader<P> newInstance(
    	Mediator mediator, ExtModelConfiguration manager, P packet)
            throws InvalidPacketException
    {
    	TemperaturesGeneratorAbstractPacket tpacket = 
    			(TemperaturesGeneratorAbstractPacket) packet;
    	
        if(packet instanceof TemperaturesGeneratorDiscoveryPacket) 
        {
            PacketReader<TemperaturesGeneratorAbstractPacket> reader = new TemperaturesGeneratorDiscoveryPacketReader(mediator);
            reader.parse(tpacket);
            return (PacketReader<P>) reader;
            
        } else {
            PacketReader<TemperaturesGeneratorAbstractPacket> reader = new TemperaturesGeneratorPacketReader(mediator);
            reader.parse(tpacket);
            return (PacketReader<P>) reader;
        }
    }
}

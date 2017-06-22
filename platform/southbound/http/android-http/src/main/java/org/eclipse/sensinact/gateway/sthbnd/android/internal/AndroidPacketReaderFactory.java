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
package org.eclipse.sensinact.gateway.sthbnd.android.internal;

import org.eclipse.sensinact.gateway.generic.packet.PacketReaderFactory;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.PacketReader;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * 
 */
public class AndroidPacketReaderFactory implements PacketReaderFactory
{

	/**
	 * @inheritDoc
	 *
	 * @see org.eclipse.sensinact.gateway.generic.PacketReaderFactory#handle(java.lang.Class)
	 */
    @Override
    public boolean handle(Class<? extends Packet> packetType)
    {
	    return AndroidPacket.class.isAssignableFrom(packetType);
    }
    
    /**
     * @inheritDoc
     *
     * @see org.eclipse.sensinact.gateway.generic.PacketReaderFactory#
     * newInstance(org.eclipse.sensinact.gateway.util.Mediator, 
     * org.eclipse.sensinact.gateway.generic.core.impl.BasisSnaManager, 
     * Packet)
     */
    @Override
    public <P extends Packet> PacketReader<P> newInstance(
    	Mediator mediator, ExtModelConfiguration manager, 
    	P packet) throws InvalidPacketException
    {
    	AndroidPacket apacket = (AndroidPacket) packet;
    	AndroidPacketReader packetReader =
    			new AndroidPacketReader(mediator);  
    	packetReader.parse(apacket);
    	return (PacketReader<P>) packetReader;
    }
}

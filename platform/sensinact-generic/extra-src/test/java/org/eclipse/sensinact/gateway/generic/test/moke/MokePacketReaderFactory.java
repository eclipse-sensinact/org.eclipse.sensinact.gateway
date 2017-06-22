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

package org.eclipse.sensinact.gateway.generic.test.moke;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.PacketReader;
import org.eclipse.sensinact.gateway.generic.packet.PacketReaderFactory;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class MokePacketReaderFactory implements PacketReaderFactory
{

	//********************************************************************//
	//						NESTED DECLARATIONS			  			      //
	//********************************************************************//

	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//

	//********************************************************************//
	//						STATIC DECLARATIONS							  //
	//********************************************************************//

	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//
	/**
	 * 
	 */
	public MokePacketReaderFactory()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @inheritDoc
	 *
	 * @see PacketReaderFactory#handle(java.lang.Class)
	 */
	@Override
	public boolean handle(Class<? extends Packet> packetType)
	{
		return MokePacket.class.isAssignableFrom(packetType);
	}

	/**
	 * @inheritDoc
	 *
	 * @see PacketReaderFactory#newInstance(Mediator, ExtModelConfiguration, Packet)
	 */	
	@Override
	public <P extends Packet> PacketReader<P> newInstance(
			Mediator mediator, ExtModelConfiguration manager, P packet)
	        throws InvalidPacketException
	{
		MokePacket mokePacket = (MokePacket) packet;
		MokePacketReader reader = new MokePacketReader(mediator);
		reader.parse(mokePacket);
		return (PacketReader<P>) reader;
	}
}

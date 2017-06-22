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
package org.eclipse.sensinact.gateway.generic.packet;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.util.ReflectUtils;

public class DefaultPacketReaderFactory
implements PacketReaderFactory {

	private List<PacketReader<? extends Packet>> packetReaders;
	private Class<? extends Packet> packetType;

	@SuppressWarnings("unchecked")
	public <P extends Packet> DefaultPacketReaderFactory(
		Mediator mediator, ExtModelConfiguration ExtModelConfiguration) 
	{
		this.packetType = ExtModelConfiguration.getPacketType();
		this.packetReaders = new ArrayList<PacketReader<? extends Packet>>();
		
		List<Class<?>> allTypes = ReflectUtils.getAllTypes(
				mediator.getContext().getBundle());

		List<Class<?>> packetReaderTypes = 
			ReflectUtils.getAssignableTypes(PacketReader.class, 
					allTypes);
		
		if(!packetReaderTypes.isEmpty())
		{
			int index =0;
			int length = packetReaderTypes==null
					?0:packetReaderTypes.size();
			
			for(;index < length; index++)
			{
				Class<?> packetReaderType = packetReaderTypes.get(index);
				PacketReader<? extends Packet> packetReader =
				ReflectUtils.<PacketReader<? extends Packet>>getTheBestInstance(
				(Class<PacketReader<? extends Packet>>)packetReaderType, 
				new Object[]{mediator, ExtModelConfiguration});
				
				if(packetReader != null)
				{
					this.packetReaders.add(packetReader);
				}
			}
		}		
		if(this.packetReaders.isEmpty())
		{
			List<Class<?>> structurePacketTypes = 
				ReflectUtils.getAssignableTypes(
					Packet.class, allTypes);
			
			if(structurePacketTypes.size() == 1)
			{
				PacketReader<P> packetReader = null;				
				try
				{
					packetReader = new StructuredPacketReader<P>(
					mediator, (Class<P>) structurePacketTypes.get(0));
					this.packetReaders.add(packetReader);
				}
				catch (InvalidPacketTypeException e)
				{
					mediator.error(e);
				}
			}
		}
	}
	
	/**
	 * @param packetType
	 * @return
	 */
	@Override
	public boolean handle(Class<? extends Packet> packetType)
	{
		return this.packetType.isAssignableFrom(packetType);
	}

	/**
	 * @param mediator
	 * @param manager
	 * @param packet
	 * @return
	 * @throws InvalidPacketException
	 */
	@Override
	public <P extends Packet> PacketReader<P> newInstance(
		Mediator mediator, ExtModelConfiguration manager, 
		P packet) throws InvalidPacketException
	{
		int index = 0;
		int length = packetReaders==null
			?0:packetReaders.size();
		
		for(;index < length; index++)
		{
			try
			{
				PacketReader<P> packetReader = (PacketReader<P>) 
						packetReaders.get(index);				
				packetReader.reset();
				packetReader.parse(packet);
				return packetReader;
				
			} catch(Exception e)
			{
				e.printStackTrace();
				continue;
			}
		}
		return null;
	}
}

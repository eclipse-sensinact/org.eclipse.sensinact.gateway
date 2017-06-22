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
package org.eclipse.sensinact.gateway.generic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketTypeException;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.PacketReader;
import org.eclipse.sensinact.gateway.generic.packet.PacketReaderFactory;
import org.eclipse.sensinact.gateway.generic.packet.DefaultPacketReaderFactory;

public class DefaultConnectorCustomizer<P extends Packet> 
implements ConnectorCustomizer<P>
{
	/**
	 * the list of available {@link PacketReaderFactory}s
	 */
	private List<PacketReaderFactory> factories;
	
	/**
	 * the {@link ExtModelConfiguration} holding the 
	 * {@link Packet} type
	 */
	private ExtModelConfiguration ExtModelConfiguration;
	
	/**
	 * the {@link Mediator} allowing to interact 
	 * with the OSGi host environment
	 */
	private Mediator mediator;

	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing
	 * to interact with the OSGi host environment
	 * 
	 * @param ExtModelConfiguration 
	 * @throws InvalidPacketTypeException 
	 */
	public DefaultConnectorCustomizer(Mediator mediator, 
			ExtModelConfiguration ExtModelConfiguration) 
	{
		this.mediator = mediator;
		this.ExtModelConfiguration = ExtModelConfiguration;
		this.factories = new ArrayList<PacketReaderFactory>();		
		
		//Mediator classloader because we don't need to retrieve
		//all declared factories in the OSGi environment, but only 
		//the one specified in the bundle instantiating this BasisSnaProcessor
        ServiceLoader<PacketReaderFactory> loader = ServiceLoader.load(
        	PacketReaderFactory.class, mediator.getClassLoader());
        
        Iterator<PacketReaderFactory> iterator = loader.iterator();        
		while(iterator.hasNext())
		{
			PacketReaderFactory factory = iterator.next();			
			if(ExtModelConfiguration.getPacketType()==null 
					|| factory.handle(ExtModelConfiguration.getPacketType()))
			{			
				factories.add(factory);	
			}
		}		
		if(this.factories.isEmpty())
		{
			this.factories.add(
				new DefaultPacketReaderFactory(mediator, 
					ExtModelConfiguration));
		}
	}
	
    /**
     * @inheritDoc
     * 
     * @see ConnectorCustomizer#
     * preProcessing(Packet)
     */
    @Override
    public boolean preProcessing(P packet)
    {  
		if(this.mediator.isDebugLoggable())
		{
			this.mediator.debug("pre-processing done");
		}
		return true;
    }

	/**
	 * @inheritDoc
	 * 
	 * @see ConnectorCustomizer#
	 * postProcessing(ExtServiceProviderImpl,
	 * PacketReader)
	 */
	@Override
	public void postProcessing(ExtServiceProviderImpl processor,
			PacketReader<P> packet)
	{
		if(this.mediator.isDebugLoggable())
		{
			this.mediator.debug("post-processing done");
		}
	}
	
	/** 
	 * @inheritDoc
	 * 
     * @see ConnectorCustomizer#
     * newPacketReader(Packet)
     */
    @Override
	public PacketReader<P> newPacketReader(P packet) 
			throws InvalidPacketException
	{
		PacketReader<P> reader = null;
		
		Iterator<PacketReaderFactory> iterator 
			= this.factories.iterator();
		
		while(iterator.hasNext())
		{
			PacketReaderFactory factory = iterator.next();
			if(factory.handle(packet.getClass()))
			{
				reader = factory.newInstance(
					this.mediator, this.ExtModelConfiguration, packet);
				break;
			}
		}
		return reader;
	}
}

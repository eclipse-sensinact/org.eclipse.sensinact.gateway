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

package org.eclipse.sensinact.gateway.simulated.slider.internal;

import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.annotation.*;

/**
 * 
 */
public class SliderPacket implements Packet
{

	@ServiceProviderID
	public String serviceProviderIdentifier = null;
	
	@ServiceID
	public String serviceId = null;
	
	@ResourceID
	public String resourceId = null;

	private final CommandType command;
	private final Object value;

	/**
	 * @param value
	 */
    public SliderPacket(String id , int value)
    {
    	this(id,  "cursor", "position", CommandType.GET,value);
    }

	/**
	 * @param value
	 */
    public SliderPacket(String id , String serviceId, String resourceId, 
    		CommandType command, Object value)
    {
    	this.serviceProviderIdentifier = id;
    	this.serviceId = serviceId;
    	this.resourceId = resourceId;
    	this.command = command;
    	this.value = value;
    }
    
    /**
	 * @return
	 */
    @CommandID
	public CommandType getCommand()
	{
		return this.command;
	}
    
    /**
	 * @return
	 */
    @Data
	public Object getValue()
	{
		return this.value;
	}
	
	/**
	 * @InheritedDoc
	 *
	 * @see Packet#getBytes()
	 */
	@Override
	public byte[] getBytes()
	{
		return null;
	}

	@Timestamp
    public long getTimestamp() {
	    return System.currentTimeMillis();
    }
}

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

import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.annotation.*;

/**
 * 
 */
public class SliderPacket implements Packet
{
	private final int value;

	@ServiceProviderID
	public final String serviceProviderIdentifier = "slider";
	@ServiceID
	public final String serviceId = "cursor";
	@ResourceID
	public final String resourceId = "position";

	/**
	 * @param value
	 */
    public SliderPacket(int value)
    {
    	this.value = value;
    }

	/**
	 * @return
	 */
    @Data
	public int getValue()
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

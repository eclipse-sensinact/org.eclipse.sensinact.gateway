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

package org.eclipse.sensinact.gateway.simulated.button.internal;

import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.annotation.Data;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ResourceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceID;
import org.eclipse.sensinact.gateway.generic.packet.annotation.ServiceProviderID;

/**
 * 
 */
public class ButtonPacket implements Packet {

	@ServiceProviderID
	public final String SERVICE_PROVIDER_IDENTIFIER = "button";
	@ServiceID
	public final String SERVICE_ID = "switch";
	@ResourceID
	public final String RESOURCE_ID = "state";
	private final boolean value;

	/**
	 * @param value hte value of the button
	 */
    public ButtonPacket(boolean value) {
    	this.value = value;
    }

	/**
	 * @return the current value
	 */
    @Data
	public boolean getValue()
	{
		return this.value;
	}
	
	/**
	 * @see Packet#getBytes()
	 */
	@Override
	public byte[] getBytes()
	{
		return null;
	}
}

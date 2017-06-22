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

import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;

/**
 * 
 */
public class SliderAdapter 
{
	private final LocalProtocolStackEndpoint<SliderPacket> connector;

	/**
	 * @param connector 
	 * 
	 */
	public SliderAdapter(LocalProtocolStackEndpoint<SliderPacket> connector)
	{
		this.connector = connector;
	}
    
    /**
     * @param value
     */
    public void mouseReleased(int value)
    {
    	try
        {
            connector.process(new SliderPacket(value));
        }
        catch (InvalidPacketException e)
        {
            e.printStackTrace();
        }
    }
}

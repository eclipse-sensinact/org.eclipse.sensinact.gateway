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
package org.eclipse.sensinact.gateway.generic.test.bundle;

import org.eclipse.sensinact.gateway.generic.packet.Packet;


/**
 * 
 */
public class GenericTestPacket implements Packet
{
	/**
	 * @inheritDoc
	 *
	 * @see Packet#getBytes()
	 */
    @Override
    public byte[] getBytes()
    {
	    return GenericTestPacket.class.getSimpleName().getBytes();
    }
}

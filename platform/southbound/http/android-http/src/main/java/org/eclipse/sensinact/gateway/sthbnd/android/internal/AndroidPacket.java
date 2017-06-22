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


import org.eclipse.sensinact.gateway.generic.packet.Packet;

/**
 * @author <a href="mailto:Jander.BotelhodoNascimento@cea.fr">Jander Botelho do Nascimento</a>
 */
public class AndroidPacket implements Packet
{ 

    private byte[] content;

	public AndroidPacket(byte[] content)
    {
        this.content = content;
    }

    @Override
    public byte[] getBytes()
    {
    	if(this.content ==null ||this.content.length ==0)
    	{
    		return new byte[0];
    	}
    	byte[] content = new byte[this.content.length];
    	System.arraycopy(this.content, 0, content, 0, this.content.length);
        return content;
    }
}

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

package org.eclipse.sensinact.gateway.system.internal;

import org.eclipse.sensinact.gateway.generic.packet.Packet;

/**
 * Extended {@link Packet} wrapping system's messages
 */
public class SystemPacket implements Packet {

    private final String message;

    /**
     * Constructor
     *
     * @param message the message of the packet
     */
    public SystemPacket(String message) {
        this.message = message;
    }

    /**
     * @inheritDoc
     *
     * @see Packet#getBytes()
     */
    @Override
    public byte[] getBytes() {
        return null;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return this.message==null?null:this.message;
    }
}

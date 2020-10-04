/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.generic;

import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.PacketReader;

public interface ConnectorCustomizer<P extends Packet> {
    /**
     * Returns a {@link PacketReader} which is able to  interpret the
     * <code>&gt;P&lt;</code> typed communication {@link Packet}
     *
     * @param packet the {@link Packet} for which to create a new
     *               {@link PacketReader}
     * @return a new {@link PacketReader}
     * @throws InvalidPacketException
     */
    PacketReader<P> newPacketReader(P packet) throws InvalidPacketException;

    /**
     * Processes a pre-processing of the communication packet object before
     * to transmit it to the targeted {@link ExtServiceProviderImpl}(s).
     * Returns true if the communication {@link Packet} object has to be
     * transmitted to the targeted processor for processing; returns false
     * otherwise
     *
     * @param packet the {@link Packet} to pre-process
     * @return true if the processor processing is required; false otherwise
     */
    boolean preProcessing(P packet);

    /**
     * Processes a post-processing of the stream object after it has been
     * transmitted to its associated {@link ExtServiceProviderImpl} instance
     * if this last one exists
     */
    void postProcessing(ExtServiceProviderImpl processor, PacketReader<P> reader);
}

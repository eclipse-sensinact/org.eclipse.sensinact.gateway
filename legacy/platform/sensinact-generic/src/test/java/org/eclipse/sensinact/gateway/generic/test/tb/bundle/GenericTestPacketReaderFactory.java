/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.test.tb.bundle;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.generic.packet.Packet;
import org.eclipse.sensinact.gateway.generic.packet.PacketReader;
import org.eclipse.sensinact.gateway.generic.packet.PacketReaderFactory;

import aQute.bnd.annotation.Resolution;
import aQute.bnd.annotation.spi.ServiceProvider;

/**
 *
 */
@ServiceProvider(value=PacketReaderFactory.class,resolution = Resolution.OPTIONAL)
public class GenericTestPacketReaderFactory implements PacketReaderFactory {
    /**
     * @inheritDoc
     * @see PacketReaderFactory#handle(java.lang.Class)
     */
    @Override
    public boolean handle(Class<? extends Packet> packetType) {
        return false;
    }

    /**
     * @inheritDoc
     * @see PacketReaderFactory#newInstance(Mediator, ExtModelConfiguration, Packet)
     */
    @Override
    public <P extends Packet> PacketReader<P> newInstance(ExtModelConfiguration manager, P packet) {
        return null;
    }
}

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
package org.eclipse.sensinact.gateway.generic.packet;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.ExtModelConfiguration;
import org.eclipse.sensinact.gateway.util.ReflectUtils;

import java.util.ArrayList;
import java.util.List;

public class DefaultPacketReaderFactory implements PacketReaderFactory {
    private List<Class<? extends PacketReader>> packetReaders;
    private Class<? extends Packet> packetType;

    @SuppressWarnings("unchecked")
    public <P extends Packet> DefaultPacketReaderFactory(Mediator mediator, ExtModelConfiguration ExtModelConfiguration) {
        this.packetType = ExtModelConfiguration.getPacketType();
        this.packetReaders = new ArrayList<Class<? extends PacketReader>>();

        List<Class<?>> allTypes = ReflectUtils.getAllTypes(mediator.getContext().getBundle());
        List<Class<?>> packetReaderTypes = ReflectUtils.getAssignableTypes(PacketReader.class, allTypes);

        if (!packetReaderTypes.isEmpty()) {
            int index = 0;
            int length = packetReaderTypes == null ? 0 : packetReaderTypes.size();

            for (; index < length; index++) {
                Class<? extends PacketReader> packetReaderType = (Class<? extends PacketReader>) packetReaderTypes.get(index);
                this.packetReaders.add(packetReaderType);
            }
        }
        if (this.packetReaders.isEmpty()) {
            this.packetReaders.add(StructuredPacketReader.class);
        }
    }

    @Override
    public boolean handle(Class<? extends Packet> packetType) {
        return this.packetType.isAssignableFrom(packetType);
    }

    @Override
    public <P extends Packet> PacketReader<P> newInstance(Mediator mediator, ExtModelConfiguration manager, P packet) throws InvalidPacketException {
        int index = 0;
        int length = packetReaders == null ? 0 : packetReaders.size();
        PacketReader<P> packetReader = null;
        for (; index < length; index++) {
            try {
                Class<? extends PacketReader<P>> packetReaderType = (Class<? extends PacketReader<P>>) packetReaders.get(index);
                if (StructuredPacketReader.class.isAssignableFrom(packetReaderType)) 
                    packetReader = new StructuredPacketReader<P>(mediator, (Class<P>) packet.getClass());
                else
                    packetReader = ReflectUtils.getTheBestInstance(packetReaderType, new Object[]{mediator, manager});
                packetReader.load(packet);
                break;

            } catch (Exception e) {
                continue;
            }
        }
        return packetReader;
    }
}

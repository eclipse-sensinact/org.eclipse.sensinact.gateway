/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
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
    public <P extends Packet> PacketReader<P> newInstance(ExtModelConfiguration manager, P packet) throws InvalidPacketException {
        int index = 0;
        int length = packetReaders == null ? 0 : packetReaders.size();
        PacketReader<P> packetReader = null;
        for (; index < length; index++) {
            try {
                Class<? extends PacketReader<P>> packetReaderType = (Class<? extends PacketReader<P>>) packetReaders.get(index);
                if (StructuredPacketReader.class.isAssignableFrom(packetReaderType)) 
                    packetReader = new StructuredPacketReader<P>((Class<P>) packet.getClass());
                else
                    packetReader = ReflectUtils.getTheBestInstance(packetReaderType, new Object[]{manager});
                packetReader.load(packet);
                break;

            } catch (Exception e) {
                continue;
            }
        }
        return packetReader;
    }
}

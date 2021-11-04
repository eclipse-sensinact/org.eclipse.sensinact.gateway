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
package org.eclipse.sensinact.gateway.simulated.temperature.generator.reader;

import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.SimplePacketReader;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.internal.TemperaturesGeneratorAbstractPacket;

public class TemperaturesGeneratorPacketReader extends SimplePacketReader<TemperaturesGeneratorAbstractPacket> {
    private TemperaturesGeneratorPacket packet;

	/**
     */
    public TemperaturesGeneratorPacketReader() {
        super();
    }

    @Override
    public void load(TemperaturesGeneratorAbstractPacket packet) throws InvalidPacketException {
       this.packet = (TemperaturesGeneratorPacket) packet;
    }

    @Override
    public void parse() throws InvalidPacketException {
        if(this.packet == null) {
        	super.configureEOF();
        	return;
    	}
    	super.setServiceProviderId(packet.getServiceProvider());
        super.setServiceId("sensor");
        super.setResourceId("temperature");
        super.setData(packet.getValue());
        this.packet = null;
        super.configure();
    }
}

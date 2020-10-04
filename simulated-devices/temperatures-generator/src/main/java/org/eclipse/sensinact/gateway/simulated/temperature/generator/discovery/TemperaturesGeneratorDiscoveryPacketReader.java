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
package org.eclipse.sensinact.gateway.simulated.temperature.generator.discovery;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.internal.TemperaturesGeneratorAbstractPacket;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.internal.TemperaturesGeneratorAbstractPacketReader;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.reader.TemperaturesGeneratorPacket;

public class TemperaturesGeneratorDiscoveryPacketReader extends TemperaturesGeneratorAbstractPacketReader {
    /**
     * @param mediator the mediator
     */
    public TemperaturesGeneratorDiscoveryPacketReader(Mediator mediator) {
        super(mediator);
    }

    @Override
    public void parse(TemperaturesGeneratorAbstractPacket packet) throws InvalidPacketException {
        super.setServiceProviderId(((TemperaturesGeneratorDiscoveryPacket) packet).getServiceProvider());
        super.isHelloMessage(true);
        super.configure();
        super.setServiceProviderId(((TemperaturesGeneratorDiscoveryPacket) packet).getServiceProvider());
        super.setServiceId("admin");
        super.setResourceId("location");
        super.setData(((TemperaturesGeneratorDiscoveryPacket) packet).getLocation());
        super.configure();  
        super.setServiceProviderId(((TemperaturesGeneratorDiscoveryPacket) packet).getServiceProvider());
        super.setServiceId("sensor");
        super.setResourceId("temperature");
        super.setData(((TemperaturesGeneratorDiscoveryPacket) packet).getValue());
        super.configure(); 
    }
}

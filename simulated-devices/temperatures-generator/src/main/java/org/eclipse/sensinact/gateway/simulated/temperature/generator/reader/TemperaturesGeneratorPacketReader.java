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

package org.eclipse.sensinact.gateway.simulated.temperature.generator.reader;

import org.eclipse.sensinact.gateway.simulated.temperature.generator.internal.TemperaturesGeneratorAbstractPacket;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.SimplePacketReader;

public class TemperaturesGeneratorPacketReader 
extends SimplePacketReader<TemperaturesGeneratorAbstractPacket>
{

	/**
	 * @param mediator the mediator
	 */
    public TemperaturesGeneratorPacketReader(Mediator mediator) {
	    super(mediator);
    }

    @Override
    public void parse(TemperaturesGeneratorAbstractPacket packet)
    		throws InvalidPacketException 
    {
        super.setServiceProviderId(((TemperaturesGeneratorPacket) packet).getServiceProvider());
        super.setServiceId("sensor");
        super.setResourceId("temperature");
        super.setData(((TemperaturesGeneratorPacket) packet).getValue());
        super.configure();
    }
}

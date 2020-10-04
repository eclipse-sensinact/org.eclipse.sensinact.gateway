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

import org.eclipse.sensinact.gateway.simulated.temperature.generator.reader.TemperaturesGeneratorPacket;

public class TemperaturesGeneratorDiscoveryPacket extends TemperaturesGeneratorPacket {
    private final String location;

    /**
     * @param serviceProviderId the id of the device
     * @param value             the value of the sensor
     */
    public TemperaturesGeneratorDiscoveryPacket(String serviceProviderId, String location, double value) {
        super(serviceProviderId,value);
    	this.location = location;
    }

    public String getLocation() {
        return this.location;
    }
}

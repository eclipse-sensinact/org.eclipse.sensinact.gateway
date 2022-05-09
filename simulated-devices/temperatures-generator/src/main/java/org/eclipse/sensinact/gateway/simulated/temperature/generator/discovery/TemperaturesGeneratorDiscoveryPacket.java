/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
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

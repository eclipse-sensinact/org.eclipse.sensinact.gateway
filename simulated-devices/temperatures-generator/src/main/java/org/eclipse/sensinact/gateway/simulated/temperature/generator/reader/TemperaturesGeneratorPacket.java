/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.simulated.temperature.generator.reader;

import org.eclipse.sensinact.gateway.simulated.temperature.generator.internal.TemperaturesGeneratorAbstractPacket;

/**
 *
 */
public class TemperaturesGeneratorPacket extends TemperaturesGeneratorAbstractPacket {
    private final String serviceProviderId;
    private final double value;

    /**
     * @param serviceProviderId the id of the device
     * @param value             the value of the sensor
     */
    public TemperaturesGeneratorPacket(String serviceProviderId, double value) {
        this.serviceProviderId = serviceProviderId;
        this.value = value;
    }

    /**
     * @return the service provider ID
     */
    public String getServiceProvider() {
        return this.serviceProviderId;
    }

    /**
     * @return the value of the sensor
     */
    public double getValue() {
        return this.value;
    }
}

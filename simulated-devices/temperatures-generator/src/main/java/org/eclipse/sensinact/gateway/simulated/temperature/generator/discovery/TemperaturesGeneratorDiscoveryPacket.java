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
package org.eclipse.sensinact.gateway.simulated.temperature.generator.discovery;

import org.eclipse.sensinact.gateway.simulated.temperature.generator.internal.TemperaturesGeneratorAbstractPacket;

public class TemperaturesGeneratorDiscoveryPacket extends TemperaturesGeneratorAbstractPacket {
    private final String serviceProviderId;
    private final String location;
    private final double value;

    /**
     * @param serviceProviderId the id of the device
     * @param value             the value of the sensor
     */
    public TemperaturesGeneratorDiscoveryPacket(String serviceProviderId, String location, double value) {
        this.serviceProviderId = serviceProviderId;
        this.location = location;
        this.value = value;
    }

    /**
     * @return the service provider ID
     */
    public String getServiceProvider() {
        return this.serviceProviderId;
    }

    public String getLocation() {
        return this.location;
    }

    /**
     * @return the value of the sensor
     */
    public double getValue() {
        return this.value;
    }
}

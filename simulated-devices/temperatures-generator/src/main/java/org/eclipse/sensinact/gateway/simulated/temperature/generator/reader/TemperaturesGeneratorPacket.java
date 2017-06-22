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

/**
 * 
 */
public class TemperaturesGeneratorPacket extends TemperaturesGeneratorAbstractPacket {

	private final String serviceProviderId;
	private final double value;

	/**
	 * @param serviceProviderId the id of the device
	 * @param value the value of the sensor
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

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
package org.eclipse.sensinact.gateway.simulated.temperature.generator.parser;

import java.util.Arrays;

public class DeviceInfo {
    private static final String DEVICE_NAME = "temperature_station_";
    private String serviceProviderId;
    private String location;
    private double[] temperatures;
    private long sleepTime;
	private int floor;

    public DeviceInfo(String serviceProviderId, String location, int floor, double[] temperatures, long sleepTime) {
        this.serviceProviderId = DEVICE_NAME + serviceProviderId;
        this.location = location;
        this.temperatures = temperatures;
        this.sleepTime = sleepTime;
        this.floor = floor;
    }

    public String getServiceProviderId() {
        return serviceProviderId;
    }

    public String getLocation() {
        return location;
    }

    public double[] getTemperatures() {
        return temperatures;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    public int getFloor() {
        return floor;
    }

    public String toString() {
        return "DeviceInfo{" + "serviceProviderId='" + serviceProviderId + '\'' + ", location='" + location + '\'' + ", temperatures=" + Arrays.toString(temperatures) + ", sleepTime=" + sleepTime + '}';
    }
}

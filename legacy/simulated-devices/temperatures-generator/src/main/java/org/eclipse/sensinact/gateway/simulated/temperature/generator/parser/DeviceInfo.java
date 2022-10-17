/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.simulated.temperature.generator.parser;

import java.util.Arrays;

public class DeviceInfo {
    private static final String DEVICE_NAME = "temperature_station_";
    private String serviceProviderId;
    private String location;
    private double[] temperatures;
    private long sleepTime;

    public DeviceInfo(String serviceProviderId, String location, double[] temperatures, long sleepTime) {
        this.serviceProviderId = DEVICE_NAME + serviceProviderId;
        this.location = location;
        this.temperatures = temperatures;
        this.sleepTime = sleepTime;
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
    
    public String toString() {
        return "DeviceInfo{" + "serviceProviderId='" + serviceProviderId + '\'' + ", location='" + location + '\'' + ", temperatures=" + Arrays.toString(temperatures) + ", sleepTime=" + sleepTime + '}';
    }
}

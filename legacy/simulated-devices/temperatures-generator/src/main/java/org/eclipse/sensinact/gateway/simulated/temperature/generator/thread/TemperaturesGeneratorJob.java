/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.simulated.temperature.generator.thread;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.discovery.TemperaturesGeneratorDiscoveryPacket;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.parser.DeviceInfo;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.reader.TemperaturesGeneratorPacket;

public class TemperaturesGeneratorJob implements Runnable {
    private final LocalProtocolStackEndpoint<TemperaturesGeneratorPacket> connector;
    private final DeviceInfo deviceInfo;
	private final ScheduledExecutorService worker;
	
	volatile int index = 1;

    public TemperaturesGeneratorJob(LocalProtocolStackEndpoint<TemperaturesGeneratorPacket> connector, DeviceInfo deviceInfo, ScheduledExecutorService worker) {
        this.connector = connector;
        this.deviceInfo = deviceInfo;
		this.worker = worker;
        try {
            this.connector.process(new TemperaturesGeneratorDiscoveryPacket(deviceInfo.getServiceProviderId(), deviceInfo.getLocation(), deviceInfo.getTemperatures()[0]));
        } catch (InvalidPacketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        int index = this.index;
        try {
        	this.connector.process(new TemperaturesGeneratorPacket(deviceInfo.getServiceProviderId(), deviceInfo.getTemperatures()[index]));
        	this.index = (index + 1) % 12;
        } catch (InvalidPacketException e) {
        	e.printStackTrace();
        }
        worker.schedule(this, deviceInfo.getSleepTime(), TimeUnit.MILLISECONDS);
    }
}

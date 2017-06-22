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

package org.eclipse.sensinact.gateway.simulated.temperature.generator.thread;

import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.discovery.TemperaturesGeneratorDiscoveryPacket;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.internal.TemperaturesGeneratorAbstractPacket;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.parser.DeviceInfo;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.reader.TemperaturesGeneratorPacket;

public class TemperaturesGeneratorThread extends Thread implements Runnable {

    private LocalProtocolStackEndpoint<TemperaturesGeneratorAbstractPacket> connector;
    private DeviceInfo deviceInfo;

    public TemperaturesGeneratorThread(LocalProtocolStackEndpoint<TemperaturesGeneratorAbstractPacket> connector,
                                       DeviceInfo deviceInfo) {
        this.connector = connector;
        this.deviceInfo = deviceInfo;

        try {
            this.connector.process(new TemperaturesGeneratorDiscoveryPacket(deviceInfo.getServiceProviderId(),
                    deviceInfo.getLocation(), deviceInfo.getTemperatures()[0]));
        } catch (InvalidPacketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        int index = 1;

        while(true) {
            try {
                this.connector.process(new TemperaturesGeneratorPacket(deviceInfo.getServiceProviderId(),
                        deviceInfo.getTemperatures()[index]));
                index = (index + 1) % 12;
            } catch (InvalidPacketException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(deviceInfo.getSleepTime());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

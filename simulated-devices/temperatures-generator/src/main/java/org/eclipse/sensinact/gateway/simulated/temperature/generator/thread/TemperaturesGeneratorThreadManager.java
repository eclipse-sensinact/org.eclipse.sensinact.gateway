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
import org.eclipse.sensinact.gateway.simulated.temperature.generator.parser.DeviceInfo;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.reader.TemperaturesGeneratorPacket;

import java.util.HashSet;
import java.util.Set;

public class TemperaturesGeneratorThreadManager {
    private Set<TemperaturesGeneratorThread> threadList;

    public TemperaturesGeneratorThreadManager(LocalProtocolStackEndpoint<TemperaturesGeneratorPacket> connector, Set<DeviceInfo> deviceInfos) {
        this.threadList = new HashSet<TemperaturesGeneratorThread>();
        for (DeviceInfo deviceInfo : deviceInfos) {
            TemperaturesGeneratorThread thread = new TemperaturesGeneratorThread(connector, deviceInfo);
            this.threadList.add(thread);
        }
    }

    public void startThreads() {
        for (TemperaturesGeneratorThread thread : threadList) {
            thread.start();
        }
    }

    public void stopThreads() {
        for (TemperaturesGeneratorThread thread : threadList) {
            thread.interrupt();
        }
    }
}

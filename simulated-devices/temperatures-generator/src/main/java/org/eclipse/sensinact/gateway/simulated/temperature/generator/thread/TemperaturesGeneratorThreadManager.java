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
package org.eclipse.sensinact.gateway.simulated.temperature.generator.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.sensinact.gateway.generic.local.LocalProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.parser.DeviceInfo;
import org.eclipse.sensinact.gateway.simulated.temperature.generator.reader.TemperaturesGeneratorPacket;

public class TemperaturesGeneratorThreadManager {
    private final ScheduledExecutorService worker;
    
    Map<String, TemperaturesGeneratorJob> jobs = new HashMap<>();

    public TemperaturesGeneratorThreadManager(LocalProtocolStackEndpoint<TemperaturesGeneratorPacket> connector, Set<DeviceInfo> deviceInfos) {
        // Make a fixed pool of three threads
        AtomicInteger counter = new AtomicInteger();
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, r -> {
            Thread t = new Thread(r, "Temperature Generation Thread " + counter.incrementAndGet());
            return t;
        });
        executor.allowCoreThreadTimeOut(false);
        executor.setCorePoolSize(3);
        executor.setMaximumPoolSize(3);
        
        worker = executor;
        
        for (DeviceInfo deviceInfo : deviceInfos) {
            TemperaturesGeneratorJob job = new TemperaturesGeneratorJob(connector, deviceInfo, worker);
            this.jobs.put(deviceInfo.getServiceProviderId(), job);
        }
    }

    public void startThreads() {
        
        for (TemperaturesGeneratorJob job : jobs.values()) {
            worker.execute(job);
        }
    }

    public void stopThreads() {
        worker.shutdownNow();
        try {
            worker.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

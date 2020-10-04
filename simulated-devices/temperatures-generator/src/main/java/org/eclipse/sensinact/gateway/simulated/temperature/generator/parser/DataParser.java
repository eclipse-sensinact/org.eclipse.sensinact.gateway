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
package org.eclipse.sensinact.gateway.simulated.temperature.generator.parser;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * The parser randomly chooses line in the data.csv file using the reservoir sampling algorithm
 */
public class DataParser {
    private Mediator mediator;

    public DataParser(Mediator mediator) {
        this.mediator = mediator;
    }

    public Set<DeviceInfo> createDeviceInfosSet(int number) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        Set<DeviceInfo> deviceInfoSet = new HashSet<DeviceInfo>();
        try {
            for (int i = 0; i < number; i++) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(mediator.getContext().getBundle().getResource("data.csv").openStream()));
                DeviceInfo deviceInfo = choose(i, new BufferedReader(reader));
                if (!map.containsKey(deviceInfo.getServiceProviderId())) {
                    map.put(deviceInfo.getServiceProviderId(), 0);
                }
                map.put(deviceInfo.getServiceProviderId(), map.get(deviceInfo.getServiceProviderId()) + 1);
                deviceInfoSet.add(deviceInfo);
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return deviceInfoSet;
    }

    private static DeviceInfo choose(int nb, BufferedReader reader) throws IOException {
        DeviceInfo result = null;
        Random rand = new Random();
        Integer sleepTimeRandom = new Random().nextInt(60000) + 1000;
        String currentLine;
        int n = 0;
        while ((currentLine = reader.readLine()) != null) {
            n++;
            String[] splittedLine = currentLine.split(",");
            double[] temperatures = new double[12];
            for (int i = 0; i < 12; i++) {
                temperatures[i] = Double.parseDouble(splittedLine[i + 2]);
            }
            DeviceInfo deviceInfo = new DeviceInfo(String.valueOf(nb), splittedLine[1] + "," + splittedLine[2], temperatures, sleepTimeRandom);
            if (rand.nextInt(n) == 0) {
                result = deviceInfo;
            }
        }
        return result;
    }
}

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * The parser randomly chooses line in the data.csv file using the reservoir sampling algorithm
 */
public class DataParser {
    private Mediator mediator;

    public DataParser(Mediator mediator) {
        this.mediator = mediator;
    }

    public Set<DeviceInfo> createDeviceInfosSet(int number) {
        found = new HashSet<String>();
        Set<DeviceInfo> deviceInfoSet = new HashSet<DeviceInfo>();
        try {
            for (int i = 0; i < number; i++) {
                DeviceInfo deviceInfo = null;
                while(deviceInfo == null)
                	deviceInfo = choose(mediator, i);                
                deviceInfoSet.add(deviceInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        found.clear();
        found = null;
        return deviceInfoSet;
    }
    
    private static BufferedReader buildReader(Mediator mediator) throws IOException {
    	return new BufferedReader(new InputStreamReader(mediator.getContext().getBundle().getResource("data.csv").openStream()));
    }
    
    private static Set<String> found;
    
    private static DeviceInfo choose(Mediator mediator, int nb) throws IOException {
        DeviceInfo result = null;
        Random rand = new Random();
        BufferedReader reader = buildReader(mediator);
        Integer sleepTimeRandom = new Random().nextInt(10000) + 1000;
        String currentLine = null;
        int n = nb;
        while (true) {
        	currentLine = reader.readLine();
        	if(currentLine == null) {
        		reader.close();
        		reader = null;
        		reader = buildReader(mediator);
            	currentLine = reader.readLine();
            	n=nb;
        	}
            if (n==0 || rand.nextInt(n) != 0) {
            	n++;
            	continue;
            }
            if(found.contains(String.valueOf(n-nb))) {
            	n++;
            	continue;
            }
            found.add(String.valueOf(n-nb));
            String[] splittedLine = currentLine.split(",");
            double[] temperatures = new double[12];
            for (int i = 0; i < 12; i++) {
                temperatures[i] = Double.parseDouble(splittedLine[i + 2]);
            }
            DeviceInfo deviceInfo = new DeviceInfo(String.valueOf(nb), splittedLine[1] + ":" + splittedLine[2], temperatures, sleepTimeRandom);
            result = deviceInfo;
            break;
        }
		reader.close();
		reader = null;
        return result;
    }
}

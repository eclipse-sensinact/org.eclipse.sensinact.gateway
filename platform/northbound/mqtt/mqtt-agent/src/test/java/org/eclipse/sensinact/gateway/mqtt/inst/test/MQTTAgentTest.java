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
package org.eclipse.sensinact.gateway.mqtt.inst.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.sensinact.gateway.util.IOUtils;
import org.json.JSONObject;
import org.junit.Test;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class MQTTAgentTest {

    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
    private static final int INSTANCES_COUNT = 4;

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//
    private final List<MidOSGiTestExtended> instances = new ArrayList<MidOSGiTestExtended>();
    private final Set<String> events = new HashSet<String>();
    
    /**
     * @throws Exception
     */
    @Test
    public void mqttAgentsTest() throws Throwable {
        events.add("[/slider/cursor/position][1]0");
        events.add("[/slider/cursor/position][2]0");
        events.add("[/slider/cursor/position][3]0");
        events.add("[/slider/cursor/position][1]45");
        events.add("[/slider/cursor/position][2]45");
        events.add("[/slider/cursor/position][3]45");
        
        for (int n = 1; n <= INSTANCES_COUNT; n++) {
        	MidOSGiTestExtended t = null;
        	if(n == INSTANCES_COUNT) {
                t = new MidOSGiTestExtended(n);
                for (int j = n-1; j > 0; j--) {
                    FileInputStream input = new FileInputStream(new File(String.format("src/test/resources/mqtt.agent.broker-ag%s.cfg", j)));
                    byte[] content = IOUtils.read(input);
                    byte[] contentPlus = new byte[content.length + 1];

                    System.arraycopy(content, 0, contentPlus, 0, content.length);
                    contentPlus[content.length] = '\n';

                    FileOutputStream output = new FileOutputStream(new File(String.format("target/felix/conf%s/mqtt.agent.broker-ag%s.cfg", n, j)));
                    IOUtils.write(contentPlus, output);
                    output.close();
                }
        	} else {
        		t = new MidOSGiTestExtendedMQTTBroker(n);
	            FileInputStream input = new FileInputStream(new File(String.format("src/test/resources/mqtt.server.%s.config", n)));
	            byte[] content = IOUtils.read(input);
	            byte[] contentPlus = new byte[content.length + 1];
	
	            System.arraycopy(content, 0, contentPlus, 0, content.length);
	            contentPlus[content.length] = '\n';
	
	            FileOutputStream output = new FileOutputStream(new File(String.format("target/felix/conf%s/mqtt.server.cfg", n)));
	            IOUtils.write(contentPlus, output);
	            output.close();
        	}
	        instances.add(t);
            t.init();
            Thread.sleep(2 * 1000);
        }
        
        Thread.sleep(10 * 1000);

        for (int n = 1; n < INSTANCES_COUNT; n++) {
        	final int C = n; 
        	
	        MqttClient client = null;
	        if (client!=null && client.isConnected()) {
	            client.disconnect();
	        }
	        
	    	client = null;
	        final String brokerUrl = String.format("%s://%s:%d", "tcp", "127.0.0.1", (1883+n));
	        client = new MqttClient(brokerUrl, UUID.randomUUID().toString(), new MemoryPersistence());        
	        client.setCallback(new MqttCallback() {

				@Override
				public void connectionLost(Throwable cause) {
					cause.printStackTrace();					
				}

				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					String m = ("["+topic+"]["+C+"]" + message);
					events.remove(m);
					System.out.println(m);
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {}
	        });
	        client.connect();
	        client.subscribe("/slider/cursor/position");
        }
        instances.get(3).moveSlider(0);
        String s = instances.get(3).get("slider", "cursor", "position");
        JSONObject j = new JSONObject(s);

        assertEquals(0, j.getJSONObject("response").getInt("value"));
        Thread.sleep(5000);
        
        instances.get(3).moveSlider(45);
        s = instances.get(3).get("slider", "cursor", "position");
        j = new JSONObject(s);

        assertEquals(45, j.getJSONObject("response").getInt("value"));
        Thread.sleep(5000);
        assertEquals(0, events.size());
    }
}

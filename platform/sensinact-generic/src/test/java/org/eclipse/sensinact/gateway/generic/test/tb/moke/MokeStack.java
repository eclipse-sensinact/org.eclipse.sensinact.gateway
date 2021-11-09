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
package org.eclipse.sensinact.gateway.generic.test.tb.moke;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.core.method.AccessMethod;
import org.eclipse.sensinact.gateway.generic.Connector;
import org.eclipse.sensinact.gateway.generic.ExtResourceConfig;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.TaskManager;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.uri.URIProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.uri.URITaskTranslator;

public class MokeStack extends URIProtocolStackEndpoint<MokePacket> {
    public MokeStack() {
        super();
    }

    @Override
    public Task createTask(Task.CommandType command, String path, String profileId, ResourceConfig resourceConfig, Object[] parameters) {
        return new MokeTask(command, (URITaskTranslator) this, path, profileId, (ExtResourceConfig) resourceConfig, parameters);
    }

    public Connector<MokePacket> getConnector() {
        return super.connector;
    }

    @Override
    public void send(String processorIdentifier, String path, Object content, Map<String, List<String>> options) {
//		System.out.println("####################################");
//		System.out.println("SENDING : " + processorIdentifier 
//				+ "[uri: "+path+"]" 
//				+ "[content: {"+ content +"}]"
//				+ options);
//		System.out.println("####################################");

        final MokePacket packet;
        String taskId = (String) options.remove("taskId").get(0);

        if (taskId.equals("device1#SERVICES_ENUMERATION")) {
            packet = new MokePacket("device1", taskId, new String[]{"pir", "ldr", "gpr"});

        } else if (taskId.equals("weather_5#SERVICES_ENUMERATION") || taskId.equals("weather_6#SERVICES_ENUMERATION") || taskId.equals("weather_7#SERVICES_ENUMERATION") || taskId.equals("weather_8#SERVICES_ENUMERATION")) {
            String sp = taskId.split("#")[0];
            packet = new MokePacket(sp, taskId, new String[]{"weather"});

        } else if (taskId.equals("hydrometers_4#SERVICES_ENUMERATION")) {
            packet = new MokePacket("hydrometers_4", taskId, new String[]{"weather"});

        } else {
            if (!taskId.endsWith("SERVICES_ENUMERATION")) {
                String[] taskIdElements = taskId.split(new String(new char[]{TaskManager.IDENTIFIER_SEP_CHAR}));
                String service = taskIdElements[taskIdElements.length - 2];
                String resource = taskIdElements[taskIdElements.length - 1];

                packet = new MokePacket(processorIdentifier, taskId, service, resource, AccessMethod.EMPTY);
            } else {
                packet = new MokePacket(processorIdentifier, taskId, new String[]{});
            }
        }
//		System.out.println("####################################");
//		System.out.println("RECIVING : " + packet);
//		System.out.println("####################################");

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (packet != null) {
                    try {
                        MokeStack.this.connector.process(packet);

                    } catch (InvalidPacketException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}

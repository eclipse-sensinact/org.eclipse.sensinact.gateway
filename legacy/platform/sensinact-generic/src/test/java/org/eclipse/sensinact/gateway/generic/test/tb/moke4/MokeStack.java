/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.generic.test.tb.moke4;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.ResourceConfig;
import org.eclipse.sensinact.gateway.generic.Connector;
import org.eclipse.sensinact.gateway.generic.ExtResourceConfig;
import org.eclipse.sensinact.gateway.generic.Task;
import org.eclipse.sensinact.gateway.generic.Task.CommandType;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.uri.URIProtocolStackEndpoint;
import org.eclipse.sensinact.gateway.generic.uri.URITaskTranslator;

public class MokeStack extends URIProtocolStackEndpoint<MokePacket> {
    public MokeStack(Mediator mediator) {
        super();
    }

    @Override
    public Task createTask(CommandType command, String path, String profileId, ResourceConfig resourceConfig, Object[] parameters) {
        return new MokeTask(command, (URITaskTranslator) this, path, profileId, (ExtResourceConfig) resourceConfig, parameters);
    }

    public Connector<MokePacket> getConnector() {
        return super.connector;
    }

    @Override
    public void send(String processorIdentifier, String path, Object content, Map<String, List<String>> options) {
    	
        final MokePacket packet;
        String taskId = (String) options.remove("taskId").get(0);
        packet = new MokePacket(processorIdentifier, taskId, new String[]{});

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (packet != null) {
                    try {
                        MokeStack.this.getConnector().process(packet);

                    } catch (InvalidPacketException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}

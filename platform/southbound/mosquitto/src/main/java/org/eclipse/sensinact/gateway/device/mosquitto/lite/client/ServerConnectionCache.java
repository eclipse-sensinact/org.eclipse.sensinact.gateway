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
package org.eclipse.sensinact.gateway.device.mosquitto.lite.client;

import java.util.HashMap;
import java.util.Map;

public class ServerConnectionCache {

    private static Map<String,MQTTClient> instances=new HashMap<String,MQTTClient>();

    private ServerConnectionCache(){

    }

    private static String convertToKey(String host,Long port){
        return String.format("%s:%s",host,port.toString());
    }

    public static MQTTClient getInstance(String server, Long port){

        if(!instances.containsKey(convertToKey(server,port))){
            //Create new instance of the server here
            MQTTClient client=MQTTConnectionFactory.create(server, port);
            instances.put(convertToKey(server,port),client);
        }

        return instances.get(convertToKey(server, port));

    }

}

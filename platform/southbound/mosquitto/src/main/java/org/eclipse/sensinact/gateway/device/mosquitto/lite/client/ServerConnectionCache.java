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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Pool of connection for the MQTT broker
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class ServerConnectionCache {

    private static Map<String,MQTTClient> instances=new HashMap<String,MQTTClient>();
    private static final Logger LOG = LoggerFactory.getLogger(ServerConnectionCache.class);

    private ServerConnectionCache(){

    }

    private static String convertToKey(String host,Long port){
        return String.format("%s:%s",host,port.toString());
    }

    public static MQTTClient getInstance(String id,String server, Long port){

        if(!instances.containsKey(id)){
            //Create new instance of the server here
            MQTTClient client=MQTTConnectionFactory.create(server, port);
            instances.put(id,client);
        }

        return instances.get(id);

    }

    public static Boolean hasInstance(String id){
        return instances.containsKey(id);
    }

    public static void disconnectInstance(String id){
        LOG.info("Trying to disconnect MQTT server instance {}",id);
        if(instances.containsKey(id)){
            //Create new instance of the server here
            LOG.info("MQTT server instance {} found, disconnecting..",id);
            MQTTClient client=instances.remove(id);
            if(client!=null)
            try {
                client.getConnection().disconnect();
                LOG.info("MQTT server instance {} disconnected.",id);
            } catch (Exception e) {
                LOG.error("MQTT server instance {} failed to disconnect.",id);
            }
            instances.put(id,client);
        }else {
            LOG.info("MQTT server instance {} does not exist",id);
        }
    }

}

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

public class MQTTConnectionFactory {

    public static MQTTClient create(String host, Long port){

        String broker=String.format("tcp://%s:%d",host,port);
        MQTTClient client=new MQTTClient(broker);//"java client"
        client.setBroker(broker);
        return client;

    }


}

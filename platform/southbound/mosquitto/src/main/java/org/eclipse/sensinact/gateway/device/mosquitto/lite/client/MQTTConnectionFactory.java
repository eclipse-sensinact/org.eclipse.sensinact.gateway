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

import org.eclipse.sensinact.gateway.device.mosquitto.lite.model.mqtt.MQTTBroker;

/**
 * Generate the connection string necessary for the MQTT broker
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class MQTTConnectionFactory {

    public static MQTTClient create(MQTTBroker broker){

        MQTTClient client=new MQTTClient(broker);//"java client"

        return client;

    }

}

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
package org.eclipse.sensinact.gateway.device.mosquitto.lite.device;

import org.eclipse.sensinact.gateway.device.mosquitto.lite.device.exception.MQTTConnectionException;

/**
 * Interface that abstracts echonet lamps
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Nascimento</a>
 */
public interface MQTTBusClient {

    String getHost();
    Long getPort();
    String getId();
    String getTopic();
    void act(String command, Object... parameters);
    void connect() throws MQTTConnectionException;
    void disconnect() throws MQTTConnectionException;

}

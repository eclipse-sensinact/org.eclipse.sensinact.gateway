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
package org.sensinact.mqtt.server;

/**
 * MQTT Server service
 */
public interface MQTTServerService {
    /**
     * Start MQTT service in assigned host and port
     *
     * @param host
     * @param port
     * @return connection id that allows to control this service disconnection
     * @throws MQTTException
     */
    String startService(String host, String port) throws MQTTException;

    /**
     * Start MQTT service in assigned port with default 127.0.0.1 host
     *
     * @param port
     * @return connection id that allows to control this service disconnection
     * @throws MQTTException
     */
    String startService(String port) throws MQTTException;

    /**
     * Start MQTT service in assigned port with default 127.0.0.1 host and 1883 port
     *
     * @return connection id that allows to control this service disconnection
     * @throws MQTTException
     */
    String startService() throws MQTTException;

    /**
     * Stop MQTT service with the connection id specified.
     *
     * @param id
     * @throws MQTTException
     */
    void stopService(String id) throws MQTTException;

    /**
     * Check that the MQTT service is active with indicated id
     *
     * @param id
     * @return
     * @throws MQTTException
     */
    Boolean activeService(String id) throws MQTTException;

    /**
     * Stop all MQTT service managed by this OSGi service.
     *
     * @throws MQTTException
     */
    void stopServer() throws MQTTException;
}

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

import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.MQTTClient;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.ServerConnectionCache;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.device.exception.MQTTConnectionException;
import org.apache.felix.ipojo.annotations.*;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "MQTTBusClient")
@Provides
/**
 * EchoNet Lamp class
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Nascimento</a>
 */
public class MQTTBusClientImpl implements MQTTBusClient {

    private static final Logger LOG = LoggerFactory.getLogger(MQTTBusClientImpl.class);

    private MQTTClient client;

    @Property
    String id;

    @Property(value="127.0.0.1",mandatory = false)
    String host;

    @Property(value="1883")
    Long port;

    @Property(mandatory = true)
    String topic;

    @Validate
    public void validate(){
        LOG.info("Instantiating Mosquitto device with the host {}:{}",new Object[]{host,port});
    }

    @Invalidate
    public void invalidate(){
        LOG.info("Instantiating Mosquitto device with the host {}:{}", new Object[]{host, port});
    }

    public String getHost() {
        return host;
    }

    public Long getPort() {
        return port;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Long port) {
        this.port = port;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public void act(String command, Object... parameters) {
        /**
         * This bridge will not invoke action on the backend service
         */
    }

    @Override
    public void connect() throws MQTTConnectionException {
        client=ServerConnectionCache.getInstance(host, port);
        client.connect();
    }

    public void disconnect() throws MQTTConnectionException{
        try {
            client.getConnection().disconnect();
        } catch (MqttException e) {
            LOG.error("Failed to disconnect {}",id,e);
        }
    }
}

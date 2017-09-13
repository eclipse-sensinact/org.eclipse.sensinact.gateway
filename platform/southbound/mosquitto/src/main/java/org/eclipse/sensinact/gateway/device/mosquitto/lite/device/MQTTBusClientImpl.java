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
import org.apache.felix.ipojo.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component(name = "mosquitto",propagation = false)
@Provides
/**
 * Implementation of the interface that will generate Sesinact device based on a file descriptor put into FileInstall directory.
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
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

    @Property(name = "processor",mandatory = false)
    String payloadFormat;

    @Property(name = "location.latitude",mandatory = false)
    Float latitude;

    @Property(name = "location.longitude",mandatory = false)
    Float longitude;

    @Property(name = "discovery.firstMessage",value = "false")
    Boolean discoveryOnFirstMessage;

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

    public String getProcessor() {
        return payloadFormat;
    }

    public void setPayloadFormat(String payloadFormat) {
        this.payloadFormat = payloadFormat;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public Boolean getDiscoveryOnFirstMessage() {
        return discoveryOnFirstMessage;
    }

    public void setDiscoveryOnFirstMessage(Boolean discoveryOnFirstMessage) {
        this.discoveryOnFirstMessage = discoveryOnFirstMessage;
    }
}

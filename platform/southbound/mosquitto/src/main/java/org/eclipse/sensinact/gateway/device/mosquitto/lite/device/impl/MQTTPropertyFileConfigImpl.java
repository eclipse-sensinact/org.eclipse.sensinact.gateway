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
package org.eclipse.sensinact.gateway.device.mosquitto.lite.device.impl;

import org.eclipse.sensinact.gateway.common.annotation.Property;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.client.MQTTClient;
import org.eclipse.sensinact.gateway.device.mosquitto.lite.device.MQTTPropertyFileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the interface that will generate Sesinact device based on a file descriptor put into FileInstall directory.
 * @author <a href="mailto:Jander.BOTELHODONASCIMENTO@cea.fr">Jander Botelho do Nascimento</a>
 */
public class MQTTPropertyFileConfigImpl implements MQTTPropertyFileConfig {

    private static final Logger LOG = LoggerFactory.getLogger(MQTTPropertyFileConfigImpl.class);

    private MQTTClient client;

    @Property
    String id;

    @Property(defaultValue = "127.0.0.1")
    String host;

    @Property(defaultValue = "1883")
    Long port;

    @Property
    String topic;

    @Property(defaultValue = "tcp")
    String protocol;

    @Property(name = "processor",mandatory = false)
    String payloadFormat;

    @Property(name = "auth.username",mandatory = false)
    String username;

    @Property(name = "auth.password",mandatory = false)
    String password;

    @Property(name = "location.latitude",mandatory = false)
    Float latitude;

    @Property(name = "location.longitude",mandatory = false)
    Float longitude;

    @Property(name = "discovery.firstMessage",defaultValue = "false")
    Boolean discoveryOnFirstMessage;

    @Property(name = "topic.type",defaultValue = "mqtt")
    String topicType;

    public String getHost() {
        return host;
    }

    public Long getPort() {
        return port;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getTopicType() {
        return topicType;
    }

    public String getTopic() {
        return topic;
    }

    public String getProcessor() {
        return payloadFormat;
    }

    public Float getLatitude() {
        return latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public Boolean getDiscoveryOnFirstMessage() {
        return discoveryOnFirstMessage;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public String getProtocol(){
        return protocol;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void act(String command, Object... parameters) {
        /**
         * This bridge will not invoke action on the backend service
         */
    }

    @Override
    public String toString() {
        return "MQTTPropertyFileConfigImpl{" +
                "client=" + client +
                ", id='" + id + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", topic='" + topic + '\'' +
                ", protocol='" + protocol + '\'' +
                ", payloadFormat='" + payloadFormat + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", discoveryOnFirstMessage=" + discoveryOnFirstMessage +
                ", topicType='" + topicType + '\'' +
                '}';
    }
}

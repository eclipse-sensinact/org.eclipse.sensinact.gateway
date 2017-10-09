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
package org.eclipse.sensinact.gateway.device.mosquitto.lite.model.mqtt;

public class MQTTBroker {

    public enum PROTOCOL {
        tcp,
        ssl
    };

    private String host;
    private Long port;
    private PROTOCOL protocol=PROTOCOL.tcp;
    private MQTTSession session=new MQTTSession();
    private MQTTAuth auth=new MQTTAuth();

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Long getPort() {
        return port;
    }

    public void setPort(Long port) {
        this.port = port;
    }

    public PROTOCOL getProtocol() {
        return protocol;
    }

    public void setProtocol(PROTOCOL protocol) {
        this.protocol = protocol;
    }

    public MQTTSession getSession() {
        return session;
    }

    public void setSession(MQTTSession session) {
        this.session = session;
    }

    public MQTTAuth getAuth() {
        return auth;
    }

    public void setAuth(MQTTAuth auth) {
        this.auth = auth;
    }
}

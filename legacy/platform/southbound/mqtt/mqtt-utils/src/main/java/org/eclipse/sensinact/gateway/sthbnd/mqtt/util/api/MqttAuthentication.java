/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.mqtt.util.api;

import java.util.Properties;

public class MqttAuthentication {
    private String username;
    private String password;
    private String certificate;
    private Properties sslProperties;

    public MqttAuthentication(MqttAuthentication.Builder builder) {
        this.username = builder.username;
        this.password = builder.password;
        this.certificate = builder.certificate;
        this.sslProperties = builder.sslProperties;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCertificate() {
        return certificate;
    }

    public Properties getSslProperties() {
        return sslProperties;
    }

    /**
     * The builder abstraction.
     */
    public static class Builder {
        private String username = null;
        private String password = null;
        private String certificate = null;
        private Properties sslProperties = new Properties();

        public MqttAuthentication.Builder username(String username) {
            this.username = username;
            return this;
        }

        public MqttAuthentication.Builder password(String password) {
            this.password = password;
            return this;
        }

        public MqttAuthentication.Builder cleanSession(String certificate) {
            this.certificate = certificate;
            return this;
        }

        public MqttAuthentication.Builder sslProperties(Properties sslProperties) {
            this.sslProperties = sslProperties;
            return this;
        }

        public MqttAuthentication build() {
            return new MqttAuthentication(this);
        }
    }
}

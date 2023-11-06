/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.mqtt.impl;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

/**
 * MQTT client configuration
 */
public @interface MqttClientConfiguration {

    /**
     * Internal ID
     */
    String id();

    /**
     * MQTT connection protocol: TCP or SSL
     */
    String protocol() default "tcp";

    /**
     * MQTT broker host
     */
    String host();

    /**
     * MQTT broker port
     */
    int port() default 1883;

    /**
     * Topics to subscribe to
     */
    String[] topics() default "";

    /**
     * MQTT client ID
     */
    String client_id();

    /**
     * Delay before trying to reconnect (in milliseconds)
     */
    int client_reconnect_delay() default 500;

    /**
     * MQTT connection timeout
     */
    int client_connection_timeout() default MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT;

    /**
     * MQTT authentication user
     */
    String user();

    /**
     * MQTT authentication password
     */
    String _password();

    /**
     * Kind of key store
     */
    String auth_keystore_type() default "PKCS12";

    /**
     * Key store path
     */
    String auth_keystore_path();

    /**
     * Key store password
     */
    String _auth_keystore_password();

    /**
     * Trust store path
     */
    String auth_truststore_path();

    /**
     * Trust store password
     */
    String _auth_truststore_password();
}

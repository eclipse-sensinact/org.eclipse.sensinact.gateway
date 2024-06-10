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
     * WebSocket MQTT path
     */
    String path() default "/";

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
     * Path to the client authentication PEM certificate (public key)
     */
    String auth_clientcert_path();

    /**
     * Path to the client private key
     */
    String auth_clientcert_key();

    /**
     * Explicit algorithm of the client private key
     */
    String auth_clientcert_key_algorithm();

    /**
     * Password of the client private key
     */
    String _auth_clientcert_key_password();

    /**
     * Kind of key store for the trust store
     */
    String auth_truststore_type() default "PKCS12";

    /**
     * Trust store path
     */
    String auth_truststore_path();

    /**
     * Trust store password
     */
    String _auth_truststore_password();

    /**
     * Path to the PEM file of the Certificate Authority that signed the client (if
     * not included in client)
     */
    String auth_clientcert_ca_path();

    /**
     * Flag to merge the given trust store with the default one
     */
    boolean auth_truststore_default_merge() default true;

    /**
     * Paths to trusted PEM certificates
     */
    String[] auth_trusted_certs();

    /**
     * Allow expired certificates
     */
    boolean auth_allow_expired() default false;
}

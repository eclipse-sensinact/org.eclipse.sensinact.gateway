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

/**
 * MQTT client configuration
 */
public @interface MqttClientConfiguration {

    /**
     * Internal ID
     */
    String id();

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
}

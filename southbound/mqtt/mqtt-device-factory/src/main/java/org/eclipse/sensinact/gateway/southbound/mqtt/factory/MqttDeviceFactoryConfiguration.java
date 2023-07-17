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
package org.eclipse.sensinact.gateway.southbound.mqtt.factory;

/**
 * Configuration of an MQTT device factory
 */
public @interface MqttDeviceFactoryConfiguration {

    /**
     * ID of the MQTT client to accept data from (null for any)
     */
    String mqtt_handler_id();

    /**
     * Allowed topics (null to accept all from the handler)
     */
    String[] mqtt_topics();

    /**
     * Device mapping configuration
     */
    String mapping();
}

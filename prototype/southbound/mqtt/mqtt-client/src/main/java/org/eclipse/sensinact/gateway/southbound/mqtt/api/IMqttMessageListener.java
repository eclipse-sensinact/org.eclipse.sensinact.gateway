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
package org.eclipse.sensinact.gateway.southbound.mqtt.api;

/**
 * SensiNact MQTT message handler
 */
public interface IMqttMessageListener {

    /**
     * Service property to define topics filters (<code>String[]</code>)
     */
    String MQTT_TOPICS_FILTERS = "sensinact.mqtt.topics.filters";

    /**
     * Notification of a new message
     *
     * @param handlerId ID of the MQTT handler that received the message
     * @param topic     Message topic
     * @param message   Message content
     */
    void onMqttMessage(String handlerId, String topic, IMqttMessage message);
}

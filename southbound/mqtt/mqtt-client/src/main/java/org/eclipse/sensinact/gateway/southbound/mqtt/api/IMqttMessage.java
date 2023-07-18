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
 * Represents a received message
 */
public interface IMqttMessage {

    /**
     * Returns the ID of the MQTT handler that received the message
     */
    String getHandlerId();

    /**
     * Returns the topic of the message
     */
    String getTopic();

    /**
     * Returns a copy of the payload of the message
     */
    byte[] getPayload();

    /**
     * Returns the quality of service for the message
     */
    int getQos();

    /**
     * Returns true if the message was retained by the server
     */
    boolean isRetained();
}

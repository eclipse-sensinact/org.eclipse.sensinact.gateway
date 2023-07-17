/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.gateway.northbount.sensorthings.mqtt;

/**
 * Error thrown when ID couldn't be parsed
 */
public class InvalidIdException extends SensorThingsMqttException {

    private static final long serialVersionUID = 1L;

    public InvalidIdException(final String id) {
        super("Invalid ID: '" + id + "'");
    }

    public InvalidIdException(final String id, final String reason) {
        super("Invalid ID '" + id + "': " + reason);
    }
}

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
package org.eclipse.sensinact.gateway.southbound.device.factory;

/**
 * Parent exception for the device factory
 */
public abstract class DeviceFactoryException extends Exception {

    private static final long serialVersionUID = 1L;

    public DeviceFactoryException(String message) {
        super(message);
    }

    public DeviceFactoryException(String message, Throwable cause) {
        super(message, cause);
    }
}

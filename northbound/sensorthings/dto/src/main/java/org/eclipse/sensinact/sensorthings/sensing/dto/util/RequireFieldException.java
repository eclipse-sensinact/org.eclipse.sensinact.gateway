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
package org.eclipse.sensinact.sensorthings.sensing.dto.util;

/**
 * Thrown when a required field is missing or null during DTO validation.
 */
public class RequireFieldException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RequireFieldException(String message) {
        super(message);
    }

    public RequireFieldException(String message, Throwable cause) {
        super(message, cause);
    }
}

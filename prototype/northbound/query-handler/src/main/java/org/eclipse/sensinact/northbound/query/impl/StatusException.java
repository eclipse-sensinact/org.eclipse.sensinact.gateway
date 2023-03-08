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
package org.eclipse.sensinact.northbound.query.impl;

import org.eclipse.sensinact.northbound.query.dto.result.ErrorResultDTO;

/**
 * Exception that can be converted easily to an {@link ErrorResultDTO}
 */
public class StatusException extends Exception {

    private static final long serialVersionUID = 1L;

    public final int statusCode;

    /**
     * Sets up the exception
     *
     * @param statusCode Status code associated to the error
     * @param message
     */
    public StatusException(int statusCode, final String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * Converts the exception to an error result
     */
    public ErrorResultDTO toErrorResult() {
        return new ErrorResultDTO(this.statusCode, getMessage());
    }
}

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
package org.eclipse.sensinact.northbound.query.dto.result;

import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;
import org.eclipse.sensinact.northbound.query.api.EResultType;

/**
 * An error DTO
 */
public class ErrorResultDTO extends AbstractResultDTO {

    public ErrorResultDTO() {
        super(EResultType.ERROR);
    }

    /**
     * @param error Throwable to generate the error from
     */
    public ErrorResultDTO(final Throwable error) {
        this();
        this.statusCode = 500;
        this.error = error != null ? error.getMessage() : "n/a";
    }

    /**
     * @param statusCode Error status code (should be in 4xx or 5xx)
     * @param message    Error description
     */
    public ErrorResultDTO(final int statusCode, final String message) {
        this();
        this.statusCode = statusCode;
        this.error = message;
    }
}

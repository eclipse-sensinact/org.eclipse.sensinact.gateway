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
package org.eclipse.sensinact.northbound.query.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents a Northbound query response
 */
public abstract class AbstractResultDTO {

    /**
     * Targeted URI as used
     */
    public String uri;

    /**
     * Placeholder for the user-given request ID
     */
    @JsonInclude(Include.NON_NULL)
    public String requestId;

    /**
     * Result code
     */
    public int statusCode;

    /**
     * Error message
     */
    @JsonInclude(Include.NON_NULL)
    public String error;

    /**
     * Result content type
     */
    public EResultType type;

    /**
     * Sets up the result type
     *
     * @param type Result type
     */
    public AbstractResultDTO(final EResultType type) {
        this.type = type;
    }
}

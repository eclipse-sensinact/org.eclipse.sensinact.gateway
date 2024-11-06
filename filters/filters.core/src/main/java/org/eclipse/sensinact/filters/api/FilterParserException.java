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
package org.eclipse.sensinact.filters.api;

/**
 * Error parsing a filter
 */
public class FilterParserException extends FilterException {

    private static final long serialVersionUID = 1L;

    public FilterParserException(final String message) {
        super(message);
    }

    public FilterParserException(final String message, final Throwable cause) {
        super(message, cause);
    }
}

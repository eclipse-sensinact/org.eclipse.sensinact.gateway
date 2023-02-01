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
package org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl;

import java.util.Arrays;

/**
 * @author thoma
 *
 */
public class InvalidResultTypeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidResultTypeException(final String message) {
        super(message);
    }

    public InvalidResultTypeException(final String message, final String expected, final Object... values) {
        this(String.format("%s. Expected %s got: %s (%s)", message, expected,
                Arrays.stream(values).map(v -> String.format("%s (%s)", v, v != null ? v.getClass().getName() : "null"))
                        .reduce((a, b) -> String.format("%s, %s", a, b))));
    }
}

/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.gateway.southbound.device.factory.impl;

import org.eclipse.sensinact.gateway.southbound.device.factory.ParserException;

/**
 * A record rejected by the policy of one of its values
 */
public class RejectedRecordException extends ParserException {

    private static final long serialVersionUID = 1L;

    private final ErrorReporter.Type type;

    public RejectedRecordException(final ErrorReporter.Type type, final String message) {
        super(message);
        this.type = type;
    }

    public RejectedRecordException(final ErrorReporter.Type type, final String message, final Throwable cause) {
        super(message, cause);
        this.type = type;
    }

    /**
     * Returns the type of error to report
     */
    public ErrorReporter.Type getType() {
        return type;
    }
}

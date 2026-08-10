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
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.history.provider;

/**
 * The requested operation needs a {@link HistoryCapability} this provider
 * does not have.
 */
public class UnsupportedHistoryOperationException extends HistoryQueryException {

    private static final long serialVersionUID = 1L;

    public UnsupportedHistoryOperationException(HistoryCapability missing) {
        super("The history provider does not support " + missing);
    }
}

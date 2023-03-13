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
*   Data In Motion - initial API and implementation
**********************************************************************/
package org.eclipse.sensinact.prototype.model.nexus.emf.change;

import java.util.UUID;

/**
 * The EMF Changeadapter decouples the model changes and makes most actions
 * atomic. Sometimes however we need to note if a values has already changed
 * during an operation. This is used to e.g. avoid implicitly overwriting a
 * timestamp. As we only work with a single Thread, this is not meant to be
 * Threadsafe.
 *
 * @author Juergen Albert
 * @since 13 Mar 2023
 */
public class Transaction implements AutoCloseable {

    private static UUID currentTransaction = null;;

    private static Transaction INSTANCE = new Transaction();

    public static Transaction startTransaction() {
        if (currentTransaction == null) {
            currentTransaction = UUID.randomUUID();
        }
        return INSTANCE;
    }

    private static void stopTransaction() {
        currentTransaction = null;
    }

    /**
     * Returns the currentTransaction.
     *
     * @return the currentTransaction ID or <code>null</code> if no transaction is
     *         running
     */
    public static UUID getCurrentTransaction() {
        return currentTransaction;
    }

    @Override
    public void close() {
        Transaction.stopTransaction();
    }

}

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
package org.eclipse.sensinact.gateway.southbound.history.core.contract;

import org.eclipse.sensinact.gateway.southbound.history.inmemory.InMemoryHistoryStorage;
import org.eclipse.sensinact.gateway.southbound.history.storage.HistoryStorage;

class InMemoryHistoryStorageContractTest extends HistoryStorageContractTest {

    @Override
    protected HistoryStorage createStorage() {
        return new InMemoryHistoryStorage(10_000);
    }
}

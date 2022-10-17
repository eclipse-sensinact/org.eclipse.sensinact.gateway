/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.datastore.api;

import org.eclipse.sensinact.gateway.common.execution.Executable;

/**
 * {@link DataStoreService} statement executor service
 *
 * @param <T> the type returned by this StatementExecutor
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public interface StatementExecutor<T> extends Executable<DataStoreService, T> {
    T execute(DataStoreService service) throws DataStoreException;
}

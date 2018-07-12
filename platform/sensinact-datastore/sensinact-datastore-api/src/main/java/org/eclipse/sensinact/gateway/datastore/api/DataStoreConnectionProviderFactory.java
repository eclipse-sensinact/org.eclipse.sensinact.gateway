/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.datastore.api;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;

/**
 * Factory of {@link DataStoreConnectionProvider}
 */
public interface DataStoreConnectionProviderFactory<C> {
    /**
     * Creates and returns a new  instance of {@link
     * DataStoreConnectionProvider}
     *
     * @param mediator
     * @param dbName
     * @return
     * @throws UnableToFindDataStoreException
     * @throws UnableToConnectToDataStoreException
     */
    DataStoreConnectionProvider<C> newInstance(Mediator mediator, String dbName) throws UnableToFindDataStoreException, UnableToConnectToDataStoreException;

    /**
     * Returns the <code>&lt;C&gt;</code> type of the connection
     * provided by the {@link DataStoreConnectionProvider} returned
     * by this factory
     *
     * @return the connection type of the built {@link
     * DataStoreConnectionProvider}
     */
    Class<C> getConnectionType();
}

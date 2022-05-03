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

/**
 * Service providing a connection to a data store
 */
public interface DataStoreConnectionProvider<C> {
    /**
     * Return a connection object to a data store or null
     * if an error occurred
     *
     * @return a connection object to a data store
     * @throws UnableToConnectToDataStoreException
     */
    C openConnection() throws UnableToConnectToDataStoreException;

    /**
     * Close the previously opened connection
     */
    void closeConnection();

    /**
     * Return the data store's name
     *
     * @return the data store's name
     */
    String getDataStoreName();
}

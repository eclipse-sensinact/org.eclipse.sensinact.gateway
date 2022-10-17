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

import jakarta.json.JsonArray;

/**
 * C(reate)R(ead)U(update)D(delete) Service signature
 */
public interface DataStoreService {
		
    /**
     * Execute the update define by the query passed as parameter
     * and return the number of impacted entries.
     *
     * @param updateQuery the update query
     * @return the number of modified entries
     * @throws DataStoreException
     */
    int update(String updateQuery) throws DataStoreException;

    /**
     * Execute the insertion defined by the query passed as parameter
     * and return the identifier of the inserted entry.
     *
     * @param insertQuery the insertion query
     * @return the identifier of the inserted entry
     * @throws DataStoreException
     */
    long insert(String insertQuery) throws DataStoreException;

    /**
     * Execute the deletion defined by the query passed as parameter
     * and return the number of impacted entries.
     *
     * @param deleteQuery the deletion query
     * @return the number of deleted entries
     * @throws DataStoreException
     */
    int delete(String deleteQuery) throws DataStoreException;

    /**
     * Execute the query passed as parameter and return
     * the resulting set as a JSON object
     *
     * @param selectQuery the selection query
     * @return the JSON object results set
     * @throws DataStoreException
     */
    JsonArray select(String selectQuery) throws DataStoreException;

}

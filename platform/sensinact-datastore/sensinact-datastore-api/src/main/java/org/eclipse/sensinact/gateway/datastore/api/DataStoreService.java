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

import org.json.JSONArray;

/**
 * C(reate)R(ead)U(update)D(delete) Service signature
 */
public interface DataStoreService 
{		
	/**
	 * Execute the update define by the query passed as parameter
	 * and return the number of impacted entries.
	 *  
	 * @param updateQuery
	 * 		the update query
	 * @return
	 * 		the number of modified entries
	 * @throws DataStoreException 
	 */
	int update(String updateQuery) throws DataStoreException;
		
	/**
	 * Execute the insertion defined by the query passed as parameter
	 * and return the identifier of the inserted entry.
	 *  
	 * @param insertQuery
	 * 		the insertion query
	 * @return
	 * 		the identifier of the inserted entry
	 * @throws DataStoreException 
	 */
	long insert(String insertQuery) throws DataStoreException;
	
	/**
	 * Execute the deletion defined by the query passed as parameter
	 * and return the number of impacted entries.
	 *  
	 * @param deleteQuery
	 * 		the deletion query
	 * @return
	 * 		the number of deleted entries
	 * @throws DataStoreException
	 */
	int delete(String deleteQuery) throws DataStoreException;
	
	/**
	 * Execute the query passed as parameter and return 
	 * the resulting set as a JSON object
	 *  
	 * @param selectQuery
	 * 		the selection query
	 * @return
	 * 		the JSON object results set
	 * @throws DataStoreException
	 */
	JSONArray select(String selectQuery) throws DataStoreException;	
	
}

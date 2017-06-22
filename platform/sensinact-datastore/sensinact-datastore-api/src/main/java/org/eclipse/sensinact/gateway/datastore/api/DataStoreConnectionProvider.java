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

/**
 * Service providing a connection to a data store
 */
public interface DataStoreConnectionProvider<C> 
{
	/**
	 * Return a connection object to a data store or null 
	 * if an error occurred
	 * 
	 * @return
	 * 		a connection object to a data store
	 * 
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
	 * @return
	 * 		the data store's name
	 */
	String getDataStoreName();
}

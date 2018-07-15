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

package org.eclipse.sensinact.gateway.datastore.sqlite;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.core.security.SecurityDataStoreService;
import org.eclipse.sensinact.gateway.core.security.SecurityDataStoreServiceFactory;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;

/**
 * Factory of {@link DataStoreService}
 */
public class SQLiteDataStoreServiceFactoryImpl implements SecurityDataStoreServiceFactory<SQLiteDataStoreService> {
	/**
	 * @inheritDoc
	 * @see org.eclipse.sensinact.gateway.core.security.SecurityDataStoreServiceFactory#
	 *      getType()
	 */
	@Override
	public Class<SQLiteDataStoreService> getType() {
		return SQLiteDataStoreService.class;
	}

	/***
	 * @inheritDoc
	 * 
	 * @see org.eclipse.sensinact.gateway.core.security.SecurityDataStoreServiceFactory#newInstance(org.eclipse.sensinact.gateway.common.bundle.Mediator)
	 */
	@Override
	public void newInstance(Mediator mediator) throws SecuredAccessException {
		SQLiteDataStoreService service = null;
		try {
			service = new SQLiteDataStoreService(mediator);

		} catch (DataStoreException e) {
			throw new SecuredAccessException(e);
		}
		Dictionary props = new Hashtable();
		props.put("org.eclipse.sensinact.data.store.provider", "jdbc");
		props.put("org.eclipse.sensinact.data.store.sgbd", "sqlite");
		mediator.register(service, SecurityDataStoreService.class, props);
	}

}

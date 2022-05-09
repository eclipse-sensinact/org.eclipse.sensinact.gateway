/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.security.dao;

import java.util.Collections;
import java.util.List;

import org.eclipse.sensinact.gateway.core.security.entity.ObjectAccessEntity;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;

/**
 * Method DAO
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ObjectAccessDAO extends AbstractImmutableSnaDAO<ObjectAccessEntity> {
	/**
	 * Constructor
	 * 
	 */
	public ObjectAccessDAO(DataStoreService dataStoreService) throws DAOException {
		super(ObjectAccessEntity.class, dataStoreService);
	}

	/**
	 * Returns the {@link ObjectAccessEntity} from the datastore matching the given
	 * Long identifier, otherwise null.
	 * 
	 * @param identifier
	 *            The Long identifier specifying the primary key of the
	 *            {@link ObjectAccessEntity} to be returned.
	 * @return the {@link ObjectAccessEntity} from the datastore matching the given
	 *         Long identifier, otherwise null.
	 * 
	 * @throws DAOException
	 *             If something fails at datastore level.
	 * @throws DataStoreException
	 */
	public ObjectAccessEntity find(final long identifier) throws DAOException, DataStoreException {
		List<ObjectAccessEntity> objectAccessEntities = super.select(Collections.singletonMap("OAID", identifier));
		if (objectAccessEntities.size() != 1) {
			return null;
		}
		return objectAccessEntities.get(0);
	}
}

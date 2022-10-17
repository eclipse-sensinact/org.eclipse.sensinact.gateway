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

import org.eclipse.sensinact.gateway.core.security.entity.ObjectEntity;
import org.eclipse.sensinact.gateway.core.security.entity.ObjectProfileEntity;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;

/**
 * Method DAO
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ObjectProfileDAO extends AbstractImmutableSnaDAO<ObjectProfileEntity> {
	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	public static final String DEFAULT_OBJECT_PROFILE = "DEFAULT";

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	/**
	 * Constructor
	 * 
	 * @param dataStoreService
	 */
	ObjectProfileDAO(DataStoreService dataStoreService) throws DAOException {
		super(ObjectProfileEntity.class, dataStoreService);
	}

	/**
	 * Returns the {@link ObjectProfileEntity} from the datastore matching the given
	 * Long identifier, otherwise null.
	 * 
	 * @param identifier
	 *            The Long identifier specifying the primary key of the
	 *            {@link ObjectProfileEntity} to be returned.
	 * @return the {@link ObjectProfileEntity} from the datastore matching the given
	 *         Long identifier, otherwise null.
	 * 
	 * @throws DAOException
	 *             If something fails at datastore level.
	 * @throws DataStoreException
	 */
	public ObjectProfileEntity find(final long identifier) throws DAOException, DataStoreException {
		List<ObjectProfileEntity> objectProfileEntities = super.select(Collections.singletonMap("OPID", identifier));

		if (objectProfileEntities.size() != 1) {
			return null;
		}
		return objectProfileEntities.get(0);
	}

	/**
	 * Returns the {@link ObjectEntity} from the datastore matching the given Long
	 * identifier, otherwise null.
	 * 
	 * @param objectProfileEntity
	 * 
	 * @return the {@link ObjectEntity} from the datastore matching the given Long
	 *         identifier, otherwise null.
	 * 
	 * @throws DAOException
	 *             If something fails at datastore level.
	 * @throws DataStoreException
	 */
	public ObjectProfileEntity find(final String name) throws DAOException, DataStoreException {
		List<ObjectProfileEntity> objectProfileEntities = super.select(Collections.singletonMap("OPNAME", name));

		if (objectProfileEntities.size() != 1) {
			return null;
		}
		return objectProfileEntities.get(0);
	}
}

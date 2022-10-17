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

import org.eclipse.sensinact.gateway.core.security.entity.MethodEntity;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;

/**
 * Method DAO
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class MethodDAO extends AbstractImmutableSnaDAO<MethodEntity> {
	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//
	/**
	 * Constructor
	 * 
	 */
	public MethodDAO(DataStoreService dataStoreService) throws DAOException {
		super(MethodEntity.class, dataStoreService);
	}

	/**
	 * Returns the {@link MethodEntity} from the datastore matching the given Long
	 * identifier, otherwise null.
	 * 
	 * @param identifier
	 *            The Long identifier specifying the primary key of the
	 *            {@link MethodEntity} to be returned.
	 * @return the {@link MethodEntity} from the datastore matching the given Long
	 *         identifier, otherwise null.
	 * 
	 * @throws DAOException
	 *             If something fails at datastore level.
	 * @throws DataStoreException
	 */
	public MethodEntity find(final long identifier) throws DAOException, DataStoreException {
		List<MethodEntity> methodEntities = super.select(Collections.singletonMap("MID", identifier));

		if (methodEntities.size() != 1) {
			return null;
		}
		return methodEntities.get(0);
	}

	/**
	 * Returns the {@link MethodEntity} from the datastore matching the given String
	 * method name, otherwise null.
	 * 
	 * @param methodName
	 *            the String method name of the searched {@link MethodEntity}
	 * 
	 * @return the {@link MethodEntity} for the specified method name.
	 * @throws DataStoreException
	 */
	public MethodEntity find(final String methodName) throws DAOException, DataStoreException {
		List<MethodEntity> methodEntities = super.select(Collections.singletonMap("MNAME", methodName));

		if (methodEntities.size() != 1) {
			return null;
		}
		return methodEntities.get(0);
	}
}

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

import org.eclipse.sensinact.gateway.core.security.entity.BundleEntity;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;

/**
 * Method DAO
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class BundleDAO extends AbstractMutableSnaDAO<BundleEntity> {
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
	 * @throws DAOException
	 */
	public BundleDAO(DataStoreService dataStoreService) throws DAOException {
		super(BundleEntity.class, dataStoreService);
	}

	/**
	 * Returns the {@link BundleEntity} from the datastore matching the given String
	 * path, otherwise null.
	 * 
	 * @param identifier
	 *            the String signature (sha-1) of the {@link BundleEntity} to be
	 *            returned.
	 * 
	 * @return the {@link BundleEntity} from the datastore matching the given String
	 *         signature, otherwise null.
	 * @throws DAOException
	 * @throws DataStoreException
	 */
	public BundleEntity find(final String sha) throws DAOException, DataStoreException {
		List<BundleEntity> bundleEntities = super.select(Collections.singletonMap("BSHA", sha));

		if (bundleEntities.size() != 1) {
			return null;
		}
		return bundleEntities.get(0);
	}

	/**
	 * Returns the {@link BundleEntity} from the datastore matching the given long
	 * identifier, otherwise null.
	 * 
	 * @param identifier
	 *            the long identifier of the {@link BundleEntity} to be returned.
	 * 
	 * @return the {@link BundleEntity} from the datastore matching the given long
	 *         identifier, otherwise null.
	 * @throws DAOException
	 * @throws DataStoreException
	 */
	public BundleEntity find(final long identifier) throws DAOException, DataStoreException {
		List<BundleEntity> bundleEntities = super.select(Collections.singletonMap("BID", identifier));

		if (bundleEntities.size() != 1) {
			return null;
		}
		return bundleEntities.get(0);
	}

}

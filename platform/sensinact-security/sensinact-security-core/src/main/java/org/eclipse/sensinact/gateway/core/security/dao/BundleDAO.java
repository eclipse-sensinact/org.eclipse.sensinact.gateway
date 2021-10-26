/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.security.dao;

import java.util.HashMap;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
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
	 * @param mediator
	 *            the {@link Mediator} allowing to interact with the OSGi host
	 *            environment
	 * @throws DAOException
	 */
	public BundleDAO(DataStoreService dataStoreService) throws DAOException {
		super(BundleEntity.class, dataStoreService);
	}

	/**
	 * Returns the {@link BundleEntity} from the datastore matching the given String
	 * path, otherwise null.
	 * 
	 * @param mediator
	 *            the {@link Mediator} allowing to interact with the OSGi host
	 *            environment
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
		List<BundleEntity> bundleEntities = super.select(new HashMap<String, Object>() {
			{
				this.put("BSHA", sha);
			}
		});

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
		List<BundleEntity> bundleEntities = super.select(new HashMap<String, Object>() {
			{
				this.put("BID", identifier);
			}
		});

		if (bundleEntities.size() != 1) {
			return null;
		}
		return bundleEntities.get(0);
	}

}

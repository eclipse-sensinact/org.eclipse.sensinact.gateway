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
package org.eclipse.sensinact.gateway.core.security.dao;

import java.util.HashMap;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.entity.ApplicationEntity;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;

/**
 * Application DAO
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ApplicationDAO extends AbstractMutableSnaDAO<ApplicationEntity> {

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
	public ApplicationDAO(Mediator mediator, DataStoreService dataStoreService) throws DAOException {
		super(mediator, ApplicationEntity.class, dataStoreService);
	}

	/**
	 * Returns the {@link ApplicationEntity} from the datastore matching the given
	 * Long identifier, otherwise null.
	 * 
	 * @param identifier
	 *            The Long identifier specifying the primary key of the
	 *            {@link ApplicationEntity} to be returned.
	 * @return the {@link ApplicationEntity} from the datastore matching the given
	 *         Long identifier, otherwise null.
	 * 
	 * @throws DAOException
	 *             If something fails at datastore level.
	 * @throws DataStoreException
	 */
	public ApplicationEntity find(final long identifier) throws DAOException, DataStoreException {
		List<ApplicationEntity> applicationEntities = super.select(new HashMap<String, Object>() {
			{
				this.put("APPID", identifier);
			}
		});

		if (applicationEntities.size() != 1) {
			return null;
		}
		return applicationEntities.get(0);
	}

	/**
	 * Returns the {@link ApplicationEntity} from the datastore matching the given
	 * String public key, otherwise null.
	 * 
	 * @param publicKey
	 *            The String public key of the {@link ApplicationEntity} to be
	 *            returned.
	 * @return the {@link ApplicationEntity} from the datastore matching the given
	 *         String public key, otherwise null.
	 * 
	 * @throws DAOException
	 *             If something fails at datastore level.
	 * @throws DataStoreException
	 */
	public ApplicationEntity findFromPublicKey(final String publicKey) throws DAOException, DataStoreException {
		if (publicKey == null) {
			return null;
		}
		List<ApplicationEntity> applicationEntities = super.select(new HashMap<String, Object>() {
			{
				this.put("APP_PUBLIC_KEY", publicKey);
			}
		});

		if (applicationEntities.size() != 1) {
			return null;
		}
		return applicationEntities.get(0);
	}

	/**
	 * Returns the {@link ApplicationEntity} from the datastore matching the given
	 * String private key, otherwise null.
	 * 
	 * @param privateKey
	 *            The String private key of the {@link ApplicationEntity} to be
	 *            returned.
	 * @return the {@link ApplicationEntity} from the datastore matching the given
	 *         String private key, otherwise null.
	 * 
	 * @throws DAOException
	 *             If something fails at datastore level.
	 * @throws DataStoreException
	 */
	public ApplicationEntity findFromPrivateKey(final String privateKey) throws DAOException, DataStoreException {
		if (privateKey == null) {
			return null;
		}
		List<ApplicationEntity> applicationEntities = super.select(new HashMap<String, Object>() {
			{
				this.put("APP_PRIVATE_KEY", privateKey);
			}
		});

		if (applicationEntities.size() != 1) {
			return null;
		}
		return applicationEntities.get(0);
	}
}

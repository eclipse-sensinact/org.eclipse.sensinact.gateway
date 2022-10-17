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

import java.util.HashMap;
import java.util.List;

import org.eclipse.sensinact.gateway.core.security.entity.AuthenticatedAccessLevelEntity;
import org.eclipse.sensinact.gateway.core.security.entity.ObjectEntity;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;

/**
 * User Access Level DAO
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class AuthenticatedAccessLevelDAO extends AbstractImmutableSnaDAO<AuthenticatedAccessLevelEntity> {
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
	private ObjectDAO objectDAO;

	/**
	 * Constructor
	 * 
	 * @throws DAOException
	 */
	public AuthenticatedAccessLevelDAO(DataStoreService dataStoreService) throws DAOException {
		super(AuthenticatedAccessLevelEntity.class, dataStoreService);
		this.objectDAO = new ObjectDAO(dataStoreService);
	}

	/**
	 * Returns the {@link AuthenticatedAccessLevelEntity} from the datastore
	 * matching the given object path and user public key.
	 * 
	 * @param objectIdentifier
	 *            the object's long identifier for which to retrieve the access
	 *            level
	 * @param publicKey
	 *            the user's public key for which to retrieve the access level
	 * 
	 * @return the {@link AuthenticatedAccessLevelEntity} for the specified object
	 *         and user.
	 * 
	 * @throws DAOException
	 * @throws DataStoreException
	 */
	protected AuthenticatedAccessLevelEntity find(final long objectIdentifier, final String publicKey)
			throws DAOException, DataStoreException {
		List<AuthenticatedAccessLevelEntity> userAccessLevelEntities = super.select(new HashMap<String, Object>() {
			private static final long serialVersionUID = 1L;
			{
				this.put("UOID", objectIdentifier);
				this.put("PUBLIC_KEY", publicKey);
			}
		});

		if (userAccessLevelEntities.size() != 1) {
			return null;
		}
		return userAccessLevelEntities.get(0);
	}

	/**
	 * Returns the {@link AuthenticatedAccessLevelEntity} from the datastore
	 * matching the given object path and user long identifier.
	 * 
	 * @param objectIdentifier
	 *            the object's long identifier for which to retrieve the access
	 *            level
	 * @param userIdentifier
	 *            the user's long identifier for which to retrieve the access level
	 * 
	 * @return the {@link AuthenticatedAccessLevelEntity} for the specified object
	 *         and user.
	 * 
	 * @throws DAOException
	 * @throws DataStoreException
	 */
	protected AuthenticatedAccessLevelEntity find(final long objectIdentifier, final long userIdentifier)
			throws DAOException, DataStoreException {
		List<AuthenticatedAccessLevelEntity> userAccessLevelEntities = super.select(new HashMap<String, Object>() {
			private static final long serialVersionUID = 1L;
			{
				this.put("UOID", objectIdentifier);
				this.put("UID", userIdentifier);
			}
		});

		if (userAccessLevelEntities.size() != 1) {
			return null;
		}
		return userAccessLevelEntities.get(0);
	}

	/**
	 * Returns the {@link AuthenticatedAccessLevelEntity} from the datastore
	 * matching the given object path and user public key.
	 * 
	 * @param path
	 *            the string path of the object for which to retrieve the access
	 *            level
	 * @param publicKey
	 *            the user's public key for which to retrieve the access level
	 * 
	 * @return the {@link AuthenticatedAccessLevelEntity} for the specified object
	 *         and user.
	 * 
	 * @throws DAOException
	 * @throws DataStoreException
	 */
	public AuthenticatedAccessLevelEntity find(String path, String publicKey) throws DAOException, DataStoreException {

		ObjectEntity objectEntity = null;
		List<ObjectEntity> objectEntities = this.objectDAO.find(path);
		if (objectEntities.size() > 0) {
			objectEntity = objectEntities.get(0);
			// TODO: define what we should do if the path can be mapped to more
			// than one ObjectEntity : should we crash ? should we select the more or
			// the less restrictive AuthenticatedAccessLevelEntity ?
			// should we ignore it as it is the case for now ?
			// Do we have to notify that the DB has not been properly configured
		} else {
			throw new DAOException(String.format("Unknown element at '%s'", path));
		}
		return this.find(objectEntity.getIdentifier(), publicKey);
	}

	/**
	 * Returns the {@link AuthenticatedAccessLevelEntity} from the datastore
	 * matching the given object path and user identifier.
	 * 
	 * @param path
	 *            the string path of the object for which to retrieve the access
	 *            level
	 * @param identifier
	 *            the user's long identifier for which to retrieve the access level
	 * 
	 * @return the {@link AuthenticatedAccessLevelEntity} for the specified object
	 *         and user.
	 * 
	 * @throws DAOException
	 * @throws DataStoreException
	 */
	public AuthenticatedAccessLevelEntity find(String path, long identifier) throws DAOException, DataStoreException {
		ObjectEntity objectEntity = null;
		List<ObjectEntity> objectEntities = this.objectDAO.find(path);
		if (objectEntities.size() > 0) {
			objectEntity = objectEntities.get(0);
			// TODO: define what we should do if the path can be mapped to more
			// than one ObjectEntity : should we crash ? should we select the more or
			// the less restrictive AuthenticatedAccessLevelEntity ?
			// should we ignore it as it is the case for now ?
			// Do we have to notify that the DB has not been properly configured
		} else {
			throw new DAOException(String.format("Unknown element at '%s'", path));
		}
		return this.find(objectEntity.getIdentifier(), identifier);
	}
}

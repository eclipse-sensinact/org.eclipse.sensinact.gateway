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
import org.eclipse.sensinact.gateway.core.security.AccessProfileOption;
import org.eclipse.sensinact.gateway.core.security.entity.ObjectProfileAccessEntity;
import org.eclipse.sensinact.gateway.core.security.entity.ObjectProfileEntity;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;

/**
 * Method DAO
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class ObjectProfileAccessDAO extends AbstractImmutableSnaDAO<ObjectProfileAccessEntity> {
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

	private ObjectProfileDAO objectProfileDAO;

	/**
	 * Constructor
	 * 
	 * @param mediator
	 *            the {@link Mediator} allowing to interact with the OSGi host
	 *            environment
	 */
	public ObjectProfileAccessDAO(Mediator mediator, DataStoreService dataStoreService) throws DAOException {
		super(mediator, ObjectProfileAccessEntity.class, dataStoreService);
		this.objectProfileDAO = new ObjectProfileDAO(mediator, dataStoreService);
	}

	/**
	 * @param objectProfile
	 * @return
	 * @throws DAOException
	 * @throws DataStoreException
	 */
	public AccessProfileOption getAccessProfileOption(long objectProfile) throws DAOException, DataStoreException {
		ObjectProfileEntity entity = this.objectProfileDAO.find(objectProfile);
		return AccessProfileOption.valueOf(entity.getName());
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
	public List<ObjectProfileAccessEntity> getObjectProfileAccesses(ObjectProfileEntity objectProfileEntity)
			throws DAOException, DataStoreException {
		return getObjectProfileAccesses(objectProfileEntity.getIdentifier());
	}

	/**
	 * Returns the {@link ObjectEntity} from the datastore matching the given Long
	 * identifier, otherwise null.
	 * 
	 * @param objectProfileEntityId
	 *            The Long identifier specifying the primary key of the
	 *            {@link ObjectProfileEntity} to be returned.
	 * @return the {@link ObjectProfileEntity} from the datastore matching the given
	 *         Long identifier, otherwise null.
	 * 
	 * @throws DAOException
	 *             If something fails at datastore level.
	 * @throws DataStoreException
	 */
	public List<ObjectProfileAccessEntity> getObjectProfileAccesses(final long identifier)
			throws DAOException, DataStoreException {
		return super.select(new HashMap<String, Object>() {
			{
				this.put("OPID", identifier);
			}
		});
	}

}

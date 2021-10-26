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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
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
	 * @param mediator
	 *            the {@link Mediator} allowing to interact with the OSGi host
	 *            environment
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

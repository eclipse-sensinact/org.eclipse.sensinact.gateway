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

import org.eclipse.sensinact.gateway.core.security.entity.SnaEntity;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Immutable;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractMutableSnaDAO<E extends SnaEntity> extends AbstractSnaDAO<E> {

	// ********************************************************************//
	// NESTED DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// ABSTRACT DECLARATIONS //
	// ********************************************************************//

	// ********************************************************************//
	// STATIC DECLARATIONS //
	// ********************************************************************//
	
	private static final Logger LOG = LoggerFactory.getLogger(AbstractMutableSnaDAO.class);
	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	/**
	 * @param mediator
	 * @param entityType
	 * @throws DAOException
	 */
	AbstractMutableSnaDAO(Class<E> entityType, DataStoreService dataStoreService) throws DAOException {
		super(entityType, dataStoreService);
		if (this.entityType.getAnnotation(Immutable.class) != null)
			throw new DAOException("Mutable type expected");
	}

	@Override
	void created(E entity, long identifier) {
		if (entity == null || identifier < 0) {
			return;
		}
		if (SnaEntity.setUniqueLongPrimaryKey(entity, identifier)) {
			LOG.debug("new %s record in the datastore", entityType.getSimpleName());

		} else {
			LOG.debug("Unable to define the new entity '{}' identifier [{}]", entityType.getSimpleName(),
					identifier);
		}
	}

	@Override
	void updated(int records) {
		LOG.debug("{} {} record(s) updated in the datastore", records, entityType.getSimpleName());
	}

	@Override
	void deleted(int records) {
		LOG.debug("{} {} record(s) deleted in the datastore", records, entityType.getSimpleName());
	}
}

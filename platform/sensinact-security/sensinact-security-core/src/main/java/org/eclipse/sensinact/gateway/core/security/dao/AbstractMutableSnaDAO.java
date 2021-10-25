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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
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
	AbstractMutableSnaDAO(Mediator mediator, Class<E> entityType, DataStoreService dataStoreService) throws DAOException {
		super(mediator, entityType, dataStoreService);
		if (this.entityType.getAnnotation(Immutable.class) != null)
			throw new DAOException("Mutable type expected");
	}

	@Override
	void created(E entity, long identifier) {
		if (entity == null || identifier < 0) {
			return;
		}
		if (SnaEntity.setUniqueLongPrimaryKey(mediator, entity, identifier)) {
			LOG.debug("new %s record in the datastore", entityType.getSimpleName());

		} else {
			LOG.debug("Unable to define the new entity '%s' identifier [%s]", entityType.getSimpleName(),
					identifier);
		}
	}

	@Override
	void updated(int records) {
		LOG.debug("%s %s record(s) updated in the datastore", records, entityType.getSimpleName());
	}

	@Override
	void deleted(int records) {
		LOG.debug("%s %s record(s) deleted in the datastore", records, entityType.getSimpleName());
	}
}

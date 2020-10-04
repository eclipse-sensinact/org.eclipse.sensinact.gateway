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

	// ********************************************************************//
	// INSTANCE DECLARATIONS //
	// ********************************************************************//

	/**
	 * @param mediator
	 * @param entityType
	 * @throws DAOException
	 */
	AbstractMutableSnaDAO(Mediator mediator, Class<E> entityType, DataStoreService dataStoreService)
			throws DAOException {
		super(mediator, entityType, dataStoreService);
		if (this.entityType.getAnnotation(Immutable.class) != null)
			throw new DAOException("Mutable type expected");
	}

	/**
	 * @inheritDoc
	 *
	 * @see AbstractSnaDAO# created(SnaEntity, java.lang.Long)
	 */
	@Override
	void created(E entity, long identifier) {
		if (entity == null || identifier < 0) {
			return;
		}
		if (SnaEntity.setUniqueLongPrimaryKey(mediator, entity, identifier)) {
			mediator.debug("new %s record in the datastore", entityType.getSimpleName());

		} else {
			mediator.debug("Unable to define the new entity '%s' identifier [%s]", entityType.getSimpleName(),
					identifier);
		}
	}

	/**
	 * @inheritDoc
	 *
	 * @see AbstractSnaDAO# updated(java.lang.Integer)
	 */
	@Override
	void updated(int records) {
		super.mediator.debug("%s %s record(s) updated in the datastore", records, entityType.getSimpleName());
	}

	/**
	 * @inheritDoc
	 *
	 * @see AbstractSnaDAO# deleted(java.lang.Integer)
	 */
	@Override
	void deleted(int records) {
		super.mediator.debug("%s %s record(s) deleted in the datastore", records, entityType.getSimpleName());
	}
}

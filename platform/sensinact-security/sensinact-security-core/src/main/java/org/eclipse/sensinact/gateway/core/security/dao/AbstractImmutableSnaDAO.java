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

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.entity.ImmutableSnaEntity;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Immutable;

/**
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public abstract class AbstractImmutableSnaDAO<E extends ImmutableSnaEntity> extends AbstractSnaDAO<E> {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    private Immutable immutable;

    /**
     * @param mediator
     * @param entityType
     */
    AbstractImmutableSnaDAO(Mediator mediator, Class<E> entityType) {
        super(mediator, entityType);
        this.immutable = super.entityType.getAnnotation(Immutable.class);
    }

    /**
     * @inheritDoc
     * @see SnaDAO#
     * create(SnaEntity)
     */
    @Override
    public void create(E entity) throws DAOException {
        throw new DAOException("Immutable type");
    }

    /**
     * @inheritDoc
     * @see SnaDAO#
     * update(SnaEntity)
     */
    @Override
    public void update(E entity) throws DAOException {
        throw new DAOException("Immutable type");
    }

    /**
     * @inheritDoc
     * @see SnaDAO#
     * delete(SnaEntity)
     */
    @Override
    public void delete(E entity) throws DAOException {
        throw new DAOException("Immutable type");
    }

    /**
     * @inheritDoc
     * @see AbstractSnaDAO#
     * created(SnaEntity, long)
     */
    @Override
    void created(E entity, long identifier) {
        // this method will never be called
    }

    /**
     * @inheritDoc
     * @see AbstractSnaDAO#updated(java.lang.Integer)
     */
    @Override
    void updated(int integer) {
        // this method will never be called
    }

    /**
     * @inheritDoc
     * @see AbstractSnaDAO#deleted(java.lang.Integer)
     */
    @Override
    void deleted(int integer) {
        // this method will never be called
    }
}

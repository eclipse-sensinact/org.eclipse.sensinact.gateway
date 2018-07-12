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
package org.eclipse.sensinact.gateway.core.security.entity;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Column;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.PrimaryKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Table;
import org.json.JSONObject;

/**
 * Method Entity
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Table(value = "METHOD")
@PrimaryKey(value = {"MID"})
public class MethodEntity extends ImmutableSnaEntity {
    @Column(value = "MID")
    private long identifier;

    @Column(value = "MNAME")
    private String name;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing to
     *                 interact with the OSGi host environment
     */
    public MethodEntity(Mediator mediator) {
        super(mediator);
    }

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing to
     *                 interact with the OSGi host environment
     * @param row
     */
    public MethodEntity(Mediator mediator, JSONObject row) {
        super(mediator, row);
    }

    /**
     * Constructor
     *
     * @param mediator   the {@link Mediator} allowing to
     *                   interact with the OSGi host environment
     * @param identifier
     * @param name
     */
    public MethodEntity(Mediator mediator, String name) {
        this(mediator);
        this.setName(name);
    }

    /**
     * @inheritDoc
     * @see SnaEntity#getIdentifier()
     */
    public long getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    public void setIdentifier(long identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
}

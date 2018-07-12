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
import org.eclipse.sensinact.gateway.core.security.entity.annotation.ForeignKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.NotNull;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.PrimaryKey;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Table;
import org.json.JSONObject;

/**
 * ObjectProfileAccess DAO Entity
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Table(value = "AUTHENTICATED")
@PrimaryKey(value = {"PUBLIC_KEY", "OID", "UAID"})
public class AuthenticatedEntity extends SnaEntity {
    @NotNull
    @Column(value = "PUBLIC_KEY")
    private String publicKey;
    @NotNull
    @Column(value = "OID")
    @ForeignKey(refer = "OID", table = "OBJECT")
    private long objectEntity;
    @NotNull
    @Column(value = "UAID")
    @ForeignKey(refer = "UAID", table = "USER_ACCESS")
    private long userAccessEntity;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing to
     *                 interact with the OSGi host environment
     */
    public AuthenticatedEntity(Mediator mediator) {
        super(mediator);
    }

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing to
     *                 interact with the OSGi host environment
     * @param row
     */
    public AuthenticatedEntity(Mediator mediator, JSONObject row) {
        super(mediator, row);
    }

    /**
     * Constructor
     *
     * @param mediator            the {@link Mediator} allowing to
     *                            interact with the OSGi host environment
     * @param methodEntity
     * @param objectProfileEntity
     * @param objectAccessEntity
     */
    public AuthenticatedEntity(Mediator mediator, String publicKey, long objectEntity, long userAccessEntity) {
        super(mediator);
        this.setPublicKey(publicKey);
        this.setObjectEntity(objectEntity);
        this.setUserAccessEntity(userAccessEntity);
    }

    /**
     * @return the userEntity
     */
    public String getPublicKey() {
        return this.publicKey;
    }

    /**
     * @param userEntity the userEntity to set
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * @return the objectEntity
     */
    public long getObjectEntity() {
        return objectEntity;
    }

    /**
     * @param objectEntity the objectEntity to set
     */
    public void setObjectEntity(long objectEntity) {
        this.objectEntity = objectEntity;
    }

    /**
     * @return the userAccessEntity
     */
    public long getUserAccessEntity() {
        return userAccessEntity;
    }

    /**
     * @param userAccessEntity the userAccessEntity to set
     */
    public void setUserAccessEntity(long userAccessEntity) {
        this.userAccessEntity = userAccessEntity;
    }


}

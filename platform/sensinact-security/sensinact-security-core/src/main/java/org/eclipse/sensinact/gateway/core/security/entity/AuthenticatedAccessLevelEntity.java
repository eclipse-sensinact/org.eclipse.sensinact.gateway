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
import org.eclipse.sensinact.gateway.core.security.AccessLevel;
import org.eclipse.sensinact.gateway.core.security.AccessLevelImpl;
import org.eclipse.sensinact.gateway.core.security.AccessLevelOption;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Column;
import org.eclipse.sensinact.gateway.core.security.entity.annotation.Table;
import org.json.JSONObject;

/**
 * UserAccessLevel DAO Entity
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Table(value = "AUTHENTICATED_ACCESS_LEVEL")
public class AuthenticatedAccessLevelEntity extends ImmutableSnaEntity implements AccessLevel {
    @Column(value = "UOID")
    private long objectId;
    @Column(value = "PUBLIC_KEY")
    private String publicKey;

    @Column(value = "UID")
    private long authenticatedId;

    @Column(value = "UAID")
    private long userAccessId;

    @Column(value = "UALEVEL")
    private int accessLevel;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing to
     *                 interact with the OSGi host environment
     */
    public AuthenticatedAccessLevelEntity(Mediator mediator) {
        super(mediator);
    }

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing to
     *                 interact with the OSGi host environment
     * @param row      the JSON formated description of the
     *                 UserAccessLevelEntity to be instantiated
     */
    public AuthenticatedAccessLevelEntity(Mediator mediator, JSONObject row) {
        super(mediator, row);
    }

    /**
     * Constructor
     *
     * @param mediator    the {@link Mediator} allowing to
     *                    interact with the OSGi host environment
     * @param publicKey
     * @param user
     * @param object
     * @param accessLevel
     */
    public AuthenticatedAccessLevelEntity(Mediator mediator, long objectId, String publicKey, long authenticatedId, long userAccessId, int accessLevel) {
        this(mediator);
        this.setObjectId(objectId);
        this.setPublicKey(publicKey);
        this.setAuthenticatedId(authenticatedId);
        this.setUserAccessId(userAccessId);
        this.setAccessLevel(accessLevel);
    }

    /**
     * @param objectId
     */
    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    /**
     * @return
     */
    public long getObjectId() {
        return this.objectId;
    }

    /**
     * @param authenticatedId
     */
    public void setAuthenticatedId(long authenticatedId) {
        this.authenticatedId = authenticatedId;
    }

    /**
     * @return
     */
    public long getAuthenticatedId() {
        return this.authenticatedId;
    }

    /**
     * @param userAccessId
     */
    public void setUserAccessId(long userAccessId) {
        this.userAccessId = userAccessId;
    }

    /**
     * @return
     */
    public long getUserAccessId() {
        return this.userAccessId;
    }

    /**
     * @return the public key of the associated user
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * @param publicKey the user's public key to set
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.security.AccessLevel#getLevel()
     */
    @Override
    public int getLevel() {
        return this.getAccessLevel();
    }

    /**
     * @return access level
     */
    public int getAccessLevel() {
        return this.accessLevel;
    }

    /**
     * @param the access level to set
     */
    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }

    /**
     * Returns the {@link AccessLevelOption} providing
     * the {@link AccessLevel} whose level is the same
     * as this UserAccessLevelEntity
     *
     * @return the {@link AccessLevelOption} with the
     * same access level as this UserAccessLevelEntity
     */
    public AccessLevelOption getAccessLevelOption() {
        return AccessLevelOption.valueOf(new AccessLevelImpl(this.accessLevel));
    }
}

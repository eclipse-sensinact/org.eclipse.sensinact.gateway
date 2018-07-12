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
@Table(value = "SNAUSER")
@PrimaryKey(value = {"SUID"})
public class UserEntity extends SnaEntity {
    @Column(value = "SUID")
    private long identifier;
    @Column(value = "SULOGIN")
    private String login;

    @Column(value = "SUPASSWORD")
    private String password;
    @Column(value = "SUMAIL")
    private String mail;
    @Column(value = "SUPUBLIC_KEY")
    private String publicKey;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing to
     *                 interact with the OSGi host environment
     */
    public UserEntity(Mediator mediator) {
        super(mediator);
    }

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing to
     *                 interact with the OSGi host environment
     * @param row
     */
    public UserEntity(Mediator mediator, JSONObject row) {
        super(mediator, row);
    }

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing to
     *                 interact with the OSGi host environment
     * @param login
     * @param password
     * @param mail
     */
    public UserEntity(Mediator mediator, String login, String password, String mail, String publicKey) {
        this(mediator);
        this.setLogin(login);
        this.setPassword(password);
        this.setMail(mail);
        this.setPublicKey(publicKey);
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
     * @return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * @param login the login to set
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the mail
     */
    public String getMail() {
        return mail;
    }

    /**
     * @param mail the mail to set
     */
    public void setMail(String mail) {
        this.mail = mail;
    }

    /**
     * @return the public key
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * @param publicKey the public key to set
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}

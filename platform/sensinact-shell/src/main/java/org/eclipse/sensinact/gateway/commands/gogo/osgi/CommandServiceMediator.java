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

package org.eclipse.sensinact.gateway.commands.gogo.osgi;

import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.core.security.Session;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.security.InvalidKeyException;

/**
 * @see Mediator
 */
public class CommandServiceMediator extends Mediator {

    private Session session;
    private String userId;

    /**
     * @ 
     * @see Mediator#Mediator(BundleContext)
     */
    CommandServiceMediator(BundleContext context)  {
        super(context);

        Core core = getCore();

        if(core != null) {
            this.session = getCore().getAnonymousSession();
            this.userId = "anonymous";
        } else {
            throw new NullPointerException("Unable to retrieve the Core service");
        }
    }

    /**
     * Switch to the anonymous user
     * @throws DataStoreException
     * @throws InvalidKeyException
     * @throws InvalidCredentialException
     */
    public void switchUser()
            throws DataStoreException, InvalidKeyException, InvalidCredentialException {

        this.switchUser(null, null);
    }

    /**
     * Switch to a different user
     * @param login the login of the user
     * @param password the password of the user
     * @throws DataStoreException
     * @throws InvalidKeyException
     * @throws InvalidCredentialException
     */
    public void switchUser(String login, String password) 
    		throws DataStoreException, InvalidKeyException, InvalidCredentialException {

        Core core = getCore();

        if(core != null) {
            if (login == null && password == null) {
                this.session = getCore().getAnonymousSession();
                this.userId = "anonymous";
            } else {
                this.session = getCore().getSession(new Credentials(login, password));
                this.userId = login;
            }
        } else {
            throw new NullPointerException("Unable to retrieve the Core service");
        }
    }

    /**
     * Returns the current user id
     * @return the user id
     */
    public String getCurrentUser() {
        return userId;
    }

    /**
     * Get the current session for the user
     * @return the session of the user
     */
    public Session getSession() {
        return session;
    }

    /**
     * Get the secured access from the OSGi registry
     * @return the secured access object
     */
    private Core getCore() {
        ServiceReference[] references = null;

        try {
            references = super.getContext().getAllServiceReferences(
            		Core.class.getCanonicalName(), null);
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }

        if(references != null) {
            if(references.length == 1) {
                return (Core) super.getContext().getService(references[0]);
            }
        }

        return null;
    }
}

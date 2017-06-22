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

import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.SecuredAccess;
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
    public CommandServiceMediator(BundleContext context)  {
        super(context);
        this.session = getSecuredAccess().getAnonymousSession();
        this.userId = "anonymous";
    }

    /**
     * Switch to a different user
     * @param login the login of the user
     * @param password the password of the user
     * @throws DataStoreException
     * @throws InvalidKeyException
     */
    public void switchUser(String login, String password) throws DataStoreException, InvalidKeyException {
        this.session = getSecuredAccess().getSession(new Credentials(login, password));
        this.userId = login;
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
     * @return the session pof the user
     */
    public Session getSession() {
        return session;
    }

    /**
     * Get the secured access from the OSGi registry
     * @return the secured access object
     */
    private SecuredAccess getSecuredAccess() {
        ServiceReference[] references = null;

        try {
            references = super.getContext().getAllServiceReferences(
            		SecuredAccess.class.getCanonicalName(), null);
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }

        if(references != null) {
            if(references.length == 1) {
                return (SecuredAccess) super.getContext().getService(references[0]);
            }
        }

        return null;
    }
}

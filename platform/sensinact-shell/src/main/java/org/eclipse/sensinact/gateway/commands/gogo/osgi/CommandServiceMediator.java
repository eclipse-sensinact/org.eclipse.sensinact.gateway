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

import org.eclipse.sensinact.gateway.commands.gogo.internal.shell.ShellOutput;
import org.eclipse.sensinact.gateway.core.security.Credentials;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundEndpoint;
import org.eclipse.sensinact.gateway.nthbnd.endpoint.NorthboundMediator;
import org.osgi.framework.BundleContext;

import java.security.InvalidKeyException;

/**
 * @see Mediator
 */
public class CommandServiceMediator extends NorthboundMediator {
    public static String uri(String serviceProvider, String service, String resource, boolean multi) {
        StringBuilder builder = new StringBuilder();
        builder.append("/sensinact");
        if (serviceProvider != null) {
            builder.append("/providers/");
            builder.append(serviceProvider);
            if (service != null) {
                builder.append("/services/");
                builder.append(service);
                if (resource != null) {
                    builder.append("/resources/");
                    builder.append(resource);
                } else if (multi) {
                    builder.append("/resources");

                }
            } else if (multi) {
                builder.append("/services");

            }
        } else if (multi) {
            builder.append("/providers");
        }
        return builder.toString();
    }

    public static String uri(String serviceProvider, String service, String resource, String method) {
        StringBuilder builder = new StringBuilder();
        builder.append(uri(serviceProvider, service, resource, false));
        builder.append("/");
        builder.append(method);
        return builder.toString();
    }

    private ShellOutput output;
    private NorthboundEndpoint endpoint;
    private String userId;

    /**
     * @throws InvalidCredentialException
     * @
     * @see Mediator#Mediator(BundleContext)
     */
    CommandServiceMediator(BundleContext context) throws InvalidCredentialException {
        super(context);
        this.endpoint = new NorthboundEndpoint(this, null);
        this.output = new ShellOutput();
        this.userId = "anonymous";
    }

    /**
     * Switches to the anonymous user
     *
     * @throws DataStoreException
     * @throws InvalidKeyException
     * @throws InvalidCredentialException
     */
    public void switchUser() throws DataStoreException, InvalidKeyException, InvalidCredentialException {
        this.switchUser(null, null);
    }

    /**
     * Switches to a different user
     *
     * @param login    the login of the user
     * @param password the password of the user
     * @throws DataStoreException
     * @throws InvalidKeyException
     * @throws InvalidCredentialException
     */
    public void switchUser(String login, String password) throws DataStoreException, InvalidKeyException, InvalidCredentialException {
        if (login == null && password == null) {
            this.endpoint = new NorthboundEndpoint(this, null);
            this.userId = "anonymous";
        } else {
            this.endpoint = new NorthboundEndpoint(this, new Credentials(login, password));
            this.userId = login;
        }
    }

    /**
     * Returns the current {@link NorthboundEndpoint} attached
     * to this CommandServiceMediator
     *
     * @return this CommandServiceMediator's {@link NorthboundEndpoint}
     */
    public NorthboundEndpoint getEndpoint() {
        return this.endpoint;
    }

    /**
     * Returns the {@link ShellOutput} of this CommandServiceMediator
     *
     * @return this CommandServiceMediator's {@link ShellOutput}
     */
    public ShellOutput getOutput() {
        return this.output;
    }

    /**
     * Returns the current user id
     *
     * @return the user id
     */
    public String getCurrentUser() {
        return userId;
    }
}

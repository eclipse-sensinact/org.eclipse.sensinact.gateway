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
package org.eclipse.sensinact.gateway.nthbnd.endpoint;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.common.execution.Executable;
import org.eclipse.sensinact.gateway.core.Core;
import org.eclipse.sensinact.gateway.core.Session;
import org.eclipse.sensinact.gateway.core.security.Authentication;
import org.eclipse.sensinact.gateway.core.security.InvalidCredentialException;
import org.osgi.framework.BundleContext;

/**
 * Extended {@link Mediator} type allowing to easily instantiate a
 * {@link Session}
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class NorthboundMediator extends Mediator {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    private static final class SessionExecutor implements Executable<Core, Session> {
        private Authentication<?> authentication;

        SessionExecutor(Authentication<?> authentication) {
            this.authentication = authentication;
        }

        /**
         * @inheritDoc
         * @see Executable#execute(java.lang.Object)
         */
        @Override
        public Session execute(Core core) throws Exception {
            Session session = null;
            try {
                if (this.authentication != null) {
                    session = core.getSession(authentication);
                }
                if (session == null) {
                    session = core.getAnonymousSession();
                }
            } catch (InvalidCredentialException e) {
                throw e;
            }
            return session;
        }
    }

    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    NorthboundEndpoints northboundEndpoints;

    LoginEndpoint loginEndpoint;

    /**
     * Constructor
     *
     * @param context the {@link BundleContext} allowing
     *                the NorthboundMediator to be instantiated to interact with
     *                the OSGi host environment
     */
    public NorthboundMediator(BundleContext context) {
        super(context);
        this.northboundEndpoints = new NorthboundEndpoints(this);
        this.loginEndpoint = new LoginEndpoint(this);
    }

    /**
     * Returns a {@link Session} for the user whose authentication
     * material is passed as parameter
     *
     * @param authentication the {@link Authentication} of a user for
     *                       who to return a {@link Session}
     * @return the {@link Session} for the specified {@link Authentication}
     */
    public Session getSession(Authentication<?> authentication) throws InvalidCredentialException {
        return super.callService(Core.class, new SessionExecutor(authentication));
    }

    /**
     * Returns a {@link Session} for an anonymous user
     *
     * @return the {@link Session} for an anonymous user
     */
    public Session getSession() throws InvalidCredentialException {
        return getSession(null);
    }

    /**
     * Returns the {@link NorthboundEndpoints} attached to this
     * NorthboundMediator
     *
     * @return this NorthboundMediator's {@link NorthboundEndpoints}
     */
    public NorthboundEndpoints getNorthboundEndpoints() {
        return this.northboundEndpoints;
    }

    /**
     * Returns the {@link LoginEndpoint} attached to this
     * NorthboundMediator
     *
     * @return this NorthboundMediator's {@link LoginEndpoint}
     */
    public LoginEndpoint getLoginEndpoint() {
        return this.loginEndpoint;
    }
}

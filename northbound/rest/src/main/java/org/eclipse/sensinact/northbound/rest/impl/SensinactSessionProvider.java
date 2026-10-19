/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.northbound.rest.impl;

import java.security.Principal;
import java.util.Collection;

import org.eclipse.sensinact.northbound.rest.impl.AuthenticationFilter.UserInfoPrincipal;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SensinactSessionProvider implements ContextResolver<SensiNactSession> {

    private static final Logger LOG = LoggerFactory.getLogger(SensinactSessionProvider.class);

    @Context
    Application application;

    @Context
    SecurityContext context;

    /**
     * User of a principal that is not a {@link UserInfoPrincipal}. The default
     * session of the user keeps this object long after the request that created
     * it, while the injected security context is only valid during a request:
     * the user id is therefore read once, at creation
     */
    class MyUserInfo implements UserInfo {

        private final String userId;

        MyUserInfo(Principal principal) {
            this.userId = principal.getName();
        }

        @Override
        public String getUserId() {
            return userId;
        }

        @Override
        public Collection<String> getGroups() {
            throw new UnsupportedOperationException("Unimplemented method 'getGroups'");
        }

        @Override
        public boolean isAuthenticated() {
            return true;
        }

        @Override
        public boolean isMemberOfGroup(String group) {
            try {
                return context.isUserInRole(group);
            } catch (IllegalStateException e) {
                // Outside of a request, the injected context can't be used
                LOG.debug("Can't check if user {} is in role {} outside of a request", userId, group, e);
                return false;
            }
        }
    }

    @Override
    public SensiNactSession getContext(Class<?> type) {
        SensiNactSessionManager manager = (SensiNactSessionManager) application.getProperties().get("session.manager");

        Principal principal = context.getUserPrincipal();
        if (principal instanceof UserInfoPrincipal) {
            UserInfoPrincipal uiPrincipal = (UserInfoPrincipal) principal;
            return manager.getDefaultSession(uiPrincipal.getUserInfo());
        } else if (principal != null) {
            return manager.getDefaultSession(new MyUserInfo(principal));
        } else {
            return manager.getDefaultSession(UserInfo.ANONYMOUS);
        }
    }
}

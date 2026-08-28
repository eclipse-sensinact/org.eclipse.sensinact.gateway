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

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SensinactSessionProvider implements ContextResolver<SensiNactSession> {

    @Context
    Application application;

    @Context
    SecurityContext context;

    class MyUserInfo implements UserInfo {
        @Override
        public String getUserId() {
            return context.getUserPrincipal().getName();
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
            return context.isUserInRole(group);
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
            return manager.getDefaultSession(new MyUserInfo());
        } else {
            return manager.getDefaultSession(UserInfo.ANONYMOUS);
        }
    }
}

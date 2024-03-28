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

import org.eclipse.sensinact.core.security.UserInfo;
import org.eclipse.sensinact.northbound.rest.impl.AuthenticationFilter.UserInfoPrincipal;
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

    @Override
    public SensiNactSession getContext(Class<?> type) {
        SensiNactSessionManager manager = (SensiNactSessionManager) application.getProperties().get("session.manager");

        Principal principal = context.getUserPrincipal();
        if (principal instanceof UserInfoPrincipal) {
            UserInfoPrincipal uiPrincipal = (UserInfoPrincipal) principal;
            return manager.getDefaultSession(uiPrincipal.getUserInfo());
        } else if (principal == null) {
            return manager.getDefaultSession(UserInfo.ANONYMOUS);
        }
        throw new IllegalArgumentException("Unable to establish user context");
    }
}

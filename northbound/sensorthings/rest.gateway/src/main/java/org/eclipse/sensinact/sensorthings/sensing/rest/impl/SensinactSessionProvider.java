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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl;

import org.eclipse.sensinact.core.security.UserInfo;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.ContextResolver;

/**
 * Provides a suitable ObjectMapper for JSON serialization
 */
public class SensinactSessionProvider implements ContextResolver<SensiNactSession> {

    @Context
    Application application;

    @Override
    public SensiNactSession getContext(Class<?> type) {
        // TODO proper user and session mapping
        SensiNactSessionManager manager = (SensiNactSessionManager) application.getProperties().get("session.manager");
        return manager.getDefaultSession(UserInfo.ANONYMOUS);
    }

}

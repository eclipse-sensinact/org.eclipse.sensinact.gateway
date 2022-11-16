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

import org.eclipse.sensinact.prototype.SensiNactSession;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.ContextResolver;

/**
 * Provides a suitable ObjectMapper for JSON serialization
 */
public class SensinactSessionProvider implements ContextResolver<SensiNactSession> {

    @Context
    SensinactSensorthingsApplication application;

    @Override
    public SensiNactSession getContext(Class<?> type) {
        // TODO proper user and session mapping
        return application.getSessionManager().getDefaultSession(null);
    }

}

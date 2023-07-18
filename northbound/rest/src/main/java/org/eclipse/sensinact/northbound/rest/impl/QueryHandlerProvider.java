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

import org.eclipse.sensinact.northbound.query.api.IQueryHandler;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class QueryHandlerProvider implements ContextResolver<IQueryHandler> {

    @Context
    Application application;

    @Override
    public IQueryHandler getContext(Class<?> type) {
        return (IQueryHandler) application.getProperties().get("query.handler");
    }
}

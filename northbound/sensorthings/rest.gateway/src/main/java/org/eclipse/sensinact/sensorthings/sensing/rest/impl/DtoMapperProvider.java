/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

import org.eclipse.sensinact.sensorthings.sensing.rest.utils.IDtoMapper;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * Provides a suitable ObjectMapper for JSON serialization
 */
@Provider
public class DtoMapperProvider implements ContextResolver<IDtoMapper> {

    @Context
    Application application;

    @Override
    public IDtoMapper getContext(Class<?> type) {
        return (IDtoMapper) application.getProperties().get("dto.mapper");
    }
}

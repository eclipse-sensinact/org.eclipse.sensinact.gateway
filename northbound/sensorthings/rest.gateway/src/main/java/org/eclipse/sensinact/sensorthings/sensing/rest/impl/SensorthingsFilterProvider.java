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

import org.eclipse.sensinact.northbound.filters.sensorthings.ISensorthingsFilterParser;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.ContextResolver;

/**
 * Provides a suitable ObjectMapper for JSON serialization
 */
public class SensorthingsFilterProvider implements ContextResolver<ISensorthingsFilterParser> {

    @Context
    Application application;

    @Override
    public ISensorthingsFilterParser getContext(Class<?> type) {
        return (ISensorthingsFilterParser) application.getProperties().get("filter.parser");
    }
}

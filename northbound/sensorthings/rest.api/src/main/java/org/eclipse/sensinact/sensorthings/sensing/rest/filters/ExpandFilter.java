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
package org.eclipse.sensinact.sensorthings.sensing.rest.filters;

import static jakarta.ws.rs.Priorities.USER;
import static org.eclipse.sensinact.sensorthings.sensing.rest.access.IFilterConstants.EXPAND_SETTINGS_STRING;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Providers;

/**
 * Implements handling (in this case rejection of) the $expand parameter
 */
@Priority(USER)
public class ExpandFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Context
    Providers providers;

    private ObjectMapper getMapper() {
        return providers.getContextResolver(ObjectMapper.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {

        ExpansionSettingsImpl es = (ExpansionSettingsImpl) requestContext.getProperty(EXPAND_SETTINGS_STRING);

        if(es == null || es.isEmpty()) {
            return;
        }

        JsonNode json = es.processExpansions(getMapper(), responseContext.getEntity());

        responseContext.setEntity(json);
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        List<String> fields = requestContext.getUriInfo().getQueryParameters().getOrDefault("$expand", List.of());
        requestContext.setProperty(EXPAND_SETTINGS_STRING, new ExpansionSettingsImpl(
                fields.stream().flatMap(s -> Arrays.stream(s.split(",")))));
    }

}

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
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Providers;

/**
 * Implements the $select query parameter
 */
@Priority(USER + 1)
public class SelectFilter implements ContainerResponseFilter {

    @Context
    Providers providers;

    private ObjectMapper getMapper() {
        return providers.getContextResolver(ObjectMapper.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        List<String> fields = requestContext.getUriInfo().getQueryParameters().getOrDefault("$select", List.of());
        if (fields.isEmpty()) {
            return;
        }

        fields = fields.stream().flatMap(s -> Arrays.stream(s.split(","))).collect(toList());

        Object entity = responseContext.getEntity();
        JsonNode json = getMapper().valueToTree(entity);

        if (entity instanceof ResultList) {
            ArrayNode values = (ArrayNode) json.get("value");
            for (JsonNode jn : values) {
                if (jn.isObject()) {
                    ObjectNode on = (ObjectNode) jn;
                    on.retain(fields);
                }
            }
        } else if (json.isObject()) {
            ObjectNode on = (ObjectNode) json;
            on.retain(fields);
        }

        responseContext.setEntity(json);
    }

}

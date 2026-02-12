/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import static jakarta.ws.rs.Priorities.ENTITY_CODER;

import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.sensinact.sensorthings.sensing.dto.DataArray;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Providers;

/**
 * Implements the $resultformat query parameter
 */
@Priority(ENTITY_CODER + 5)
public class ResultFormatFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String RESULT_FORMAT_PROP = "org.eclipse.sensinact.sensorthings.sensing.rest.resultformat";

    private DataArray convertToDataArray(String request, ResultList<? extends Self> resultList, List<String> components,
            String entityType) {

        String[] split = request.split("/");
        String plural = split[split.length - 1];
        String entityName = plural.substring(0, plural.length() - 1);
        List<List<Object>> rows = resultList.value().stream()
                .<List<Object>>map(dto -> DtoMapperSimple.getRecordField(dto)).toList();
        return new DataArray(null, entityName, request, components, rows);

    }

    private String resolveEntityTypeName(Object obj) {
        return obj.getClass().getSimpleName();

    }

    private List<String> determineComponents(Object record) {
        List<String> components = new ArrayList<>();
        RecordComponent[] recordFields = record.getClass().getRecordComponents();
        for (RecordComponent rc : recordFields) {
            String name = rc.getName();
            if (name.equalsIgnoreCase("id")) {
                components.add("@iot.id");
            }
            if (name.equals("selfLink")) {
                components.add("@iot.selfLink");
            }
        }
        return components;
    }

    @Context
    Providers providers;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        String resultFormat = (String) requestContext.getProperty(RESULT_FORMAT_PROP);
        if (!"dataArray".equalsIgnoreCase(resultFormat))
            return;

        Object entity = responseContext.getEntity();
        if (entity instanceof ResultList<?> resultList && !resultList.value().isEmpty()) {

            Object firstItem = resultList.value().get(0);
            String typeName = resolveEntityTypeName(firstItem);
            List<String> components = determineComponents(firstItem);

            DataArray dataArrayValue = convertToDataArray(requestContext.getUriInfo().getRequestUri().toString(),
                    resultList, components, typeName);

            responseContext
                    .setEntity(new ResultList<>(resultList.count(), resultList.nextLink(), List.of(dataArrayValue)));
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        List<String> list = requestContext.getUriInfo().getQueryParameters().getOrDefault("$resultFormat", List.of());
        if (list.size() > 1) {
            requestContext.abortWith(
                    Response.status(Status.BAD_REQUEST).entity("Only one $count parameter may be provided").build());
        }
        if (!list.isEmpty())
            requestContext.setProperty(RESULT_FORMAT_PROP, list.get(0));

    }

}

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

import static jakarta.ws.rs.Priorities.ENTITY_CODER;

import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.Priority;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Providers;

/**
 * Implements the $orderby query parameter
 */
@Priority(ENTITY_CODER + 3)
public class OrderByFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String ORDERBY_PROP = "org.eclipse.sensinact.sensorthings.sensing.rest.orderby";

    @Context
    Providers providers;

    private ObjectMapper getMapper() {
        return providers.getContextResolver(ObjectMapper.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        @SuppressWarnings("unchecked")
        Comparator<Object> comparator = (Comparator<Object>) requestContext.getProperty(ORDERBY_PROP);
        if (comparator == null) {
            return;
        }

        Object entity = responseContext.getEntity();
        if (entity instanceof ResultList) {
            ResultList<? extends Self> resultList = (ResultList<?>) entity;
            ResultList<? extends Self> newEntity = new ResultList<>(resultList.count(), resultList.nextLink(),
                    resultList.value().stream().sorted(comparator).toList());
            responseContext.setEntity(newEntity);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        List<String> list = requestContext.getUriInfo().getQueryParameters().getOrDefault("$orderby", List.of());

        try {
            Comparator<Object> comparator = list.stream().flatMap(s -> Arrays.stream(s.split(",")))
                    .map(this::toComparator).reduce(Comparator::thenComparing).orElseGet(() -> toComparator("id"));
            requestContext.setProperty(ORDERBY_PROP, comparator);
        } catch (Exception e) {
            requestContext.abortWith(
                    Response.status(Status.BAD_REQUEST).entity("Not a valid orderby definition " + list).build());
        }
    }

    private static final Comparator<Comparable<Object>> BASE_COMPARATOR = Comparator
            .nullsFirst(Comparator.naturalOrder());

    private Comparator<Object> toComparator(String s) {

        String clause = s.trim();
        boolean ascending;
        if (clause.endsWith("asc")) {
            ascending = true;
            clause = clause.substring(0, clause.length() - 3).trim();
        } else if (clause.endsWith("desc")) {
            ascending = false;
            clause = clause.substring(0, clause.length() - 4).trim();
        } else {
            ascending = true;
        }

        final String[] path = clause.split("/");
        Comparator<Object> result = (a, b) -> {
            return BASE_COMPARATOR.compare(get(a, path), get(b, path));
        };

        return ascending ? result : result.reversed();
    }

    @SuppressWarnings("unchecked")
    public Comparable<Object> get(Object o, String[] path) {
        Object result = o;
        for (String s : path) {
            if (result == null) {
                break;
            }
            try {
                if (o instanceof Record) {
                    RecordComponent[] components = o.getClass().getRecordComponents();
                    RecordComponent component = Arrays.stream(components).filter(rc -> rc.getName().equals(s))
                            .findFirst().get();
                    result = component.getAccessor().invoke(result);
                } else if (o instanceof JsonNode jn) {
                    if (jn.has(s)) {
                        result = jn.get(s);
                    } else {
                        throw new IllegalArgumentException("No property " + s + " in object " + jn);
                    }
                } else {
                    JsonNode jn = getMapper().convertValue(o, JsonNode.class);
                    if (jn.has(s)) {
                        result = jn.get(s);
                    } else {
                        throw new IllegalArgumentException("No property " + s + " in object " + jn);
                    }
                }
            } catch (Exception e) {
                throw new BadRequestException("Failed to order objects by " + Arrays.toString(path));
            }
        }
        return (Comparable<Object>) result;
    }

}

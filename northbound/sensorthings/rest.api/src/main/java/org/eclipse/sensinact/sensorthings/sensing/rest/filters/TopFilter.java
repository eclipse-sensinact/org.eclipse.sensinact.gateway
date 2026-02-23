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
package org.eclipse.sensinact.sensorthings.sensing.rest.filters;

import static jakarta.ws.rs.Priorities.ENTITY_CODER;

import java.io.IOException;
import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PaginationLimit;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * This filter implements the $top query parameter
 */
@Priority(ENTITY_CODER + 1)
public class TopFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String TOP_PROP = "org.eclipse.sensinact.sensorthings.sensing.rest.top";

    @Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        Integer top = (Integer) requestContext.getProperty(TOP_PROP);
        if (top == null) {
            return;
        }

        Object entity = responseContext.getEntity();
        if (entity instanceof ResultList) {
            ResultList<? extends Self> resultList = (ResultList<?>) entity;
            int size = resultList.value().size();
            List<? extends Self> value = resultList.value().subList(0, Math.min(top, size));

            Integer skip = (Integer) requestContext.getProperty(SkipFilter.SKIP_PROP);
            Integer nextSkip = (skip == null) ? top : top + skip;
            String nextLink = null;
            if (top < size) {
                nextLink = requestContext.getUriInfo().getRequestUriBuilder().replaceQueryParam("$skip", nextSkip)
                        .build().toString();
            }
            responseContext.setEntity(new ResultList<>(resultList.count(), nextLink, value));
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        int top = 0;

        // Limit the pagination to the limit set on the method, if present
        PaginationLimit paginationLimit = resourceInfo.getResourceMethod().getAnnotation(PaginationLimit.class);

        List<String> list = requestContext.getUriInfo().getQueryParameters().getOrDefault("$top", List.of());
        if (list.size() > 1) {
            requestContext.abortWith(
                    Response.status(Status.BAD_REQUEST).entity("Only one $top parameter may be provided").build());
            return;
        } else {
            if (list.isEmpty()) {
                if (paginationLimit != null) {
                    top = paginationLimit.value();
                } else {
                    // No top value to use
                    return;
                }
            } else {
                String s = list.get(0);
                try {
                    top = Integer.parseInt(s);
                } catch (NumberFormatException nfe) {
                    requestContext.abortWith(Response.status(Status.BAD_REQUEST)
                            .entity("The $top parameter must be an integer greater than zero").build());
                    return;
                }
                if (paginationLimit != null) {
                    top = Math.min(top, paginationLimit.value());
                }
            }
        }

        if (top <= 0) {
            requestContext.abortWith(Response.status(Status.BAD_REQUEST)
                    .entity("The $top parameter must be an integer greater than zero").build());
            return;
        }
        requestContext.setProperty(TOP_PROP, top);
    }

}

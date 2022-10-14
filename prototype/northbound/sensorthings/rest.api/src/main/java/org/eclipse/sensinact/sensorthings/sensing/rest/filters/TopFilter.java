/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.sensorthings.sensing.rest.filters;

import static jakarta.ws.rs.Priorities.ENTITY_CODER;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * This filter implements the $top query parameter
 */
@Priority(ENTITY_CODER + 4)
public class TopFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String TOP_PROP = "org.eclipse.sensinact.sensorthings.sensing.rest.top";

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        Integer top = (Integer) requestContext.getProperty(TOP_PROP);
        
        Object entity = responseContext.getEntity();
        if(entity instanceof ResultList) {
            @SuppressWarnings("unchecked")
            ResultList<Self> resultList = (ResultList<Self>) entity;
            resultList.value = resultList.value.stream().limit(top).collect(toList());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        int top = 0;
        
        List<String> list = requestContext.getUriInfo().getQueryParameters().get("$top");
        if(list.size() > 1) {
            requestContext.abortWith(Response
                    .status(Status.BAD_REQUEST)
                    .entity("Only one $top parameter may be provided")
                    .build());
        } else if (!list.isEmpty()) {
            String s = list.get(0);
            try {
                top = Integer.parseInt(s);
            } catch (NumberFormatException nfe) {
                requestContext.abortWith(Response
                        .status(Status.BAD_REQUEST)
                        .entity("The $top parameter must be an integer greater than zero")
                        .build());
            }
        }
        
        if(top < 0) {
            requestContext.abortWith(Response
                    .status(Status.BAD_REQUEST)
                    .entity("The $top parameter must be an integer greater than zero")
                    .build());
        }
        
        requestContext.setProperty(TOP_PROP, top);
    }

}

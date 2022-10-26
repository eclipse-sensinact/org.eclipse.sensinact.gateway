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
import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Implements handling of the $count query parameter
 */
@Priority(ENTITY_CODER + 1)
public class CountFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String COUNT_PROP = "org.eclipse.sensinact.sensorthings.sensing.rest.count";

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        Boolean addCount = (Boolean) requestContext.getProperty(COUNT_PROP);
        
        if(addCount) {
            Object entity = responseContext.getEntity();
            if(entity instanceof ResultList) {
                ResultList<?> resultList = (ResultList<?>) entity;
                resultList.count = resultList.value.size();
            }
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        boolean addCount = false;
        
        List<String> list = requestContext.getUriInfo().getQueryParameters().get("$count");
        if(list.size() > 1) {
            requestContext.abortWith(Response
                    .status(Status.BAD_REQUEST)
                    .entity("Only one $count parameter may be provided")
                    .build());
        } else if (!list.isEmpty()) {
            String s = list.get(0);
            if("true".equals(s)) {
                addCount = true;
            } else if(!"false".equals(s)) {
                requestContext.abortWith(Response
                        .status(Status.BAD_REQUEST)
                        .entity("The $count parameter must be \"true\" or \"false\"")
                        .build());
            }
        }
        requestContext.setProperty(COUNT_PROP, addCount);
    }

}

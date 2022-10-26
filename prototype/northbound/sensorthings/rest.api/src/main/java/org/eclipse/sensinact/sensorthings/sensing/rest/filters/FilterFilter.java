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

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Implements handling (in this case rejection of) the $filter parameter
 */
@Priority(ENTITY_CODER)
public class FilterFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        // TODO filter the returned results
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        
        List<String> list = requestContext.getUriInfo().getQueryParameters().get("$filter");
        if (!list.isEmpty()) {
            requestContext.abortWith(Response
                    .status(Status.NOT_IMPLEMENTED)
                    .entity("Filtering is not implemented yet")
                    .build());
        }
    }

}

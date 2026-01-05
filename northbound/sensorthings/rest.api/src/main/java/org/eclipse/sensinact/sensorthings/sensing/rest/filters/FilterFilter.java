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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.rest.access.IFilterConstants;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Implements handling (in this case rejection of) the $filter parameter
 */
@Priority(ENTITY_CODER + 5)
public class FilterFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        List<String> list = requestContext.getUriInfo().getQueryParameters().getOrDefault("$filter", List.of());
        int nbFilters = list.size();
        if (nbFilters == 1) {
            // Store the decoded filter
            final String filter = URLDecoder.decode(list.get(0), StandardCharsets.UTF_8);
            requestContext.setProperty(IFilterConstants.PROP_FILTER_STRING, filter);
        } else if (nbFilters > 1) {
            requestContext.abortWith(Response
                    .status(Status.BAD_REQUEST)
                    .entity("Only one filter can be given at a time")
                    .build());
        }
    }

}

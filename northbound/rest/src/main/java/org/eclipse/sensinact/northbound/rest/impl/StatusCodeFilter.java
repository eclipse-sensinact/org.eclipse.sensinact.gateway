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
package org.eclipse.sensinact.northbound.rest.impl;

import java.io.IOException;

import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;

/**
 * Sets the response status code according to the result one
 */
@Priority(Priorities.HEADER_DECORATOR)
public class StatusCodeFilter implements ContainerResponseFilter {

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
            throws IOException {
        Object entity = responseContext.getEntity();
        if (entity instanceof AbstractResultDTO) {
            final int statusCode = ((AbstractResultDTO) entity).statusCode;
            if (statusCode != 204) {
                responseContext.setStatus(statusCode);
            } else {
                // Special case: use 204 in SensiNact nomenclature (no content in resource)
                // but not in HTTP as the body would be ignored
                responseContext.setStatus(200);
            }
        }
    }
}

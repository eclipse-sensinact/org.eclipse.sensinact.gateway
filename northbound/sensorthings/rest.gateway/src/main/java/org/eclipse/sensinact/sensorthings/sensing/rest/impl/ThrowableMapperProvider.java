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
package org.eclipse.sensinact.sensorthings.sensing.rest.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ThrowableMapperProvider implements ExceptionMapper<Throwable> {

    private static final Logger LOG = LoggerFactory.getLogger(SensinactSensorthingsApplication.class);

    @Override
    public Response toResponse(Throwable e) {
        if (e instanceof WebApplicationException webEx) {
            // Log at WARN instead of ERROR
            LOG.warn("WebApplicationException caught: status {}", webEx.getResponse().getStatus());

            return webEx.getResponse(); // preserve original status
        }
        LOG.error("Unhandled exception while processing request", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal server error").build();
    }
}

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

import org.eclipse.sensinact.sensorthings.sensing.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ThrowableMapperProvider implements ExceptionMapper<Throwable> {

    private static final Logger LOG = LoggerFactory.getLogger(SensinactSensorthingsApplication.class);

    private String getErrorCodeFromStatus(int status) {
        switch (status) {
        case 400:
            return "BadRequest";
        case 404:
            return "NotFound";
        case 405:
            return "MethodNotAllowed";
        case 409:
            return "Conflict";
        case 415:
            return "UnsupportedMediaType";
        case 501:
            return "NotImplemented";
        default:
            return "ServerError";
        }
    }

    @Override
    public Response toResponse(Throwable e) {
        int status = 500;
        if (e instanceof UnrecognizedPropertyException) {
            status = 400;
        } else if (e instanceof WebApplicationException webEx) {
            status = webEx.getResponse().getStatus();
        }

        if (status < 500) {
            LOG.warn("{} caught: message {} status {}", e.getClass().getSimpleName(), e.getMessage(), status);
        } else {
            LOG.error("Exception while processing request", e);
        }

        ErrorResponse error = new ErrorResponse(getErrorCodeFromStatus(status), e.getMessage());
        return Response.status(status).entity(error).build();
    }
}

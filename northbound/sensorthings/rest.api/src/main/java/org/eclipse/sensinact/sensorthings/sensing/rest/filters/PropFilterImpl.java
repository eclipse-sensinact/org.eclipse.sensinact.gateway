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

import java.io.IOException;
import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PropFilter;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

/**
 * This filter restricts the serialized output to contain only the named property, or property value
 */
@PropFilter
public class PropFilterImpl implements WriterInterceptor {

    @Context
    UriInfo uriInfo;

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        Object entity = context.getEntity();

        String propName = uriInfo.getPathParameters().getFirst("prop");
        if(propName == null || propName.isEmpty()) {
            throw new BadRequestException("Invalid property filter");
        }

        boolean rawValue = uriInfo.getPath().endsWith("/$value");

        try {
            Object prop = entity.getClass().getField(propName).get(entity);
            context.setEntity(rawValue ? prop : Map.of(propName, prop));
        } catch (Exception e) {
            throw new BadRequestException("Failed to locate property " + propName, e);
        }

        context.proceed();
    }
}

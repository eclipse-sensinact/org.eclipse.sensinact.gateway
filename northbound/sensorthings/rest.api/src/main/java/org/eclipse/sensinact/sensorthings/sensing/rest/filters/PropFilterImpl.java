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
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.rest.annotation.PropFilter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Providers;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

/**
 * This filter restricts the serialized output to contain only the named
 * property, or property value
 */
@PropFilter
public class PropFilterImpl implements WriterInterceptor {

    @Context
    UriInfo uriInfo;

    @Context
    Providers providers;

    private ObjectMapper getMapper() {
        return providers.getContextResolver(ObjectMapper.class, MediaType.WILDCARD_TYPE).getContext(null);
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        Object entity = context.getEntity();

        String propName = uriInfo.getPathParameters().getFirst("prop");
        if (propName == null || propName.isEmpty()) {
            throw new BadRequestException("Invalid property filter");
        }

        boolean rawValue = uriInfo.getPath().endsWith("/$value");

        try {
            Object prop;
            if (entity instanceof Record) {
                RecordComponent[] components = entity.getClass().getRecordComponents();
                RecordComponent component = Arrays.stream(components).filter(rc -> rc.getName().equals(propName))
                        .findFirst().get();
                prop = component.getAccessor().invoke(entity);
            } else if (entity instanceof JsonNode jn) {
                if (jn.has(propName)) {
                    prop = jn.get(propName);
                } else {
                    throw new IllegalArgumentException("No property " + propName + " in object " + jn);
                }
            } else {
                JsonNode jn = getMapper().convertValue(entity, JsonNode.class);
                if (jn.has(propName)) {
                    prop = jn.get(propName);
                } else {
                    throw new IllegalArgumentException("No property " + propName + " in object " + jn);
                }
            }

            context.setEntity(rawValue ? prop : Map.of(propName, prop));
            System.out.println(context.getEntity().toString());
        } catch (Exception e) {
            if (rawValue) {
                Map<String, Object> map = new HashMap<>();
                map.put(propName, null);
                context.setEntity(map);
            } else {
                context.setEntity(null);
            }

        }

        context.proceed();
    }
}

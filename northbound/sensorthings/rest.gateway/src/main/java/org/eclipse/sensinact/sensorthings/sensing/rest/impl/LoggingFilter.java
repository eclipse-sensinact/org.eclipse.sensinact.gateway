/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonInclude;

@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = LoggerFactory.getLogger(SensinactSensorthingsApplication.class);
    private static ObjectMapper MAPPER;

    private static ObjectMapper getMapper() {
        if (MAPPER == null) {
            MAPPER = new ObjectMapper();
            MAPPER.registerModule(new JavaTimeModule());
            MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        }
        return MAPPER;
    }

    @Override
    public void filter(ContainerRequestContext request) throws IOException {
        try {
            // Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if (LOG.isinfoEnabled()) {

        LOG.info("{} - Query     {} : {}", Instant.now().toString(), request.getMethod(),
                request.getUriInfo().getRequestUri());

        if (request.hasEntity()) {
            String body = readStream(request.getEntityStream());
            LOG.info("{} - Body : {}", Instant.now().toString(), body);

            request.setEntityStream(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        }
//        }
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
//        if (LOG.isinfoEnabled()) {
        LOG.info("{} - Status  : {}", Instant.now().toString(), response.getStatus());

        if (response.getEntity() != null) {
            try {
                String json = getMapper().writeValueAsString(response.getEntity());
                LOG.info("{} - Body    {} : {}", Instant.now().toString(), response.getStatus(), json);
            } catch (Exception e) {
                LOG.info("Could not serialize response entity to JSON", e);
                LOG.info("{} - Body    {} : {}", Instant.now().toString(), response.getStatus(), response.getEntity());
            }
        }
//        }
    }

    private String readStream(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}

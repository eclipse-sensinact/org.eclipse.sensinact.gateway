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

@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = LoggerFactory.getLogger(SensinactSensorthingsApplication.class);
    private static ObjectMapper MAPPER;

    private static ObjectMapper getMapper() {
        if (MAPPER == null) {
            MAPPER = new ObjectMapper();
            MAPPER.registerModule(new JavaTimeModule());
        }
        return MAPPER;
    }

    @Override
    public void filter(ContainerRequestContext request) throws IOException {
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        LOG.info("{} - Method  : {}", Instant.now().toString(), request.getMethod());
        LOG.info("{} - URI     : {}", Instant.now().toString(), request.getUriInfo().getRequestUri());
        // LOG.info("Headers : {}", request.getHeaders());

        if (request.hasEntity()) {
            String body = readStream(request.getEntityStream());
            LOG.info("{} - Body : {}", Instant.now().toString(), body);

            // reset stream for endpoint consumption
            request.setEntityStream(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        }
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        LOG.info("{} - Status  : {}", Instant.now().toString(), response.getStatus());
        // LOG.info("Headers : {}", response.getHeaders());

        if (response.getEntity() != null) {
            try {
                // Convert entity to JSON string for logging
                String json = getMapper().writeValueAsString(response.getEntity());
                LOG.info("{} - Body    : {}", Instant.now().toString(), json);
            } catch (Exception e) {
                LOG.warn("Could not serialize response entity to JSON", e);
                LOG.info("{} - Body    : {}", Instant.now().toString(), response.getEntity());
            }
        }
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

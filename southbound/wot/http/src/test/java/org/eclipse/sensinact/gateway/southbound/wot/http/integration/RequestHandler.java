/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.gateway.southbound.wot.http.integration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HTTP request handler for tests
 */
public class RequestHandler extends AbstractHandler {

    public static interface CustomHandler {
        Object handle(String path, Map<String, Object> args) throws Exception;
    }

    final ObjectMapper mapper = JsonMapper.builder().build();

    private final Map<String, CustomHandler> serverHandlers = new HashMap<>();
    private final Map<String, AtomicInteger> serverVisits = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Instant> lastVisitTimes = Collections.synchronizedMap(new HashMap<>());

    public void setHandler(final String path, final CustomHandler handler) {
        serverHandlers.put(path, handler);
    }

    public void clear() {
        serverHandlers.clear();
        serverVisits.clear();
        lastVisitTimes.clear();
    }

    public int nbVisitedPaths() {
        return serverVisits.size();
    }

    public int nbVisits(final String path) {
        return serverVisits.getOrDefault(path, new AtomicInteger()).get();
    }

    public Instant lastVisitTime(final String path) {
        return lastVisitTimes.get(path);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        baseRequest.setHandled(true);

        lastVisitTimes.put(target, Instant.now());

        CustomHandler handler = serverHandlers.get(target);
        if (handler != null) {
            serverVisits.computeIfAbsent(target, (k) -> new AtomicInteger()).incrementAndGet();
            try {
                Map<String, Object> args = null;
                if (baseRequest.getMethod().equals(HttpMethod.POST.asString())) {
                    args = mapper.readValue(baseRequest.getInputStream(), new TypeReference<Map<String, Object>>() {
                    });
                }
                Object result = handler.handle(target, args);
                final String strResult = mapper.writeValueAsString(result);
                response.setStatus(200);
                response.setContentLength(strResult.length());
                response.getOutputStream().write(strResult.getBytes(StandardCharsets.UTF_8));
                response.flushBuffer();
            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(500);
            }
        } else {
            response.sendError(404);
        }
    }
}

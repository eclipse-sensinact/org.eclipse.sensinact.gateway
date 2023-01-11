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
package org.eclipse.sensinact.gateway.southbound.http.factory.integration;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HTTP request handler for tests
 */
public class RequestHandler extends AbstractHandler {

    private final Map<String, byte[]> serverData = new HashMap<>();
    private final Map<String, Integer> queryPause = new HashMap<>();
    private final Map<String, AtomicInteger> serverVisits = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, Instant> lastVisitTimes = Collections.synchronizedMap(new HashMap<>());

    public void setData(final String path, final byte[] data) {
        serverData.put(path, data);
    }

    public void setData(final String path, final String data) {
        serverData.put(path, data.getBytes());
    }

    public void setPause(final String path, final int pause) {
        if (pause <= 0) {
            queryPause.remove(path);
        } else {
            queryPause.put(path, pause);
        }
    }

    public void clear() {
        serverData.clear();
        serverVisits.clear();
        queryPause.clear();
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

        try {
            baseRequest.authenticate(response);
        } catch (ServletException e) {
            response.sendError(401);
            return;
        }

        lastVisitTimes.put(target, Instant.now());

        Integer pause = queryPause.get(target);
        if (pause != null) {
            try {
                Thread.sleep(pause);
            } catch (InterruptedException e) {
                response.sendError(500);
                return;
            }
        }

        byte[] content = serverData.get(target);
        if (content != null) {
            serverVisits.computeIfAbsent(target, (k) -> new AtomicInteger()).incrementAndGet();
            response.setStatus(200);
            response.setContentLength(content.length);
            response.getOutputStream().write(content);
            response.flushBuffer();
        } else {
            response.sendError(404);
        }
    }
}

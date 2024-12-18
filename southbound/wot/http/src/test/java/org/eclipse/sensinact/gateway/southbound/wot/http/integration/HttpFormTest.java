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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.gateway.southbound.wot.api.Thing;
import org.eclipse.sensinact.gateway.southbound.wot.api.constants.WoTConstants;
import org.eclipse.sensinact.gateway.southbound.wot.api.handlers.ThingManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

public class HttpFormTest {
    static QueuedThreadPool threadPool;
    static Server server;
    static RequestHandler handler;
    static int httpPort;

    @InjectService
    GatewayThread thread;

    @InjectService
    ThingManager manager;

    @BeforeAll
    static void setup() throws Exception {
        threadPool = new QueuedThreadPool();
        threadPool.setName("server");
        handler = new RequestHandler();
        server = new Server(threadPool);
        ServerConnector conn = new ServerConnector(server);
        conn.setPort(0);
        server.addConnector(conn);
        server.setHandler(handler);
        server.start();
        httpPort = conn.getLocalPort();
    }

    @AfterAll
    static void teardown() throws Exception {
        server.stop();
        server = null;
        threadPool.stop();
        threadPool = null;
    }

    /**
     * Reads a file from the test folder
     *
     * @param filename File name
     * @return File content as string
     * @throws Exception Error reading file
     */
    String readFile(final String filename) throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream("/data/" + filename)) {
            if (inputStream == null) {
                throw new FileNotFoundException(filename);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    <T> T runWoTInThread(String provider, String resource,
            BiFunction<SensinactResource, PromiseFactory, Promise<T>> callable) throws Exception {
        return thread.execute(new AbstractTwinCommand<T>() {
            @Override
            protected Promise<T> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                try {
                    SensinactResource rc = twin.getResource(provider, WoTConstants.WOT_SERVICE, resource);
                    if (rc == null) {
                        return pf.failed(new AssertionError("Resource not found: "
                                + String.join("/", provider, WoTConstants.WOT_SERVICE, resource)));
                    }
                    return callable.apply(rc, pf);
                } catch (Exception e) {
                    return pf.failed(e);
                }
            }
        }).getValue();
    }

    @Test
    void testHttpFormHandler() throws Exception {
        final String rawContent = readFile("test.td.jsonld").replace("{{HTTP}}", "http://localhost:" + httpPort);
        final Thing thing = handler.mapper.readValue(rawContent, Thing.class);
        final String providerName = manager.registerThing(thing);

        handler.setHandler("/status", (t, a) -> a == null ? "test-status" : "NON-NULL ARGS");

        handler.setHandler("/echo", (t, a) -> {
            if (a == null) {
                return "No args";
            } else if (a.size() != 1) {
                return "Arg size: " + a.size();
            }

            return "echo:" + a.values().iterator().next();
        });

        handler.setHandler("/add", (t, a) -> {
            if (a == null) {
                return "No args";
            } else if (a.size() != 2) {
                return "Arg size: " + a.size() + " - " + a;
            }

            final Map<String, Long> typedArgs = a.entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, e -> (Long) e.getValue()));
            return Map.of("result", typedArgs.get("a") + typedArgs.get("b"));
        });

        assertEquals("test-status",
                runWoTInThread(providerName, "status", (rc, pf) -> rc.getValue(String.class).map(tv -> tv.getValue())));

        assertEquals("echo:ping", runWoTInThread(providerName, "echo",
                (rc, pf) -> rc.act(Map.of(WoTConstants.DEFAULT_ARG_NAME, "ping")).map(String.class::cast)));

        assertEquals(42L, (Long) runWoTInThread(providerName, "add",
                (rc, pf) -> rc.act(Map.of("a", 20, "b", 22))));
    }
}

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.security.UserInfo;
import org.eclipse.sensinact.northbound.session.ResourceDescription;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectService;

/**
 * Tests the HTTP device factory behavior with parallel calls
 */
public class HttpDeviceFactoryParallelQueries {

    static final String TEMPLATE = "Name,Value\n%s,%s\n";
    static QueuedThreadPool threadPool;
    static Server server1, server2;
    static int httpPort1, httpPort2;
    static RequestHandler handler1, handler2;

    @InjectService
    SensiNactSessionManager sessionManager;
    SensiNactSession session;

    @InjectService
    ConfigurationAdmin configAdmin;

    BlockingQueue<ResourceDataNotification> queue1, queue2;

    @BeforeAll
    static void setup() throws Exception {
        threadPool = new QueuedThreadPool();
        threadPool.setName("test-server");

        server1 = new Server(threadPool);
        ServerConnector conn1 = new ServerConnector(server1);
        conn1.setPort(0);
        server1.addConnector(conn1);
        handler1 = new RequestHandler();
        server1.setHandler(handler1);
        server1.start();
        httpPort1 = conn1.getLocalPort();

        server2 = new Server(threadPool);
        ServerConnector conn2 = new ServerConnector(server2);
        conn2.setPort(0);
        server2.addConnector(conn2);
        handler2 = new RequestHandler();
        server2.setHandler(handler2);
        server2.start();
        httpPort2 = conn2.getLocalPort();
    }

    @AfterAll
    static void teardown() throws Exception {
        server1.stop();
        server1 = null;
        server2.stop();
        server2 = null;
        threadPool.stop();
        threadPool = null;
    }

    @BeforeEach
    void start() throws InterruptedException {
        session = sessionManager.getDefaultSession(UserInfo.ANONYMOUS);
        queue1 = new ArrayBlockingQueue<>(32);
        queue2 = new ArrayBlockingQueue<>(32);
    }

    @AfterEach
    void stop() {
        session.activeListeners().keySet().forEach(session::removeListener);
        session = null;
        handler1.clear();
        handler2.clear();
    }

    /**
     * Opens the given file from resources
     */
    byte[] readFile(final String filename) throws IOException {
        try (InputStream inStream = getClass().getClassLoader().getResourceAsStream("/" + filename)) {
            return inStream.readAllBytes();
        }
    }

    /**
     * Tests a configuration with 2 sources and ensure they are executed in parallel
     */
    @Test
    void testParallelQuery() throws Exception {
        // Setup server
        final String provider1 = "http-parallel-query1";
        final String provider2 = "http-parallel-query2";
        final int value1 = 42;
        final int value2 = 21;

        handler1.setData("/data", String.format(TEMPLATE, provider1, value1));
        handler1.setPause("/data", 600);
        handler2.setData("/data", String.format(TEMPLATE, provider2, value2));
        handler2.setPause("/data", 800);

        // Register listener
        session.addListener(List.of(provider1 + "/*"), (t, e) -> queue1.offer(e), null, null, null);
        session.addListener(List.of(provider2 + "/*"), (t, e) -> queue2.offer(e), null, null, null);

        // Providers shouldn't exist yet
        assertNull(session.describeProvider(provider1));
        assertNull(session.describeProvider(provider2));

        final String mappingConfig = new String(readFile("csv-station-dynamic-mapping.json"));
        Configuration config = configAdmin.createFactoryConfiguration("sensinact.http.device.factory", "?");
        try {
            final Instant start = Instant.now();
            config.update(new Hashtable<>(Map.of("tasks.oneshot",
                    "[{\"url\": \"http://localhost:" + httpPort1 + "/data\", \"mapping\": " + mappingConfig + "},"
                            + "{\"url\": \"http://localhost:" + httpPort2 + "/data\", \"mapping\": " + mappingConfig
                            + "}]")));
            // Wait for the providers to appear
            assertNotNull(queue1.poll(2, TimeUnit.SECONDS));
            assertNotNull(queue2.poll(2, TimeUnit.SECONDS));

            final ResourceDescription rc1 = session.describeResource(provider1, "data", "value");
            final ResourceDescription rc2 = session.describeResource(provider2, "data", "value");

            assertTrue(rc1.timestamp.isAfter(start));
            assertTrue(rc1.timestamp.isAfter(handler1.lastVisitTime("/data")));
            assertTrue(rc2.timestamp.isAfter(rc1.timestamp));
            assertTrue(rc2.timestamp.isAfter(handler2.lastVisitTime("/data")));

            // Requests must have been handled around the same time (less than the pause of
            // the first handler)
            assertTrue(Duration.between(handler1.lastVisitTime("/data"), handler2.lastVisitTime("/data")).abs()
                    .toMillis() < 400);

            assertFalse(rc2.timestamp.isAfter(start.plus(1200, ChronoUnit.MILLIS)));

            assertEquals(42, rc1.value);
            assertEquals(21, rc2.value);

            assertEquals(1, handler1.nbVisits("/data"));
            assertEquals(1, handler2.nbVisits("/data"));
        } finally {
            config.delete();
        }
    }
}

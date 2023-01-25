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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.prototype.ResourceDescription;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.eclipse.sensinact.prototype.notification.ResourceDataNotification;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectService;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests the HTTP device factory
 */
public class HttpDeviceFactoryTest {

    static QueuedThreadPool threadPool;
    static Server server;
    static RequestHandler handler;
    static int httpPort;

    final ObjectMapper mapper = new ObjectMapper();

    @InjectService
    SensiNactSessionManager sessionManager;
    SensiNactSession session;
    BlockingQueue<ResourceDataNotification> queue, queue2;

    @InjectService
    ConfigurationAdmin configAdmin;

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

    @BeforeEach
    void start() throws InterruptedException {
        session = sessionManager.getDefaultSession("user");
        queue = new ArrayBlockingQueue<>(32);
        queue2 = new ArrayBlockingQueue<>(32);
    }

    @AfterEach
    void stop() {
        session.activeListeners().keySet().forEach(session::removeListener);
        session = null;
        handler.clear();
    }

    void setupProvidersHandling(final String provider1, final String provider2) {
        assertNull(session.describeProvider(provider1));
        session.addListener(List.of(provider1 + "/*"), (t, e) -> queue.offer(e), null, null, null);

        if (provider2 != null) {
            assertNull(session.describeProvider(provider2));
            session.addListener(List.of(provider1 + "/*"), (t, e) -> queue2.offer(e), null, null, null);
        }
    }

    /**
     * Opens the given file from resources
     */
    byte[] readFile(final String filename) throws IOException {
        try (InputStream inStream = getClass().getClassLoader().getResourceAsStream("/" + filename)) {
            return inStream.readAllBytes();
        }
    }

    @Test
    void testSimpleTask() throws Exception {
        // Excepted providers
        final String provider1 = "typed-provider1";
        final String provider2 = "typed-provider2";

        // Register listener
        setupProvidersHandling(provider1, provider2);

        final String inputFileName = "csv-header-typed";
        final String mappingConfig = new String(readFile(inputFileName + "-mapping.json"));
        handler.setData("/data", readFile(inputFileName + ".csv"));

        Configuration config = configAdmin.createFactoryConfiguration("sensinact.http.device.factory", "?");
        try {
            config.update(new Hashtable<>(Map.of("tasks.oneshot",
                    "[{\"url\": \"http://localhost:" + httpPort + "/data\", \"mapping\": " + mappingConfig + "}]")));
            // Wait for the providers to appear
            assertNotNull(queue.poll(1, TimeUnit.SECONDS));
            assertNotNull(queue2.poll(1, TimeUnit.SECONDS));

            // Ensure resource type
            assertEquals(42, session.getResourceValue(provider1, "data", "value", Integer.class));
            assertEquals(84, session.getResourceValue(provider2, "data", "value", Integer.class));

            // Ensure timestamp
            Instant timestamp1 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 14, 0).atOffset(ZoneOffset.UTC));
            assertEquals(timestamp1, session.describeResource(provider1, "data", "value").timestamp);

            Instant timestamp2 = Instant.from(LocalDateTime.of(2021, 10, 20, 18, 17, 0).atOffset(ZoneOffset.UTC));
            assertEquals(timestamp2, session.describeResource(provider2, "data", "value").timestamp);

            // Ensure location update (and its timestamp)
            ResourceDescription location1 = session.describeResource(provider1, "admin", "location");
            assertEquals(timestamp1, location1.timestamp);
            assertNotNull(location1.value);
            Point geoPoint = (Point) location1.value;
            assertEquals(1.2, geoPoint.coordinates.latitude, 0.001);
            assertEquals(3.4, geoPoint.coordinates.longitude, 0.001);
            assertTrue(Double.isNaN(geoPoint.coordinates.elevation));

            ResourceDescription location2 = session.describeResource(provider2, "admin", "location");
            assertNotNull(location2.value);
            assertEquals(timestamp2, location2.timestamp);
            geoPoint = (Point) location2.value;
            assertEquals(5.6, geoPoint.coordinates.latitude, 0.001);
            assertEquals(7.8, geoPoint.coordinates.longitude, 0.001);
            assertTrue(Double.isNaN(geoPoint.coordinates.elevation));

            // Only 1 call should have been made
            assertEquals(1, handler.nbVisitedPaths());
            assertEquals(1, handler.nbVisits("/data"));
        } finally {
            config.delete();
        }
    }

    @Test
    void testPeriodicTask() throws Exception {
        // Excepted providers
        final String provider1 = "dynamic-provider1";
        final String provider2 = "dynamic-provider2";

        // Register listener
        setupProvidersHandling(provider1, provider2);

        final String inputFileName = "csv-header-dynamic";
        final String template = new String(readFile(inputFileName + ".csv"));
        final String mappingConfig = new String(readFile(inputFileName + "-mapping.json"));

        String content = template.replace("$val1$", "1").replace("$val2$", "2");
        handler.setData("/data", content);

        Configuration config = configAdmin.createFactoryConfiguration("sensinact.http.device.factory", "?");
        try {
            // Get initial timestamp
            final Instant start = Instant.now();

            // Use a 2-seconds period
            config.update(new Hashtable<>(Map.of("tasks.periodic", "[{\"period\": 2, \"url\": \"http://localhost:"
                    + httpPort + "/data\", \"mapping\": " + mappingConfig + "}]")));

            // Wait for the provider to appear
            assertNotNull(queue.poll(1, TimeUnit.SECONDS));

            // 1 call should have been made yet
            assertEquals(1, handler.nbVisitedPaths());
            assertEquals(1, handler.nbVisits("/data"));

            // Check timestamp (after or equal to start)
            final Instant firstTimestamp = session.describeResource(provider1, "data", "value").timestamp;
            assertFalse(firstTimestamp.isBefore(start));

            // Ensure resource value
            assertEquals(1, session.getResourceValue(provider1, "data", "value", Integer.class));
            assertEquals(2, session.getResourceValue(provider2, "data", "value", Integer.class));

            // Clear the queue
            queue.clear();
            queue2.clear();

            // Update value
            content = template.replace("$val1$", "10").replace("$val2$", "20");
            handler.setData("/data", content);

            // Wait for an update (wait 4 seconds to be fair with the 2-seconds poll)
            assertNotNull(queue.poll(4, TimeUnit.SECONDS));
            assertNotNull(queue2.poll(1, TimeUnit.SECONDS));

            // Check timestamp
            final Instant secondTimestamp = session.describeResource(provider1, "data", "value").timestamp;
            assertTrue(secondTimestamp.isAfter(firstTimestamp.plus(1, ChronoUnit.SECONDS)));

            // Ensure resource value
            assertEquals(10, session.getResourceValue(provider1, "data", "value", Integer.class));
            assertEquals(20, session.getResourceValue(provider2, "data", "value", Integer.class));

            // 2 calls should have been made
            assertEquals(1, handler.nbVisitedPaths());
            assertEquals(2, handler.nbVisits("/data"));
        } finally {
            config.delete();
        }
    }

    @Test
    void testCombined() throws Exception {
        // Excepted providers
        final String provider1 = "station1";
        final String provider2 = "station2";

        // Register listener
        setupProvidersHandling(provider1, provider2);

        final String staticInputFileName = "csv-station-static";
        handler.setData("/static", readFile(staticInputFileName + ".csv"));
        final String staticMappingConfig = new String(readFile(staticInputFileName + "-mapping.json"));

        final String dynamicInputFileName = "csv-station-dynamic";
        final String template = new String(readFile(dynamicInputFileName + ".csv"));
        final String dynamicMappingConfig = new String(readFile(dynamicInputFileName + "-mapping.json"));
        handler.setData("/dynamic", template.replace("$val1$", "21").replace("$val2$", "42"));

        Configuration config = configAdmin.createFactoryConfiguration("sensinact.http.device.factory", "?");
        try {
            // Get initial timestamp
            final Instant start = Instant.now();

            // Use a 2-seconds period
            config.update(new Hashtable<>(Map.of("tasks.oneshot",
                    "[{\"url\": \"http://localhost:" + httpPort + "/static\", \"mapping\": " + staticMappingConfig
                            + "}]",
                    "tasks.periodic", "[{\"period\": 2, \"url\": \"http://localhost:" + httpPort
                            + "/dynamic\", \"mapping\": " + dynamicMappingConfig + "}]")));

            // Wait for the provider to fully appear
            boolean gotLocation = false;
            boolean gotValue = false;
            while (!gotLocation || !gotValue) {
                final ResourceDataNotification notif = queue.poll(1, TimeUnit.SECONDS);
                assertNotNull(notif);
                switch (notif.resource) {
                case "value":
                    gotValue = true;
                    break;

                case "location":
                    gotLocation = true;
                    break;

                default:
                    if (Duration.between(start, Instant.now()).toMillis() > 1500) {
                        // Too slow
                        fail("Timeout waiting for updates");
                    }
                    break;
                }
            }
            assertNotNull(queue2.poll(1, TimeUnit.SECONDS));

            // 2 calls should have been made yet
            assertEquals(2, handler.nbVisitedPaths());
            assertEquals(1, handler.nbVisits("/static"));
            assertEquals(1, handler.nbVisits("/dynamic"));

            // Check timestamp (after or equal to start)
            final Instant firstValueTimestamp = session.describeResource(provider1, "data", "value").timestamp;
            assertFalse(firstValueTimestamp.isBefore(start));

            // Ensure resource value
            assertEquals(21, session.getResourceValue(provider1, "data", "value", Integer.class));
            assertEquals(42, session.getResourceValue(provider2, "data", "value", Integer.class));

            // Ensure location
            ResourceDescription location1 = session.describeResource(provider1, "admin", "location");
            final Instant firstLocationTimestamp = location1.timestamp;
            assertNotNull(location1.value);
            Point geoPoint = (Point) location1.value;
            assertEquals(45.185, geoPoint.coordinates.latitude, 0.001);
            assertEquals(5.735, geoPoint.coordinates.longitude, 0.001);
            assertTrue(Double.isNaN(geoPoint.coordinates.elevation));

            // Update returned value
            handler.setData("/dynamic", template.replace("$val1$", "38").replace("$val2$", "15"));

            // Clear the queue to wait for next event
            queue.clear();
            queue2.clear();

            // Wait for an update (wait 4 seconds to be fair with the 2-seconds poll)
            assertNotNull(queue.poll(4, TimeUnit.SECONDS));
            assertNotNull(queue2.poll(1, TimeUnit.SECONDS));

            // Check timestamp
            final Instant secondTimestamp = session.describeResource(provider1, "data", "value").timestamp;
            assertTrue(secondTimestamp.isAfter(firstValueTimestamp.plus(1, ChronoUnit.SECONDS)));

            // Ensure resource value
            assertEquals(38, session.getResourceValue(provider1, "data", "value", Integer.class));
            assertEquals(15, session.getResourceValue(provider2, "data", "value", Integer.class));

            // Ensure that the locations weren't updated
            assertNotEquals(firstLocationTimestamp, secondTimestamp);
            assertEquals(firstLocationTimestamp, session.describeResource(provider1, "admin", "location").timestamp);

            // Static shouldn't be rechecked, dynamic should have 1 more call
            assertEquals(2, handler.nbVisitedPaths());
            assertEquals(1, handler.nbVisits("/static"));
            assertEquals(2, handler.nbVisits("/dynamic"));
        } finally {
            config.delete();
        }
    }
}

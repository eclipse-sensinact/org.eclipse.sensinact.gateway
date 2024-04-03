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
import java.time.temporal.ChronoUnit;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.security.Password;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.ResourceDescription;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.namespace.service.ServiceNamespace;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectService;

/**
 * Tests the HTTP device factory with authentication
 */
@Requirement(namespace = ServiceNamespace.SERVICE_NAMESPACE, filter = "(objectClass=org.eclipse.sensinact.northbound.session.SensiNactSessionManager)")
public class HttpDeviceFactoryAuthTest {

    static QueuedThreadPool threadPool;
    static Server server;
    static RequestHandler handler;
    static int httpPort;

    @InjectService
    SensiNactSessionManager sessionManager;
    SensiNactSession session;
    BlockingQueue<ResourceDataNotification> queue;

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
        final ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        security.setAuthenticator(new BasicAuthenticator());
        final UserStore users = new UserStore();
        users.addUser("user1", new Password("pass1"), new String[0]);
        users.addUser("user2", new Password("pass2"), new String[0]);
        final HashLoginService loginService = new HashLoginService();
        loginService.setUserStore(users);
        security.setLoginService(loginService);
        security.setHandler(handler);
        server.setHandler(security);
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
        session = sessionManager.getDefaultSession(UserInfo.ANONYMOUS);
        queue = new ArrayBlockingQueue<>(32);
    }

    @AfterEach
    void stop() {
        session.activeListeners().keySet().forEach(session::removeListener);
        session = null;
        handler.clear();
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
    void testCombined() throws Exception {
        // Excepted providers
        final String providerBase = "auth-station";
        final String provider1 = providerBase + "1";
        final String provider2 = providerBase + "2";

        // Register listener
        session.addListener(List.of(provider1 + "/*"), (t, e) -> queue.offer(e), null, null, null);

        // Providers shouldn't exist yet
        assertNull(session.describeProvider(provider1));
        assertNull(session.describeProvider(provider2));

        final String staticInputFileName = "csv-station-static";
        handler.setData("/static", new String(readFile(staticInputFileName + ".csv")).replace("station", providerBase));
        final String staticMappingConfig = new String(readFile(staticInputFileName + "-mapping.json"));

        final String dynamicInputFileName = "csv-station-dynamic";
        final String template = new String(readFile(dynamicInputFileName + ".csv")).replace("station", providerBase);
        final String dynamicMappingConfig = new String(readFile(dynamicInputFileName + "-mapping.json"));
        handler.setData("/dynamic", template.replace("$val1$", "21").replace("$val2$", "42"));

        // Ensure we need authentication to get the values
        HttpClient clt = new HttpClient();
        clt.start();
        try {
            assertEquals(401, clt.newRequest("http://localhost:" + httpPort + "/static").send().getStatus());
            assertEquals(401, clt.newRequest("http://localhost:" + httpPort + "/dynamic").send().getStatus());
        } finally {
            clt.stop();
        }

        Configuration config = configAdmin.createFactoryConfiguration("sensinact.http.device.factory", "?");
        try {
            // Get initial timestamp
            final Instant start = Instant.now();

            // Use a 2-seconds period
            config.update(new Hashtable<>(Map.of("tasks.oneshot",
                    "[{\"url\": \"http://localhost:" + httpPort + "/static\", \"mapping\": " + staticMappingConfig
                            + ", \"auth.user\": \"user1\", \"auth.password\": \"pass1\"}]",
                    "tasks.periodic",
                    "[{\"period\": 2, \"period.unit\": \"SECONDS\", \"url\": \"http://localhost:" + httpPort
                            + "/dynamic\", \"mapping\": " + dynamicMappingConfig
                            + ", \"auth.user\": \"user2\", \"auth.password\": \"pass2\"}]")));

            // Wait for the provider to fully appear
            boolean gotLocation = false;
            boolean gotValue = false;
            while (!gotLocation || !gotValue) {
                final ResourceDataNotification notif = queue.poll(2, TimeUnit.SECONDS);
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

            // Update returned value
            handler.setData("/dynamic", template.replace("$val1$", "38").replace("$val2$", "15"));

            // Clear the queue to wait for next event
            queue.clear();

            // Wait for an update (wait 4 seconds to be fair with the 2-seconds poll)
            assertNotNull(queue.poll(4, TimeUnit.SECONDS));

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

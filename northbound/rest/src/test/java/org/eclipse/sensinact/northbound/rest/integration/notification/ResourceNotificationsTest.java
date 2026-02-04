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
package org.eclipse.sensinact.northbound.rest.integration.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.sensinact.core.notification.LifecycleNotification;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.northbound.query.dto.notification.ResourceDataNotificationDTO;
import org.eclipse.sensinact.northbound.query.dto.notification.ResourceLifecycleNotificationDTO;
import org.eclipse.sensinact.northbound.rest.integration.TestUtils;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.osgi.service.cm.Configuration;
import org.osgi.service.jakartars.client.SseEventSourceFactory;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.sse.SseEventSource;

@WithConfiguration(pid = "sensinact.session.manager", properties = {
        @Property(key = "auth.policy", value = "ALLOW_ALL"),
        @Property(key = "expiry", value = "" + ResourceNotificationsTest.SESSION_EXPIRY_SECONDS),
        @Property(key = "activity.check.interval", value = ""
                + ResourceNotificationsTest.SESSION_ACTIVITY_INTERVAL_SECONDS),
        @Property(key = "activity.check.extension", value = ""
                + ResourceNotificationsTest.SESSION_ACTIVITY_EXTENSION_SECONDS),
        @Property(key = "activity.check.threshold", value = "1"),
        @Property(key = "name", value = "test-session"),
})
public class ResourceNotificationsTest {

    public static final int SESSION_EXPIRY_SECONDS = 3;
    public static final int SESSION_ACTIVITY_INTERVAL_SECONDS = 1;
    public static final int SESSION_ACTIVITY_EXTENSION_SECONDS = 3;

    private static final Logger logger = LoggerFactory.getLogger(ResourceNotificationsTest.class);

    @BeforeEach
    public void await(
            @InjectConfiguration(withConfig = @WithConfiguration(pid = "sensinact.northbound.rest", location = "?", properties = {
                    @Property(key = "allow.anonymous", value = "true"),
                    @Property(key = "buzz", value = "fizzbuzz") })) Configuration cm,
            @InjectService(filter = "(buzz=fizzbuzz)", cardinality = 0) ServiceAware<Application> a)
            throws InterruptedException {
        a.waitForService(5000);
        for (int i = 0; i < 10; i++) {
            try {
                if (utils.queryStatus("/").statusCode() == 200)
                    return;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(200);
        }
        throw new AssertionFailedError("REST API did not appear");
    }

    private static final UserInfo USER = UserInfo.ANONYMOUS;

    private static final String PROVIDER = "RestNotificationProvider";
    private static final String PROVIDER_TOPIC = PROVIDER + "/" + PROVIDER + "/*";

    @InjectService
    protected SseEventSourceFactory sseClient;

    @InjectService(filter = "(name=test-session)", timeout = 1000)
    SensiNactSessionManager sessionManager;

    @InjectService
    DataUpdate push;

    @InjectService
    ClientBuilder clientBuilder;

    BlockingQueue<ResourceDataNotification> queue;

    final TestUtils utils = new TestUtils();

    @BeforeEach
    void start() throws InterruptedException {
        queue = new ArrayBlockingQueue<>(32);
        SensiNactSession session = sessionManager.getDefaultSession(USER);
        session.addListener(List.of(PROVIDER_TOPIC), (t, e) -> queue.offer(e), null, null, null);
        assertNull(queue.poll(500, TimeUnit.MILLISECONDS));
    }

    @AfterEach
    void stop() {
        SensiNactSession session = sessionManager.getDefaultSession(USER);
        session.activeListeners().keySet().forEach(session::removeListener);
    }

    /**
     * Check resource creation & update notification
     */
    @Test
    void resourceNotificationNonExistent() throws Exception {

        final String service = "new";
        final String resource = "new-resource";

        // Subscribe to a non-existent resource
        final Client client = clientBuilder.connectTimeout(3, TimeUnit.SECONDS).register(JacksonJsonProvider.class)
                .build();
        final SseEventSource sseSource = sseClient
                .newSource(client.target("http://localhost:8185/sensinact/").path("providers").path(PROVIDER)
                        .path("services").path(service).path("resources").path(resource).path("SUBSCRIBE"));

        final BlockingArrayQueue<ResourceLifecycleNotificationDTO> lifeCycleEvents = new BlockingArrayQueue<>();
        final BlockingArrayQueue<ResourceDataNotificationDTO> dataEvents = new BlockingArrayQueue<>();
        sseSource.register(ise -> {
            switch (ise.getName()) {
                case "lifecycle":
                    lifeCycleEvents.add(ise.readData(ResourceLifecycleNotificationDTO.class));
                    break;

                case "data":
                    dataEvents.add(ise.readData(ResourceDataNotificationDTO.class));
                    break;

                default:
                    break;
            }
        });
        sseSource.open();

        try {
            // Register the resource
            int initialValue = 42;
            GenericDto dto = utils.makeDto(PROVIDER, service, resource, initialValue, Integer.class);
            push.pushUpdate(dto);

            // Wait for it locally
            // First will be admin description
            ResourceDataNotification description = queue.poll(1, TimeUnit.SECONDS);
            assertNotNull(description);
            assertEquals(ProviderPackage.Literals.ADMIN__DESCRIPTION.getName(), description.resource(),
                    "First event was not Description, so the Provider already exists. It is likely that the data folder wasn't cleared and this is a remnant of a previous testrun.");
            // second will be admin friendlyName
            ResourceDataNotification friendlyName = queue.poll(1, TimeUnit.SECONDS);
            assertNotNull(friendlyName);
            assertEquals(ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), friendlyName.resource(),
                    "Second event was not FriendlyName, so the Provider already exists. It is likely that the data folder wasn't cleared and this is a remnant of a previous testrun.");
            // second will be will be admin model
            assertNotNull(queue.poll(1, TimeUnit.SECONDS));
            // third will be will be admin model package uri
            assertNotNull(queue.poll(1, TimeUnit.SECONDS));
            // now ours should arrive
            ResourceDataNotification localNotif = queue.poll(2, TimeUnit.SECONDS);
            utils.assertNotification(dto, localNotif);

            // Wait for its SSE equivalent
            final ResourceLifecycleNotificationDTO lifeCycleNotif = lifeCycleEvents.poll(1, TimeUnit.SECONDS);

            // Check life cycle event
            assertNotNull(lifeCycleNotif);
            assertEquals(PROVIDER, lifeCycleNotif.provider);
            assertEquals(dto.service, lifeCycleNotif.service);
            assertEquals(dto.resource, lifeCycleNotif.resource);
            assertEquals(LifecycleNotification.Status.RESOURCE_CREATED, lifeCycleNotif.status);
            assertFalse(localNotif.timestamp().isAfter(Instant.ofEpochMilli(lifeCycleNotif.timestamp)),
                    "Lifecycle timestamp is too early");

            // Check data event
            ResourceDataNotificationDTO dataNotif = dataEvents.poll(500, TimeUnit.MILLISECONDS);
            assertNotNull(dataNotif);
            assertEquals(PROVIDER, dataNotif.provider);
            assertEquals(dto.service, dataNotif.service);
            assertEquals(dto.resource, dataNotif.resource);
            assertNull(dataNotif.oldValue, "Old value is not null");
            assertEquals(dto.value, dataNotif.newValue);
            assertEquals(localNotif.timestamp().toEpochMilli(), dataNotif.timestamp);

            // Update value
            dto.value = initialValue + 10;
            push.pushUpdate(dto);

            // Wait for it locally
            localNotif = queue.poll(1, TimeUnit.SECONDS);
            utils.assertNotification(dto, localNotif);

            // Wait for its SSE equivalent
            dataNotif = dataEvents.poll(500, TimeUnit.MILLISECONDS);
            assertNotNull(dataNotif);
            assertEquals(PROVIDER, dataNotif.provider);
            assertEquals(dto.service, dataNotif.service);
            assertEquals(dto.resource, dataNotif.resource);
            assertEquals(initialValue, dataNotif.oldValue);
            assertEquals(dto.value, dataNotif.newValue);
            assertEquals(localNotif.timestamp().toEpochMilli(), dataNotif.timestamp);

            // We shouldn't have any lifecycle event here
            assertNull(lifeCycleEvents.poll());
        } finally {
            sseSource.close();
        }
    }

    /**
     * Ensure that SSE sessions expire when the client closes the connection
     */
    @Test
    @Disabled("Client closure is not detected properly on server side due to framework limitations")
    void sseExpireOnClientClose() throws Exception {

        // Ensure that no session is present at start
        final SensiNactSession defaultSession = sessionManager.getDefaultSession(USER);
        for (String sessionId : sessionManager.getSessionIds(USER)) {
            SensiNactSession session = sessionManager.getSession(USER, sessionId);
            if (session.getSessionId() != defaultSession.getSessionId()) {
                session.expire();
            }
        }
        assertEquals(List.of(defaultSession.getSessionId()), sessionManager.getSessionIds(USER));

        final String service = "new";
        final String resource = "new-resource";

        // Subscribe to a non-existent resource
        final Client client = clientBuilder.connectTimeout(3, TimeUnit.SECONDS).register(JacksonJsonProvider.class)
                .build();
        final SseEventSource sseSource = sseClient
                .newSource(client.target("http://localhost:8185/sensinact/").path("providers").path(PROVIDER)
                        .path("services").path(service).path("resources").path(resource).path("SUBSCRIBE"));

        final BlockingArrayQueue<Instant> expirationEvent = new BlockingArrayQueue<>();
        sseSource.register(ise -> {
            final String comment = ise.getComment();
            if ("session-expired".equals(comment)) {
                logger.debug("Got session expired message");
                expirationEvent.add(Instant.now());
            }
        });
        sseSource.open();

        try {
            // Wait a bit to ensure the session is up
            Thread.sleep(100);

            // Look for the session
            SensiNactSession sseSession = null;
            for (String sessionId : sessionManager.getSessionIds(USER)) {
                SensiNactSession session = sessionManager.getSession(USER, sessionId);
                if (session != defaultSession && !sessionId.equals(defaultSession.getSessionId())) {
                    sseSession = session;
                    break;
                }
            }
            assertNotNull(sseSession, "SSE session not found");
            final CountDownLatch snaExpired = new CountDownLatch(1);
            sseSession.addExpirationListener(s -> {
                logger.debug("SSE session {} expired on server side", s.getSessionId());
                snaExpired.countDown();
            });
            logger.debug("Detected SSE session: {}", sseSession.getSessionId());

            // Wait for session to pass first expiry check
            Instant expiration = expirationEvent.poll(SESSION_EXPIRY_SECONDS + SESSION_ACTIVITY_INTERVAL_SECONDS + 1,
                    TimeUnit.SECONDS);
            assertNull(expiration, "Session expired too early");

            // Close the client side
            logger.debug("Closing SSE connection from client side");
            assertTrue(sseSource.close(5, TimeUnit.SECONDS), "SSE connection not closed in time from client side");
            logger.debug("SSE connection closed from client side");
            assertFalse(sseSource.isOpen(), "SSE connection still open after close from client side");

            // Wait a bit to ensure the server processes the closing
            expiration = expirationEvent.poll(SESSION_EXPIRY_SECONDS + SESSION_ACTIVITY_INTERVAL_SECONDS + 1,
                    TimeUnit.SECONDS);
            // We should not have gotten an expiration message, as the client closed the
            // connection
            assertNull(expiration, "Expiration message received");

            // Ensure the session is expired on the server side
            assertTrue(snaExpired.await(200, TimeUnit.MILLISECONDS),
                    "Session did not expire on server side after client closed the connection");

            assertNull(sessionManager.getSession(USER, sseSession.getSessionId()),
                    "Session did not expire after client closed the connection");
        } finally {
            if (sseSource.isOpen()) {
                sseSource.close();
            }
        }
    }

    /**
     * Ensure that SSE is closed when the server expires the session
     */
    @Test
    void sseExpireOnServiceSide() throws Exception {

        // Ensure that no session is present at start
        final SensiNactSession defaultSession = sessionManager.getDefaultSession(USER);
        for (String sessionId : sessionManager.getSessionIds(USER)) {
            SensiNactSession session = sessionManager.getSession(USER, sessionId);
            if (session.getSessionId() != defaultSession.getSessionId()) {
                session.expire();
            }
        }
        assertEquals(List.of(defaultSession.getSessionId()), sessionManager.getSessionIds(USER));

        final String service = "new";
        final String resource = "new-resource";

        // Subscribe to a non-existent resource
        final Client client = clientBuilder.connectTimeout(3, TimeUnit.SECONDS).register(JacksonJsonProvider.class)
                .build();
        final SseEventSource sseSource = sseClient
                .newSource(client.target("http://localhost:8185/sensinact/").path("providers").path(PROVIDER)
                        .path("services").path(service).path("resources").path(resource).path("SUBSCRIBE"));

        final BlockingArrayQueue<Instant> expirationEvent = new BlockingArrayQueue<>();
        sseSource.register(ise -> {
            final String comment = ise.getComment();
            if ("session-expired".equals(comment)) {
                expirationEvent.add(Instant.now());
            }
        });
        sseSource.open();

        try {
            // Wait a bit to ensure the session is up
            Thread.sleep(100);

            // Look for the session
            SensiNactSession sseSession = null;
            for (String sessionId : sessionManager.getSessionIds(USER)) {
                SensiNactSession session = sessionManager.getSession(USER, sessionId);
                if (session != defaultSession && !sessionId.equals(defaultSession.getSessionId())) {
                    sseSession = session;
                    break;
                }
            }
            assertNotNull(sseSession, "SSE session not found");

            // Wait for session to pass first expiry check
            Instant expiration = expirationEvent.poll(SESSION_EXPIRY_SECONDS + SESSION_ACTIVITY_INTERVAL_SECONDS + 1,
                    TimeUnit.SECONDS);
            assertNull(expiration, "Session expired too early");

            // Expire the session on the server side
            final Instant closingTime = Instant.now();
            sseSession.expire();

            // Expiration should be received quickly
            expiration = expirationEvent.poll(500, TimeUnit.MILLISECONDS);
            assertNotNull(expiration, "Session expired event not received");
            assertFalse(expiration.isBefore(closingTime), "Session expired too early");
        } finally {
            if (sseSource.isOpen()) {
                sseSource.close();
            }
        }
    }
}

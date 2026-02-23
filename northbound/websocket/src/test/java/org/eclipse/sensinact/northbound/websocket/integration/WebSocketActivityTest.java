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

package org.eclipse.sensinact.northbound.websocket.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.northbound.query.api.AbstractQueryDTO;
import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;
import org.eclipse.sensinact.northbound.query.api.EResultType;
import org.eclipse.sensinact.northbound.query.dto.SensinactPath;
import org.eclipse.sensinact.northbound.query.dto.query.QueryListDTO;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@WithConfiguration(pid = "sensinact.northbound.websocket", properties = @Property(key = "allow.anonymous", value = "true"))
@WithConfiguration(pid = "sensinact.session.manager", properties = {
        @Property(key = "auth.policy", value = "ALLOW_ALL"),
        @Property(key = "expiry", value = "" + WebSocketActivityTest.SESSION_EXPIRY_SECONDS),
        @Property(key = "activity.check.interval", value = ""
                + WebSocketActivityTest.SESSION_ACTIVITY_INTERVAL_SECONDS),
        @Property(key = "activity.check.extension", value = ""
                + WebSocketActivityTest.SESSION_ACTIVITY_EXTENSION_SECONDS),
        @Property(key = "activity.check.threshold", value = "1"),
        @Property(key = "name", value = "test-session"),
})
public class WebSocketActivityTest {

    public static final int SESSION_EXPIRY_SECONDS = 4;
    public static final int SESSION_ACTIVITY_INTERVAL_SECONDS = 1;
    public static final int SESSION_ACTIVITY_EXTENSION_SECONDS = 3;

    @InjectService(filter = "(name=test-session)", timeout = 1000)
    SensiNactSessionManager sessionManager;

    final ObjectMapper mapper = new ObjectMapper();

    static URI wsUri;

    @BeforeAll
    static void setup() throws URISyntaxException {
        wsUri = new URI("ws://localhost:14001/ws/sensinact");
    }

    /**
     * Utility class to auto-close a web socket client
     */
    class WSClient implements AutoCloseable {
        WebSocketClient ws;

        public WSClient() throws Exception {
            ws = new WebSocketClient();
            ws.start();
        }

        @Override
        public void close() throws Exception {
            ws.stop();
            ws.destroy();
        }
    }

    /**
     * Constructs a DTO to use with DataUpdate
     */
    public GenericDto makeDto(String provider, String service, String resource, Object value, Class<?> type) {
        GenericDto dto = new GenericDto();
        dto.model = provider;
        dto.provider = provider;
        dto.service = service;
        dto.resource = resource;
        dto.value = value;
        dto.type = type;
        return dto;
    }

    /**
     * Sends a DTO over the websocket
     */
    void sendDTO(final Session session, final AbstractQueryDTO dto) {
        try {
            session.getRemote().sendString(mapper.writeValueAsString(dto));
        } catch (Exception e) {
            fail("Error sending DTO", e);
        }
    }

    /**
     * Check how the session manager handles activity check over websockets, with
     * the session explicitly closed from the sensiNact side
     */
    @Test
    void testActivityCheckCloseFromSensinact() throws Exception {
        final WSHandler handler = new WSHandler();
        final CountDownLatch closeBarrier = new CountDownLatch(1);
        final CountDownLatch messageBarrier = new CountDownLatch(1);

        final QueryListDTO query = new QueryListDTO();
        query.uri = new SensinactPath();
        query.requestId = String.valueOf(new Random().nextInt());
        handler.onConnect = s -> sendDTO(s, query);

        final AtomicReference<Throwable> error = new AtomicReference<>();
        handler.onError = (s, t) -> {
            error.set(t);
            closeBarrier.countDown();
        };
        handler.onClose = (s, c) -> closeBarrier.countDown();

        final AtomicReference<AbstractResultDTO> resultHolder = new AtomicReference<>();
        handler.onMessage = (s, m) -> {
            try {
                resultHolder.set(mapper.readValue(m, AbstractResultDTO.class));
                messageBarrier.countDown();
            } catch (JsonProcessingException e) {
                fail("Error parsing WS response", e);
            }
        };

        // List active sessions
        final Set<String> existingSessionIds = Set.copyOf(sessionManager.getAnonymousSessionIds());
        SensiNactSession session;
        try (final WSClient client = new WSClient()) {
            // Connect to the websocket
            client.ws.connect(handler, wsUri).get();

            // Find our session
            final String sessionId = Set.copyOf(sessionManager.getAnonymousSessionIds()).stream()
                    .filter(id -> !existingSessionIds.contains(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No new session created for WS connection"));
            session = sessionManager.getAnonymousSession(sessionId);
            assertNotNull(session, "Couldn't get hold of our test session");

            final Instant initialExpiry = session.getExpiry();
            assertNotNull(initialExpiry, "Session expiry is null after creation");

            // Get the query result
            assertTrue(messageBarrier.await(500, TimeUnit.MILLISECONDS), "Timeout waiting for WS message");
            AbstractResultDTO result = resultHolder.get();
            assertNotNull(result, "No result received from WS");
            assertEquals(EResultType.PROVIDERS_LIST, result.type, "Unexpected result type from WS");

            // Wait for the session expiration delay: we should still be there
            assertFalse(closeBarrier.await(
                    Duration.between(Instant.now(), initialExpiry).plusMillis(200).toMillis(),
                    TimeUnit.MILLISECONDS), "WS session closed by timeout");

            // Check expiry has been extended
            final Instant extendedExpiry = session.getExpiry();
            assertNotNull(extendedExpiry, "Session expiry is null after activity check");
            assertTrue(extendedExpiry.isAfter(initialExpiry), "Session expiry was not extended after activity check");
            assertFalse(extendedExpiry.isBefore(initialExpiry.plusSeconds(2)),
                    "Session expiry was not properly extended after activity check");

            // Close the session on sensiNact side
            session.expire();

            // Wait for the close event
            assertTrue(
                    closeBarrier.await(Duration.ofSeconds(SESSION_ACTIVITY_INTERVAL_SECONDS).plusMillis(200).toMillis(),
                            TimeUnit.MILLISECONDS),
                    "Timeout waiting for WS close event");
        }

        if (error.get() != null) {
            fail(error.get());
        }
    }

    /**
     * Check how the session manager handles activity check over websockets, with
     * the session implicitly closed from the websocket side
     */
    @Test
    void testActivityCheckCloseFromWebSocket() throws Exception {
        final WSHandler handler = new WSHandler();
        final CountDownLatch closeBarrier = new CountDownLatch(1);
        final CountDownLatch messageBarrier = new CountDownLatch(1);
        final CountDownLatch expirationBarrier = new CountDownLatch(1);

        final QueryListDTO query = new QueryListDTO();
        query.uri = new SensinactPath();
        query.requestId = String.valueOf(new Random().nextInt());
        handler.onConnect = s -> sendDTO(s, query);

        final AtomicReference<Throwable> error = new AtomicReference<>();
        handler.onError = (s, t) -> {
            error.set(t);
            closeBarrier.countDown();
        };
        handler.onClose = (s, c) -> closeBarrier.countDown();

        final AtomicReference<AbstractResultDTO> resultHolder = new AtomicReference<>();
        handler.onMessage = (s, m) -> {
            try {
                resultHolder.set(mapper.readValue(m, AbstractResultDTO.class));
                messageBarrier.countDown();
            } catch (JsonProcessingException e) {
                fail("Error parsing WS response", e);
            }
        };

        // List active sessions
        final Set<String> existingSessionIds = Set.copyOf(sessionManager.getAnonymousSessionIds());
        SensiNactSession session;
        Instant extendedExpiry;
        try (final WSClient client = new WSClient()) {
            // Connect to the websocket
            client.ws.connect(handler, wsUri).get();

            // Find our session
            final String sessionId = Set.copyOf(sessionManager.getAnonymousSessionIds()).stream()
                    .filter(id -> !existingSessionIds.contains(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No new session created for WS connection"));
            session = sessionManager.getAnonymousSession(sessionId);
            assertNotNull(session, "Couldn't get hold of our test session");
            session.addExpirationListener(s -> expirationBarrier.countDown());

            final Instant initialExpiry = session.getExpiry();
            assertNotNull(initialExpiry, "Session expiry is null after creation");

            // Get the query result
            assertTrue(messageBarrier.await(500, TimeUnit.MILLISECONDS), "Timeout waiting for WS message");
            AbstractResultDTO result = resultHolder.get();
            assertNotNull(result, "No result received from WS");
            assertEquals(EResultType.PROVIDERS_LIST, result.type, "Unexpected result type from WS");

            // Wait for the session expiration delay: we should still be there
            assertFalse(closeBarrier.await(
                    Duration.between(Instant.now(), initialExpiry).plusMillis(200).toMillis(),
                    TimeUnit.MILLISECONDS), "WS session closed by timeout");

            // Check expiry has been extended
            extendedExpiry = session.getExpiry();
            assertNotNull(extendedExpiry, "Session expiry is null after activity check");
            assertTrue(extendedExpiry.isAfter(initialExpiry), "Session expiry was not extended after activity check");
            assertFalse(extendedExpiry.isBefore(initialExpiry.plusSeconds(2)),
                    "Session expiry was not properly extended after activity check");

            // Close the websocket from client side
        }

        // Wait for the session to expire due to closure
        // This must be nearly immediate as it is detected by the WebSocket client
        assertTrue(expirationBarrier.await(100, TimeUnit.MILLISECONDS),
                "Timeout waiting for session expiration after WS close");

        if (error.get() != null) {
            fail(error.get());
        }
    }
}

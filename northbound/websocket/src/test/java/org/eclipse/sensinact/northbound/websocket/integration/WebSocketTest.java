/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.northbound.query.api.AbstractQueryDTO;
import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;
import org.eclipse.sensinact.northbound.query.api.EResultType;
import org.eclipse.sensinact.northbound.query.dto.SensinactPath;
import org.eclipse.sensinact.northbound.query.dto.notification.ResourceDataNotificationDTO;
import org.eclipse.sensinact.northbound.query.dto.notification.ResultResourceNotificationDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryListDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QuerySubscribeDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryUnsubscribeDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultListServicesDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultSubscribeDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultUnsubscribeDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@WithConfiguration(pid = "sensinact.northbound.websocket", properties = @Property(key = "allow.anonymous", value = "true"))
public class WebSocketTest {

    @InjectService
    DataUpdate push;

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
     * Simple query test
     */
    @Test
    void testQuery() throws Exception {
        GenericDto dto = makeDto("wsTestProvider", "svc", "data", 42, Integer.class);
        push.pushUpdate(dto).getValue();

        final WSHandler handler = new WSHandler();
        final CountDownLatch barrier = new CountDownLatch(1);

        final QueryListDTO query = new QueryListDTO();
        query.uri = new SensinactPath(dto.provider);
        query.requestId = String.valueOf(new Random().nextInt());
        handler.onConnect = s -> sendDTO(s, query);

        final AtomicReference<Throwable> error = new AtomicReference<>();
        handler.onError = (s, t) -> {
            error.set(t);
            barrier.countDown();
        };
        handler.onClose = (s, c) -> barrier.countDown();

        final AtomicReference<AbstractResultDTO> resultHolder = new AtomicReference<>();
        handler.onMessage = (s, m) -> {
            try {
                resultHolder.set(mapper.readValue(m, AbstractResultDTO.class));
                barrier.countDown();
            } catch (JsonProcessingException e) {
                fail("Error parsing WS response", e);
            }
        };

        try (final WSClient client = new WSClient()) {
            client.ws.connect(handler, wsUri).get();
            barrier.await();
        }

        if (error.get() != null) {
            fail(error.get());
        }

        final AbstractResultDTO result = resultHolder.get();
        assertNotNull(result, "No WS query result");
        assertEquals(200, result.statusCode);
        assertEquals(query.requestId, result.requestId);
        assertEquals(EResultType.SERVICES_LIST, result.type);

        final ResultListServicesDTO svcList = (ResultListServicesDTO) result;
        assertTrue(svcList.services.contains("admin"), "Admin service is missing");
        assertTrue(svcList.services.contains(dto.service), "Provider service is missing");
    }

    @Test
    void testSubscription() throws Exception {
        GenericDto dto = makeDto("wsTestProviderSub", "svc", "data", 42, Integer.class);
        push.pushUpdate(dto).getValue();

        final WSHandler handler = new WSHandler();
        final CountDownLatch barrier = new CountDownLatch(1);

        final AtomicReference<Session> sessionRef = new AtomicReference<>();
        handler.onConnect = s -> {
            sessionRef.set(s);
            barrier.countDown();
        };

        final AtomicReference<Throwable> error = new AtomicReference<>();
        handler.onError = (s, t) -> {
            error.set(t);
            barrier.countDown();
        };
        handler.onClose = (s, c) -> barrier.countDown();

        final BlockingQueue<AbstractResultDTO> resultsHolder = new BlockingArrayQueue<>();
        handler.onMessage = (s, m) -> {
            try {
                resultsHolder.add(mapper.readValue(m, AbstractResultDTO.class));
            } catch (JsonProcessingException e) {
                error.set(new Exception("Error parsing WS response", e));
            }
        };

        try (final WSClient client = new WSClient()) {
            client.ws.connect(handler, wsUri).get();

            // Wait for the websocket to connect connection
            barrier.await(2, TimeUnit.SECONDS);
            final Session session = sessionRef.get();

            // Send the notification
            final QuerySubscribeDTO querySub = new QuerySubscribeDTO();
            querySub.uri = new SensinactPath(dto.provider, dto.service, dto.resource);
            querySub.requestId = String.valueOf(new Random().nextInt());
            sendDTO(session, querySub);

            // Result must be the subscription result
            AbstractResultDTO rawResult = resultsHolder.poll(1, TimeUnit.SECONDS);
            assertNotNull(rawResult, "No result to subscribe");
            ResultSubscribeDTO subscribeResult = (ResultSubscribeDTO) rawResult;
            assertEquals(querySub.requestId, rawResult.requestId);
            assertNotNull(subscribeResult.subscriptionId, "No subscription ID");

            // Update the value
            Instant updateTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
            Object oldValue = dto.value;
            dto.value = -12;
            push.pushUpdate(dto).getValue();

            // Second result must be the notification
            rawResult = resultsHolder.poll(1, TimeUnit.SECONDS);
            if (rawResult == null && error.get() != null) {
                fail(error.get());
            }
            assertNotNull(rawResult, "No notification");
            ResultResourceNotificationDTO notif = (ResultResourceNotificationDTO) rawResult;
            assertEquals(querySub.requestId, subscribeResult.requestId);
            assertEquals(subscribeResult.subscriptionId, notif.subscriptionId);
            assertNotNull(notif.notification);
            assertEquals(oldValue, ((ResourceDataNotificationDTO) notif.notification).oldValue);
            assertEquals(dto.value, ((ResourceDataNotificationDTO) notif.notification).newValue);
            assertEquals(dto.provider, notif.notification.provider);
            assertEquals(dto.service, notif.notification.service);
            assertEquals(dto.resource, notif.notification.resource);
            final Instant firstNotifTime = Instant.ofEpochMilli(notif.notification.timestamp);
            assertFalse(updateTime.isAfter(firstNotifTime));

            // New update
            Instant updateTime2 = Instant.now().truncatedTo(ChronoUnit.MILLIS);
            oldValue = dto.value;
            dto.value = 128;
            push.pushUpdate(dto).getValue();

            // Second result must be the notification
            rawResult = resultsHolder.poll(1, TimeUnit.SECONDS);
            assertNotNull(rawResult, "No notification");
            notif = (ResultResourceNotificationDTO) rawResult;
            assertEquals(querySub.requestId, subscribeResult.requestId);
            assertEquals(subscribeResult.subscriptionId, notif.subscriptionId);
            assertNotNull(notif.notification);
            assertEquals(oldValue, ((ResourceDataNotificationDTO) notif.notification).oldValue);
            assertEquals(dto.value, ((ResourceDataNotificationDTO) notif.notification).newValue);
            final Instant secondNotifTime = Instant.ofEpochMilli(notif.notification.timestamp);
            assertFalse(firstNotifTime.isAfter(secondNotifTime));
            assertFalse(updateTime2.isAfter(secondNotifTime));

            // Unsubscribe
            final QueryUnsubscribeDTO unsubQuery = new QueryUnsubscribeDTO();
            unsubQuery.subscriptionId = subscribeResult.subscriptionId;
            sendDTO(session, unsubQuery);

            // Wait for the result
            final Instant timeout = Instant.now().plus(5, ChronoUnit.SECONDS);
            boolean found = false;
            while (Instant.now().isBefore(timeout)) {
                rawResult = resultsHolder.poll(1, TimeUnit.SECONDS);
                if (rawResult != null && rawResult.type == EResultType.UNSUBSCRIPTION_RESPONSE) {
                    // Got it
                    found = true;
                    break;
                }
            }

            assertTrue(found, "Didn't get the unsubscription response");

            ResultUnsubscribeDTO unsubResult = (ResultUnsubscribeDTO) rawResult;
            assertEquals(subscribeResult.subscriptionId, unsubResult.subscriptionId);

            // New update
            dto.value = 512;
            push.pushUpdate(dto).getValue();

            // Wait of a notification
            assertNull(resultsHolder.poll(1, TimeUnit.SECONDS), "Got a notification");
        }
    }

    @Test
    void testSubscriptionWithFilter() throws Exception {
        // Push 2 providers
        final GenericDto dto1 = makeDto("wsTestProviderSubFilter1", "svc", "data", 42, Integer.class);
        final GenericDto dto2 = makeDto("wsTestProviderSubFilter2", "svc", "data", 21, Integer.class);
        final BulkGenericDto bulk = new BulkGenericDto();
        bulk.dtos = List.of(dto1, dto2);
        push.pushUpdate(bulk).getValue();

        final WSHandler handler = new WSHandler();
        final CountDownLatch barrier = new CountDownLatch(1);

        final AtomicReference<Session> sessionRef = new AtomicReference<>();
        handler.onConnect = s -> {
            sessionRef.set(s);
            barrier.countDown();
        };

        final AtomicReference<Throwable> error = new AtomicReference<>();
        handler.onError = (s, t) -> {
            error.set(t);
            barrier.countDown();
        };
        handler.onClose = (s, c) -> barrier.countDown();

        final BlockingQueue<AbstractResultDTO> resultsHolder = new BlockingArrayQueue<>();
        handler.onMessage = (s, m) -> {
            try {
                resultsHolder.add(mapper.readValue(m, AbstractResultDTO.class));
            } catch (JsonProcessingException e) {
                error.set(new Exception("Error parsing WS response", e));
            }
        };

        try (final WSClient client = new WSClient()) {
            client.ws.connect(handler, wsUri).get();

            // Wait for the websocket to connect connection
            barrier.await(2, TimeUnit.SECONDS);
            final Session session = sessionRef.get();

            // Send the notification
            final QuerySubscribeDTO querySub = new QuerySubscribeDTO();
            querySub.uri = new SensinactPath();
            querySub.requestId = String.valueOf(new Random().nextInt());
            querySub.filter = "(PROVIDER=" + dto1.provider + ")";
            querySub.filterLanguage = "ldap";
            sendDTO(session, querySub);

            // Result must be the subscription result
            AbstractResultDTO rawResult = resultsHolder.poll(1, TimeUnit.SECONDS);
            assertNotNull(rawResult, "No result to subscribe");
            ResultSubscribeDTO subscribeResult = (ResultSubscribeDTO) rawResult;
            assertEquals(querySub.requestId, rawResult.requestId);
            assertNotNull(subscribeResult.subscriptionId, "No subscription ID");

            // Update the value
            Instant updateTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
            Object oldValue = dto1.value;
            dto1.value = -12;
            push.pushUpdate(dto1).getValue();

            // Second result must be the notification
            rawResult = resultsHolder.poll(1, TimeUnit.SECONDS);
            if (rawResult == null && error.get() != null) {
                fail(error.get());
            }
            assertNotNull(rawResult, "No notification");
            ResultResourceNotificationDTO notif = (ResultResourceNotificationDTO) rawResult;
            assertEquals(querySub.requestId, subscribeResult.requestId);
            assertEquals(subscribeResult.subscriptionId, notif.subscriptionId);
            assertNotNull(notif.notification);
            assertEquals(dto1.provider, notif.notification.provider);
            assertEquals(oldValue, ((ResourceDataNotificationDTO) notif.notification).oldValue);
            assertEquals(dto1.value, ((ResourceDataNotificationDTO) notif.notification).newValue);
            assertEquals(dto1.provider, notif.notification.provider);
            assertEquals(dto1.service, notif.notification.service);
            assertEquals(dto1.resource, notif.notification.resource);
            final Instant firstNotifTime = Instant.ofEpochMilli(notif.notification.timestamp);
            assertFalse(updateTime.isAfter(firstNotifTime));

            // New update on second provider
            dto2.value = 128;
            push.pushUpdate(dto2).getValue();

            // Notification shouldn't be sent
            rawResult = resultsHolder.poll(1, TimeUnit.SECONDS);
            assertNull(rawResult, "Got notified on filtered out provider");

            // Second result must be the notification
            Instant updateTime2 = Instant.now().truncatedTo(ChronoUnit.MILLIS);
            oldValue = dto1.value;
            dto1.value = -128;
            push.pushUpdate(dto1).getValue();

            rawResult = resultsHolder.poll(1, TimeUnit.SECONDS);
            assertNotNull(rawResult, "No notification");
            notif = (ResultResourceNotificationDTO) rawResult;
            assertEquals(querySub.requestId, subscribeResult.requestId);
            assertEquals(subscribeResult.subscriptionId, notif.subscriptionId);
            assertNotNull(notif.notification);
            assertEquals(dto1.provider, notif.notification.provider);
            assertEquals(oldValue, ((ResourceDataNotificationDTO) notif.notification).oldValue);
            assertEquals(dto1.value, ((ResourceDataNotificationDTO) notif.notification).newValue);
            final Instant secondNotifTime = Instant.ofEpochMilli(notif.notification.timestamp);
            assertFalse(firstNotifTime.isAfter(secondNotifTime));
            assertFalse(updateTime2.isAfter(secondNotifTime));

            // Unsubscribe
            final QueryUnsubscribeDTO unsubQuery = new QueryUnsubscribeDTO();
            unsubQuery.subscriptionId = subscribeResult.subscriptionId;
            sendDTO(session, unsubQuery);

            // Wait for the result
            final Instant timeout = Instant.now().plus(5, ChronoUnit.SECONDS);
            boolean found = false;
            while (Instant.now().isBefore(timeout)) {
                rawResult = resultsHolder.poll(1, TimeUnit.SECONDS);
                if (rawResult != null && rawResult.type == EResultType.UNSUBSCRIPTION_RESPONSE) {
                    // Got it
                    found = true;
                    break;
                }
            }

            assertTrue(found, "Didn't get the unsubscription response");

            ResultUnsubscribeDTO unsubResult = (ResultUnsubscribeDTO) rawResult;
            assertEquals(subscribeResult.subscriptionId, unsubResult.subscriptionId);

            // New update for both
            dto1.value = 512;
            dto2.value = 256;
            push.pushUpdate(bulk).getValue();

            // Wait of a notification
            assertNull(resultsHolder.poll(1, TimeUnit.SECONDS), "Got a notification");
        }
    }
}

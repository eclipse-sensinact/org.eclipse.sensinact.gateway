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

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.sensinact.northbound.query.dto.notification.ResourceDataNotificationDTO;
import org.eclipse.sensinact.northbound.query.dto.notification.ResourceLifecycleNotificationDTO;
import org.eclipse.sensinact.northbound.rest.integration.TestUtils;
import org.eclipse.sensinact.prototype.PrototypePush;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.eclipse.sensinact.prototype.generic.dto.GenericDto;
import org.eclipse.sensinact.prototype.notification.LifecycleNotification;
import org.eclipse.sensinact.prototype.notification.ResourceDataNotification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.jakartars.client.SseEventSourceFactory;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.service.ServiceExtension;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.sse.SseEventSource;

@ExtendWith(ServiceExtension.class)
public class ResourceNotificationsTest {

    private static final String USER = "user";

    private static final String PROVIDER = "RestNotificationProvider";
    private static final String PROVIDER_TOPIC = PROVIDER + "/*";

    @InjectService
    protected SseEventSourceFactory sseClient;

    @InjectService
    SensiNactSessionManager sessionManager;

    @InjectService
    PrototypePush push;

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
            ResourceDataNotification localNotif = queue.poll(1, TimeUnit.SECONDS);
            utils.assertNotification(dto, localNotif);

            // Wait for its SSE equivalent
            final ResourceLifecycleNotificationDTO lifeCycleNotif = lifeCycleEvents.poll(1, TimeUnit.SECONDS);

            // Check life cycle event
            assertNotNull(lifeCycleNotif);
            assertEquals(PROVIDER, lifeCycleNotif.provider);
            assertEquals(dto.service, lifeCycleNotif.service);
            assertEquals(dto.resource, lifeCycleNotif.resource);
            assertEquals(LifecycleNotification.Status.RESOURCE_CREATED, lifeCycleNotif.status);
            assertFalse(localNotif.timestamp.isAfter(Instant.ofEpochMilli(lifeCycleNotif.timestamp)),
                    "Lifecycle timestamp is too early");

            // Check data event
            ResourceDataNotificationDTO dataNotif = dataEvents.poll(500, TimeUnit.MILLISECONDS);
            assertNotNull(dataNotif);
            assertEquals(PROVIDER, dataNotif.provider);
            assertEquals(dto.service, dataNotif.service);
            assertEquals(dto.resource, dataNotif.resource);
            assertNull(dataNotif.oldValue, "Old value is not null");
            assertEquals(dto.value, dataNotif.newValue);
            assertEquals(localNotif.timestamp.toEpochMilli(), dataNotif.timestamp);

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
            assertEquals(localNotif.timestamp.toEpochMilli(), dataNotif.timestamp);

            // We shouldn't have any lifecycle event here
            assertNull(lifeCycleEvents.poll());
        } finally {
            sseSource.close();
        }
    }
}

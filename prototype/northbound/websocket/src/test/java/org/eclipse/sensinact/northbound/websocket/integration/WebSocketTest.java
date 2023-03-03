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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.sensinact.northbound.query.api.AbstractQueryDTO;
import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;
import org.eclipse.sensinact.northbound.query.api.EResultType;
import org.eclipse.sensinact.northbound.query.dto.SensinactPath;
import org.eclipse.sensinact.northbound.query.dto.query.QueryListDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultListServicesDTO;
import org.eclipse.sensinact.prototype.PrototypePush;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.eclipse.sensinact.prototype.generic.dto.GenericDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.service.ServiceExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(ServiceExtension.class)
public class WebSocketTest {

    @InjectService
    SensiNactSessionManager sessionManager;

    @InjectService
    PrototypePush push;

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
     * Constructs a DTO to use with PrototypePush
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

    // TODO test subscription
    @Test
    void testSubscription() {

    }
}

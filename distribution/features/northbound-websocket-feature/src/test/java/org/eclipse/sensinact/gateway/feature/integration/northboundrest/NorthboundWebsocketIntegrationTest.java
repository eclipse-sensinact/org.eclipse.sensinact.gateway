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
package org.eclipse.sensinact.gateway.feature.integration.northboundrest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URI;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.sensinact.gateway.feature.utilities.test.ServerProcessHandler;
import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;
import org.eclipse.sensinact.northbound.query.dto.SensinactPath;
import org.eclipse.sensinact.northbound.query.dto.query.QueryListDTO;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

class NorthboundWebsocketIntegrationTest {

    private static ServerProcessHandler server = new ServerProcessHandler();

    @BeforeAll
    static void startServer() throws Exception {
        server.startSensinact();
    }

    @AfterAll
    static void stopServer() throws Exception {
        server.stopSensinact();
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

    @Test
    void testWebsocket() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();

        for (int i = 0; i < 5; i++) {
            WebSocketClient ws = new WebSocketClient();
            ws.start();
            try {
                final WSHandler handler = new WSHandler();
                final CountDownLatch barrier = new CountDownLatch(1);

                final QueryListDTO query = new QueryListDTO();
                query.uri = new SensinactPath();
                query.requestId = String.valueOf(new Random().nextInt());
                handler.onConnect = s -> {
                    try {
                        s.getRemote().sendString(mapper.writeValueAsString(query));
                    } catch (IOException e1) {
                        fail(e1);
                    }
                };

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
                    client.ws.connect(handler, new URI("ws://localhost:14001/ws/sensinact")).get();
                    barrier.await();
                }

                if (error.get() != null) {
                    fail(error.get());
                }

                assertNotNull(resultHolder.get(), "No data received");
            } catch (Exception e) {
                // Wait a bit: the websocket server was likely not ready
                Thread.sleep(2000);
                continue;
            } finally {
                ws.stop();
                ws.destroy();
            }
        }
    }
}

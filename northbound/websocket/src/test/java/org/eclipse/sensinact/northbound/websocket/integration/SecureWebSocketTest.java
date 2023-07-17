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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.exceptions.UpgradeException;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.sensinact.core.push.PrototypePush;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.security.UserInfo;
import org.eclipse.sensinact.core.session.SensiNactSessionManager;
import org.eclipse.sensinact.northbound.query.api.AbstractQueryDTO;
import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;
import org.eclipse.sensinact.northbound.query.api.EResultType;
import org.eclipse.sensinact.northbound.query.dto.SensinactPath;
import org.eclipse.sensinact.northbound.query.dto.query.QueryGetDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseGetDTO;
import org.eclipse.sensinact.northbound.query.dto.result.TypedResponse;
import org.eclipse.sensinact.northbound.security.api.Authenticator;
import org.eclipse.sensinact.northbound.security.api.Authenticator.Scheme;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith({ ServiceExtension.class, ConfigurationExtension.class })
public class SecureWebSocketTest {

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

    @Test
    void testConnectNoCredentialsOrAuthenticators() throws Exception {
        final WSHandler handler = new WSHandler();

        try (final WSClient client = new WSClient()) {
            client.ws.connect(handler, wsUri).join();
            fail("Should fail to connect");
        } catch (CompletionException ce) {
            Throwable cause = ce.getCause();
            assertInstanceOf(UpgradeException.class, cause);
            assertEquals(503, ((UpgradeException) cause).getResponseStatusCode());
        }
    }

    @Test
    void testConnectNoCredentials(@InjectBundleContext BundleContext ctx) throws Exception {

        TestAuthenticator auth = new TestAuthenticator("test_realm", Scheme.USER_PASSWORD, "test", "testPw");

        ctx.registerService(Authenticator.class, auth, new Hashtable<>());

        final WSHandler handler = new WSHandler();

        try (final WSClient client = new WSClient()) {
            client.ws.connect(handler, wsUri).join();
            fail("Should fail to connect");
        } catch (CompletionException ce) {
            Throwable cause = ce.getCause();
            assertInstanceOf(UpgradeException.class, cause);
            assertEquals(401, ((UpgradeException) cause).getResponseStatusCode());
        }
    }

    @Test
    void testConnectBadCredentials(@InjectBundleContext BundleContext ctx) throws Exception {

        TestAuthenticator auth = new TestAuthenticator("test_realm", Scheme.USER_PASSWORD, "test", "testPw");

        ctx.registerService(Authenticator.class, auth, new Hashtable<>());

        ClientUpgradeRequest req = new ClientUpgradeRequest();
        req.setHeader("Authorization", Base64.getUrlEncoder().encodeToString("test:incorrect".getBytes(UTF_8)));

        final WSHandler handler = new WSHandler();

        try (final WSClient client = new WSClient()) {
            client.ws.connect(handler, wsUri).join();
            fail("Should fail to connect");
        } catch (CompletionException ce) {
            Throwable cause = ce.getCause();
            assertInstanceOf(UpgradeException.class, cause);
            assertEquals(401, ((UpgradeException) cause).getResponseStatusCode());
        }
    }

    @Test
    void testConnectBasicCredentials(@InjectBundleContext BundleContext ctx) throws Exception {

        TestAuthenticator auth = new TestAuthenticator("test_realm", Scheme.USER_PASSWORD, "test", "testPw");

        ctx.registerService(Authenticator.class, auth, new Hashtable<>());

        final WSHandler handler = new WSHandler();
        final CountDownLatch barrier = new CountDownLatch(1);

        final QueryGetDTO query = new QueryGetDTO();
        query.uri = new SensinactPath("sensiNact", "admin", "friendlyName");
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

        ClientUpgradeRequest req = new ClientUpgradeRequest();
        req.setHeader("Authorization", "Basic " + Base64.getUrlEncoder().encodeToString("test:testPw".getBytes(UTF_8)));
        try (final WSClient client = new WSClient()) {
            client.ws.connect(handler, wsUri, req).get();
            barrier.await();
        }

        if (error.get() != null) {
            fail(error.get());
        }

        final AbstractResultDTO result = resultHolder.get();
        assertNotNull(result, "No WS query result");
        assertEquals(200, result.statusCode);
        assertEquals(query.requestId, result.requestId);
        assertEquals(EResultType.GET_RESPONSE, result.type);

        @SuppressWarnings("unchecked")
        final TypedResponse<ResponseGetDTO> typed = (TypedResponse<ResponseGetDTO>) result;
        assertEquals("sensiNact", typed.response.value);
    }

    @Test
    void testConnectBearerCredentials(@InjectBundleContext BundleContext ctx) throws Exception {

        TestAuthenticator auth = new TestAuthenticator("test_realm", Scheme.TOKEN, null, "my_token");

        ctx.registerService(Authenticator.class, auth, new Hashtable<>());

        final WSHandler handler = new WSHandler();
        final CountDownLatch barrier = new CountDownLatch(1);

        final QueryGetDTO query = new QueryGetDTO();
        query.uri = new SensinactPath("sensiNact", "admin", "friendlyName");
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

        ClientUpgradeRequest req = new ClientUpgradeRequest();
        req.setHeader("Authorization", "Bearer my_token");
        try (final WSClient client = new WSClient()) {
            client.ws.connect(handler, wsUri, req).get();
            barrier.await();
        }

        if (error.get() != null) {
            fail(error.get());
        }

        final AbstractResultDTO result = resultHolder.get();
        assertNotNull(result, "No WS query result");
        assertEquals(200, result.statusCode);
        assertEquals(query.requestId, result.requestId);
        assertEquals(EResultType.GET_RESPONSE, result.type);

        @SuppressWarnings("unchecked")
        final TypedResponse<ResponseGetDTO> typed = (TypedResponse<ResponseGetDTO>) result;
        assertEquals("sensiNact", typed.response.value);
    }

    private static class TestAuthenticator implements Authenticator {

        private final String realm;

        private final Scheme scheme;

        private final String user;

        private final String credential;

        private final UserInfo info = new UserInfo() {

            @Override
            public boolean isMemberOfGroup(String group) {
                return false;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public boolean isAnonymous() {
                return false;
            }

            @Override
            public String getUserId() {
                return user;
            }
        };

        public TestAuthenticator(String realm, Scheme scheme, String user, String credential) {
            super();
            this.realm = realm;
            this.scheme = scheme;
            this.user = user;
            this.credential = credential;
        }

        @Override
        public UserInfo authenticate(String user, String credential) {
            if (this.user == null && user != null) {
                return null;
            }

            if (this.user != null && !this.user.equals(user)) {
                return null;
            }

            if (!this.credential.equals(credential)) {
                return null;
            }
            return info;
        }

        @Override
        public String getRealm() {
            return realm;
        }

        @Override
        public Scheme getScheme() {
            return scheme;
        }

    }
}

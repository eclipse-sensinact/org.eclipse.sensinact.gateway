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
package org.eclipse.sensinact.gateway.feature.integration.northbound.security.oidc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.sensinact.gateway.feature.utilities.test.ServerProcessHandler;
import org.eclipse.sensinact.gateway.northbound.security.oidc.Certificates;
import org.eclipse.sensinact.gateway.northbound.security.oidc.Certificates.KeyInfo;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.sun.net.httpserver.HttpServer;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.jackson.io.JacksonSerializer;

class OIDCAuthenticatorIntegrationTest {

    private static final String KEY_ID = "test";
    private static final ObjectMapper mapper = JsonMapper.builder().build();

    private static ServerProcessHandler server = new ServerProcessHandler();
    private static HttpServer httpServer;
    private static KeyPair keyPair;

    @BeforeAll
    static void startServer() throws Exception {

        KeyPairGenerator rsaKpg = KeyPairGenerator.getInstance("RSA");
        rsaKpg.initialize(4096);
        keyPair = rsaKpg.genKeyPair();

        Certificates certificates = new Certificates();

        KeyInfo info = new KeyInfo();
        info.setType("RSA");
        info.setAlgorithm("RS512");
        info.setKeyId(KEY_ID);
        Encoder encoder = Base64.getUrlEncoder();
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec spec = kf.getKeySpec(keyPair.getPublic(), RSAPublicKeySpec.class);
        info.setRsaModulus(encoder.encodeToString(spec.getModulus().toByteArray()));
        info.setRsaExponent(encoder.encodeToString(spec.getPublicExponent().toByteArray()));

        certificates.setKeys(List.of(info));

        httpServer = com.sun.net.httpserver.HttpServer.create();
        httpServer.bind(new InetSocketAddress("127.0.0.1", 24680), 0);
        httpServer.start();

        httpServer.createContext("/discovery", ex -> {
            ex.sendResponseHeaders(200, 0);
            mapper.writeValue(ex.getResponseBody(), Map.of("jwks_uri", "http://127.0.0.1:24680/certificates"));
        });
        httpServer.createContext("/certificates", ex -> {
            ex.sendResponseHeaders(200, 0);
            mapper.writeValue(ex.getResponseBody(), certificates);
        });

        server.startSensinact();
    }

    @AfterAll
    static void stopServer() throws Exception {
        server.stopSensinact();
        httpServer.stop(0);
    }

    @Test
    void testHttp() throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

        Date start = new Date(Instant.now().minus(Duration.ofHours(1)).toEpochMilli());
        Date end = new Date(Instant.now().plus(Duration.ofHours(1)).toEpochMilli());

        String token = Jwts.builder().subject("testUser").issuedAt(start).expiration(end)
                .header().keyId(KEY_ID).and()
                .json(new JacksonSerializer<>(mapper))
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS512).compact();

        awaitServer(client);

        HttpRequest request = HttpRequest
                .newBuilder(URI.create(
                        "http://localhost:8083/sensinact/providers/temp1/services/sensor/resources/temperature/GET"))
                .header("Authorization", "Bearer " + token).GET().build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        assertFalse(response.body().isBlank());
        assertTrue(response.body().contains("\"statusCode\":200"), () -> "Body was " + response.body());

        Pattern valueCapture = Pattern.compile(".+\\\"value\\\":(\\d+\\.\\d+).+");

        Matcher matcher = valueCapture.matcher(response.body());

        assertTrue(matcher.matches());
        assertEquals(15.0d, Double.parseDouble(matcher.group(1)), 30.0d);
    }

    private void awaitServer(HttpClient client) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8083/sensinact")).GET().build();

        // Try a few times to check an Http endpoint is there
        for (int i = 0; i < 10; i++) {
            if (!server.isAlive()) {
                fail("Server process lost");
            }

            try {
                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
                if (401 == response.statusCode()) {
                    return;
                }
            } catch (ConnectException | HttpConnectTimeoutException timeout) {
                // Just try again in a little while
            }
            Thread.sleep(1000);
        }
        fail("Did not contact the server");
    }
}

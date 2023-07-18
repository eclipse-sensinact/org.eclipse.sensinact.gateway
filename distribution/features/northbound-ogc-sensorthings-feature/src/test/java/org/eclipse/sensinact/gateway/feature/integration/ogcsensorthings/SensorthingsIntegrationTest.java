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
package org.eclipse.sensinact.gateway.feature.integration.ogcsensorthings;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import org.eclipse.sensinact.gateway.feature.utilities.test.ServerProcessHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SensorthingsIntegrationTest {

    private static ServerProcessHandler server = new ServerProcessHandler();

    @BeforeAll
    static void startServer() throws Exception {
        server.startSensinact();
    }

    @AfterAll
    static void stopServer() throws Exception {
        server.stopSensinact();
    }

    @Test
    void testHttp() throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8081/v1.1")).GET().build();

        // Try a few times to check an Http endpoint is there
        for (int i = 0; i < 10; i++) {
            if (!server.isAlive()) {
                fail("Server process lost");
            }

            try {
                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
                if (200 == response.statusCode()) {
                    if (200 == response.statusCode()) {
                        // TODO come up with a better test of this
                        assertFalse(response.body().isBlank());
                        return;
                    }

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

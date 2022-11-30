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
package org.eclipse.sensinact.gateway.feature.integration.jakartaservlet;

import static java.lang.ProcessBuilder.Redirect.PIPE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ServletWhiteboardIntegrationTest {

    private static Process OSGI_PROCESS;

    private static Thread outputThread;

    @BeforeAll
    static void startServer() throws Exception {

        String javaCmd = ProcessHandle.current().info().command().orElse("java");
        OSGI_PROCESS = new ProcessBuilder(javaCmd, "-Dsensinact.config.dir=src/it/resources/config",
                "-jar", "target/it/launcher.jar")
                .redirectInput(PIPE)
                .redirectOutput(PIPE)
                .redirectErrorStream(true)
                .start();

        outputThread = new Thread(new InputStreamConsumer(OSGI_PROCESS.getInputStream()));
        outputThread.start();
    }

    @AfterAll
    static void stopServer() throws Exception {

        try {
            OSGI_PROCESS.destroy();
            OSGI_PROCESS.waitFor(5, SECONDS);
        } finally {
            if (OSGI_PROCESS.isAlive()) {
                OSGI_PROCESS.destroyForcibly();
            }
        }

        outputThread.join(1000);
    }

    @Test
    void testHttp() throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8081")).GET().build();

        // Try a few times to check an Http endpoint is there
        for (int i = 0; i < 10; i++) {
            if (!OSGI_PROCESS.isAlive()) {
                fail("Server process lost");
            }

            try {
                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
                assertEquals(404, response.statusCode());
                return;
            } catch (ConnectException | HttpConnectTimeoutException timeout) {
                // Just try again in a little while
                Thread.sleep(1000);
            }
        }
        fail("Did not contact the server");
    }
}

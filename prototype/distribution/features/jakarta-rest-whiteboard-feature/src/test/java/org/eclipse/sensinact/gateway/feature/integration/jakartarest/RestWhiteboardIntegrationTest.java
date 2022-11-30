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
package org.eclipse.sensinact.gateway.feature.integration.jakartarest;

import static java.lang.ProcessBuilder.Redirect.PIPE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import org.eclipse.sensinact.gateway.feature.integration.jakartarest.resource.Resource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.Constants;

class RestWhiteboardIntegrationTest {

    private static Process OSGI_PROCESS;

    @BeforeAll
    static void startServer() throws Exception {

        OSGI_PROCESS = new ProcessBuilder("java", "-Dsensinact.config.dir=src/it/resources/config", "-jar",
                "target/it/launcher.jar").redirectInput(PIPE).redirectOutput(PIPE).redirectErrorStream(true).start();

        try (InputStream is = TinyBundles.bundle()
                .symbolicName("org.eclipse.sensinact.gateway.feature.integration.jakartarest").add(Resource.class)
                .set(Constants.BUNDLE_VERSION, "0.0.1").set("-noee", "true").build(TinyBundles.withBnd())) {
            Files.copy(is, Paths.get("target/it/test.jar"), REPLACE_EXISTING);

            OutputStream stream = OSGI_PROCESS.getOutputStream();
            stream.write(("\nb = install file:target/it/test.jar\n").getBytes(UTF_8));
            stream.flush();

            // Slightly odd, but the previous output is "Bundle ID: <id>"
            stream.write("\nstart ($b substring 11)\n".getBytes(UTF_8));
            stream.flush();
        }
    }

    @AfterAll
    static void stopServer() throws Exception {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            InputStream is = OSGI_PROCESS.getInputStream();

            do {
                baos.writeBytes(is.readNBytes(is.available()));
            } while (is.available() > 0);

            System.out.print(baos.toString(UTF_8));
        } catch (IOException ioe) {
        }

        try {
            OSGI_PROCESS.destroy();
            OSGI_PROCESS.waitFor(5, SECONDS);
        } finally {
            if (OSGI_PROCESS.isAlive()) {
                OSGI_PROCESS.destroyForcibly();
            }
        }
    }

    @Test
    void testHttp() throws Exception {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8082/test")).GET().build();

        // Try a few times to check an Http endpoint is there
        for (int i = 0; i < 10; i++) {
            try {
                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
                if (200 == response.statusCode()) {
                    assertEquals("Hello World", response.body());

                    request = HttpRequest.newBuilder(URI.create("http://localhost:8082/test"))
                            .POST(BodyPublishers.ofString("foobar")).build();
                    response = client.send(request, BodyHandlers.ofString());

                    assertEquals(200, response.statusCode());
                    assertEquals("Hello foobar", response.body());

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

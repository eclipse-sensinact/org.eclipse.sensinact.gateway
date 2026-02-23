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
package org.eclipse.sensinact.gateway.test.testcontainers.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.abort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;

import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

public class TestContainersTest {

    @Test
    void startContainer() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(DockerClientFactory.class.getClassLoader());
        try {
            try {
                DockerClientFactory.lazyClient().versionCmd().exec();
            } catch (Throwable t) {
                abort("No docker executable on the path, so tests will be skipped");
            }
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }

        try(GenericContainer<?> container = new GenericContainer<>(
                DockerImageName.parse("lipanski/docker-static-website")
                .withTag("2.6.0"))) {
            container.addExposedPort(3000);
            container.start();
            container.copyFileToContainer(Transferable.of("Hello!"), "/home/static/hello.txt");

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create("http://" + container.getHost()
                + ":" + container.getMappedPort(3000) + "/hello.txt")).GET().build();
            BodyHandler<String> handler = BodyHandlers.ofString();
            HttpResponse<String> response = client.send(request, handler);
            assertEquals(200, response.statusCode());
            assertEquals("Hello!", response.body());
        }
    }
}


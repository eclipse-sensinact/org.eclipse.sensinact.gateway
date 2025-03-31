/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.northbound.rest.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodySubscribers;
import java.util.Map;

import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;
import org.eclipse.sensinact.northbound.query.api.EResultType;
import org.eclipse.sensinact.northbound.query.dto.result.TypedResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestUtils {

    private final ObjectMapper mapper = new ObjectMapper();

    static final HttpClient client = HttpClient.newHttpClient();

    /**
     * Constructs a DTO to use with DataUpdate
     */
    public GenericDto makeDto(String provider, String service, String resource, Object value, Class<?> type) {
        return makeDto(provider, provider, service, resource, value, type);
    }

    /**
     * Constructs a DTO to use with DataUpdate
     */
    public GenericDto makeDto(String model, String provider, String service, String resource, Object value,
            Class<?> type) {
        GenericDto dto = new GenericDto();
        dto.model = model;
        dto.provider = provider;
        dto.service = service;
        dto.resource = resource;
        dto.value = value;
        dto.type = type;
        return dto;
    }

    /**
     * Executes a GET request and returns its parsed content
     */
    public HttpResponse<?> queryStatus(final String path) throws IOException, InterruptedException {
        // Normalize URI
        final URI targetUri;
        if (path.startsWith("/")) {
            targetUri = URI.create("http://localhost:8185/sensinact" + path);
        } else {
            targetUri = URI.create("http://localhost:8185/sensinact/" + path);
        }

        final HttpRequest req = HttpRequest.newBuilder(targetUri).build();
        return client.send(req, (x) -> BodySubscribers.discarding());
    }

    /**
     * Executes a GET request and returns its parsed content
     */
    public <T> T queryJson(final String path, final Class<T> resultType) throws IOException, InterruptedException {
        return queryJson(path, resultType, Map.of());
    }

    public <T> T queryJson(final String path, final Class<T> resultType, Map<String, String> headers)
            throws IOException, InterruptedException {
        // Normalize URI
        final URI targetUri;
        if (path.startsWith("/")) {
            targetUri = URI.create("http://localhost:8185/sensinact" + path);
        } else {
            targetUri = URI.create("http://localhost:8185/sensinact/" + path);
        }

        Builder builder = HttpRequest.newBuilder(targetUri);
        headers.forEach((a, b) -> builder.header(a, b));

        final HttpRequest req = builder.build();
        final HttpResponse<InputStream> response = client.send(req, (x) -> BodySubscribers.ofInputStream());
        return mapper.createParser(response.body()).readValueAs(resultType);
    }

    /**
     * Executes a POST request and returns its parsed content
     */
    public <T> T queryJson(final String path, final Object body, final Class<T> resultType)
            throws IOException, InterruptedException {
        // Normalize URI
        final URI targetUri;
        if (path.startsWith("/")) {
            targetUri = URI.create("http://localhost:8185/sensinact" + path);
        } else {
            targetUri = URI.create("http://localhost:8185/sensinact/" + path);
        }

        final HttpRequest req = HttpRequest.newBuilder(targetUri).header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(mapper.writeValueAsString(body))).build();
        final HttpResponse<InputStream> response = client.send(req, (x) -> BodySubscribers.ofInputStream());
        return mapper.createParser(response.body()).readValueAs(resultType);
    }

    /**
     * Converts the content of the parsed response
     */
    public <T> T convert(final TypedResponse<?> dto, Class<T> type) {
        return convert(dto.response, type);
    }

    public <T> T convert(final Object o, Class<T> type) {
        return mapper.convertValue(o, type);
    }

    /**
     * Ensures the notification matches the resource update DTO
     */
    public void assertNotification(final GenericDto expected, final ResourceDataNotification notification) {
        assertNotNull(notification);
        assertEquals(expected.provider, notification.provider());
        assertEquals(expected.service, notification.service());
        assertEquals(expected.resource, notification.resource());
        assertEquals(expected.value, notification.newValue());
    }

    /**
     * Checks if the parsed result is successful
     */
    public void assertResultSuccess(final AbstractResultDTO result, final EResultType expectedType,
            final String... uriParts) {
        assertEquals(200, result.statusCode, "Invalid status code");
        assertNull(result.error, "Got an error");
        assertEquals(expectedType, result.type, "Invalid result type");
        assertEquals("/" + String.join("/", uriParts), result.uri, "Invalid URI");
    }

    /**
     * Checks if the parsed result yields a no content
     */
    public void assertResultNoContent(final AbstractResultDTO result, final EResultType expectedType,
            final String... uriParts) {
        assertEquals(204, result.statusCode, "Invalid status code");
        assertEquals("No value set", result.error);
        assertEquals(expectedType, result.type, "Invalid result type");
        assertEquals("/" + String.join("/", uriParts), result.uri, "Invalid URI");
    }
}

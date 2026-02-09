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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.sensorthing.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodySubscribers;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.push.dto.GenericDto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class TestSensorthingsUtils {

    private final Method[] objectMethods = Object.class.getMethods();

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    static final HttpClient client = HttpClient.newHttpClient();

    public ObjectMapper getMapper() {
        return mapper;
    }

    /**
     * Constructs a DTO to use with DataUpdate
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
     * Executes a GET request and returns its response body as a string
     */
    public HttpResponse<String> query(final String path) throws IOException, InterruptedException {
        // Normalize URI
        final URI targetUri;
        if (path.contains("://")) {
            targetUri = URI.create(path);
        } else if (path.startsWith("/")) {
            targetUri = URI.create("http://localhost:8185/v1.1" + path);
        } else {
            targetUri = URI.create("http://localhost:8185/v1.1/" + path);
        }

        final HttpRequest req = HttpRequest.newBuilder(targetUri).build();
        return client.send(req, (x) -> BodySubscribers.ofString(StandardCharsets.UTF_8));
    }

    /**
     * Executes a GET request and returns its parsed content
     */
    public <T> T queryJson(final String path, final TypeReference<T> resultType)
            throws IOException, InterruptedException {
        final HttpResponse<String> response = query(path);
        return mapper.createParser(response.body()).readValueAs(resultType);
    }

    /**
     * Executes a GET request and returns its parsed content
     */
    public <T> T queryJson(final String path, final Class<T> resultType) throws IOException, InterruptedException {
        return queryJson(path, new TypeReference<T>() {
            @Override
            public Type getType() {
                return resultType;
            }
        });
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
     * Executes a GET requests and validates its response code is 200
     */
    public void assertURLStatus(final String url) {
        assertURLStatus(url, 200);
    }

    /**
     * Executes a GET requests and validates its response code
     */
    public void assertURLStatus(final String url, final int expectedStatusCode) {
        final URI targetUri = URI.create(url);
        final HttpRequest req = HttpRequest.newBuilder(targetUri).build();

        try {
            final HttpResponse<InputStream> response = client.send(req, (x) -> BodySubscribers.ofInputStream());
            assertEquals(expectedStatusCode, response.statusCode(), String.format(
                    "Expected <%s> but was: <%s> for URL <%s>", expectedStatusCode, response.statusCode(), url));
        } catch (InterruptedException ex) {
            fail("HTTP request interrupted");
        } catch (IOException e) {
            e.printStackTrace();
            fail("Error querying URL " + targetUri);
        }
    }

    /**
     * Checks if the given object equals the expected one
     */
    public void assertDtoEquals(Object expected, Object actual, Class<?> type) {
        assertDtoEquals(expected, actual, type, "", null);
    }

    private void assertDtoEquals(Object expected, Object actual, Class<?> type, String path, String message) {
        if (expected == null) {
            assertNull(actual, message);
            return;
        }
        assertNotNull(actual, message);

        // Look at class public fields
        for (final Field field : type.getFields()) {
            if((field.getModifiers() & Modifier.STATIC) != 0) {
                // Ignore static fields
                continue;
            }
            if (field.canAccess(expected)) {
                try {
                    Class<?> fieldType = field.getType();
                    Object expectedFieldValue = field.get(expected);
                    Object actualFieldValue = field.get(actual);

                    if (Arrays.stream(fieldType.getDeclaredMethods()).filter(m -> !m.isSynthetic()).count() == 0
                            && fieldType.getMethods().length == objectMethods.length
                            && fieldType.getFields().length > 0) {
                        // We have found another DTO: no method, only public fields
                        assertDtoEquals(expectedFieldValue, actualFieldValue, fieldType, path + "/" + field.getName(),
                                String.format("%s: field <%s> (%s) differs: expected <%s> but was <%s>",
                                        path + "/" + field.getName(), field.getName(), fieldType.getSimpleName(),
                                        expectedFieldValue, actualFieldValue));
                    } else {
                        try {
                            assertEquals(field.get(expected), field.get(actual),
                                    String.format("%s: field <%s> (%s) differs: expected <%s> but was <%s>", path,
                                            field.getName(), fieldType.getSimpleName(), expectedFieldValue,
                                            actualFieldValue));
                        } catch (Throwable t) {
                            System.out.println("Failed on field:" + field);
                            fail(t);
                        }
                    }
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                    fail("Error comparing DTOs");
                }
            }
        }
    }
}

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
package org.eclipse.sensinact.northbound.query.test.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;
import org.eclipse.sensinact.northbound.query.api.EResultType;
import org.eclipse.sensinact.northbound.query.dto.result.TypedResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestUtils {

    private final ObjectMapper mapper = new ObjectMapper();

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
     * Converts the content of the parsed response
     */
    public <T> T convert(final TypedResponse<?> dto, Class<T> type) {
        return mapper.convertValue(dto.response, type);
    }

    /**
     * Ensures the notification matches the resource update DTO
     */
    public void assertNotification(final GenericDto expected, final ResourceDataNotification notification) {
        assertNotNull(notification);
        assertEquals(expected.provider, notification.provider);
        assertEquals(expected.service, notification.service);
        assertEquals(expected.resource, notification.resource);
        assertEquals(expected.value, notification.newValue);
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

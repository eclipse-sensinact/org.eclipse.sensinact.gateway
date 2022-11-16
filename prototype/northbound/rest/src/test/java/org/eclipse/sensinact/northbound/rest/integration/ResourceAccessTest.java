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
package org.eclipse.sensinact.northbound.rest.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.sensinact.northbound.rest.dto.AccessMethodCallParameterDTO;
import org.eclipse.sensinact.northbound.rest.dto.GetResponse;
import org.eclipse.sensinact.northbound.rest.dto.ResultTypedResponseDTO;
import org.eclipse.sensinact.prototype.PrototypePush;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.eclipse.sensinact.prototype.generic.dto.GenericDto;
import org.eclipse.sensinact.prototype.notification.ResourceDataNotification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(ServiceExtension.class)
public class ResourceAccessTest {

    private static final String USER = "user";

    private static final String PROVIDER = "RestAccessProvider";
    private static final String PROVIDER_TOPIC = PROVIDER + "/*";
    private static final String SERVICE = "service";
    private static final String RESOURCE = "resource";
    private static final Integer VALUE = 42;
    private static final Integer VALUE_2 = 84;

    @InjectService
    SensiNactSessionManager sessionManager;

    @InjectService
    PrototypePush push;

    BlockingQueue<ResourceDataNotification> queue;

    final TestUtils utils = new TestUtils();

    @BeforeEach
    void start() throws InterruptedException {
        queue = new ArrayBlockingQueue<>(32);
        SensiNactSession session = sessionManager.getDefaultSession(USER);
        session.addListener(List.of(PROVIDER_TOPIC), (t, e) -> queue.offer(e), null, null, null);
        assertNull(queue.poll(500, TimeUnit.MILLISECONDS));
    }

    @AfterEach
    void stop() {
        SensiNactSession session = sessionManager.getDefaultSession(USER);
        session.activeListeners().keySet().forEach(session::removeListener);
    }

    /**
     * Get the resource value
     */
    @Test
    void resourceGet() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto);
        Instant updateTime = Instant.now();
        // Wait for it
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Check for success
        ResultTypedResponseDTO<?> result = utils.queryJson(
                String.join("/", "providers", PROVIDER, "services", SERVICE, "resources", RESOURCE, "GET"),
                ResultTypedResponseDTO.class);
        utils.assertResultSuccess(result, "GET_RESPONSE", PROVIDER, SERVICE, RESOURCE);
        GetResponse<?> response = utils.convert(result, GetResponse.class);
        assertEquals(RESOURCE, response.name);
        assertEquals(VALUE, response.value);
        assertEquals(dto.type.getName(), response.type);
        assertFalse(updateTime.isBefore(Instant.ofEpochMilli(response.timestamp)), "Timestamp wasn't updated");
    }

    /**
     * Check resource update
     */
    @Test
    void resourceUpdate() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto);
        Instant firstTime = Instant.now();

        // Wait for it
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Check response
        ResultTypedResponseDTO<?> result = utils.queryJson(
                String.join("/", "providers", PROVIDER, "services", SERVICE, "resources", RESOURCE, "GET"),
                ResultTypedResponseDTO.class);
        GetResponse<?> response = utils.convert(result, GetResponse.class);
        assertEquals(VALUE, response.value);
        assertFalse(firstTime.isBefore(Instant.ofEpochMilli(response.timestamp)), "Timestamp wasn't updated");

        // Update value
        dto.value = VALUE_2;
        push.pushUpdate(dto);
        Instant secondTime = Instant.now();

        // Wait for it
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Check for success
        result = utils.queryJson(
                String.join("/", "providers", PROVIDER, "services", SERVICE, "resources", RESOURCE, "GET"),
                ResultTypedResponseDTO.class);
        response = utils.convert(result, GetResponse.class);
        assertEquals(VALUE_2, response.value);
        assertFalse(secondTime.isBefore(Instant.ofEpochMilli(response.timestamp)), "Timestamp wasn't updated");
    }

    /**
     * Update the resource value from the REST endpoint
     */
    @Test
    void resourceSet() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto);
        Instant firstUpdateTime = Instant.now();
        // Wait for it
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Check response
        ResultTypedResponseDTO<?> result = utils.queryJson(
                String.join("/", "providers", PROVIDER, "services", SERVICE, "resources", RESOURCE, "GET"),
                ResultTypedResponseDTO.class);
        GetResponse<?> response = utils.convert(result, GetResponse.class);
        assertEquals(VALUE, response.value);

        Instant firstTimestamp = Instant.ofEpochMilli(response.timestamp);
        assertFalse(firstUpdateTime.isBefore(firstTimestamp), "Timestamp wasn't updated");

        AccessMethodCallParameterDTO param = new AccessMethodCallParameterDTO();
        param.name = "value";
        param.type = Integer.class.getName();
        param.value = VALUE_2;
        result = utils.queryJson(
                String.join("/", "providers", PROVIDER, "services", SERVICE, "resources", RESOURCE, "SET"),
                List.of(param), ResultTypedResponseDTO.class);
        utils.assertResultSuccess(result, "SET_RESPONSE", PROVIDER, SERVICE, RESOURCE);
        response = utils.convert(result, GetResponse.class);
        assertEquals(RESOURCE, response.name);
        assertEquals(param.type, response.type);
        assertEquals(VALUE_2, response.value);

        // Wait for internal notification
        dto.value = VALUE_2;
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Check access
        result = utils.queryJson(
                String.join("/", "providers", PROVIDER, "services", SERVICE, "resources", RESOURCE, "GET"),
                ResultTypedResponseDTO.class);
        response = utils.convert(result, GetResponse.class);
        assertEquals(VALUE_2, response.value);
        assertTrue(firstTimestamp.isBefore(Instant.ofEpochMilli(response.timestamp)), "Timestamp wasn't updated");
    }

    /**
     * Get a resource value from the admin service
     */
    @Test
    void adminDefaultValues() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto);
        // Wait for it
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // friendlyName should be the provider name
        ResultTypedResponseDTO<?> result = utils.queryJson(
                String.join("/", "providers", PROVIDER, "services", "admin", "resources", "friendlyName", "GET"),
                ResultTypedResponseDTO.class);
        utils.assertResultSuccess(result, "GET_RESPONSE", PROVIDER, "admin", "friendlyName");
        GetResponse<?> response = utils.convert(result, GetResponse.class);
        assertEquals(String.class.getName(), response.type);
        assertEquals(PROVIDER, response.value);

        // Location should be null, but set
        result = utils.queryJson(
                String.join("/", "providers", PROVIDER, "services", "admin", "resources", "location", "GET"),
                ResultTypedResponseDTO.class);
        utils.assertResultSuccess(result, "GET_RESPONSE", PROVIDER, "admin", "location");
        response = utils.convert(result, GetResponse.class);
        assertEquals(String.class.getName(), response.type);
        assertNull(response.value);
    }
}

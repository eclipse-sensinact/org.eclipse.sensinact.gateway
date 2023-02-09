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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.sensinact.northbound.rest.dto.GetResponse;
import org.eclipse.sensinact.northbound.rest.dto.ProviderDescriptionDTO;
import org.eclipse.sensinact.northbound.rest.dto.ResultResourcesListDTO;
import org.eclipse.sensinact.northbound.rest.dto.ResultServicesListDTO;
import org.eclipse.sensinact.northbound.rest.dto.ResultTypedResponseDTO;
import org.eclipse.sensinact.prototype.PrototypePush;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.eclipse.sensinact.prototype.generic.dto.BulkGenericDto;
import org.eclipse.sensinact.prototype.generic.dto.GenericDto;
import org.eclipse.sensinact.prototype.notification.ResourceDataNotification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(ServiceExtension.class)
public class MissingEntityTest {

    private static final String USER = "user";

    private static final String PROVIDER = "RestMissingSvcProvider";
    private static final String PROVIDER_TOPIC = PROVIDER + "/*";
    private static final String SERVICE = "service";
    private static final String RESOURCE = "resource";
    private static final Integer VALUE = 42;

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
        queue.clear();
        queue = null;
    }

    /**
     * Missing provider should return a 404
     */
    @Test
    void missingProvider() throws Exception {
        final String missingProvider = PROVIDER + "__missing__";
        ResultTypedResponseDTO<?> typedResult;

        // Service description
        typedResult = utils.queryJson(String.join("/", "providers", missingProvider, "services", SERVICE),
                ResultTypedResponseDTO.class);
        assertEquals(404, typedResult.statusCode);
        assertNull(typedResult.response);
        assertNotNull(typedResult.error);
        assertFalse(typedResult.error.isEmpty());

        // Resources list
        ResultResourcesListDTO rcListResult = utils.queryJson(
                String.join("/", "providers", missingProvider, "services", SERVICE, "resources"),
                ResultResourcesListDTO.class);
        assertEquals(404, rcListResult.statusCode);
        assertNull(rcListResult.resources);
        assertNotNull(rcListResult.error);
        assertFalse(rcListResult.error.isEmpty());

        // Resource description
        typedResult = utils.queryJson(
                String.join("/", "providers", missingProvider, "services", SERVICE, "resources", RESOURCE),
                ResultTypedResponseDTO.class);
        assertEquals(404, typedResult.statusCode);
        assertNull(typedResult.response);
        assertNotNull(typedResult.error);
        assertFalse(typedResult.error.isEmpty());

        // Resource GET
        typedResult = utils.queryJson(
                String.join("/", "providers", missingProvider, "services", SERVICE, "resources", RESOURCE, "GET"),
                ResultTypedResponseDTO.class);
        assertEquals(404, typedResult.statusCode);
        assertNull(typedResult.response);
        assertNotNull(typedResult.error);
        assertFalse(typedResult.error.isEmpty());

        // Provider description
        typedResult = utils.queryJson(String.join("/", "providers", missingProvider), ResultTypedResponseDTO.class);
        assertEquals(404, typedResult.statusCode);
        assertNull(typedResult.response);
        assertNotNull(typedResult.error);
        assertFalse(typedResult.error.isEmpty());

        // Services list
        ResultServicesListDTO svcListResult = utils
                .queryJson(String.join("/", "providers", missingProvider, "services"), ResultServicesListDTO.class);
        assertEquals(404, svcListResult.statusCode);
        assertNull(svcListResult.services);
        assertNotNull(svcListResult.error);
        assertFalse(svcListResult.error.isEmpty());
    }

    /**
     * Missing service should return a 404
     */
    @Test
    void missingService() throws Exception {
        String provider_service = PROVIDER + "_service";
        // Register the resource
        GenericDto dto = utils.makeDto(provider_service, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto).getValue();

        // Check value
        ResultTypedResponseDTO<?> result = utils.queryJson(
                String.join("/", "providers", provider_service, "services", SERVICE, "resources", RESOURCE, "GET"),
                ResultTypedResponseDTO.class);
        GetResponse<?> response = utils.convert(result, GetResponse.class);
        assertEquals(VALUE, response.value);

        final String missingService = SERVICE + "__missing__";
        ResultTypedResponseDTO<?> typedResult;

        // Service description
        typedResult = utils.queryJson(String.join("/", "providers", provider_service, "services", missingService),
                ResultTypedResponseDTO.class);
        assertEquals(404, typedResult.statusCode);
        assertNull(typedResult.response);
        assertNotNull(typedResult.error);
        assertFalse(typedResult.error.isEmpty());

        // Resources list
        ResultResourcesListDTO rcListResult = utils.queryJson(
                String.join("/", "providers", provider_service, "services", missingService, "resources"),
                ResultResourcesListDTO.class);
        assertEquals(404, rcListResult.statusCode);
        assertNull(rcListResult.resources);
        assertNotNull(rcListResult.error);
        assertFalse(rcListResult.error.isEmpty());

        // Resource description
        typedResult = utils.queryJson(
                String.join("/", "providers", provider_service, "services", missingService, "resources", RESOURCE),
                ResultTypedResponseDTO.class);
        assertEquals(404, typedResult.statusCode);
        assertNull(typedResult.response);
        assertNotNull(typedResult.error);
        assertFalse(typedResult.error.isEmpty());

        // Resource GET
        typedResult = utils.queryJson(String.join("/", "providers", provider_service, "services", missingService,
                "resources", RESOURCE, "GET"), ResultTypedResponseDTO.class);
        assertEquals(404, typedResult.statusCode);
        assertNull(typedResult.response);
        assertNotNull(typedResult.error);
        assertFalse(typedResult.error.isEmpty());

        // Provider description
        typedResult = utils.queryJson(String.join("/", "providers", provider_service), ResultTypedResponseDTO.class);
        assertEquals(200, typedResult.statusCode);
        ProviderDescriptionDTO provider = utils.convert(typedResult, ProviderDescriptionDTO.class);
        assertEquals(provider_service, provider.name);
        assertFalse(provider.services.contains(missingService), "Missing service is registered");

        // Services list
        ResultServicesListDTO svcListResult = utils
                .queryJson(String.join("/", "providers", provider_service, "services"), ResultServicesListDTO.class);
        assertEquals(200, svcListResult.statusCode);
        assertFalse(svcListResult.services.contains(missingService), "Missing service is registered");
    }

    /**
     * Missing resource should return a 404 Not Found
     */
    @Test
    void missingResource() throws Exception {
        String provider_resource = PROVIDER + "_resource";
        // Register the resource
        GenericDto dto = utils.makeDto(provider_resource, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto).getValue();

        final String missingResource = RESOURCE + "__missing__";

        // Check resources list
        ResultResourcesListDTO rcListResult = utils.queryJson(
                String.join("/", "providers", provider_resource, "services", SERVICE, "resources"),
                ResultResourcesListDTO.class);
        assertEquals(200, rcListResult.statusCode);
        assertFalse(rcListResult.resources.contains(missingResource), "Missing resource is registered");

        // Get value
        ResultTypedResponseDTO<?> result = utils.queryJson(String.join("/", "providers", provider_resource, "services",
                SERVICE, "resources", missingResource, "GET"), ResultTypedResponseDTO.class);
        assertEquals(404, result.statusCode);
        assertNotNull(result.error, "No warning message set");
        assertFalse(result.error.isEmpty(), "No warning message set");
    }

    /**
     * Unset resources should return a 204 No content
     */
    @Test
    void unsetResource() throws Exception {
        String provider2 = PROVIDER + "_2";
        // Register Resource in the model and create a second provider without it set
        BulkGenericDto dto = new BulkGenericDto();
        dto.dtos = List.of(utils.makeDto("model", PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class),
                utils.makeDto("model", provider2, "admin", "friendlyName", "test", String.class));

        // Push and wait for it
        push.pushUpdate(dto).getValue();

        // Check resources list
        ResultResourcesListDTO rcListResult = utils.queryJson(
                String.join("/", "providers", provider2, "services", SERVICE, "resources"),
                ResultResourcesListDTO.class);
        assertEquals(200, rcListResult.statusCode);
        assertTrue(rcListResult.resources.contains(RESOURCE), "Resource is not registered");

        // Get value
        Instant queryTime = Instant.now();
        ResultTypedResponseDTO<?> result = utils.queryJson(
                String.join("/", "providers", provider2, "services", SERVICE, "resources", RESOURCE, "GET"),
                ResultTypedResponseDTO.class);
        assertEquals(204, result.statusCode);
        assertNotNull(result.error, "No warning message set");
        assertFalse(result.error.isEmpty(), "No warning message set");
        GetResponse<?> response = utils.convert(result, GetResponse.class);
        assertNotNull(response, "No empty value response");
        assertFalse(queryTime.isAfter(Instant.ofEpochMilli(response.timestamp)), "Missing resource has a timestamp");
        assertNull(response.value, "Got a value for a missing resource");
    }
}

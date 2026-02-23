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

import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.northbound.query.dto.result.ErrorResultDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseDescribeProviderDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseGetDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultListResourcesDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultListServicesDTO;
import org.eclipse.sensinact.northbound.query.dto.result.TypedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.service.ServiceAware;

import jakarta.ws.rs.core.Application;

@WithConfiguration(pid = "sensinact.session.manager", properties = @Property(key = "auth.policy", value = "ALLOW_ALL"))
public class MissingEntityTest {

    @BeforeEach
    public void await(
            @InjectConfiguration(withConfig = @WithConfiguration(pid = "sensinact.northbound.rest", location = "?", properties = {
                    @Property(key = "allow.anonymous", value = "true"),
                    @Property(key = "bar", value = "foobar") })) Configuration cm,
            @InjectService(filter = "(bar=foobar)", cardinality = 0) ServiceAware<Application> a)
            throws InterruptedException {
        a.waitForService(5000);
        for (int i = 0; i < 10; i++) {
            try {
                HttpResponse<?> queryStatus = utils.queryStatus("/");
                if (queryStatus.statusCode() == 200)
                    return;
                else
                    System.err.println(String.format("Response: %d - %s",
                            queryStatus.statusCode(), queryStatus.body()));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Thread.sleep(200);
        }
        throw new AssertionFailedError("REST API did not appear");
    }

    private static final String PROVIDER = "RestMissingSvcProvider";
    private static final String SERVICE = "service";
    private static final String RESOURCE = "resource";
    private static final Integer VALUE = 42;

    @InjectService
    DataUpdate push;

    final TestUtils utils = new TestUtils();

    /**
     * Missing provider should return a 404
     */
    @Test
    void missingProvider() throws Exception {
        final String missingProvider = PROVIDER + "__missing__";
        ErrorResultDTO typedResult;

        // Service description
        typedResult = utils.queryJson(String.join("/", "providers", missingProvider, "services", SERVICE),
                ErrorResultDTO.class);
        assertEquals(404, typedResult.statusCode);
        assertNotNull(typedResult.error);
        assertFalse(typedResult.error.isEmpty());

        // Resources list
        ErrorResultDTO rcListResult = utils.queryJson(
                String.join("/", "providers", missingProvider, "services", SERVICE, "resources"), ErrorResultDTO.class);
        assertEquals(404, rcListResult.statusCode);
        assertNotNull(rcListResult.error);
        assertFalse(rcListResult.error.isEmpty());

        // Resource description
        typedResult = utils.queryJson(
                String.join("/", "providers", missingProvider, "services", SERVICE, "resources", RESOURCE),
                ErrorResultDTO.class);
        assertEquals(404, typedResult.statusCode);
        assertNotNull(typedResult.error);
        assertFalse(typedResult.error.isEmpty());

        // Resource GET
        typedResult = utils.queryJson(
                String.join("/", "providers", missingProvider, "services", SERVICE, "resources", RESOURCE, "GET"),
                ErrorResultDTO.class);
        assertEquals(404, typedResult.statusCode);
        assertNotNull(typedResult.error);
        assertFalse(typedResult.error.isEmpty());

        // Provider description
        typedResult = utils.queryJson(String.join("/", "providers", missingProvider), ErrorResultDTO.class);
        assertEquals(404, typedResult.statusCode);
        assertNotNull(typedResult.error);
        assertFalse(typedResult.error.isEmpty());

        // Services list
        ErrorResultDTO svcListResult = utils.queryJson(String.join("/", "providers", missingProvider, "services"),
                ErrorResultDTO.class);
        assertEquals(404, svcListResult.statusCode);
        assertNotNull(svcListResult.error);
        assertFalse(svcListResult.error.isEmpty());
    }

    /**
     * Missing service should return a 404
     */
    @Test
    void missingService() throws Exception {
        String provider_service = PROVIDER + "Service";
        // Register the resource
        GenericDto dto = utils.makeDto(provider_service, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto).getValue();

        // Check value
        TypedResponse<?> result = utils.queryJson(
                String.join("/", "providers", provider_service, "services", SERVICE, "resources", RESOURCE, "GET"),
                TypedResponse.class);
        ResponseGetDTO response = utils.convert(result, ResponseGetDTO.class);
        assertEquals(VALUE, response.value);

        final String missingService = SERVICE + "__missing__";
        ErrorResultDTO errorResult;

        // Service description
        errorResult = utils.queryJson(String.join("/", "providers", provider_service, "services", missingService),
                ErrorResultDTO.class);
        assertEquals(404, errorResult.statusCode);
        assertNotNull(errorResult.error);
        assertFalse(errorResult.error.isEmpty());

        // Resources list
        errorResult = utils.queryJson(
                String.join("/", "providers", provider_service, "services", missingService, "resources"),
                ErrorResultDTO.class);
        assertEquals(404, errorResult.statusCode);
        assertNotNull(errorResult.error);
        assertFalse(errorResult.error.isEmpty());

        // Resource description
        errorResult = utils.queryJson(
                String.join("/", "providers", provider_service, "services", missingService, "resources", RESOURCE),
                ErrorResultDTO.class);
        assertEquals(404, errorResult.statusCode);
        assertNotNull(errorResult.error);
        assertFalse(errorResult.error.isEmpty());

        // Resource GET
        errorResult = utils.queryJson(String.join("/", "providers", provider_service, "services", missingService,
                "resources", RESOURCE, "GET"), ErrorResultDTO.class);
        assertEquals(404, errorResult.statusCode);
        assertNotNull(errorResult.error);
        assertFalse(errorResult.error.isEmpty());

        // Provider description
        TypedResponse<?> typedResult = utils.queryJson(String.join("/", "providers", provider_service),
                TypedResponse.class);
        assertEquals(200, typedResult.statusCode);
        ResponseDescribeProviderDTO provider = utils.convert(typedResult, ResponseDescribeProviderDTO.class);
        assertEquals(provider_service, provider.name);
        assertFalse(provider.services.contains(missingService), "Missing service is registered");

        // Services list
        ResultListServicesDTO svcListResult = utils
                .queryJson(String.join("/", "providers", provider_service, "services"), ResultListServicesDTO.class);
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
        ResultListResourcesDTO rcListResult = utils.queryJson(
                String.join("/", "providers", provider_resource, "services", SERVICE, "resources"),
                ResultListResourcesDTO.class);
        assertEquals(200, rcListResult.statusCode);
        assertFalse(rcListResult.resources.contains(missingResource), "Missing resource is registered");

        // Get value
        ErrorResultDTO result = utils.queryJson(String.join("/", "providers", provider_resource, "services", SERVICE,
                "resources", missingResource, "GET"), ErrorResultDTO.class);
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
        ResultListResourcesDTO rcListResult = utils.queryJson(
                String.join("/", "providers", provider2, "services", SERVICE, "resources"),
                ResultListResourcesDTO.class);
        assertEquals(200, rcListResult.statusCode);
        assertTrue(rcListResult.resources.contains(RESOURCE), "Resource is not registered");

        // Get value
        Instant queryTime = Instant.now();
        TypedResponse<?> result = utils.queryJson(
                String.join("/", "providers", provider2, "services", SERVICE, "resources", RESOURCE, "GET"),
                TypedResponse.class);
        assertEquals(204, result.statusCode);
        assertNotNull(result.error, "No warning message set");
        assertFalse(result.error.isEmpty(), "No warning message set");
        ResponseGetDTO response = utils.convert(result, ResponseGetDTO.class);
        assertNotNull(response, "No empty value response");
        assertFalse(queryTime.isAfter(Instant.ofEpochMilli(response.timestamp)), "Missing resource has a timestamp");
        assertNull(response.value, "Got a value for a missing resource");
    }
}

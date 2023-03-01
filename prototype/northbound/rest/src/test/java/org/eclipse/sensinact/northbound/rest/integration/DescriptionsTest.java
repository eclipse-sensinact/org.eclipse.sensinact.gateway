/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.sensinact.northbound.query.api.EResultType;
import org.eclipse.sensinact.northbound.query.dto.result.CompleteProviderDescriptionDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseDescribeProviderDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseDescribeResourceDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseDescribeServiceDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultDescribeProvidersDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultListProvidersDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultListResourcesDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultListServicesDTO;
import org.eclipse.sensinact.northbound.query.dto.result.TypedResponse;
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
public class DescriptionsTest {

    private static final String USER = "user";

    private static final String PROVIDER = "RestDescriptionProvider";
    private static final String PROVIDER_2 = PROVIDER + "_2";
    private static final String PROVIDER_3 = PROVIDER + "_3";
    private static final String PROVIDER_TOPIC = PROVIDER + "/*";
    private static final String PROVIDER_2_TOPIC = PROVIDER_2 + "/*";
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
        session.addListener(List.of(PROVIDER_TOPIC, PROVIDER_2_TOPIC), (t, e) -> queue.offer(e), null, null, null);
        assertNull(queue.poll(500, TimeUnit.MILLISECONDS));
    }

    @AfterEach
    void stop() {
        SensiNactSession session = sessionManager.getDefaultSession(USER);
        session.activeListeners().keySet().forEach(session::removeListener);
    }

    /**
     * Check the <code>/sensinact/</code> endpoint: full description of providers,
     * services and resources
     */
    @Test
    void completeList() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto);
        // Wait for it
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Check for success
        final ResultDescribeProvidersDTO result = utils.queryJson("/", ResultDescribeProvidersDTO.class);
        utils.assertResultSuccess(result, EResultType.COMPLETE_LIST);

        // Check content
        final CompleteProviderDescriptionDTO providerDto = result.providers.stream()
                .filter((p) -> PROVIDER.equals(p.name)).findFirst().get();

        // Admin + test service
        boolean gotAdmin = false;
        boolean gotService = false;

        for (final ResponseDescribeServiceDTO svc : providerDto.services) {
            final List<String> resources = svc.resources.stream().map((r) -> r.name).collect(Collectors.toList());

            switch (svc.name) {
            case "admin":
                gotAdmin = true;
                assertTrue(resources.contains("location"), "Location admin resource is missing");
                assertTrue(resources.contains("friendlyName"), "FriendlyName admin resource is missing");
                break;

            case SERVICE:
                gotService = true;
                // Look for the resource
                assertEquals(1, svc.resources.size(), "Too many resources for the test service: " + resources);
                assertEquals(RESOURCE, svc.resources.get(0).name, "Wrong test service resource name");
                break;

            default:
                fail("Unknown service found: " + svc.name);
            }
        }
        assertTrue(gotAdmin, "Admin service not found");
        assertTrue(gotService, "Test service not found");
    }

    /**
     * Check the <code>/sensinact/providers</code> endpoint: list of names of
     * providers
     */
    @Test
    void providersList() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto);
        // Wait for it
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Check the list of providers
        ResultListProvidersDTO result = utils.queryJson("/providers", ResultListProvidersDTO.class);
        utils.assertResultSuccess(result, EResultType.PROVIDERS_LIST);
        assertFalse(result.providers.contains(PROVIDER_2), "Unexpected provider is present");
        assertTrue(result.providers.contains("sensiNact"), "sensiNact provider is missing");
        assertTrue(result.providers.contains(PROVIDER), "Expected provider is missing");

        // Add another provider
        dto.provider = PROVIDER_2;
        dto.model = dto.provider;
        push.pushUpdate(dto);
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Check the new list
        result = utils.queryJson("/providers", ResultListProvidersDTO.class);
        utils.assertResultSuccess(result, EResultType.PROVIDERS_LIST);
        assertTrue(result.providers.contains("sensiNact"), "sensiNact provider is missing");
        assertTrue(result.providers.contains(PROVIDER), "Expected provider is missing");
        assertTrue(result.providers.contains(PROVIDER_2), "Expected provider 2 is missing");
    }

    /**
     * Check the <code>/sensinact/providers/{PROVIDER}</code> endpoint: description
     * of a single provider
     */
    @Test
    void providerDescription() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto);
        // Wait for it
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Check the list of providers
        TypedResponse<?> result = utils.queryJson("/providers/" + PROVIDER, TypedResponse.class);
        utils.assertResultSuccess(result, EResultType.DESCRIBE_PROVIDER, PROVIDER);
        ResponseDescribeProviderDTO descr = utils.convert(result, ResponseDescribeProviderDTO.class);
        assertEquals(PROVIDER, descr.name);
        assertEquals(Set.of("admin", SERVICE), Set.copyOf(descr.services));
    }

    /**
     * Check the <code>/sensinact/providers/{PROVIDER}/service</code> endpoint: list
     * of services of a single provider
     */
    @Test
    void servicesList() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto);
        // Wait for it
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Check the list of providers
        ResultListServicesDTO result = utils.queryJson("/providers/" + PROVIDER + "/services",
                ResultListServicesDTO.class);
        utils.assertResultSuccess(result, EResultType.SERVICES_LIST, PROVIDER);
        assertEquals(Set.of("admin", SERVICE), Set.copyOf(result.services));
    }

    /**
     * Check the <code>/sensinact/providers/{PROVIDER}/service/{SERVICE}</code>
     * endpoint: description of a single service
     */
    @Test
    void serviceDescription() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto);
        // Wait for it
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Check the list of providers
        TypedResponse<?> result = utils.queryJson("/providers/" + PROVIDER + "/services/" + SERVICE,
                TypedResponse.class);
        utils.assertResultSuccess(result, EResultType.DESCRIBE_SERVICE, PROVIDER, SERVICE);
        ResponseDescribeServiceDTO descr = utils.convert(result, ResponseDescribeServiceDTO.class);
        assertEquals(SERVICE, descr.name);
        assertEquals(List.of(RESOURCE), descr.resources.stream().map((r) -> r.name).collect(Collectors.toList()));
    }

    /**
     * Check the
     * <code>/sensinact/providers/{PROVIDER}/service/{SERVICE}/resources</code>
     * endpoint: list the resources of a single service
     */
    @Test
    void resourcesList() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto);
        // Wait for it
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Check the list of providers
        ResultListResourcesDTO result = utils.queryJson(
                "/providers/" + PROVIDER + "/services/" + SERVICE + "/resources", ResultListResourcesDTO.class);
        utils.assertResultSuccess(result, EResultType.RESOURCES_LIST, PROVIDER, SERVICE);
        assertEquals(List.of(RESOURCE), result.resources);
    }

    /**
     * Check the
     * <code>/sensinact/providers/{PROVIDER}/service/{SERVICE}/resources/{RESOURCE}</code>
     * endpoint: list the resources of a single service
     */
    @Test
    void resourceDescription() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto);
        // Wait for it
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Check the list of providers
        TypedResponse<?> result = utils.queryJson(
                "/providers/" + PROVIDER + "/services/" + SERVICE + "/resources/" + RESOURCE, TypedResponse.class);
        utils.assertResultSuccess(result, EResultType.DESCRIBE_RESOURCE, PROVIDER, SERVICE, RESOURCE);
        assertEquals(RESOURCE, utils.convert(result, ResponseDescribeResourceDTO.class).name);
    }

    /**
     * Check filter passing
     */
    @Test
    void filterTest() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, 12345678, Integer.class);
        push.pushUpdate(dto).getValue();

        GenericDto dto2 = utils.makeDto(PROVIDER_3, SERVICE, RESOURCE, 12345679, Integer.class);
        push.pushUpdate(dto2).getValue();

        ResultDescribeProvidersDTO result = utils.queryJson(
                "/?filter=(" + SERVICE + "." + RESOURCE + "=" + dto.value + ")", ResultDescribeProvidersDTO.class);
        utils.assertResultSuccess(result, EResultType.COMPLETE_LIST);

        // Check content
        assertEquals(1, result.providers.size());
        assertEquals(PROVIDER, result.providers.get(0).name);

        result = utils.queryJson("/?filter=(" + SERVICE + "." + RESOURCE + "=" + dto2.value + ")",
                ResultDescribeProvidersDTO.class);
        utils.assertResultSuccess(result, EResultType.COMPLETE_LIST);

        // Check content
        assertEquals(1, result.providers.size());
        assertEquals(PROVIDER_3, result.providers.get(0).name);
    }
}

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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.sensinact.northbound.rest.dto.CompleteProviderDescriptionDTO;
import org.eclipse.sensinact.northbound.rest.dto.CompleteResourceDescriptionDTO;
import org.eclipse.sensinact.northbound.rest.dto.CompleteServiceDescriptionDTO;
import org.eclipse.sensinact.northbound.rest.dto.ProviderDescriptionDTO;
import org.eclipse.sensinact.northbound.rest.dto.ResultCompleteListDTO;
import org.eclipse.sensinact.northbound.rest.dto.ResultProvidersListDTO;
import org.eclipse.sensinact.northbound.rest.dto.ResultResourcesListDTO;
import org.eclipse.sensinact.northbound.rest.dto.ResultServicesListDTO;
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
public class DescriptionsTest {

    private static final String USER = "user";

    private static final String PROVIDER = "RestDescriptionProvider";
    private static final String PROVIDER_2 = PROVIDER + "_2";
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
        final ResultCompleteListDTO result = utils.queryJson("/", ResultCompleteListDTO.class);
        utils.assertResultSuccess(result, "COMPLETE_LIST");

        // Check content
        final CompleteProviderDescriptionDTO providerDto = result.providers.stream()
                .filter((p) -> PROVIDER.equals(p.name)).findFirst().get();

        // Admin + test service
        boolean gotAdmin = false;
        boolean gotService = false;

        for (final CompleteServiceDescriptionDTO svc : providerDto.services) {
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
        ResultProvidersListDTO result = utils.queryJson("/providers", ResultProvidersListDTO.class);
        utils.assertResultSuccess(result, "PROVIDERS_LIST");
        assertEquals(result.providers, List.of(PROVIDER));

        // Add another provider
        dto.provider = PROVIDER_2;
        dto.model = dto.provider;
        push.pushUpdate(dto);
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Check the new list
        result = utils.queryJson("/providers", ResultProvidersListDTO.class);
        utils.assertResultSuccess(result, "PROVIDERS_LIST");
        assertEquals(Set.of(PROVIDER, PROVIDER_2), Set.copyOf(result.providers));
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
        ResultTypedResponseDTO<?> result = utils.queryJson("/providers/" + PROVIDER, ResultTypedResponseDTO.class);
        utils.assertResultSuccess(result, "DESCRIBE_PROVIDER", PROVIDER);
        ProviderDescriptionDTO descr = utils.convert(result, ProviderDescriptionDTO.class);
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
        ResultServicesListDTO result = utils.queryJson("/providers/" + PROVIDER + "/services",
                ResultServicesListDTO.class);
        utils.assertResultSuccess(result, "SERVICES_LIST", PROVIDER);
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
        ResultTypedResponseDTO<?> result = utils.queryJson("/providers/" + PROVIDER + "/services/" + SERVICE,
                ResultTypedResponseDTO.class);
        utils.assertResultSuccess(result, "DESCRIBE_SERVICE", PROVIDER, SERVICE);
        CompleteServiceDescriptionDTO descr = utils.convert(result, CompleteServiceDescriptionDTO.class);
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
        ResultResourcesListDTO result = utils.queryJson(
                "/providers/" + PROVIDER + "/services/" + SERVICE + "/resources", ResultResourcesListDTO.class);
        utils.assertResultSuccess(result, "RESOURCES_LIST", PROVIDER, SERVICE);
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
        ResultTypedResponseDTO<?> result = utils.queryJson(
                "/providers/" + PROVIDER + "/services/" + SERVICE + "/resources/" + RESOURCE,
                ResultTypedResponseDTO.class);
        utils.assertResultSuccess(result, "DESCRIBE_RESOURCE", PROVIDER, SERVICE, RESOURCE);
        assertEquals(RESOURCE, utils.convert(result, CompleteResourceDescriptionDTO.class).name);
    }
}

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
package org.eclipse.sensinact.northbound.query.test.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;
import org.eclipse.sensinact.northbound.query.api.EResultType;
import org.eclipse.sensinact.northbound.query.api.IQueryHandler;
import org.eclipse.sensinact.northbound.query.dto.SensinactPath;
import org.eclipse.sensinact.northbound.query.dto.query.QueryDescribeDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryListDTO;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(ServiceExtension.class)
public class DescriptionsTest {

    private static final String USER = "user";

    private static final String PROVIDER = "QueryHandlerDescriptionProvider";
    private static final String PROVIDER_2 = PROVIDER + "_2";
    private static final String SERVICE = "service";
    private static final String RESOURCE = "resource";
    private static final Integer VALUE = 42;

    @InjectService
    SensiNactSessionManager sessionManager;
    SensiNactSession session;

    @InjectService
    PrototypePush push;

    @InjectService
    IQueryHandler handler;

    final TestUtils utils = new TestUtils();

    @BeforeEach
    void start() {
        session = sessionManager.getDefaultSession(USER);
    }

    @AfterEach
    void stop() {
        session = null;
    }

    /**
     * Complete system description
     */
    @Test
    void completeList() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto).getValue();

        final QueryDescribeDTO query = new QueryDescribeDTO();
        query.uri = new SensinactPath();

        // Check for success
        final AbstractResultDTO rawResult = handler.handleQuery(session, query);
        utils.assertResultSuccess(rawResult, EResultType.COMPLETE_LIST);
        final ResultDescribeProvidersDTO result = (ResultDescribeProvidersDTO) rawResult;

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
     * List of names of providers
     */
    @Test
    void providersList() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto).getValue();

        // Check the list of providers
        final QueryListDTO query = new QueryListDTO();
        query.uri = new SensinactPath();
        AbstractResultDTO rawResult = handler.handleQuery(session, query);
        utils.assertResultSuccess(rawResult, EResultType.PROVIDERS_LIST);

        ResultListProvidersDTO result = (ResultListProvidersDTO) rawResult;
        assertEquals(Set.of("sensiNact", PROVIDER), Set.copyOf(result.providers));

        // Add another provider
        dto.provider = PROVIDER_2;
        dto.model = dto.provider;
        push.pushUpdate(dto).getValue();

        // Check the new list
        rawResult = handler.handleQuery(session, query);
        utils.assertResultSuccess(rawResult, EResultType.PROVIDERS_LIST);
        result = (ResultListProvidersDTO) rawResult;
        assertEquals(Set.of("sensiNact", PROVIDER, PROVIDER_2), Set.copyOf(result.providers));
    }

    /**
     * Description of a single provider
     */
    @Test
    void providerDescription() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto).getValue();

        // Check the list of providers
        final QueryDescribeDTO query = new QueryDescribeDTO();
        query.uri = new SensinactPath(PROVIDER);
        AbstractResultDTO rawResult = handler.handleQuery(session, query);
        utils.assertResultSuccess(rawResult, EResultType.DESCRIBE_PROVIDER, PROVIDER);

        TypedResponse<?> result = (TypedResponse<?>) rawResult;
        ResponseDescribeProviderDTO descr = utils.convert(result, ResponseDescribeProviderDTO.class);
        assertEquals(PROVIDER, descr.name);
        assertEquals(Set.of("admin", SERVICE), Set.copyOf(descr.services));
    }

    /**
     * List of services of a single provider
     */
    @Test
    void servicesList() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto).getValue();

        // Check the list of services
        final QueryListDTO query = new QueryListDTO();
        query.uri = new SensinactPath(PROVIDER);
        AbstractResultDTO rawResult = handler.handleQuery(session, query);
        utils.assertResultSuccess(rawResult, EResultType.SERVICES_LIST, PROVIDER);

        ResultListServicesDTO result = (ResultListServicesDTO) rawResult;
        assertEquals(Set.of("admin", SERVICE), Set.copyOf(result.services));
    }

    /**
     * Description of a single service
     */
    @Test
    void serviceDescription() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto).getValue();

        // Check the description of service
        final QueryDescribeDTO query = new QueryDescribeDTO();
        query.uri = new SensinactPath(PROVIDER, SERVICE);
        AbstractResultDTO rawResult = handler.handleQuery(session, query);
        utils.assertResultSuccess(rawResult, EResultType.DESCRIBE_SERVICE, PROVIDER, SERVICE);

        TypedResponse<?> result = (TypedResponse<?>) rawResult;
        ResponseDescribeServiceDTO descr = utils.convert(result, ResponseDescribeServiceDTO.class);
        assertEquals(SERVICE, descr.name);
        assertEquals(List.of(RESOURCE), descr.resources.stream().map((r) -> r.name).collect(Collectors.toList()));
    }

    /**
     * List the resources of a single service
     */
    @Test
    void resourcesList() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto).getValue();

        // Check the list of resources
        final QueryListDTO query = new QueryListDTO();
        query.uri = new SensinactPath(PROVIDER, SERVICE);
        AbstractResultDTO rawResult = handler.handleQuery(session, query);
        utils.assertResultSuccess(rawResult, EResultType.RESOURCES_LIST, PROVIDER, SERVICE);

        ResultListResourcesDTO result = (ResultListResourcesDTO) rawResult;
        assertEquals(List.of(RESOURCE), result.resources);
    }

    /**
     * Describes a resource
     */
    @Test
    void resourceDescription() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto).getValue();

        // Check the resource
        final QueryDescribeDTO query = new QueryDescribeDTO();
        query.uri = new SensinactPath(PROVIDER, SERVICE, RESOURCE);
        AbstractResultDTO rawResult = handler.handleQuery(session, query);
        utils.assertResultSuccess(rawResult, EResultType.DESCRIBE_RESOURCE, PROVIDER, SERVICE, RESOURCE);

        TypedResponse<?> result = (TypedResponse<?>) rawResult;
        utils.assertResultSuccess(result, EResultType.DESCRIBE_RESOURCE, PROVIDER, SERVICE, RESOURCE);
        assertEquals(RESOURCE, utils.convert(result, ResponseDescribeResourceDTO.class).name);
    }
}

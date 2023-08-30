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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.BulkGenericDto;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.security.UserInfo;
import org.eclipse.sensinact.core.session.SensiNactSession;
import org.eclipse.sensinact.core.session.SensiNactSessionManager;
import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;
import org.eclipse.sensinact.northbound.query.api.EResultType;
import org.eclipse.sensinact.northbound.query.api.IQueryHandler;
import org.eclipse.sensinact.northbound.query.dto.SensinactPath;
import org.eclipse.sensinact.northbound.query.dto.query.QueryDescribeDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryGetDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryListDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ErrorResultDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseDescribeProviderDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseGetDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultListResourcesDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultListServicesDTO;
import org.eclipse.sensinact.northbound.query.dto.result.TypedResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(ServiceExtension.class)
public class MissingEntityTest {

    private static final UserInfo USER = UserInfo.ANONYMOUS;

    private static final String PROVIDER = "QueryHandlerMissingSvcProvider";
    private static final String SERVICE = "service";
    private static final String RESOURCE = "resource";
    private static final Integer VALUE = 42;

    @InjectService
    SensiNactSessionManager sessionManager;
    SensiNactSession session;

    @InjectService
    DataUpdate push;

    @InjectService
    IQueryHandler handler;

    final TestUtils utils = new TestUtils();

    @BeforeEach
    void start() throws InterruptedException {
        session = sessionManager.getDefaultSession(USER);
    }

    @AfterEach
    void stop() {
        session = null;
    }

    /**
     * Missing provider should return a 404
     */
    @Test
    void missingProvider() throws Exception {
        final String missingProvider = PROVIDER + "__missing__";
        final SensinactPath rcPath = new SensinactPath(missingProvider, SERVICE, RESOURCE);

        for (SensinactPath missingProviderPath : Arrays.asList(new SensinactPath(missingProvider),
                new SensinactPath(missingProvider, SERVICE), rcPath)) {
            // Item description
            final QueryDescribeDTO describeQuery = new QueryDescribeDTO();
            describeQuery.uri = missingProviderPath;
            AbstractResultDTO result = handler.handleQuery(session, describeQuery);
            assertEquals(404, result.statusCode);
            assertEquals(EResultType.ERROR, result.type);
            assertNotNull(result.error);

            if (!rcPath.hasResource()) {
                // Children list
                final QueryListDTO listQuery = new QueryListDTO();
                listQuery.uri = missingProviderPath;
                result = handler.handleQuery(session, listQuery);
                assertEquals(404, result.statusCode);
                assertEquals(EResultType.ERROR, result.type);
                assertNotNull(result.error);
            }
        }

        // Resource GET
        final QueryGetDTO getQuery = new QueryGetDTO();
        getQuery.uri = rcPath;
        AbstractResultDTO result = handler.handleQuery(session, getQuery);
        assertEquals(404, result.statusCode);
        assertEquals(EResultType.ERROR, result.type);
        assertNotNull(result.error);
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

        final String missingService = SERVICE + "__missing__";
        final SensinactPath rcPath = new SensinactPath(provider_service, missingService, RESOURCE);
        for (SensinactPath missingProviderPath : Arrays.asList(new SensinactPath(provider_service, missingService),
                rcPath)) {
            // Item description
            final QueryDescribeDTO describeQuery = new QueryDescribeDTO();
            describeQuery.uri = missingProviderPath;
            AbstractResultDTO result = handler.handleQuery(session, describeQuery);
            assertEquals(404, result.statusCode);
            assertEquals(EResultType.ERROR, result.type);
            assertNotNull(result.error);

            if (!rcPath.hasResource()) {
                // Children list
                final QueryListDTO listQuery = new QueryListDTO();
                listQuery.uri = missingProviderPath;
                result = handler.handleQuery(session, listQuery);
                assertEquals(404, result.statusCode);
                assertEquals(EResultType.ERROR, result.type);
                assertNotNull(result.error);
            }
        }

        // Resource GET
        final QueryGetDTO getQuery = new QueryGetDTO();
        getQuery.uri = rcPath;
        AbstractResultDTO result = handler.handleQuery(session, getQuery);
        assertEquals(404, result.statusCode);
        assertEquals(EResultType.ERROR, result.type);
        assertNotNull(result.error);

        // Provider description
        final QueryDescribeDTO describeQuery = new QueryDescribeDTO();
        describeQuery.uri = new SensinactPath(provider_service);
        result = handler.handleQuery(session, describeQuery);
        assertEquals(200, result.statusCode);
        assertEquals(EResultType.DESCRIBE_PROVIDER, result.type);
        assertNull(result.error);

        ResponseDescribeProviderDTO provider = utils.convert((TypedResponse<?>) result,
                ResponseDescribeProviderDTO.class);
        assertEquals(provider_service, provider.name);
        assertFalse(provider.services.contains(missingService), "Missing service is registered");

        // Services list
        final QueryListDTO listQuery = new QueryListDTO();
        listQuery.uri = new SensinactPath(provider_service);
        result = handler.handleQuery(session, listQuery);
        assertEquals(200, result.statusCode);
        assertEquals(EResultType.SERVICES_LIST, result.type);
        assertNull(result.error);

        ResultListServicesDTO svcListResult = (ResultListServicesDTO) result;
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
        final QueryListDTO listQuery = new QueryListDTO();
        listQuery.uri = new SensinactPath(provider_resource, SERVICE);
        ResultListResourcesDTO rcListResult = (ResultListResourcesDTO) handler.handleQuery(session, listQuery);
        assertEquals(200, rcListResult.statusCode);
        assertFalse(rcListResult.resources.contains(missingResource), "Missing resource is registered");

        // Get value
        final QueryGetDTO getQuery = new QueryGetDTO();
        getQuery.uri = new SensinactPath(provider_resource, SERVICE, missingResource);
        ErrorResultDTO result = (ErrorResultDTO) handler.handleQuery(session, getQuery);
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
        final QueryListDTO listQuery = new QueryListDTO();
        listQuery.uri = new SensinactPath(PROVIDER, SERVICE);
        ResultListResourcesDTO rcListResult = (ResultListResourcesDTO) handler.handleQuery(session, listQuery);
        assertEquals(200, rcListResult.statusCode);
        assertTrue(rcListResult.resources.contains(RESOURCE), "Resource is not registered");

        // Get value
        Instant queryTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        final QueryGetDTO getQuery = new QueryGetDTO();
        getQuery.uri = new SensinactPath(provider2, SERVICE, RESOURCE);

        AbstractResultDTO result = handler.handleQuery(session, getQuery);
        assertEquals(204, result.statusCode);
        assertNotNull(result.error, "No warning message set");
        assertFalse(result.error.isEmpty(), "No warning message set");

        ResponseGetDTO response = utils.convert((TypedResponse<?>) result, ResponseGetDTO.class);
        assertNotNull(response, "No empty value response");

        assertFalse(queryTime.isAfter(Instant.ofEpochMilli(response.timestamp)), "Missing resource has a timestamp");
        assertNull(response.value, "Got a value for a missing resource");
    }
}

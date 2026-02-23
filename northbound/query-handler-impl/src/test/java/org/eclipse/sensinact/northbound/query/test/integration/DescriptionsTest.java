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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.northbound.query.api.AbstractResultDTO;
import org.eclipse.sensinact.northbound.query.api.EResultType;
import org.eclipse.sensinact.northbound.query.api.IQueryHandler;
import org.eclipse.sensinact.northbound.query.dto.SensinactPath;
import org.eclipse.sensinact.northbound.query.dto.query.QueryDescribeDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryLinkDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryListDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QuerySnapshotDTO.SnapshotLinkOption;
import org.eclipse.sensinact.northbound.query.dto.result.CompleteProviderDescriptionDTO;
import org.eclipse.sensinact.northbound.query.dto.result.MetadataDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseDescribeProviderDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseDescribeResourceDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseDescribeServiceDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultDescribeProvidersDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultListProvidersDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultListResourcesDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultListServicesDTO;
import org.eclipse.sensinact.northbound.query.dto.result.TypedResponse;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.ProviderDescription;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.annotation.bundle.Requirement;
import org.osgi.namespace.service.ServiceNamespace;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

@Requirement(namespace = ServiceNamespace.SERVICE_NAMESPACE, filter = "(objectClass=org.eclipse.sensinact.northbound.session.SensiNactSessionManager)")
@WithConfiguration(pid = "sensinact.session.manager", properties = {
        @Property(key = "auth.policy", value = "ALLOW_ALL"),
        @Property(key = "name", value = "test-session"),
})
public class DescriptionsTest {

    private static final UserInfo USER = UserInfo.ANONYMOUS;

    private static final String PROVIDER = "QueryHandlerDescriptionProvider";
    private static final String PROVIDER_2 = PROVIDER + "_2";
    private static final String SERVICE = "service";
    private static final String RESOURCE = "resource";
    private static final Integer VALUE = 42;

    SensiNactSession session;

    @InjectService
    DataUpdate push;

    @InjectService
    GatewayThread thread;

    @InjectService
    IQueryHandler handler;

    final TestUtils utils = new TestUtils();

    @BeforeEach
    void start(@InjectService(filter = "(name=test-session)", timeout = 1000) SensiNactSessionManager sessionManager) {
        session = sessionManager.getDefaultSession(USER);
    }

    @AfterEach
    void stop() throws Exception {
        List<ProviderDescription> providers = session.listProviders();
            thread.execute(new AbstractSensinactCommand<Void>() {
                @Override
                protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr,
                        PromiseFactory promiseFactory) {
                    for(ProviderDescription pd : providers) {
                        if("sensiNact".equals(pd.provider)) {
                            continue;
                        }
                        twin.getProvider(pd.provider).delete();
                    }
                    return promiseFactory.resolved(null);
                }
            }).getValue();
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

    @Test
    void listLinks() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto).getValue();
        dto = utils.makeDto(PROVIDER_2, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto).getValue();

        session.linkProviders(PROVIDER, PROVIDER_2);

        final QueryDescribeDTO query = new QueryDescribeDTO();
        query.uri = new SensinactPath();

        // Check for success
        AbstractResultDTO rawResult = handler.handleQuery(session, query);
        utils.assertResultSuccess(rawResult, EResultType.COMPLETE_LIST);
        ResultDescribeProvidersDTO result = (ResultDescribeProvidersDTO) rawResult;

        // Check content
        CompleteProviderDescriptionDTO providerDto = result.providers.stream()
                .filter((p) -> PROVIDER.equals(p.name)).findFirst().get();
        assertEquals(List.of(), providerDto.linkedProviders);

        query.linkOptions = List.of(SnapshotLinkOption.ID_ONLY);
        rawResult = handler.handleQuery(session, query);
        utils.assertResultSuccess(rawResult, EResultType.COMPLETE_LIST);
        result = (ResultDescribeProvidersDTO) rawResult;

        // Check content
        providerDto = result.providers.stream()
                .filter((p) -> PROVIDER.equals(p.name)).findFirst().get();
        assertEquals(List.of(PROVIDER_2), providerDto.linkedProviders);

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

        ResponseDescribeResourceDTO describeResourceDTO = utils.convert((TypedResponse<?>) rawResult, ResponseDescribeResourceDTO.class);
        assertEquals(RESOURCE, describeResourceDTO.name);
        assertTrue(describeResourceDTO.attributes.isEmpty());

        // Add metadata
        dto.metadata = Map.of("unit", "dB");
        dto.type = null;
        dto.value = null;
        push.pushUpdate(dto).getValue();

        // Check the resource
        rawResult = handler.handleQuery(session, query);
        utils.assertResultSuccess(rawResult, EResultType.DESCRIBE_RESOURCE, PROVIDER, SERVICE, RESOURCE);

        describeResourceDTO = utils.convert((TypedResponse<?>) rawResult, ResponseDescribeResourceDTO.class);
        assertEquals(RESOURCE, describeResourceDTO.name);
        assertEquals(1, describeResourceDTO.attributes.size());

        MetadataDTO metadataDTO = describeResourceDTO.attributes.get(0);
        assertEquals("unit", metadataDTO.name);
        assertEquals("dB", metadataDTO.value);
        assertEquals(String.class.getName(), metadataDTO.type);
    }

    /**
     * Description of a single provider after a link
     */
    @Test
    void providerLinkDescription() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto).getValue();
        dto = utils.makeDto(PROVIDER_2, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto).getValue();

        // Check the list of providers
        final QueryLinkDTO query = new QueryLinkDTO();
        query.uri = new SensinactPath(PROVIDER);
        query.child = PROVIDER_2;
        AbstractResultDTO rawResult = handler.handleQuery(session, query);
        utils.assertResultSuccess(rawResult, EResultType.DESCRIBE_PROVIDER, PROVIDER);

        TypedResponse<?> result = (TypedResponse<?>) rawResult;
        ResponseDescribeProviderDTO descr = utils.convert(result, ResponseDescribeProviderDTO.class);
        assertEquals(PROVIDER, descr.name);
        assertEquals(Set.of("admin", SERVICE), Set.copyOf(descr.services));
        assertEquals(Set.of(PROVIDER_2), Set.copyOf(descr.linkedProviders));
    }
}

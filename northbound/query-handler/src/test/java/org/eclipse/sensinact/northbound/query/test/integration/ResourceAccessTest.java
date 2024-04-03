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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.sensinact.core.annotation.verb.ACT;
import org.eclipse.sensinact.core.annotation.verb.ActParam;
import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.northbound.query.api.EResultType;
import org.eclipse.sensinact.northbound.query.api.IQueryHandler;
import org.eclipse.sensinact.northbound.query.dto.SensinactPath;
import org.eclipse.sensinact.northbound.query.dto.query.QueryActDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QueryGetDTO;
import org.eclipse.sensinact.northbound.query.dto.query.QuerySetDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseGetDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultActDTO;
import org.eclipse.sensinact.northbound.query.dto.result.TypedResponse;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(ServiceExtension.class)
public class ResourceAccessTest {

    private static final UserInfo USER = UserInfo.ANONYMOUS;

    private static final String PROVIDER = "QueryHandlerAccessProvider";
    private static final String PROVIDER_TOPIC = PROVIDER + "/*";
    private static final String SERVICE = "service";
    private static final String RESOURCE = "resource";
    private static final Integer VALUE = 42;
    private static final Integer VALUE_2 = 84;

    SensiNactSession session;

    @InjectService
    DataUpdate push;

    @InjectService
    IQueryHandler handler;

    BlockingQueue<ResourceDataNotification> queue;

    final TestUtils utils = new TestUtils();

    @BeforeEach
    void start(@InjectService SensiNactSessionManager sessionManager) throws Exception {
        session = sessionManager.getDefaultSession(USER);
    }

    @AfterEach
    void stop() {
        session.activeListeners().keySet().forEach(session::removeListener);
        session.expire();
    }

    /**
     * Get the resource value
     */
    @Test
    void resourceGet() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        Instant updateTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        push.pushUpdate(dto).getValue();

        // Check for success
        final QueryGetDTO getQuery = new QueryGetDTO();
        getQuery.uri = new SensinactPath(PROVIDER, SERVICE, RESOURCE);
        TypedResponse<?> result = (TypedResponse<?>) handler.handleQuery(session, getQuery);
        utils.assertResultSuccess(result, EResultType.GET_RESPONSE, PROVIDER, SERVICE, RESOURCE);
        ResponseGetDTO response = utils.convert(result, ResponseGetDTO.class);
        assertEquals(RESOURCE, response.name);
        assertEquals(VALUE, response.value);
        assertEquals(dto.type.getName(), response.type);
        assertFalse(Instant.ofEpochMilli(response.timestamp).isBefore(updateTime), "Timestamp wasn't updated");
    }

    /**
     * Check resource update
     */
    @Test
    void resourceUpdate() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        Instant firstTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        push.pushUpdate(dto).getValue();

        // Check response
        final QueryGetDTO getQuery = new QueryGetDTO();
        getQuery.uri = new SensinactPath(PROVIDER, SERVICE, RESOURCE);
        TypedResponse<?> result = (TypedResponse<?>) handler.handleQuery(session, getQuery);
        ResponseGetDTO response = utils.convert(result, ResponseGetDTO.class);
        assertEquals(VALUE, response.value);
        assertFalse(Instant.ofEpochMilli(response.timestamp).isBefore(firstTime), "Timestamp wasn't updated");

        // Update value
        dto.value = VALUE_2;
        Instant secondTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        push.pushUpdate(dto).getValue();

        // Check for success
        result = (TypedResponse<?>) handler.handleQuery(session, getQuery);
        response = utils.convert(result, ResponseGetDTO.class);
        assertEquals(VALUE_2, response.value);
        assertFalse(Instant.ofEpochMilli(response.timestamp).isBefore(secondTime), "Timestamp wasn't updated");
    }

    /**
     * Update the resource value from the REST endpoint
     */
    @Test
    void resourceSet() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        Instant firstUpdateTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        push.pushUpdate(dto).getValue();

        // Check response
        final QueryGetDTO getQuery = new QueryGetDTO();
        getQuery.uri = new SensinactPath(PROVIDER, SERVICE, RESOURCE);
        TypedResponse<?> result = (TypedResponse<?>) handler.handleQuery(session, getQuery);
        ResponseGetDTO response = utils.convert(result, ResponseGetDTO.class);
        assertEquals(VALUE, response.value);

        Instant firstTimestamp = Instant.ofEpochMilli(response.timestamp);
        assertFalse(firstTimestamp.isBefore(firstUpdateTime), "Timestamp wasn't updated");

        final QuerySetDTO query = new QuerySetDTO();
        query.uri = new SensinactPath(PROVIDER, SERVICE, RESOURCE);
        query.valueType = Integer.class.getName();
        query.value = VALUE_2;

        queue = new ArrayBlockingQueue<>(32);
        session.addListener(List.of(PROVIDER_TOPIC), (t, e) -> queue.offer(e), null, null, null);
        assertNull(queue.poll(500, TimeUnit.MILLISECONDS));

        result = (TypedResponse<?>) handler.handleQuery(session, query);

        utils.assertResultSuccess(result, EResultType.SET_RESPONSE, PROVIDER, SERVICE, RESOURCE);
        response = utils.convert(result, ResponseGetDTO.class);
        assertEquals(RESOURCE, response.name);
        assertEquals(query.valueType, response.type);
        assertEquals(VALUE_2, response.value);

        // Wait for internal notification
        dto.value = VALUE_2;
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Check access
        result = (TypedResponse<?>) handler.handleQuery(session, getQuery);
        response = utils.convert(result, ResponseGetDTO.class);
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
        push.pushUpdate(dto).getValue();

        // friendlyName should be the provider name
        final QueryGetDTO query = new QueryGetDTO();
        query.uri = new SensinactPath(PROVIDER, "admin", "friendlyName");
        TypedResponse<?> result = (TypedResponse<?>) handler.handleQuery(session, query);
        utils.assertResultSuccess(result, EResultType.GET_RESPONSE, PROVIDER, "admin", "friendlyName");
        ResponseGetDTO response = utils.convert(result, ResponseGetDTO.class);
        assertEquals(String.class.getName(), response.type);
        assertEquals(PROVIDER, response.value);

        // Location should be null, but set
        query.uri = new SensinactPath(PROVIDER, "admin", "location");
        result = (TypedResponse<?>) handler.handleQuery(session, query);
        utils.assertResultNoContent(result, EResultType.GET_RESPONSE, PROVIDER, "admin", "location");
        response = utils.convert(result, ResponseGetDTO.class);
        assertEquals(GeoJsonObject.class.getName(), response.type);
        assertNull(response.value);
    }

    public static class TestAction {
        @ACT(model = PROVIDER, service = SERVICE, resource = "action")
        public Double toDoubleDouble(@ActParam("input") Long longValue) {
            return longValue.doubleValue() * 2.0d;
        }
    }

    @Test
    void resourceAct(@InjectBundleContext BundleContext context) throws Exception {

        context.registerService(TestAction.class, new TestAction(),
                new Hashtable<>(Map.of("sensiNact.whiteboard.resource", true)));

        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto).getValue();

        final QueryActDTO actQuery = new QueryActDTO();
        actQuery.uri = new SensinactPath(PROVIDER, SERVICE, "action");
        actQuery.parameters = Map.of("input", 123);

        ResultActDTO response = (ResultActDTO) handler.handleQuery(session, actQuery);
        assertNotNull(response);
        assertEquals(200, response.statusCode);
        assertNotNull(response.response);
        assertEquals(246d, response.response);

    }
}

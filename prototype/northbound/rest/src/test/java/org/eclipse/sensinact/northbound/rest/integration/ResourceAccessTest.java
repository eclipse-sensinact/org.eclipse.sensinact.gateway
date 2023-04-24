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
import java.time.temporal.ChronoUnit;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.northbound.query.api.EResultType;
import org.eclipse.sensinact.northbound.query.dto.query.AccessMethodCallParameterDTO;
import org.eclipse.sensinact.northbound.query.dto.query.WrappedAccessMethodCallParametersDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseGetDTO;
import org.eclipse.sensinact.northbound.query.dto.result.ResultActDTO;
import org.eclipse.sensinact.northbound.query.dto.result.TypedResponse;
import org.eclipse.sensinact.prototype.PrototypePush;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.eclipse.sensinact.prototype.annotation.verb.ACT;
import org.eclipse.sensinact.prototype.annotation.verb.ActParam;
import org.eclipse.sensinact.prototype.generic.dto.GenericDto;
import org.eclipse.sensinact.prototype.notification.ResourceDataNotification;
import org.eclipse.sensinact.prototype.security.UserInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.ws.rs.core.Application;

@ExtendWith({ ServiceExtension.class, ConfigurationExtension.class })
public class ResourceAccessTest {

    @BeforeEach
    public void await(
            @InjectConfiguration(withConfig = @WithConfiguration(pid = "sensinact.northbound.rest", location = "?", properties = {
                    @Property(key = "allow.anonymous", value = "true"),
                    @Property(key = "foobar", value = "fizz") })) Configuration cm,
            @InjectService(filter = "(foobar=fizz)", cardinality = 0) ServiceAware<Application> a)
            throws InterruptedException {
        a.waitForService(5000);
        for (int i = 0; i < 10; i++) {
            try {
                if (utils.queryStatus("/").statusCode() == 200)
                    return;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Thread.sleep(200);
        }
        throw new AssertionFailedError("REST API did not appear");
    }

    @AfterEach
    public void clear(@InjectConfiguration("sensinact.northbound.rest") Configuration cm) throws Exception {
        cm.delete();
        Thread.sleep(500);
    }

    private static final UserInfo USER = UserInfo.ANONYMOUS;

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

    @AfterEach
    void stop() {
        if (queue != null) {
            SensiNactSession session = sessionManager.getDefaultSession(USER);
            session.activeListeners().keySet().forEach(session::removeListener);
            queue = null;
        }
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
        TypedResponse<?> result = utils.queryJson(
                String.join("/", "providers", PROVIDER, "services", SERVICE, "resources", RESOURCE, "GET"),
                TypedResponse.class);
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
        TypedResponse<?> result = utils.queryJson(
                String.join("/", "providers", PROVIDER, "services", SERVICE, "resources", RESOURCE, "GET"),
                TypedResponse.class);
        ResponseGetDTO response = utils.convert(result, ResponseGetDTO.class);
        assertEquals(VALUE, response.value);
        assertFalse(Instant.ofEpochMilli(response.timestamp).isBefore(firstTime), "Timestamp wasn't updated");

        // Update value
        dto.value = VALUE_2;
        Instant secondTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        push.pushUpdate(dto).getValue();

        // Check for success
        result = utils.queryJson(
                String.join("/", "providers", PROVIDER, "services", SERVICE, "resources", RESOURCE, "GET"),
                TypedResponse.class);
        response = utils.convert(result, ResponseGetDTO.class);
        assertEquals(VALUE_2, response.value);
        assertFalse(Instant.ofEpochMilli(response.timestamp).isBefore(secondTime), "Timestamp wasn't updated");
    }

    /**
     * Update the resource value from the REST endpoint
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void resourceSet(boolean wrapParams) throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        Instant firstUpdateTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        push.pushUpdate(dto).getValue();

        // Check response
        TypedResponse<?> result = utils.queryJson(
                String.join("/", "providers", PROVIDER, "services", SERVICE, "resources", RESOURCE, "GET"),
                TypedResponse.class);
        ResponseGetDTO response = utils.convert(result, ResponseGetDTO.class);
        assertEquals(VALUE, response.value);

        Instant firstTimestamp = Instant.ofEpochMilli(response.timestamp);
        assertFalse(firstTimestamp.isBefore(firstUpdateTime), "Timestamp wasn't updated");

        queue = new ArrayBlockingQueue<>(32);
        SensiNactSession session = sessionManager.getDefaultSession(USER);
        session.addListener(List.of(PROVIDER_TOPIC), (t, e) -> queue.offer(e), null, null, null);
        assertNull(queue.poll(500, TimeUnit.MILLISECONDS));

        AccessMethodCallParameterDTO param = new AccessMethodCallParameterDTO();
        param.name = "value";
        param.type = Integer.class.getName();
        param.value = VALUE_2;
        result = utils.queryJson(
                String.join("/", "providers", PROVIDER, "services", SERVICE, "resources", RESOURCE, "SET"),
                wrapParams(wrapParams, List.of(param)), TypedResponse.class);
        utils.assertResultSuccess(result, EResultType.SET_RESPONSE, PROVIDER, SERVICE, RESOURCE);
        response = utils.convert(result, ResponseGetDTO.class);
        assertEquals(RESOURCE, response.name);
        assertEquals(param.type, response.type);
        assertEquals(VALUE_2, response.value);

        // Wait for internal notification
        dto.value = VALUE_2;
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Check access
        result = utils.queryJson(
                String.join("/", "providers", PROVIDER, "services", SERVICE, "resources", RESOURCE, "GET"),
                TypedResponse.class);
        response = utils.convert(result, ResponseGetDTO.class);
        assertEquals(VALUE_2, response.value);
        assertTrue(firstTimestamp.isBefore(Instant.ofEpochMilli(response.timestamp)), "Timestamp wasn't updated");
    }

    private Object wrapParams(boolean wrap, List<AccessMethodCallParameterDTO> params) {
        if (wrap) {
            WrappedAccessMethodCallParametersDTO dto = new WrappedAccessMethodCallParametersDTO();
            dto.parameters = params;
            return dto;
        }
        return params;
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
        TypedResponse<?> result = utils.queryJson(
                String.join("/", "providers", PROVIDER, "services", "admin", "resources", "friendlyName", "GET"),
                TypedResponse.class);
        utils.assertResultSuccess(result, EResultType.GET_RESPONSE, PROVIDER, "admin", "friendlyName");
        ResponseGetDTO response = utils.convert(result, ResponseGetDTO.class);
        assertEquals(String.class.getName(), response.type);
        assertEquals(PROVIDER, response.value);

        // Location should be null, but set
        result = utils.queryJson(
                String.join("/", "providers", PROVIDER, "services", "admin", "resources", "location", "GET"),
                TypedResponse.class);
        utils.assertResultSuccess(result, EResultType.GET_RESPONSE, PROVIDER, "admin", "location");
        response = utils.convert(result, ResponseGetDTO.class);
        assertEquals(GeoJsonObject.class.getName(), response.type);
        assertNull(response.value);
    }

    public static class TestAction {
        @ACT(model = PROVIDER, service = SERVICE, resource = "action")
        public Double toDoubleDouble(@ActParam(name = "input") Long longValue) {
            return longValue.doubleValue() * 2.0d;
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void resourceAct(boolean wrapParams, @InjectBundleContext BundleContext context) throws Exception {

        context.registerService(TestAction.class, new TestAction(),
                new Hashtable<>(Map.of("sensiNact.whiteboard.resource", true)));

        GenericDto dto = utils.makeDto(PROVIDER, SERVICE, RESOURCE, VALUE, Integer.class);
        push.pushUpdate(dto).getValue();

        AccessMethodCallParameterDTO param = new AccessMethodCallParameterDTO();
        param.name = "input";
        param.type = Long.class.getName();
        param.value = 123L;

        ResultActDTO response = utils.queryJson(
                String.join("/", "providers", PROVIDER, "services", SERVICE, "resources", "action", "ACT"),
                wrapParams(wrapParams, List.of(param)), ResultActDTO.class);

        assertNotNull(response);
        assertEquals(200, response.statusCode);
        assertNotNull(response.response);
        assertEquals(246d, response.response);
    }
}

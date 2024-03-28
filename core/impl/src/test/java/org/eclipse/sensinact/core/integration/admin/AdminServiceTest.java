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
package org.eclipse.sensinact.core.integration.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.command.ResourceCommand;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.twin.SensinactResource;
import org.eclipse.sensinact.core.twin.TimedValue;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests the behavior around the admin service
 */
public class AdminServiceTest {

    private static final String PROVIDER = "AdminServiceTestProvider";
    private static final String SERVICE = "service";
    private static final String RESOURCE = "resource";

    @InjectService
    DataUpdate push;
    @InjectService
    GatewayThread gt;

    private TimedValue<?> getValue(String provider, String service, String resource) throws Exception {
        return gt.execute(new ResourceCommand<TimedValue<?>>(provider, service, resource) {
            @Override
            protected Promise<TimedValue<?>> call(SensinactResource resource, PromiseFactory pf) {
                return resource.getValue();
            }
        }).getValue();
    }

    private void setValue(String provider, String service, String resource, Object value, Instant timestamp) throws Exception {
        gt.execute(new ResourceCommand<Void>(provider, service, resource) {
            @Override
            protected Promise<Void> call(SensinactResource resource, PromiseFactory pf) {
                return resource.setValue(value, timestamp);
            }
        }).getValue();
    }

    /**
     * Tests admin resource creation with provider and update
     */
    @Test
    void testAdminCreateUpdate() throws Exception {
        final Instant timestamp = Instant.now();

        // Create resource & provider using a push
        GenericDto dto = new GenericDto();
        dto.provider = PROVIDER;
        dto.service = SERVICE;
        dto.resource = RESOURCE;
        dto.value = 42;
        dto.type = Integer.class;
        dto.timestamp = timestamp;
        push.pushUpdate(dto).getValue();

        // Admin resources must have a timestamp
        // friendlyName has a set value, so it's timestamp is set
        TimedValue<?> descr = getValue(PROVIDER, "admin", "friendlyName");
        assertEquals(PROVIDER, descr.getValue());
        assertEquals(timestamp, descr.getTimestamp());

        // location is not set, so it's timestamp is EPOCH
        descr = getValue(PROVIDER, "admin", "location");
        assertNull(descr.getValue());
        assertNull(descr.getTimestamp());

        // Ensure we reject setting a value with an earlier timestamp
        setValue(PROVIDER, "admin", "friendlyName", "foo", timestamp.minusSeconds(1));
        descr = getValue(PROVIDER, "admin", "friendlyName");
        assertEquals(PROVIDER, descr.getValue());
        assertEquals(timestamp, descr.getTimestamp());

        // Location can now be set to null (new timestamp)
        setValue(PROVIDER, "admin", "location", null, timestamp);
        descr = getValue(PROVIDER, "admin", "location");
        assertNull(descr.getValue());
        assertEquals(timestamp, descr.getTimestamp());

        // Reject earlier values
        setValue(PROVIDER, "admin", "location",
                "{\"type\":\"Point\",\"coordinates\":[-0.119700, 51.503300]}", timestamp.minusSeconds(1));
        descr = getValue(PROVIDER, "admin", "location");
        assertNull(descr.getValue());
        assertEquals(timestamp, descr.getTimestamp());

        // Set the value with the same timestamp
        setValue(PROVIDER, "admin", "friendlyName", "foo", timestamp);
        descr = getValue(PROVIDER, "admin", "friendlyName");
        assertEquals("foo", descr.getValue());
        assertEquals(timestamp, descr.getTimestamp());

        setValue(PROVIDER, "admin", "location",
                "{\"type\":\"Point\",\"coordinates\":[-0.119700, 51.503300]}", timestamp);
        descr = getValue(PROVIDER, "admin", "location");
        assertEquals(Map.of("type", "Point", "coordinates", List.of(-0.119700d, 51.503300d)),
                new ObjectMapper().convertValue(descr.getValue(), Map.class));
        assertEquals(timestamp, descr.getTimestamp());

        // Set the value with a future timestamp
        final Instant future = timestamp.plusSeconds(1);
        setValue(PROVIDER, "admin", "friendlyName", "eclipse", future);
        descr = getValue(PROVIDER, "admin", "friendlyName");
        assertEquals("eclipse", descr.getValue());
        assertEquals(future, descr.getTimestamp());
        setValue(PROVIDER, "admin", "location",
                "{\"type\":\"Point\",\"coordinates\":[2.295049, 48.873785]}", future);
        descr = getValue(PROVIDER, "admin", "location");
        assertEquals(Map.of("type", "Point", "coordinates", List.of(2.295049d, 48.873785d)),
                new ObjectMapper().convertValue(descr.getValue(), Map.class));
        assertEquals(future, descr.getTimestamp());
    }
}

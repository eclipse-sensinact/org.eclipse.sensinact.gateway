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
package org.eclipse.sensinact.prototype.integration.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.security.UserInfo;
import org.eclipse.sensinact.core.session.ResourceDescription;
import org.eclipse.sensinact.core.session.SensiNactSession;
import org.eclipse.sensinact.core.session.SensiNactSessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.service.ServiceExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests the behavior around the admin service
 */
@ExtendWith(ServiceExtension.class)
public class AdminServiceTest {

    private static final UserInfo USER = UserInfo.ANONYMOUS;

    private static final String PROVIDER = "AdminServiceTestProvider";
    private static final String SERVICE = "service";
    private static final String RESOURCE = "resource";

    @InjectService
    DataUpdate push;
    @InjectService
    SensiNactSessionManager sessionManager;
    SensiNactSession session;

    @BeforeEach
    void start() {
        session = sessionManager.getDefaultSession(USER);
    }

    @AfterEach
    void stop() {
        session = null;
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
        ResourceDescription descr = session.describeResource(PROVIDER, "admin", "friendlyName");
        assertEquals(PROVIDER, descr.value);
        assertEquals(timestamp, descr.timestamp);

        // location is not set, so it's timestamp is EPOCH
        descr = session.describeResource(PROVIDER, "admin", "location");
        assertNull(descr.value);
        assertNull(descr.timestamp);

        // Ensure we reject setting a value with an earlier timestamp
        session.setResourceValue(PROVIDER, "admin", "friendlyName", "foo", timestamp.minusSeconds(1));
        descr = session.describeResource(PROVIDER, "admin", "friendlyName");
        assertEquals(PROVIDER, descr.value);
        assertEquals(timestamp, descr.timestamp);

        // Location can now be set to null (new timestamp)
        session.setResourceValue(PROVIDER, "admin", "location", null, timestamp);
        descr = session.describeResource(PROVIDER, "admin", "location");
        assertNull(descr.value);
        assertEquals(timestamp, descr.timestamp);

        // Reject earlier values
        session.setResourceValue(PROVIDER, "admin", "location",
                "{\"type\":\"Point\",\"coordinates\":[-0.119700, 51.503300]}", timestamp.minusSeconds(1));
        descr = session.describeResource(PROVIDER, "admin", "location");
        assertNull(descr.value);
        assertEquals(timestamp, descr.timestamp);

        // Set the value with the same timestamp
        session.setResourceValue(PROVIDER, "admin", "friendlyName", "foo", timestamp);
        descr = session.describeResource(PROVIDER, "admin", "friendlyName");
        assertEquals("foo", descr.value);
        assertEquals(timestamp, descr.timestamp);

        session.setResourceValue(PROVIDER, "admin", "location",
                "{\"type\":\"Point\",\"coordinates\":[-0.119700, 51.503300]}", timestamp);
        descr = session.describeResource(PROVIDER, "admin", "location");
        assertEquals(Map.of("type", "Point", "coordinates", List.of(-0.119700d, 51.503300d)),
                new ObjectMapper().convertValue(descr.value, Map.class));
        assertEquals(timestamp, descr.timestamp);

        // Set the value with a future timestamp
        final Instant future = timestamp.plusSeconds(1);
        session.setResourceValue(PROVIDER, "admin", "friendlyName", "eclipse", future);
        descr = session.describeResource(PROVIDER, "admin", "friendlyName");
        assertEquals("eclipse", descr.value);
        assertEquals(future, descr.timestamp);
        session.setResourceValue(PROVIDER, "admin", "location",
                "{\"type\":\"Point\",\"coordinates\":[2.295049, 48.873785]}", future);
        descr = session.describeResource(PROVIDER, "admin", "location");
        assertEquals(Map.of("type", "Point", "coordinates", List.of(2.295049d, 48.873785d)),
                new ObjectMapper().convertValue(descr.value, Map.class));
        assertEquals(future, descr.timestamp);
    }
}

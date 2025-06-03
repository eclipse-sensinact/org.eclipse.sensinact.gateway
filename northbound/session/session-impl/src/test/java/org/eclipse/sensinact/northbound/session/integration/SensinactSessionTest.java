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
package org.eclipse.sensinact.northbound.session.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.eclipse.sensinact.core.command.AbstractTwinCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.NotPermittedException;
import org.eclipse.sensinact.northbound.session.ResourceDescription;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.eclipse.sensinact.northbound.session.impl.TestUserInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Tests the get/set behaviour of the Session
 */
@WithConfiguration(pid = "sensinact.session.manager", properties = @Property(key = "auth.policy", value = "AUTHENTICATED_ONLY"))
public class SensinactSessionTest {

    private static final UserInfo ANON = new TestUserInfo("<ANON>", false);
    private static final UserInfo BOB = new TestUserInfo("bob", true);

    private static final String PROVIDER = "SensinactSessionTestProvider";
    private static final String SERVICE = "service";
    private static final String RESOURCE = "resource";

    private Instant timestamp;

    @BeforeEach
    void setupProvider(@InjectService DataUpdate push) throws Exception {
        timestamp = Instant.now();
        // Create resource & provider using a push
        GenericDto dto = new GenericDto();
        dto.provider = PROVIDER;
        dto.service = SERVICE;
        dto.resource = RESOURCE;
        dto.value = 42;
        dto.type = Integer.class;
        dto.timestamp = timestamp;
        push.pushUpdate(dto).getValue();
    }

    @AfterEach
    void deleteProvider(@InjectService GatewayThread gt) throws Exception {
        gt.execute(new AbstractTwinCommand<Void>() {
            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, PromiseFactory pf) {
                SensinactProvider sp = twin.getProvider(PROVIDER);
                if(sp != null) {
                    sp.delete();
                }
                return pf.resolved(null);
            }
        }).getValue();
    }

    @InjectService
    SensiNactSessionManager sessionManager;
    SensiNactSession anonSession;
    SensiNactSession bobSession;

    @BeforeEach
    void start() {
        anonSession = sessionManager.getDefaultSession(ANON);
        bobSession = sessionManager.getDefaultSession(BOB);
    }

    @AfterEach
    void stop() {
        anonSession.expire();
        bobSession.expire();
    }

    @Nested
    class GetTests {
        @Test
        void getResourceValue() {
            // Admin resources must have a timestamp
            // friendlyName has a set value, so it's timestamp is set
            assertThrows(NotPermittedException.class, () -> anonSession.getResourceValue(PROVIDER, "admin", "friendlyName", String.class));

            String name = bobSession.getResourceValue(PROVIDER, "admin", "friendlyName", String.class);
            assertNotNull(name);
        }

        @Test
        void getResourceNeverSet() {
            // location is not set, so it's timestamp is EPOCH
            assertThrows(NotPermittedException.class, () -> anonSession.getResourceValue(PROVIDER, "admin", "location", String.class));

            String location = bobSession.getResourceValue(PROVIDER, "admin", "location", String.class);
            assertNull(location);
        }
    }

    @Nested
    class DescribeTests {
        @Test
        void describeResource() {
            // Admin resources must have a timestamp
            // friendlyName has a set value, so it's timestamp is set
            assertThrows(NotPermittedException.class, () -> anonSession.describeResource(PROVIDER, "admin", "friendlyName"));

            ResourceDescription descr = bobSession.describeResource(PROVIDER, "admin", "friendlyName");
            assertEquals(PROVIDER, descr.value);
            assertEquals(timestamp, descr.timestamp);
        }

        @Test
        void describeResourceNeverSet() {
            // location is not set, so it's timestamp is EPOCH
            assertThrows(NotPermittedException.class, () -> anonSession.describeResource(PROVIDER, "admin", "location"));

            ResourceDescription descr = bobSession.describeResource(PROVIDER, "admin", "location");
            assertNull(descr.value);
            assertNull(descr.timestamp);
        }
    }

    @Nested
    class SetTests {
        @Test
        void setResourceValue() {
            // Set the value with a future timestamp
            final Instant future = timestamp.plusSeconds(1);

            assertThrows(NotPermittedException.class, () -> anonSession.setResourceValue(PROVIDER, "admin", "friendlyName", "eclipse", future));

            bobSession.setResourceValue(PROVIDER, "admin", "friendlyName", "eclipse", future);
            ResourceDescription descr = bobSession.describeResource(PROVIDER, "admin", "friendlyName");
            assertEquals("eclipse", descr.value);
            assertEquals(future, descr.timestamp);
        }

        @Test
        void blockSetWithEarlyTimestamp() {
            // Ensure we reject setting a value with an earlier timestamp
            assertThrows(NotPermittedException.class, () -> anonSession.setResourceValue(PROVIDER, "admin", "friendlyName", "foo", timestamp.minusSeconds(1)));

            bobSession.setResourceValue(PROVIDER, "admin", "friendlyName", "foo", timestamp.minusSeconds(1));
            ResourceDescription descr = bobSession.describeResource(PROVIDER, "admin", "friendlyName");
            assertNotEquals("foo", descr.value);
            assertEquals(timestamp, descr.timestamp);
        }
    }
}

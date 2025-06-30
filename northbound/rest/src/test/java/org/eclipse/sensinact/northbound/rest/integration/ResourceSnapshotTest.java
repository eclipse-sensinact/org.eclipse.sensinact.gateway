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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.filters.resource.selector.api.CompactResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.Selection;
import org.eclipse.sensinact.northbound.query.dto.result.ResponseSnapshotDTO;
import org.eclipse.sensinact.northbound.query.dto.result.SnapshotProviderDTO;
import org.eclipse.sensinact.northbound.query.dto.result.SnapshotResourceDTO;
import org.eclipse.sensinact.northbound.query.dto.result.SnapshotServiceDTO;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.test.common.service.ServiceAware;

import jakarta.ws.rs.core.Application;

@WithConfiguration(pid = "sensinact.session.manager", properties = @Property(key = "auth.policy", value = "ALLOW_ALL"))
public class ResourceSnapshotTest {

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

    private static final UserInfo USER = UserInfo.ANONYMOUS;

    @InjectService
    SensiNactSessionManager sessionManager;

    @InjectService
    DataUpdate push;

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
    void resourceGetSnapshot() throws Exception {
        // Register the resource
        GenericDto dto = utils.makeDto("M1", "P1", "S1", "R1", "V1", String.class);
        Instant updateTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        push.pushUpdate(dto).getValue();

        List<ResourceSelector> request = new ArrayList<>();
        ResourceSelector rs = new CompactResourceSelector(null, new Selection("M1", null, false),
                null, new Selection("S1", null, false), null, List.of(), List.of()).toResourceSelector();
        request.add(rs);
        ResponseSnapshotDTO response = utils.queryJson("snapshot", request, ResponseSnapshotDTO.class);

        assertEquals(1, response.providers.size());
        SnapshotProviderDTO providerDTO = response.providers.get("P1");
        assertEquals("P1", providerDTO.name);
        assertEquals("M1", providerDTO.modelName);
        assertEquals(1, providerDTO.services.size());
        SnapshotServiceDTO serviceDTO = providerDTO.services.get("S1");
        assertEquals("S1", serviceDTO.name);
        assertEquals(1, serviceDTO.resources.size());
        SnapshotResourceDTO resourceDTO = serviceDTO.resources.get("R1");
        assertEquals("R1", resourceDTO.name);
        assertEquals("java.lang.String", resourceDTO.type);
        assertEquals("V1", resourceDTO.value);
    }
}

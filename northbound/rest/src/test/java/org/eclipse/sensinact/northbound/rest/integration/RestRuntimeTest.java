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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodySubscribers;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.northbound.rest.impl.BundleDTO;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.Application;

@WithConfiguration(pid = "sensinact.session.manager", properties = @Property(key = "auth.policy", value = "ALLOW_ALL"))
public class RestRuntimeTest {

    ObjectMapper mapper = new ObjectMapper();
    HttpClient client = HttpClient.newHttpClient();

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
     * Get the bundles
     */
    @Test
    void getBundles() throws Exception {
        URI uri = URI.create("http://localhost:8185/sensinact/runtime/bundles");
        final HttpRequest req = HttpRequest.newBuilder(uri).build();
        final HttpResponse<InputStream> response = client.send(req, (x) -> BodySubscribers.ofInputStream());
        List<BundleDTO> bundles = mapper.readValue(response.body(), new TypeReference<List<BundleDTO>>() {});

        assertTrue(bundles.size() > 50);
        assertTrue(bundles.size() < 150);
        List<BundleDTO> jakartaBundles = bundles.stream().filter(v -> v.name.equals("jakarta.activation-api")).toList();
        assertEquals(1, jakartaBundles.size());
        assertEquals("2.1.0", jakartaBundles.get(0).version);
    }
}

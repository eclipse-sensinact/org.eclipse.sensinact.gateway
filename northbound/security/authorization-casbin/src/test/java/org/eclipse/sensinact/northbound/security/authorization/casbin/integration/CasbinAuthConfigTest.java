/*********************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Kentyou - initial implementation
 **********************************************************************/

package org.eclipse.sensinact.northbound.security.authorization.casbin.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.annotation.verb.ACT;
import org.eclipse.sensinact.core.annotation.verb.ActParam;
import org.eclipse.sensinact.core.authorization.NotPermittedException;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.ResourceType;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.model.Service;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.whiteboard.WhiteboardConstants;
import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.security.authorization.casbin.Constants;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.Type;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

@WithConfiguration(pid = "sensinact.session.manager", properties = {
        @Property(key = "auth.policy", value = "ALLOW_ALL"),
        @Property(key = "name", value = "test-session"),
})
public class CasbinAuthConfigTest {

    @InjectBundleContext
    BundleContext ctx;

    @InjectService(filter = "(name=test-session)", timeout = 1000)
    SensiNactSessionManager sessionManager;

    @InjectService
    GatewayThread thread;

    @InjectService
    DataUpdate push;

    UserInfo makeUser(final String name, final String... groups) {
        return new UserInfo() {
            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public String getUserId() {
                return name;
            }

            @Override
            public Collection<String> getGroups() {
                return Arrays.asList(groups);
            }
        };
    }

    @BeforeEach
    void setup() throws Exception {
        // Make sure the providers exist
        GenericDto dto = new GenericDto();
        dto.provider = "provider";
        dto.service = "svc";
        dto.resource = "rc";
        dto.type = Integer.class;
        dto.value = 42;
        push.pushUpdate(dto);

        dto.provider = "nope";
        push.pushUpdate(dto);

        dto.model = "public";
        dto.provider = "public";
        dto.service = "data";
        push.pushUpdate(dto);

        dto.service = "action";
        push.pushUpdate(dto);
    }

    @Test
    @WithConfiguration(pid = Constants.CONFIGURATION_PID, location = "?", properties = {
            @Property(key = "allowByDefault", value = "true"),
            @Property(key = "policies", type = Type.Array, value = {
                    "role:user, *, *, *, *, *, describe|read, allow, 1000", "role:user, *, *, nope, *, *, *, deny, 0",
                    "role:user, *, *, *, *, *, *, allow, 1000", }) })
    void userTest(@InjectService AuthorizationEngine engine) throws Exception {
        final SensiNactSession session = sessionManager.createNewSession(makeUser("foo", "user"));

        // Access the sensiNact provider
        Instant startValue = session.getResourceValue("sensiNact", "system", "started", Instant.class);
        assertNotNull(startValue);
        // Try writing it
        assertThrows(NotPermittedException.class,
                () -> session.setResourceValue("sensiNact", "system", "started", Instant.now()));

        // Access another provider
        assertNotNull(session.describeResource("provider", "svc", "rc"));
        Integer value = session.getResourceValue("provider", "svc", "rc", Integer.class);
        assertEquals(42, value);
        // Write it
        session.setResourceValue("provider", "svc", "rc", 21);
        value = session.getResourceValue("provider", "svc", "rc", Integer.class);
        assertEquals(21, value);

        // Access the "nope" provider
        assertThrows(NotPermittedException.class, () -> session.describeResource("nope", "svc", "rc"));
        assertThrows(NotPermittedException.class, () -> session.getResourceValue("nope", "svc", "rc", Integer.class));
        assertThrows(NotPermittedException.class, () -> session.setResourceValue("nope", "svc", "rc", 21));
    }

    @Test
    @WithConfiguration(pid = Constants.CONFIGURATION_PID, location = "?", properties = {
            @Property(key = "allowByDefault", value = "false"),
            @Property(key = "policies", type = Type.Array, value = { "anonymous, *, *, public, data, *, read, allow, 0",
                    "anonymous, *, *, public, action, comment, describe|act, allow, 0", }) })
    void anonymousTest(@InjectService AuthorizationEngine engine) throws Exception {
        final SensiNactSession session = sessionManager.createNewAnonymousSession();
        // Access the sensiNact provider
        Instant startValue = session.getResourceValue("sensiNact", "system", "started", Instant.class);
        assertNotNull(startValue);
        // Try writing it
        assertThrows(NotPermittedException.class,
                () -> session.setResourceValue("sensiNact", "system", "started", Instant.now()));

        // Access another provider
        assertThrows(NotPermittedException.class,
                () -> session.getResourceValue("provider", "svc", "rc", Integer.class));
        assertThrows(NotPermittedException.class, () -> session.setResourceValue("provider", "svc", "rc", 21));

        // Access the "nope" provider
        assertThrows(NotPermittedException.class, () -> session.describeResource("nope", "svc", "rc"));
        assertThrows(NotPermittedException.class, () -> session.getResourceValue("nope", "svc", "rc", Integer.class));
        assertThrows(NotPermittedException.class, () -> session.setResourceValue("nope", "svc", "rc", 21));

        // Access the public provider
        assertThrows(NotPermittedException.class, () -> session.describeResource("public", "data", "rc"));
        Integer value = session.getResourceValue("public", "data", "rc", Integer.class);
        assertEquals(42, value);

        assertThrows(NotPermittedException.class, () -> session.describeResource("nope", "action", "rc"));
        assertThrows(NotPermittedException.class,
                () -> session.getResourceValue("nope", "action", "rc", Integer.class));

        // Register the method
        thread.execute(new AbstractSensinactCommand<Void>() {
            @Override
            protected Promise<Void> call(SensinactDigitalTwin twin, SensinactModelManager modelMgr, PromiseFactory pf) {
                Service service = modelMgr.getModel("public").getServices().get("action");
                service.createResource("comment").withResourceType(ResourceType.ACTION).withType(String.class)
                        .withAction(List.of(Map.entry("text", String.class))).build();
                return pf.resolved(null);
            }
        }).getValue();

        // Register the whiteboard pattern
        var svcReg = ctx.registerService(CommentHandler.class, new CommentHandler(),
                new Hashtable<>(Map.of(WhiteboardConstants.PROP_RESOURCE, "true")));

        // Ensure we can act
        try {
            assertEquals("comment:toto", session.actOnResource("public", "action", "comment", Map.of("text", "toto")));
        } finally {
            svcReg.unregister();
        }
    }

    public static class CommentHandler {
        @ACT(model = "public", service = "action", resource = "comment")
        public String comment(@ActParam("text") String text) {
            return "comment:" + text;
        }
    }
}

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

package org.eclipse.sensinact.northbound.security.authorization.casbin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.sensinact.core.authorization.PermissionLevel;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.northbound.security.api.PreAuthorizer;
import org.eclipse.sensinact.northbound.security.api.PreAuthorizer.PreAuth;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.osgi.util.promise.Promise;

public class CasbinBehaviourTest {

    CasbinAuthorizationEngine engine;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        final GatewayThread gatewayMock = Mockito.mock(GatewayThread.class);
        when(gatewayMock.execute(any())).thenReturn(Mockito.mock(Promise.class));

        engine = new CasbinAuthorizationEngine();
        engine.gateway = gatewayMock;
    }

    CasbinAuthorizationConfiguration configure(final boolean allowByDefault, final String[] policies) {
        return new CasbinAuthorizationConfiguration() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return CasbinAuthorizationConfiguration.class;
            }

            @Override
            public String[] policies() {
                return policies;
            }

            @Override
            public boolean allowByDefault() {
                return allowByDefault;
            }
        };
    }

    UserInfo makeUser(final String name, final String... groups) {
        return new UserInfo() {
            @Override
            public boolean isAuthenticated() {
                return !"<<ANONYMOUS>>".equals(name);
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

    @Test
    void parsingTest() throws Exception {
        // Specific target
        Policy policy = engine.parsePolicy("user, provider, svc, rc, READ, deny, -1000");
        assertEquals("user", policy.subject());
        assertEquals("provider", policy.provider());
        assertEquals("svc", policy.service());
        assertEquals("rc", policy.resource());
        assertEquals("READ", policy.level());
        assertEquals(PolicyEffect.deny, policy.eft());
        assertEquals(-1000, policy.priority());
        assertEquals(List.of("user", "provider", "svc", "rc", "READ", "deny", "-1000"), policy.toList());

        // Wildcard policy
        policy = engine.parsePolicy("*, *, *, *, describe, allow, 1000");
        assertEquals("*", policy.subject());
        assertEquals("*", policy.provider());
        assertEquals("*", policy.service());
        assertEquals("*", policy.resource());
        assertEquals(PermissionLevel.DESCRIBE.name(), policy.level());
        assertEquals(PolicyEffect.allow, policy.eft());
        assertEquals(1000, policy.priority());
        assertEquals(List.of("*", ".*", ".*", ".*", "DESCRIBE", "allow", "1000"), policy.toList());

        // All levels
        policy = engine.parsePolicy("*, *, *, *, *, allow, 0");
        assertEquals("*", policy.level());
        assertEquals(List.of("*", ".*", ".*", ".*", ".*", "allow", "0"), policy.toList());

        // Invalid priority
        assertNull(engine.parsePolicy("*, *, *, *, *, allow, xxx"));
        // Invalid effect
        assertNull(engine.parsePolicy("*, *, *, *, *, xxx, 1000"));
        // Invalid number of fields
        assertNull(engine.parsePolicy("prefix, *, *, *, *, *, allow, 1000"));
        assertNull(engine.parsePolicy("*, *, *, *, *, allow, 1000, suffix"));
        assertNull(engine.parsePolicy("prefix, *, *, *, *, *, allow, 1000, suffix"));
    }

    @Test
    void defaultPoliciesTest() throws Exception {
        // Deny by default
        engine.activate(configure(false, null));

        PreAuthorizer adminAuth = engine.createPreAuthorizer(makeUser("admin", "admin"));
        PreAuthorizer userAuth = engine.createPreAuthorizer(makeUser("foo", "user"));
        PreAuthorizer anonAuth = engine.createPreAuthorizer(makeUser("<<ANONYMOUS>>", "anonymous"));

        assertEquals(PreAuth.ALLOW, adminAuth.preAuthResource(PermissionLevel.DESCRIBE, "provider", "svc", "rc"));
        assertEquals(PreAuth.ALLOW, userAuth.preAuthResource(PermissionLevel.DESCRIBE, "provider", "svc", "rc"));
        assertEquals(PreAuth.DENY, anonAuth.preAuthResource(PermissionLevel.DESCRIBE, "provider", "svc", "rc"));

        assertEquals(PreAuth.ALLOW, adminAuth.preAuthResource(PermissionLevel.READ, "provider", "svc", "rc"));
        assertEquals(PreAuth.DENY, userAuth.preAuthResource(PermissionLevel.READ, "provider", "svc", "rc"));
        assertEquals(PreAuth.DENY, anonAuth.preAuthResource(PermissionLevel.READ, "provider", "svc", "rc"));

        assertEquals(PreAuth.ALLOW, adminAuth.preAuthResource(PermissionLevel.UPDATE, "provider", "svc", "rc"));
        assertEquals(PreAuth.DENY, userAuth.preAuthResource(PermissionLevel.UPDATE, "provider", "svc", "rc"));
        assertEquals(PreAuth.DENY, anonAuth.preAuthResource(PermissionLevel.UPDATE, "provider", "svc", "rc"));

        assertEquals(PreAuth.ALLOW, adminAuth.preAuthResource(PermissionLevel.ACT, "provider", "svc", "rc"));
        assertEquals(PreAuth.DENY, userAuth.preAuthResource(PermissionLevel.ACT, "provider", "svc", "rc"));
        assertEquals(PreAuth.DENY, anonAuth.preAuthResource(PermissionLevel.ACT, "provider", "svc", "rc"));

        // Allow by default
        engine.deactivate();
        engine.activate(configure(true, null));

        adminAuth = engine.createPreAuthorizer(makeUser("admin", "admin"));
        userAuth = engine.createPreAuthorizer(makeUser("foo", "user"));
        anonAuth = engine.createPreAuthorizer(makeUser("<<ANONYMOUS>>", "anonymous"));

        assertEquals(PreAuth.ALLOW, adminAuth.preAuthResource(PermissionLevel.DESCRIBE, "provider", "svc", "rc"));
        assertEquals(PreAuth.ALLOW, userAuth.preAuthResource(PermissionLevel.DESCRIBE, "provider", "svc", "rc"));
        assertEquals(PreAuth.DENY, anonAuth.preAuthResource(PermissionLevel.DESCRIBE, "provider", "svc", "rc"));

        assertEquals(PreAuth.ALLOW, adminAuth.preAuthResource(PermissionLevel.READ, "provider", "svc", "rc"));
        assertEquals(PreAuth.ALLOW, userAuth.preAuthResource(PermissionLevel.READ, "provider", "svc", "rc"));
        assertEquals(PreAuth.DENY, anonAuth.preAuthResource(PermissionLevel.READ, "provider", "svc", "rc"));

        assertEquals(PreAuth.ALLOW, adminAuth.preAuthResource(PermissionLevel.UPDATE, "provider", "svc", "rc"));
        assertEquals(PreAuth.ALLOW, userAuth.preAuthResource(PermissionLevel.UPDATE, "provider", "svc", "rc"));
        assertEquals(PreAuth.DENY, anonAuth.preAuthResource(PermissionLevel.UPDATE, "provider", "svc", "rc"));

        assertEquals(PreAuth.ALLOW, adminAuth.preAuthResource(PermissionLevel.ACT, "provider", "svc", "rc"));
        assertEquals(PreAuth.ALLOW, userAuth.preAuthResource(PermissionLevel.ACT, "provider", "svc", "rc"));
        assertEquals(PreAuth.DENY, anonAuth.preAuthResource(PermissionLevel.ACT, "provider", "svc", "rc"));
    }

    @Test
    void basicTest() throws Exception {
        engine.activate(configure(false, new String[] {
                // Describe for all and everything
                "*, *, *, *, DESCRIBE, allow, 1000",
                // Read svc/rc for all
                "*, provider, svc, rc, READ, allow, 100",
                // Foo explicitly can't update svc/rc
                "foo, provider, svc, rc, UPDATE, deny, 0",
                // Bar explicitly can update svc/rc
                "bar, provider, svc, rc, UPDATE, allow, 0",
                // Actors can act everywhere, but can't describe
                "role:actor, provider, svc, rc, DESCRIBE|READ|UPDATE, deny, 0",
                "role:actor, provider, svc, rc, ACT, allow, 0", }));

        final PreAuthorizer fooAuth = engine.createPreAuthorizer(makeUser("foo"));
        final PreAuthorizer barAuth = engine.createPreAuthorizer(makeUser("bar"));
        final PreAuthorizer foobarAuth = engine.createPreAuthorizer(makeUser("foobar", "actor"));

        assertEquals(PreAuth.ALLOW, fooAuth.preAuthResource(PermissionLevel.DESCRIBE, "provider", "svc", "rc"));
        assertEquals(PreAuth.ALLOW, barAuth.preAuthResource(PermissionLevel.DESCRIBE, "provider", "svc", "rc"));
        assertEquals(PreAuth.DENY, foobarAuth.preAuthResource(PermissionLevel.DESCRIBE, "provider", "svc", "rc"));

        assertEquals(PreAuth.ALLOW, fooAuth.preAuthResource(PermissionLevel.READ, "provider", "svc", "rc"));
        assertEquals(PreAuth.ALLOW, barAuth.preAuthResource(PermissionLevel.READ, "provider", "svc", "rc"));
        assertEquals(PreAuth.DENY, foobarAuth.preAuthResource(PermissionLevel.READ, "provider", "svc", "rc"));

        assertEquals(PreAuth.DENY, fooAuth.preAuthResource(PermissionLevel.UPDATE, "provider", "svc", "rc"));
        assertEquals(PreAuth.ALLOW, barAuth.preAuthResource(PermissionLevel.UPDATE, "provider", "svc", "rc"));
        assertEquals(PreAuth.DENY, foobarAuth.preAuthResource(PermissionLevel.UPDATE, "provider", "svc", "rc"));

        // Expect unknown as no explicit rule forbid nor allow actions
        assertEquals(PreAuth.UNKNOWN, fooAuth.preAuthResource(PermissionLevel.ACT, "provider", "svc", "rc"));
        assertEquals(PreAuth.UNKNOWN, barAuth.preAuthResource(PermissionLevel.ACT, "provider", "svc", "rc"));
        assertEquals(PreAuth.ALLOW, foobarAuth.preAuthResource(PermissionLevel.ACT, "provider", "svc", "rc"));
    }
}

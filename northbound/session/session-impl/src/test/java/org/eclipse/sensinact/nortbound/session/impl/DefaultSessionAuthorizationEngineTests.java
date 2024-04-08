/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.nortbound.session.impl;

import static org.eclipse.sensinact.nortbound.session.impl.DefaultAuthPolicy.ALLOW_ALL;
import static org.eclipse.sensinact.nortbound.session.impl.DefaultAuthPolicy.AUTHENTICATED_ONLY;
import static org.eclipse.sensinact.nortbound.session.impl.DefaultAuthPolicy.DENY_ALL;
import static org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.Authorizer.PreAuth.ALLOW;
import static org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.Authorizer.PreAuth.DENY;
import static org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.PermissionLevel.DESCRIBE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine.Authorizer;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests the get/set behaviour of the Session
 */
public class DefaultSessionAuthorizationEngineTests {

    private static final String MODEL_URI = "test/model/uri";
    private static final String MODEL = "model";
    private static final String PROVIDER = "provider";
    private static final String SERVICE = "service";
    private static final String RESOURCE = "resource";

    @ParameterizedTest
    @MethodSource("testArgs")
    void testAuthorizer(DefaultAuthPolicy policy, UserInfo user, Authorizer.PreAuth preAuth, boolean permission,
            Function<Collection<String>, Collection<String>> transform) {
        DefaultSessionAuthorizationEngine engine = new DefaultSessionAuthorizationEngine(policy);
        Authorizer authorizer = engine.createAuthorizer(user);

        assertEquals(preAuth, authorizer.preAuthProvider(DESCRIBE, PROVIDER));
        assertEquals(preAuth, authorizer.preAuthService(DESCRIBE, PROVIDER, SERVICE));
        assertEquals(preAuth, authorizer.preAuthResource(DESCRIBE, PROVIDER, SERVICE, RESOURCE));

        assertEquals(permission, authorizer.hasProviderPermission(DESCRIBE, MODEL_URI, MODEL, PROVIDER));
        assertEquals(permission, authorizer.hasServicePermission(DESCRIBE, MODEL_URI, MODEL, PROVIDER, SERVICE));
        assertEquals(permission, authorizer.hasResourcePermission(DESCRIBE, MODEL_URI, MODEL, PROVIDER, SERVICE, RESOURCE));

        Set<String> set = Set.of(SERVICE);
        assertEquals(transform.apply(set), authorizer.visibleServices(MODEL_URI, MODEL, PROVIDER, set));

        set = Set.of(RESOURCE);
        assertEquals(transform.apply(set), authorizer.visibleResources(MODEL_URI, MODEL, PROVIDER, SERVICE, set));
    }

    static List<Arguments> testArgs () {
        UserInfo anon = new TestUserInfo("<ANON>", false);
        UserInfo bob = new TestUserInfo("bob", true);
        Function<Collection<String>, Collection<String>> copy = List::copyOf;
        Function<Collection<String>, Collection<String>> remove = x -> List.of();

        return List.of(
                arguments(ALLOW_ALL, anon, ALLOW, true, copy),
                arguments(ALLOW_ALL, bob, ALLOW, true, copy),
                arguments(AUTHENTICATED_ONLY, anon, DENY, false, remove),
                arguments(AUTHENTICATED_ONLY, bob, ALLOW, true, copy),
                arguments(DENY_ALL, anon, DENY, false, remove),
                arguments(DENY_ALL, bob, DENY, false, remove)
            );
    }
}

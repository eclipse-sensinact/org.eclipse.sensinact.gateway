/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.northbound.rest.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Map;

import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.SecurityContext;

/**
 * The default session of a user outlives the request that created it, so the
 * user info it keeps must not depend on the security context of that request
 */
public class SensinactSessionProviderTest {

    private final SensiNactSessionManager sessionManager = mock(SensiNactSessionManager.class);

    private final SecurityContext securityContext = mock(SecurityContext.class);

    private final SensinactSessionProvider provider = new SensinactSessionProvider();

    @BeforeEach
    void setup() {
        Application application = mock(Application.class);
        when(application.getProperties()).thenReturn(Map.of("session.manager", sessionManager));
        when(sessionManager.getDefaultSession(any())).thenReturn(mock(SensiNactSession.class));
        provider.application = application;
        provider.context = securityContext;
    }

    /**
     * Gets the session of a principal that is not a sensiNact one, as done by an
     * external authentication filter, and returns the user info given to the
     * session manager
     */
    private UserInfo sessionUserOf(String userId) {
        Principal principal = () -> userId;
        when(securityContext.getUserPrincipal()).thenReturn(principal);
        when(securityContext.isUserInRole("admin")).thenReturn(true);

        provider.getContext(SensiNactSession.class);

        ArgumentCaptor<UserInfo> captor = ArgumentCaptor.forClass(UserInfo.class);
        verify(sessionManager).getDefaultSession(captor.capture());
        return captor.getValue();
    }

    /**
     * Makes the injected context behave as outside of a request: HK2 then
     * reports that its service locator has been shut down
     */
    private void endRequest() {
        IllegalStateException shutDown = new IllegalStateException("ServiceLocatorImpl has been shut down");
        when(securityContext.getUserPrincipal()).thenThrow(shutDown);
        when(securityContext.isUserInRole(any())).thenThrow(shutDown);
    }

    @Test
    void userIdOfTheSessionIsKeptAfterTheRequest() {
        UserInfo user = sessionUserOf("adminTester");

        endRequest();

        // Checking if the session expired reads its user id
        assertEquals("adminTester", user.getUserId());
    }

    @Test
    void userIdIsUpdatedDuringARequest() {
        UserInfo user = sessionUserOf("adminTester");

        Principal renamed = () -> "adminTester2";
        when(securityContext.getUserPrincipal()).thenReturn(renamed);
        assertEquals("adminTester2", user.getUserId());

        // The last id read is the one kept outside of a request
        endRequest();
        assertEquals("adminTester2", user.getUserId());
    }

    @Test
    void groupsAreCheckedDuringTheRequest() {
        UserInfo user = sessionUserOf("adminTester");

        assertTrue(user.isMemberOfGroup("admin"));
    }

    @Test
    void userIsNotMemberOfAGroupOutsideOfARequest() {
        UserInfo user = sessionUserOf("adminTester");

        endRequest();

        assertFalse(user.isMemberOfGroup("admin"));
    }
}

/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static jakarta.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE;
import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;
import static java.lang.Boolean.FALSE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.eclipse.sensinact.northbound.security.api.Authenticator.Scheme.TOKEN;
import static org.eclipse.sensinact.northbound.security.api.Authenticator.Scheme.USER_PASSWORD;

import java.io.IOException;
import java.security.Principal;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.sensinact.northbound.security.api.Authenticator;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.security.api.Authenticator.Scheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 * Sets the security context based on the supplied credentials
 */
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Context
    Application application;

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {

        String authHeader = requestContext.getHeaderString(AUTHORIZATION);

        if (authHeader == null) {
            if (!(Boolean) application.getProperties().getOrDefault("raw.anonymous.access", FALSE)) {
                requestContext.abortWith(unauthorizedResponse());
            }
        } else {
            String[] headerChunks = authHeader.split(" ", 2);

            if (headerChunks.length != 2) {
                requestContext.abortWith(Response.status(BAD_REQUEST).build());
            }

            Scheme authScheme;
            String userid;
            String credential;
            if ("Bearer".equals(headerChunks[0])) {
                authScheme = TOKEN;
                userid = null;
                credential = headerChunks[1];
            } else if ("Basic".equals(headerChunks[0])) {
                authScheme = USER_PASSWORD;
                String cred = new String(Base64.getMimeDecoder().decode(headerChunks[1]), UTF_8);
                String[] credChunks = cred.split(":", 2);
                userid = credChunks[0];
                credential = credChunks[1];
            } else {
                authScheme = null;
                userid = null;
                credential = null;
            }

            Optional<UserInfo> user = getAuthenticators().stream().filter(a -> a.getScheme() == authScheme)
                    .map(a -> tryAuth(a, userid, credential)).filter(u -> u != null).findFirst();

            if (user.isEmpty()) {
                requestContext.abortWith(unauthorizedResponse());
            } else {
                requestContext.setSecurityContext(new UserInfoSecurityContext(TOKEN.getHttpScheme(), user.get()));
            }
        }
    }

    private Response unauthorizedResponse() {
        Collection<Authenticator> authenticators = getAuthenticators();
        if (authenticators.isEmpty()) {
            return Response.status(SERVICE_UNAVAILABLE).build();
        } else {
            return Response.status(UNAUTHORIZED).header(WWW_AUTHENTICATE, getAuthHeader(authenticators)).build();
        }
    }

    private String getAuthHeader(Collection<Authenticator> authenticators) {
        return authenticators.stream()
                .map(a -> String.format("%s realm=%s", a.getScheme().getHttpScheme(), a.getRealm()))
                .collect(Collectors.joining(", "));
    }

    @SuppressWarnings("unchecked")
    private Collection<Authenticator> getAuthenticators() {
        return (Collection<Authenticator>) application.getProperties().getOrDefault("authentication.providers",
                Set.of());
    }

    private UserInfo tryAuth(Authenticator a, String user, String credential) {
        UserInfo ui = null;
        try {
            ui = a.authenticate(user, credential);
        } catch (Exception e) {
            LOG.warn("Failed to authenticate user {}", user, e);
        }
        return ui;
    }

    public static class UserInfoPrincipal implements Principal {
        private final UserInfo info;

        public UserInfoPrincipal(UserInfo info) {
            this.info = info;
        }

        @Override
        public String getName() {
            return info.getUserId();
        }

        public UserInfo getUserInfo() {
            return info;
        }
    }

    private static class UserInfoSecurityContext implements SecurityContext {

        private final String scheme;
        private final UserInfoPrincipal principal;

        public UserInfoSecurityContext(String scheme, UserInfo info) {
            this.scheme = scheme;
            this.principal = new UserInfoPrincipal(info);
        }

        @Override
        public Principal getUserPrincipal() {
            return principal;
        }

        @Override
        public boolean isUserInRole(String role) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public String getAuthenticationScheme() {
            return scheme;
        }

    }
}

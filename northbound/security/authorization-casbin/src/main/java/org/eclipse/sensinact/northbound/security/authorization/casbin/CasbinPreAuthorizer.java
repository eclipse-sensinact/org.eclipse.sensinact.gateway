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

import org.casbin.jcasbin.main.Enforcer;
import org.eclipse.sensinact.core.authorization.PermissionLevel;
import org.eclipse.sensinact.northbound.security.api.PreAuthorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CasbinPreAuthorizer implements PreAuthorizer {

    private static final Logger logger = LoggerFactory.getLogger(CasbinPreAuthorizer.class);

    /**
     * User associated to this authorizer
     */
    private final String subject;

    /**
     * Casbin enforcer
     */
    private final Enforcer enforcer;

    /**
     * Flag to allow operations if something went wrong checking it
     */
    private final boolean allowByDefault;

    /**
     * Prepares the pre-authorizer for a user session
     *
     * @param subject  User ID
     * @param enforcer Enforcer to use to authorize operations
     */
    public CasbinPreAuthorizer(final String subject, final Enforcer enforcer) {
        this(subject, enforcer, false);
    }

    /**
     * Prepares the pre-authorizer for a user session
     *
     * @param subject        User ID
     * @param enforcer       Enforcer to use to authorize operations
     * @param allowByDefault Allow operations if something went wrong checking it
     */
    public CasbinPreAuthorizer(final String subject, final Enforcer enforcer, final boolean allowByDefault) {
        this.subject = subject;
        this.enforcer = enforcer;
        this.allowByDefault = allowByDefault;
    }

    /**
     * Check if the operation is allowed
     *
     * @param modelPackageUri Target provider model package URI
     * @param model           Target provider model name
     * @param provider        Target provider name
     * @param service         Target service
     * @param resource        Target resource
     * @param level           Requested access level
     * @return True if allowed, else false
     */
    public PreAuth authorize(final String modelPackageUri, final String model, final String provider,
            final String service, final String resource, final PermissionLevel level) {
        try {
            final boolean allowed = enforcer.enforce(subject, modelPackageUri, model, provider, service, resource,
                    level.name());
            return allowed ? PreAuth.ALLOW : PreAuth.DENY;
        } catch (Exception e) {
            logger.error("Error checking authorization of {} on {}", subject,
                    String.join("/", provider, service, resource), e);
            return PreAuth.UNKNOWN;
        }
    }

    /**
     * Converts a pre-authorization result to a boolean
     *
     * @param preAuth {@link PreAuth} result of
     *                {@link #authorize(String, String, String, String, String, PermissionLevel)}
     * @return True if allowed, else false
     */
    private boolean normalize(final PreAuth preAuth) {
        switch (preAuth) {
        case ALLOW:
            return true;

        case DENY:
            return false;

        case UNKNOWN:
        default:
            return allowByDefault;
        }
    }

    @Override
    public boolean hasProviderPermission(PermissionLevel level, String modelPackageUri, String model, String provider) {
        return normalize(authorize(modelPackageUri, model, provider, "<unknown>", "<unknown>", level));
    }

    @Override
    public boolean hasServicePermission(PermissionLevel level, String modelPackageUri, String model, String provider,
            String service) {
        return normalize(authorize(modelPackageUri, model, provider, service, "<unknown>", level));
    }

    @Override
    public boolean hasResourcePermission(PermissionLevel level, String modelPackageUri, String model, String provider,
            String service, String resource) {
        return normalize(authorize(modelPackageUri, model, provider, service, resource, level));
    }

    @Override
    public PreAuth preAuthProvider(PermissionLevel level, String provider) {
        return authorize("<unknown>", "<unknown>", provider, "<unknown>", "<unknown>", level);
    }

    @Override
    public PreAuth preAuthService(PermissionLevel level, String provider, String service) {
        return authorize("<unknown>", "<unknown>", provider, service, "<unknown>", level);
    }

    @Override
    public PreAuth preAuthResource(PermissionLevel level, String provider, String service, String resource) {
        return authorize("<unknown>", "<unknown>", provider, service, resource, level);
    }
}

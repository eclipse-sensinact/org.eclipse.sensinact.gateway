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

import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.casbin.jcasbin.main.Enforcer;
import org.eclipse.sensinact.core.authorization.Authorizer;
import org.eclipse.sensinact.core.authorization.PermissionLevel;
import org.eclipse.sensinact.northbound.security.api.PreAuthorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CasbinPreAuthorizer implements PreAuthorizer, Authorizer {

    private static final Logger logger = LoggerFactory.getLogger(CasbinPreAuthorizer.class);

    /**
     * User associated to this authorizer
     */
    private final String subject;

    /**
     * Casbin enforcer
     */
    private final Enforcer enforcer;

    private final boolean allowByDefault;

    /**
     * Prepares the pre-authorizer for a user session
     *
     * @param subject        User ID
     * @param enforcer       Enforcer to use to authorize operations
     * @param allowByDefault Allow actions without explicit rules
     */
    public CasbinPreAuthorizer(final String subject, final Enforcer enforcer, final boolean allowByDefault) {
        this.subject = subject;
        this.enforcer = enforcer;
        this.allowByDefault = allowByDefault;
    }

    /**
     * Check if the operation is allowed
     *
     * @param provider Target provider name
     * @param service  Target service
     * @param resource Target resource
     * @param level    Requested access level
     * @return True if allowed, else false
     */
    public PreAuth authorize(final String provider, final String service, final String resource,
            final PermissionLevel level) {
        try {
            if (enforcer.enforce(subject, provider, service, resource, level.name())) {
                // Explicit allow
                return PreAuth.ALLOW;
            } else if (hasExplicitPolicy(provider, service, resource, level)) {
                // Explicit denial
                return PreAuth.DENY;
            } else {
                // No explicit rule
                return PreAuth.UNKNOWN;
            }
        } catch (Exception e) {
            logger.error("Error checking authorization of {} on {}", subject,
                    String.join("/", provider, service, resource), e);
            return PreAuth.UNKNOWN;
        }
    }

    /**
     * Checks if an explicit policy exists for the given permission
     *
     * @param provider Target provider
     * @param service  Target service
     * @param resource Target resource
     * @param level    Target permission level
     * @return True if an explicit rule exists, else false
     */
    boolean hasExplicitPolicy(final String provider, final String service, final String resource,
            final PermissionLevel level) {
        return Stream
                .concat(enforcer.getNamedImplicitPermissionsForUser("p", subject, new String[0]).stream(),
                        enforcer.getNamedImplicitPermissionsForUser("p", "*", new String[0]).stream())
                .anyMatch(fields -> policyMatchField(fields.get(1), provider)
                        && policyMatchField(fields.get(2), service) && policyMatchField(fields.get(3), resource)
                        && policyMatchField(fields.get(4), level.name()));
    }

    /**
     * Checks if the given tested field matches its policy field
     *
     * @param policyField Field value in policy definition
     * @param testedField Tested field value
     * @return True if the tested field matches the policy definition
     */
    boolean policyMatchField(final String policyField, final String testedField) {
        return policyField.equals("*") || policyField.equals(testedField) || Pattern.matches(policyField, testedField);
    }

    @Override
    public PreAuth preAuthProvider(PermissionLevel level, String provider) {
        return authorize(provider, Constants.UNKNOWN_FIELD, Constants.UNKNOWN_FIELD, level);
    }

    @Override
    public PreAuth preAuthService(PermissionLevel level, String provider, String service) {
        return authorize(provider, service, Constants.UNKNOWN_FIELD, level);
    }

    @Override
    public PreAuth preAuthResource(PermissionLevel level, String provider, String service, String resource) {
        return authorize(provider, service, resource, level);
    }

    boolean normalize(final PreAuth preAuth) {
        return preAuth == PreAuth.ALLOW || (preAuth == PreAuth.UNKNOWN && allowByDefault);
    }

    @Override
    public boolean hasProviderPermission(PermissionLevel level, String modelPackageUri, String model, String provider) {
        return normalize(preAuthProvider(level, provider));
    }

    @Override
    public boolean hasServicePermission(PermissionLevel level, String modelPackageUri, String model, String provider,
            String service) {
        return normalize(preAuthService(level, provider, service));
    }

    @Override
    public boolean hasResourcePermission(PermissionLevel level, String modelPackageUri, String model, String provider,
            String service, String resource) {
        return normalize(preAuthResource(level, provider, service, resource));
    }
}

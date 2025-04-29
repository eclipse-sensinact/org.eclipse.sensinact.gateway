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

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.eclipse.sensinact.core.authorization.PermissionLevel;
import org.eclipse.sensinact.northbound.security.api.PreAuthorizer;
import org.eclipse.sensinact.northbound.security.api.UserInfo;

public class CasbinPreAuthorizer implements PreAuthorizer {

    /**
     * Subject of the authorizer
     */
    private final String subject;

    /**
     * Flag to consider unknown authorizations to be allowed
     */
    private final boolean allowByDefault;

    /**
     * Casbin enforcer
     */
    private final Enforcer enforcer;

    /**
     * Prepares the pre-authorizer for a user session
     *
     * @param userInfo                 User information
     * @param modelPermissionExtractor Function to call to load authorization
     *                                 details from the sensiNact model
     * @param allowByDefault           Either allow (true) or deny (false) if the
     *                                 authorization couldn't be determined
     */
    public CasbinPreAuthorizer(final UserInfo userInfo,
            final BiFunction<UserInfo, SensinactAccess, PreAuth> modelPermissionExtractor,
            final boolean allowByDefault) {
        // Keep track of the subject
        this.subject = userInfo.getUserId();
        this.allowByDefault = allowByDefault;

        final List<String> snaFields = List.of("modelPackageUri", "model", "provider", "service", "resource", "level");
        final List<String> commonArgs = Stream.concat(Stream.of("sub"), snaFields.stream()).toList();

        // Define a basic model
        final Model model = new Model();
        // Request definition
        model.addDef("r", "r", String.join(", ", commonArgs));
        // Policy definition
        model.addDef("p", "p",
                String.join(", ", Stream.concat(commonArgs.stream(), Stream.of("eft", "priority")).toList()));
        // Roles
        model.addDef("g", "g", "_, _");
        // Policy effect
        model.addDef("e", "e", "priority(p.eft) || deny");

        // Matchers
        final String authFromModelCall = String.format("%s(%s)", Constants.AUTH_FROM_MODEL,
                commonArgs.stream().map(f -> "r." + f).collect(Collectors.joining(", ")));
        final String fieldsMatch = snaFields.stream().map(f -> "regexMatch(r.{field}, p.{field})".replace("{field}", f))
                .collect(Collectors.joining(" && "));
        model.addDef("m", "m", String.join(" && ", "(p.sub == '*' || g(r.sub, p.sub))", String
                .format("(%s != deny && (%s == allow || %s))", authFromModelCall, authFromModelCall, fieldsMatch)));

        // Prepare the enforcer
        enforcer = new Enforcer(model);

        // ... add the method that will extract authorization from the EMF
        enforcer.addFunction(Constants.AUTH_FROM_MODEL,
                new ModelAuthorizationChecker(userInfo, modelPermissionExtractor));

        // Keep track of the user roles
        if (!userInfo.isAnonymous()) {
            userInfo.getGroups().stream()
                    .forEach(g -> enforcer.addGroupingPolicy(subject, String.format("role:%s", g)));
        } else {
            enforcer.addGroupingPolicy(subject, "anonymous");
        }

        // Set the default policies:
        // - restrict write access for the sensiNact provider
        // - allow full access for the admin role
        // - allow describe access to users in all cases
        // - allow full access to users if allowByDefault is true
        // - soft deny access to anonymous if allowByDefault is true
        // - hard deny access to anonymous if allowByDefault is true (
        // Arguments: subject, modelPackageUri, model, provider, service, resource,
        // level, effect, priority
        // Effect can be "allow" or "deny"
        // The lower the priority integer value, the more important it is
        enforcer.addPolicy("*", ".*", ".*", "sensiNact", ".*", ".*", ".*", "deny", "-10000");
        enforcer.addPolicy("*", ".*", ".*", "sensiNact", ".*", ".*", "describe|read", "allow", "-10001");
        enforcer.addPolicy("role:admin", ".*", ".*", ".*", ".*", ".*", ".*", "allow", "-1000");
        if (allowByDefault) {
            enforcer.addPolicy("anonymous", ".*", ".*", ".*", ".*", "sensor-.*", ".*", "deny", "10000");
            enforcer.addPolicy("role:user", ".*", ".*", ".*", ".*", ".*", ".*", "allow", "10000");
        } else {
            enforcer.addPolicy("anonymous", ".*", ".*", ".*", ".*", ".*", ".*", "deny", "-100000");
            enforcer.addPolicy("role:user", ".*", ".*", ".*", ".*", ".*", ".*", "deny", "10001");
            enforcer.addPolicy("role:user", ".*", ".*", ".*", ".*", ".*", "describe", "allow", "10000");
        }
    }

    public PreAuth authorize(final String modelPackageUri, final String model, final String provider,
            final String service, final String resource, final PermissionLevel level) {
        try {
            final boolean allowed = enforcer.enforce(subject, modelPackageUri, model, provider, service, resource,
                    level.name());
            return allowed ? PreAuth.ALLOW : PreAuth.DENY;
        } catch (Exception e) {
            e.printStackTrace();
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
        return normalize(authorize(modelPackageUri, model, provider, modelPackageUri, model, level));
    }

    @Override
    public boolean hasServicePermission(PermissionLevel level, String modelPackageUri, String model, String provider,
            String service) {
        return normalize(authorize(modelPackageUri, model, provider, service, "", level));
    }

    @Override
    public boolean hasResourcePermission(PermissionLevel level, String modelPackageUri, String model, String provider,
            String service, String resource) {
        return normalize(authorize(modelPackageUri, model, provider, "", "", level));
    }

    @Override
    public PreAuth preAuthProvider(PermissionLevel level, String provider) {
        return authorize("", "", provider, "", "", level);
    }

    @Override
    public PreAuth preAuthService(PermissionLevel level, String provider, String service) {
        return authorize("", "", provider, service, "", level);
    }

    @Override
    public PreAuth preAuthResource(PermissionLevel level, String provider, String service, String resource) {
        return authorize("", "", provider, service, resource, level);
    }
}

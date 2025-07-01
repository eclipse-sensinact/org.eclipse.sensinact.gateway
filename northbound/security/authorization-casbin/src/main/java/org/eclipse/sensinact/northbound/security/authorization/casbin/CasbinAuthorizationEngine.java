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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.casbin.jcasbin.main.Enforcer;
import org.eclipse.sensinact.core.authorization.Authorizer;
import org.eclipse.sensinact.core.authorization.PermissionLevel;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.Model;
import org.eclipse.sensinact.core.notification.LifecycleNotification;
import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine;
import org.eclipse.sensinact.northbound.security.api.PreAuthorizer;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, configurationPid = Constants.CONFIGURATION_PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class CasbinAuthorizationEngine implements AuthorizationEngine {

    private static final Logger logger = LoggerFactory.getLogger(CasbinAuthorizationEngine.class);

    /**
     * sensiNact gateway thread
     */
    @Reference
    GatewayThread gateway;

    /**
     * Providers model cache
     */
    @Reference
    ProvidersModelCache cache;

    /**
     * Policies enforcer
     */
    private Enforcer enforcer;

    /**
     * Allow access without explicit rule
     */
    private boolean allowByDefault;

    /**
     * Component activated
     */
    @Activate
    void activate(final CasbinAuthorizationConfiguration configuration) throws Exception {
        allowByDefault = configuration.allowByDefault();

        // Create the enforcer
        enforcer = new Enforcer(CasbinUtils.makeModel());

        // Add default policies
        enforcer.addPolicies(CasbinUtils.defaultPolicies(allowByDefault).stream().map(Policy::toList).toList());

        // Add configured policies
        enforcer.addPolicies(parsePolicies(configuration.policies()).stream().map(Policy::toList).toList());
    }

    /**
     * Component deactivated
     */
    @Deactivate
    void deactivate() {
        enforcer = null;
    }

    /**
     * Parses the policy row string
     *
     * @param row Casbin policy row
     * @return The parsed policy or null if it has issues
     */
    Policy parsePolicy(String row) {
        // Normalize parts
        final String[] parts = Arrays.stream(row.split(",")).map(String::trim).map(p -> {
            switch (p) {
            case "":
            case "null":
                return null;

            default:
                return p;
            }
        }).toArray(String[]::new);

        // Keep track of indexes in case of a change in the number of entry fields
        final int levelIdx = 6;
        final int effectIdx = levelIdx + 1;
        final int priorityIdx = effectIdx + 1;

        // Check length
        if (parts.length != Policy.EXPECTED_POLICY_FIELDS) {
            logger.warn("Invalid row: {} (got {} fields, expected {})", row, parts.length,
                    Policy.EXPECTED_POLICY_FIELDS);
            return null;
        }

        // Parse level(s)
        String level = parts[levelIdx];
        if (level != null && !"*".equals(level)) {
            final List<PermissionLevel> levels = Arrays.stream(level.toUpperCase().split("\\|")).map(String::trim)
                    .map(s -> {
                        try {
                            return PermissionLevel.valueOf(s);
                        } catch (IllegalArgumentException e) {
                            logger.error("Invalid permission level '{}' in {}", s, row);
                            return null;
                        }
                    }).toList();
            if (levels.stream().anyMatch(Objects::isNull)) {
                return null;
            }

            level = levels.stream().map(Enum::name).collect(Collectors.joining("|"));
        }

        // Parse effect
        final PolicyEffect effect;
        try {
            effect = PolicyEffect.valueOf(parts[effectIdx].toLowerCase());
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("No policy effect in row: {}", row);
            return null;
        } catch (Exception e) {
            logger.warn("Invalid policy effect '{}' in row: {}", parts[effectIdx], row);
            return null;
        }

        // Parse priority
        final int priority;
        try {
            priority = Integer.parseInt(parts[priorityIdx]);
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("No priority in row: {}", row);
            return null;
        } catch (Exception e) {
            logger.warn("Invalid priority '{}' in row: {} ({})", parts[priorityIdx], row, e.getMessage());
            return null;
        }

        return new Policy(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], level, effect, priority);
    }

    /**
     * Parses the policies provided in the configuration
     *
     * @param strPolicies
     * @return
     */
    List<Policy> parsePolicies(String[] strPolicies) {
        if (strPolicies == null || strPolicies.length == 0) {
            return List.of();
        }

        return Arrays.stream(strPolicies).map(this::parsePolicy).filter(Objects::nonNull).toList();
    }

    @Override
    public PreAuthorizer createPreAuthorizer(final UserInfo user) {
        // Ensure we don't have previous roles definitions
        final String userName = user.getUserId();
        enforcer.deleteRolesForUser(userName);
        if (user.isAnonymous()) {
            enforcer.addRoleForUser(userName, Constants.ROLE_ANONYMOUS);
        } else {
            user.getGroups().forEach(g -> enforcer.addRoleForUser(userName, String.format("role:%s", g)));
        }

        return new CasbinPreAuthorizer(userName, cache, enforcer, allowByDefault);
    }

    @Override
    public Authorizer createAuthorizer(UserInfo user) {
        return (CasbinPreAuthorizer) createPreAuthorizer(user);
    }

    /**
     * Prepares a long model URI based on its package URI and its name
     *
     * @param packageUri Model package URI
     * @param name       Model name
     * @return A long model URI
     */
    String makeModelUri(final String packageUri, final String name) {
        return packageUri + name;
    }

    /**
     * Prepares a long model URI
     *
     * @param model sensiNact model
     * @return A long model URI
     */
    String makeModelUri(final Model model) {
        return makeModelUri(model.getPackageUri(), model.getName());
    }

    /**
     * Prepares a long model URI
     *
     * @param event Provider life cycle event
     * @return A long model URI
     */
    String makeModelUri(final LifecycleNotification event) {
        return makeModelUri(event.modelPackageUri(), event.model());
    }
}

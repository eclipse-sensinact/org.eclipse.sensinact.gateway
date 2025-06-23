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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.casbin.jcasbin.main.Enforcer;
import org.eclipse.sensinact.core.authorization.Authorizer;
import org.eclipse.sensinact.core.authorization.PermissionLevel;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.Model;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.notification.LifecycleNotification;
import org.eclipse.sensinact.core.notification.LifecycleNotification.Status;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.northbound.security.api.AuthorizationEngine;
import org.eclipse.sensinact.northbound.security.api.PreAuthorizer;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.typedevent.TypedEventHandler;
import org.osgi.service.typedevent.propertytypes.EventTopics;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true, configurationPid = Constants.CONFIGURATION_PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
@EventTopics({ "LIFECYCLE/*" })
public class CasbinAuthorizationEngine implements AuthorizationEngine, TypedEventHandler<LifecycleNotification> {

    private static final Logger logger = LoggerFactory.getLogger(CasbinAuthorizationEngine.class);

    /**
     * sensiNact gateway thread
     */
    @Reference
    GatewayThread gateway;

    /**
     * Model authorization policies
     */
    private final Map<String, List<Policy>> modelsPolicies = new HashMap<>();

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

        try {
            // Load policies of existing models
            gateway.execute(new AbstractSensinactCommand<Void>() {
                @Override
                protected Promise<Void> call(final SensinactDigitalTwin twin, final SensinactModelManager modelMgr,
                        final PromiseFactory pf) {
                    twin.getProviders().stream().map(p -> modelMgr.getModel(p.getModelPackageUri(), p.getModelName()))
                            .filter(Objects::nonNull).distinct().forEach(model -> {
                                try {
                                    loadPolicies(model);
                                } catch (Exception e) {
                                    logger.error("Error loading policies from {}", makeModelUri(model));
                                }
                            });
                    return pf.resolved(null);
                }
            }).getValue();
        } catch (Exception e) {
            logger.error("Error loading policies of existing models", e);
            throw e;
        }

        // Create the enforcer
        enforcer = new Enforcer(CasbinUtils.makeModel());

        // Add default policies
        enforcer.addPolicies(CasbinUtils.defaultPolicies(allowByDefault).stream().map(Policy::toList).toList());

        // Add configured policies
        enforcer.addPolicies(parsePolicies(configuration.policies()).stream().map(Policy::toList).toList());

        // Add models policies
        modelsPolicies.values().stream().flatMap(l -> l.stream()).forEach(p -> enforcer.addPolicy(p.toList()));
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
        final int levelIdx = 4;
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

        return new Policy(parts[0], parts[1], parts[2], parts[3], level, effect, priority);
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

        return new CasbinPreAuthorizer(userName, enforcer, allowByDefault);
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

    @Override
    public void notify(final String topic, final LifecycleNotification event) {
        if (event.status() != Status.PROVIDER_CREATED) {
            // No new model expected
            return;
        }

        final String modelUri = makeModelUri(event);
        if (modelsPolicies.containsKey(modelUri)) {
            // Model is already known
            return;
        }

        // New model: look its content
        try {
            gateway.execute(new AbstractSensinactCommand<Void>() {
                @Override
                protected Promise<Void> call(final SensinactDigitalTwin twin, final SensinactModelManager modelMgr,
                        final PromiseFactory pf) {
                    final Model model = modelMgr.getModel(event.modelPackageUri(), event.model());
                    if (model == null) {
                        logger.error("Incoherent state: model manager doesn't know {}", modelUri);
                        return pf.resolved(null);
                    }

                    // Load authentication policies from model
                    loadPolicies(model);

                    return pf.resolved(null);
                }
            }).getValue();
        } catch (Exception ex) {
            logger.error("Error loading authentication policies from model {}", modelUri, ex);
        }
    }

    /**
     * Loads policies from the given model into the {@link #modelsPolicies} map
     *
     * @param model Model to load
     */
    private void loadPolicies(final Model model) {
        // TODO load from model
        final List<Policy> loadedPolicies = new ArrayList<>();

        // Filter them
        // TODO: filer while loading policies
        final Iterator<Policy> iterator = loadedPolicies.iterator();
        while (iterator.hasNext()) {
            final Policy policy = iterator.next();
            if (policy.priority() < Constants.MIN_MODEL_PRIORITY) {
                logger.warn("Ignoring model policy {}: priority is too low");
                iterator.remove();
            }
        }

        // Store in cache
        modelsPolicies.put(makeModelUri(model), loadedPolicies);

        if (!loadedPolicies.isEmpty()) {
            // Update the enforcer
            enforcer.addPolicies(loadedPolicies.stream().map(Policy::toList).toList());
        }
    }
}

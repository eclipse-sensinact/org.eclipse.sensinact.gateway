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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.casbin.jcasbin.model.Model;
import org.eclipse.sensinact.core.authorization.PermissionLevel;

public class CasbinUtils {

    /**
     * Sets up the Casbin model to use
     */
    public static Model makeModel() {
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
        final String fieldsMatch = snaFields.stream().map(f -> "((e == allow && r.{field} == '<unknown>') || regexMatch(r.{field}, p.{field}))".replace("{field}", f))
                .collect(Collectors.joining(" && "));
        model.addDef("m", "m", String.join(" && ", "(p.sub == '*' || g(r.sub, p.sub))", fieldsMatch));
        return model;
    }

    /**
     * Generates the default policies
     *
     * @param allowByDefault Flag to allow all access to users by default
     * @return The default policies
     */
    public static List<Policy> defaultPolicies(final boolean allowByDefault) {
        final List<Policy> policies = new ArrayList<>();
        // Nobody can write to sensiNact
        policies.add(new Policy("*", null, null, "sensiNact", null, null, null, PolicyEffect.deny, -10000));
        policies.add(new Policy("*", null, null, "sensiNact", null, null, Stream
                .of(PermissionLevel.DESCRIBE, PermissionLevel.READ).map(Enum::name).collect(Collectors.joining("|")),
                PolicyEffect.allow, -10001));

        // Admin has access to everything
        policies.add(new Policy("role:admin", null, null, null, null, null, null, PolicyEffect.allow, -1000));

        if (allowByDefault) {
            // Soft access denial to anonymous
            policies.add(
                    new Policy(Constants.ROLE_ANONYMOUS, null, null, null, null, null, null, PolicyEffect.deny, 10000));

            // Soft access to users
            policies.add(new Policy("role:user", null, null, null, null, null, null, PolicyEffect.allow, 10000));
        } else {
            // Stronger denial for anonymous (models can't enable it)
            policies.add(
                    new Policy(Constants.ROLE_ANONYMOUS, null, null, null, null, null, null, PolicyEffect.deny, 1));

            // Soft denial for users. Allow to describe resources
            policies.add(new Policy("role:user", null, null, null, null, null, null, PolicyEffect.deny, 10001));
            policies.add(new Policy("role:user", null, null, null, null, null, PermissionLevel.DESCRIBE.name(),
                    PolicyEffect.allow, 10000));
        }

        return policies;
    }
}

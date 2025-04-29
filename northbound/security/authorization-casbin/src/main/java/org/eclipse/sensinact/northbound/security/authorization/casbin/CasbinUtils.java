/*
 * Copyright 2025 Kentyou
 * Proprietary and confidential
 *
 * All Rights Reserved.
 * Unauthorized copying of this file is strictly prohibited
 */

package org.eclipse.sensinact.northbound.security.authorization.casbin;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.casbin.jcasbin.model.Model;

public class CasbinUtils {

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
        final String fieldsMatch = snaFields.stream().map(f -> "regexMatch(r.{field}, p.{field})".replace("{field}", f))
                .collect(Collectors.joining(" && "));
        model.addDef("m", "m", String.join(" && ", "(p.sub == '*' || g(r.sub, p.sub))", fieldsMatch));
        return model;
    }

    public static List<List<String>> defaultPolicies(final boolean allowByDefault) {
        final List<Policy> policies = new ArrayList<>();
        // Nobody can write to sensiNact
        policies.add(new Policy("*", null, null, "sensiNact", null, null, null, PolicyEffect.deny, -10000));
        policies.add(new Policy("*", null, null, "sensiNact", null, null, "describe|read", PolicyEffect.allow, -10001));

        // Admin has access to everything
        policies.add(new Policy("role:admin", null, null, null, null, null, null, PolicyEffect.allow, -1000));

        if (allowByDefault) {
            // Soft access denial to anonymous
            policies.add(
                    new Policy(Constants.ROLE_ANONYMOUS, null, null, null, null, null, null, PolicyEffect.deny, 10000));

            // Soft access to users
            policies.add(new Policy("role:user", null, null, null, null, null, null, PolicyEffect.allow, 10000));
        } else {
            // Strong denial for anonymous
            policies.add(new Policy(Constants.ROLE_ANONYMOUS, null, null, null, null, null, null, PolicyEffect.deny,
                    -100000));

            // Soft denial for users. Allow to describe resources
            policies.add(new Policy("role:user", null, null, null, null, null, null, PolicyEffect.deny, 10001));
            policies.add(new Policy("role:user", null, null, null, null, null, "describe", PolicyEffect.allow, 10000));
        }

        return policies.stream().map(Policy::toList).toList();
    }
}

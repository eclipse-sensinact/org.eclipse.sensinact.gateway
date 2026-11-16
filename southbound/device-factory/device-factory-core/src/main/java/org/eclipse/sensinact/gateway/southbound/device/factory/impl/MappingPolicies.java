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
package org.eclipse.sensinact.gateway.southbound.device.factory.impl;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.southbound.device.factory.dto.MappingPolicy;

/**
 * Policies of a mapping level (built-in, global or entry): at most one policy
 * per pair, an unset pair taking the policy of the level below
 */
public final class MappingPolicies {

    /**
     * Pairs of opposite policies
     */
    enum Pair {
        /**
         * What to do with a missing value
         */
        PRESENCE,

        /**
         * What to do with a value that can't be converted
         */
        CONVERSION,

        /**
         * What a tolerated value becomes
         */
        FALLBACK;

        /**
         * Returns the pair of a policy
         */
        static Pair of(final MappingPolicy policy) {
            return switch (policy) {
            case REQUIRED, OPTIONAL -> PRESENCE;
            case STRICT, LENIENT -> CONVERSION;
            case IGNORE, DEFAULT -> FALLBACK;
            };
        }
    }

    /**
     * Built-in policies, below the global ones
     */
    public static final MappingPolicies DEFAULTS = of(
            List.of(MappingPolicy.REQUIRED, MappingPolicy.STRICT, MappingPolicy.IGNORE));

    /**
     * No policy set
     */
    public static final MappingPolicies NONE = new MappingPolicies(Map.of());

    private final Map<Pair, MappingPolicy> policies;

    private MappingPolicies(final Map<Pair, MappingPolicy> policies) {
        this.policies = policies;
    }

    /**
     * Builds the policies of a level
     *
     * @param policies Policies of the level, can be null
     * @return The policies
     * @throws IllegalArgumentException Both members of a pair
     */
    public static MappingPolicies of(final Collection<MappingPolicy> policies) {
        if (policies == null || policies.isEmpty()) {
            return NONE;
        }

        final Map<Pair, MappingPolicy> map = new EnumMap<>(Pair.class);
        for (final MappingPolicy policy : policies) {
            if (policy == null) {
                throw new IllegalArgumentException("Null mapping policy in " + policies);
            }

            final MappingPolicy previous = map.putIfAbsent(Pair.of(policy), policy);
            if (previous != null && previous != policy) {
                throw new IllegalArgumentException(
                        String.format("Opposite mapping policies %s and %s in %s", previous, policy, policies));
            }
        }
        return new MappingPolicies(Map.copyOf(map));
    }

    /**
     * Parses the policies of a mapping entry
     *
     * @param raw Raw value of the entry policies: an array of policy names
     * @return The policies
     * @throws IllegalArgumentException Not an array, unknown policy or both
     *                                  members of a pair
     */
    public static MappingPolicies parse(final Object raw) {
        if (raw == null) {
            return NONE;
        } else if (!(raw instanceof Collection)) {
            throw new IllegalArgumentException("Mapping policies must be an array: " + raw);
        }

        return of(((Collection<?>) raw).stream().map(p -> MappingPolicy.fromString(String.valueOf(p))).toList());
    }

    /**
     * Returns these policies over a lower level: a pair set here wins, the
     * others come from the lower level
     *
     * @param lower Lower level policies
     * @return The merged policies
     */
    public MappingPolicies over(final MappingPolicies lower) {
        if (policies.isEmpty()) {
            return lower;
        } else if (lower.policies.isEmpty()) {
            return this;
        }

        final Map<Pair, MappingPolicy> map = new EnumMap<>(lower.policies);
        map.putAll(policies);
        return new MappingPolicies(Map.copyOf(map));
    }

    /**
     * A missing value is tolerated
     */
    public boolean isOptional() {
        return policies.get(Pair.PRESENCE) == MappingPolicy.OPTIONAL;
    }

    /**
     * A value that can't be converted is tolerated
     */
    public boolean isLenient() {
        return policies.get(Pair.CONVERSION) == MappingPolicy.LENIENT;
    }

    /**
     * A tolerated value is replaced by the default value
     */
    public boolean usesDefault() {
        return policies.get(Pair.FALLBACK) == MappingPolicy.DEFAULT;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof MappingPolicies other && policies.equals(other.policies);
    }

    @Override
    public int hashCode() {
        return policies.hashCode();
    }

    @Override
    public String toString() {
        return policies.values().toString();
    }
}

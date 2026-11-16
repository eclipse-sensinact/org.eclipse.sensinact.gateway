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
package org.eclipse.sensinact.gateway.southbound.device.factory.dto;

import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Policy applied to a mapping entry when its value is missing or invalid.
 * Policies go by pairs of opposites: a policy array can't hold both members of
 * a pair.
 */
public enum MappingPolicy {

    /**
     * A missing value rejects the record (default)
     */
    REQUIRED,

    /**
     * A missing value is tolerated
     */
    OPTIONAL,

    /**
     * A value that can't be converted rejects the record (default)
     */
    STRICT,

    /**
     * A value that can't be converted is tolerated
     */
    LENIENT,

    /**
     * A tolerated value is ignored: the resource keeps its previous value
     * (default)
     */
    IGNORE,

    /**
     * A tolerated value is replaced by the value of the <code>default</code> key
     * of the entry, null if the entry has no such key. Not to be confused with
     * the <code>default</code> key itself, which implies this policy.
     */
    DEFAULT;

    /**
     * Returns the policy with the given name, ignoring case
     *
     * @param name Policy name
     * @return The policy
     * @throws IllegalArgumentException Unknown policy
     */
    @JsonCreator
    public static MappingPolicy fromString(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Null mapping policy");
        }

        try {
            return valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown mapping policy: " + name, e);
        }
    }

    @JsonValue
    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}

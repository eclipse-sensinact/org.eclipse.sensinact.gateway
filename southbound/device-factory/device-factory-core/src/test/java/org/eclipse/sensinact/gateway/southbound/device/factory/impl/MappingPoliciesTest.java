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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.sensinact.gateway.southbound.device.factory.dto.MappingPolicy;
import org.junit.jupiter.api.Test;

/**
 * Tests the parsing and merging of the mapping policies
 */
public class MappingPoliciesTest {

    @Test
    void testDefaults() {
        assertFalse(MappingPolicies.DEFAULTS.isOptional());
        assertFalse(MappingPolicies.DEFAULTS.isLenient());
        assertFalse(MappingPolicies.DEFAULTS.usesDefault());
    }

    @Test
    void testParse() {
        final MappingPolicies policies = MappingPolicies.parse(List.of("Optional", " lenient ", "DEFAULT"));
        assertTrue(policies.isOptional());
        assertTrue(policies.isLenient());
        assertTrue(policies.usesDefault());

        // A repeated policy is not a conflict
        assertTrue(MappingPolicies.parse(List.of("optional", "optional")).isOptional());
        assertSame(MappingPolicies.NONE, MappingPolicies.parse(null));
        assertSame(MappingPolicies.NONE, MappingPolicies.parse(List.of()));
    }

    @Test
    void testParseErrors() {
        assertThrows(IllegalArgumentException.class, () -> MappingPolicies.parse("optional"));
        assertThrows(IllegalArgumentException.class, () -> MappingPolicies.parse(List.of("lax")));
        assertThrows(IllegalArgumentException.class, () -> MappingPolicies.parse(List.of("optional", "required")));
        assertThrows(IllegalArgumentException.class, () -> MappingPolicies.parse(List.of("strict", "lenient")));
        assertThrows(IllegalArgumentException.class, () -> MappingPolicies.parse(List.of("ignore", "default")));
    }

    @Test
    void testOverByPair() {
        final MappingPolicies global = MappingPolicies.of(List.of(MappingPolicy.OPTIONAL, MappingPolicy.LENIENT))
                .over(MappingPolicies.DEFAULTS);
        final MappingPolicies entry = MappingPolicies.of(List.of(MappingPolicy.REQUIRED)).over(global);

        // Set on the entry
        assertFalse(entry.isOptional());
        // From the global level
        assertTrue(entry.isLenient());
        // From the built-in defaults
        assertFalse(entry.usesDefault());

        assertEquals(global, MappingPolicies.NONE.over(global));
        assertEquals(global, global.over(MappingPolicies.NONE));
    }
}

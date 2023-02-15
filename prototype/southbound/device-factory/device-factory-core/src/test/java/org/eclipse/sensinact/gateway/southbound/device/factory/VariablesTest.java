/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.gateway.southbound.device.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 *
 */
public class VariablesTest {

    @Test
    void testPatterns() {
        for (String validKey : Arrays.asList("$a", "$1", "$_")) {
            assertTrue(VariableSolver.isValidKey(validKey), validKey);
        }

        for (String invalidKey : Arrays.asList("${a}", "$[", "$]", "${", "$}", "${1}", "${_}", "abc", "123", "[abc]",
                "$[abc]")) {
            assertFalse(VariableSolver.isValidKey(invalidKey), invalidKey);
        }

        for (String invalidUse : Arrays.asList("$a", "$1", "$_", "abc", "123", "[abc]", "$[abc]", "$[", "$]", "${",
                "$}")) {
            assertFalse(VariableSolver.containsVariables(invalidUse), invalidUse);
            assertFalse(VariableSolver.containsVariables("prefix" + invalidUse + "suffix"), invalidUse);
        }

        for (String validUse : Arrays.asList("${a}", "${1}", "${_}")) {
            assertTrue(VariableSolver.containsVariables(validUse), validUse);
        }
    }

    @Test
    void testSubstitution() throws VariableNotFoundException {
        final Map<String, String> subs = Map.of("$a", "42", "$b", "toto");
        assertEquals("42", VariableSolver.fillInVariables("${a}", subs));
        assertEquals("toto", VariableSolver.fillInVariables("${b}", subs));
        assertEquals("answer: 42", VariableSolver.fillInVariables("answer: ${a}", subs));
        assertThrows(VariableNotFoundException.class, () -> VariableSolver.fillInVariables("${invalid}", subs));
        assertEquals("answer: $_", VariableSolver.fillInVariables("answer: $_", subs));
        assertEquals("answer: $a", VariableSolver.fillInVariables("answer: $a", subs));
    }

    @Test
    void testSubstring() throws VariableNotFoundException {
        assertTrue(VariableSolver.containsVariables("${b[0]}"), "Substring not detected");
        assertTrue(VariableSolver.containsVariables("${b[0:]}"), "Substring not detected");
        assertTrue(VariableSolver.containsVariables("${b[0:2]}"), "Substring not detected");
        assertTrue(VariableSolver.containsVariables("${b[0:-2]}"), "Substring not detected");

        final Map<String, String> subs = Map.of("$a", "0123", "$b", "Hello World!");
        assertEquals("0", VariableSolver.fillInVariables("${a[0]}", subs));
        assertEquals("2", VariableSolver.fillInVariables("${a[2]}", subs));
        assertEquals("123", VariableSolver.fillInVariables("${a[1:]}", subs));
        assertEquals("123", VariableSolver.fillInVariables("${a[1:4]}", subs));
        assertEquals("Hello World", VariableSolver.fillInVariables("${b[0:-1]}", subs));
        assertEquals("Hello", VariableSolver.fillInVariables("${b[0:5]}", subs));
        assertEquals("Hello World", VariableSolver.fillInVariables("${b[:-1]}", subs));

        assertEquals("World Hello !", VariableSolver.fillInVariables("${b[6:-1]} ${b[:5]} ${b[-1]}", subs));
    }
}

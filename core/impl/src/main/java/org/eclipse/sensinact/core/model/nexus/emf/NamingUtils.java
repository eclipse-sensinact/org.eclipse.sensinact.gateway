/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.core.model.nexus.emf;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Handles model, provider, service and resource names from the user
 */
public class NamingUtils {

    /**
     * List of Java keywords according to its specification
     */
    public static final String[] keywords = { "abstract", "continue", "for", "new", "switch", "assert", "default", "if",
            "package", "synchronized", "boolean", "do", "goto", "private", "this", "break", "double", "implements",
            "protected", "throw", "byte", "else", "import", "public", "throws", "case", "enum", "instanceof", "return",
            "transient", "catch", "extends", "int", "short", "try", "char", "final", "interface", "static", "void",
            "class", "finally", "long", "strictfp", "volatile", "const", "float", "native", "super", "while" };

    static {
        // Final array can be sorted (in-place sort)
        Arrays.sort(keywords);
    }

    /**
     * Checks if the given name is a Java keyword
     *
     * @param name Name to test
     * @return True if the given name is a Java keyword
     */
    public static boolean isJavaKeyword(final String name) {
        return Arrays.binarySearch(keywords, name) >= 0;
    }

    /**
     * Ensures that the given name is only based on ASCII letters, digits and the
     * underscore
     *
     * @param name   Input name
     * @param isPath Flag to allow slashes (<code>/</code>) in the name
     * @return A name that contains only ASCII letters, digits or underscore, or
     *         null if the input is empty or null
     */
    public static String asciiSanitizeName(final String name, final boolean isPath) {
        if (name == null || name.isBlank()) {
            return null;
        }

        if (isPath) {
            // Treat each part separately then join everything
            return Arrays.stream(name.split("/")).map(p -> asciiSanitizeName(p, false))
                    .collect(Collectors.joining("/"));
        } else {
            // Normalize diacritics
            final String normalized = Normalizer.normalize(name.strip(), Normalizer.Form.NFKD).replaceAll("\\p{M}", "");
            final String sanitized;
            if (normalized.isEmpty()) {
                // All characters were invalid, create a name with as many underscores as input
                // characters
                sanitized = name.replaceAll(".", "_");
            } else {
                // Replace all non acceptable characters with an underscore
                sanitized = normalized.replaceAll("[^_A-Za-z0-9]", "_");
            }

            if (sanitized.isEmpty()) {
                return "_";
            } else if (!Character.isJavaIdentifierStart(sanitized.charAt(0)) || isJavaKeyword(name)) {
                // Make sure we don't start with an invalid character
                return "_" + sanitized;
            } else {
                return sanitized;
            }
        }
    }

    public static String firstToUpper(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Ensures the given name is accepted as a Java identifier
     *
     * @param name   Input name
     * @param isPath Flag to allow slashes (<code>/</code>) in the name
     * @return A name that can be used as Java identifier, or null if the input is
     *         empty or null
     */
    public static String sanitizeName(final String name, final boolean isPath) {
        if (name == null || name.isBlank()) {
            return null;
        }

        if (isPath) {
            // Treat each part separately then join everything
            return Arrays.stream(name.split("/")).map(p -> sanitizeName(p, false)).collect(Collectors.joining("/"));
        } else {
            // Replace invalid Java identifier letters
            final String sanitized = name.strip().chars().mapToObj(
                    c -> Character.isJavaIdentifierPart(c) || (isPath && c == '/') ? Character.toString((char) c) : "_")
                    .collect(Collectors.joining());
            if (sanitized.isEmpty()) {
                return "_";
            } else if (!Character.isJavaIdentifierStart(sanitized.charAt(0)) || isJavaKeyword(name)) {
                // Make sure we don't start with an invalid character
                return "_" + sanitized;
            } else {
                return firstToUpper(sanitized);
            }
        }
    }
}

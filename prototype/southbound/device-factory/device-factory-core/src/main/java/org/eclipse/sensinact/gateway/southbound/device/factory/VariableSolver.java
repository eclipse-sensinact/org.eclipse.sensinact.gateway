/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to handle variables in paths
 */
public class VariableSolver {

    private static final Logger logger = LoggerFactory.getLogger(VariableSolver.class);

    /**
     * Variable definition matching pattern
     */
    public static final Pattern varKeyPattern = Pattern.compile("\\$[^\\{\\}\\[\\]]+");

    /**
     * Variable usage matching pattern
     */
    public static final Pattern varUsePattern = Pattern
            .compile("\\$\\{([^\\}\\[\\]]+)(\\[(-?\\d+)?((\\:)?(-?\\d+)?\\]))?\\}");

    /**
     * Checks if the given key is a valid variable name
     *
     * @param key Resource key
     * @return True if the variable name is valid
     */
    public static boolean isValidKey(final String key) {
        return varKeyPattern.matcher(key).matches();
    }

    /**
     * Checks if the string representation of the given object matches a variable
     *
     * @param value Input value
     * @return True if the given value contains a variable
     */
    public static boolean containsVariables(final Object value) {
        if (value == null) {
            return false;
        }

        return varUsePattern.matcher(String.valueOf(value)).find();
    }

    /**
     * Looks for variables in the given string value and replaces them with the
     * resolved values
     *
     * @param strValue  Input value
     * @param variables Resolved variables
     * @return The resolved value
     */
    public static String fillInVariables(final String strValue, final Map<String, String> variables)
            throws VariableNotFoundException {
        final Matcher matcher = varUsePattern.matcher(strValue);
        String newValue = strValue;
        if (matcher.find()) {
            do {
                final String innerVar = matcher.group(1);
                final Object resolvedPath = variables.get("$" + innerVar);
                if (resolvedPath == null) {
                    throw new VariableNotFoundException("Variable not found $" + innerVar + " in value: " + strValue);
                }

                final String injectedValue;
                if (matcher.group(2) != null) {
                    // Got a substring group
                    injectedValue = applySubstring(String.valueOf(resolvedPath), matcher);
                } else {
                    injectedValue = String.valueOf(resolvedPath);
                }

                newValue = newValue.replace(matcher.group(), injectedValue);
            } while (matcher.find());
        }

        return newValue;
    }

    /**
     * Applies a call to substring
     *
     * @param strValue String value to work on
     * @param matcher  Regex matcher for {@link #varUsePattern}
     * @return The applied substring
     */
    private static String applySubstring(final String strValue, final Matcher matcher) {
        final String strStart = matcher.group(3);
        final boolean toEnd = matcher.group(5) != null;
        final String strEnd = matcher.group(6);

        try {
            // Compute start position
            int startPos;
            if (strStart == null) {
                startPos = 0;
            } else {
                startPos = Integer.valueOf(strStart);
                if (startPos < 0) {
                    startPos = strValue.length() + startPos;
                }
            }

            if (!toEnd) {
                // Single character
                return strValue.substring(startPos, startPos + 1);
            } else if (strEnd == null) {
                // For start position to end of string
                return strValue.substring(startPos);
            } else {
                // Between 2 positions
                int endPos = Integer.valueOf(strEnd);
                if (endPos < 0) {
                    endPos = strValue.length() + endPos;
                }

                return strValue.substring(startPos, endPos);
            }
        } catch (NumberFormatException e) {
            logger.error("Coudln't parse substring index: {}", e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            logger.debug("Substring indexes out of bound: start={} end={} len={}", strStart,
                    strEnd != null ? strEnd : "<end>", strValue.length());
        }
        return "";
    }
}

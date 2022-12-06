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

/**
 * Utility class to handle variables in paths
 */
public class VariableSolver {

    /**
     * Variable matching pattern
     */
    public static final Pattern varPattern = Pattern.compile("\\$\\{([^\\}]+)\\}");

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

        return varPattern.matcher(String.valueOf(value)).find();
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
        Matcher matcher = varPattern.matcher(strValue);
        String newValue = strValue;
        if (matcher.find()) {
            do {
                final String innerVar = matcher.group(1);
                final Object resolvedPath = variables.get("$" + innerVar);
                if (resolvedPath == null) {
                    throw new VariableNotFoundException("Variable not found $" + innerVar + " in value: " + strValue);
                }
                newValue = newValue.replace("${" + innerVar + "}", String.valueOf(resolvedPath));
            } while (matcher.find());
        }

        return newValue;
    }
}

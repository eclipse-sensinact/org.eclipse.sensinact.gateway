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
package org.eclipse.sensinact.filters.resource.selector.api;

/**
 * Defines a selection for the name of a token in the URI
 */
public class Selection {

    /**
     * The value to match
     */
    public String value;

    /**
     * The type of matching to use
     */
    public MatchType type;

    /**
     * If true then the result of the test will be negated
     */
    public boolean negate;

    public static enum MatchType {
        /**
         * The value must exactly equal the name of the selected item
         */
        EXACT,
        /**
         * The value will be treated as a regular expression
         * which must match the entire name of the selected item
         */
        REGEX,
        /**
         * The value will be treated as a regular expression
         * which must match some part of the name of the selected item
         */
        REGEX_REGION;
    }

    @Override
    public String toString() {
        return "Selection [value=" + value + ", type=" + type + ", negate=" + negate + "]";
    }
}

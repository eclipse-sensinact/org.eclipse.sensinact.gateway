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
package org.eclipse.sensinact.filters.ldap.antlr.impl;

/**
 * Holds a tested string value
 */
public interface IStringValue {

    /**
     * Returns true if this value is a RegEx
     */
    boolean isRegex();

    /**
     * Equality match (either string equality or RegEx match)
     *
     * @param other        Tested string
     * @param approximated If True, check if the tested string matches without
     *                     accented chars
     */
    boolean matches(final String other, final boolean approximated);

    /**
     * Returns the string value if {@link #isRegex()} returns false, else null.
     */
    String getString();

    /**
     * Returns the RegEx pattern string if {@link #isRegex()} returns true, else
     * null.
     */
    String getPattern();
}

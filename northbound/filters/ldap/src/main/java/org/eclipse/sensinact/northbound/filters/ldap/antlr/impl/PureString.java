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
package org.eclipse.sensinact.northbound.filters.ldap.antlr.impl;

import java.text.Normalizer;
import java.text.Normalizer.Form;

/**
 * Pure string matcher
 */
public class PureString implements IStringValue {

    /**
     * The string value
     */
    private final String value;

    public PureString(final String value) {
        this.value = value;
    }

    @Override
    public boolean isRegex() {
        return false;
    }

    @Override
    public boolean matches(final String other, final boolean approximated) {
        final String testedString;
        if (!approximated) {
            testedString = other;
        } else if (other != null) {
            testedString = Normalizer.normalize(other, Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        } else {
            testedString = null;
        }
        return value.equalsIgnoreCase(testedString);
    }

    @Override
    public String getString() {
        return value;
    }

    @Override
    public String getPattern() {
        return null;
    }

    @Override
    public String toString() {
        return String.format("{STRING:%s}", value);
    }
}

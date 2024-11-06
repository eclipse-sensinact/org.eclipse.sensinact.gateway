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

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.regex.Pattern;

/**
 * RegEx matcher
 */
public class RegexString implements IStringValue {

    /**
     * Pre-compiled pattern
     */
    private Pattern pattern;

    private final String strPattern;

    public RegexString(final Pattern pattern) {
        this.pattern = pattern;
        this.strPattern = pattern.pattern();
    }

    public RegexString(final String pattern) {
        this.strPattern = pattern;
    }

    @Override
    public boolean isRegex() {
        return true;
    }

    @Override
    public boolean matches(final String other, final boolean approximated) {
        if (pattern == null) {
            pattern = Pattern.compile("^" + strPattern + "$", Pattern.CASE_INSENSITIVE);
        }

        final String testedString;
        if (!approximated) {
            testedString = other;
        } else if (other != null) {
            testedString = Normalizer.normalize(other, Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        } else {
            return false;
        }

        return pattern.matcher(testedString).matches();
    }

    @Override
    public String getString() {
        return null;
    }

    @Override
    public String getPattern() {
        return strPattern;
    }

    @Override
    public String toString() {
        return String.format("{REGEX:%s}", strPattern);
    }
}

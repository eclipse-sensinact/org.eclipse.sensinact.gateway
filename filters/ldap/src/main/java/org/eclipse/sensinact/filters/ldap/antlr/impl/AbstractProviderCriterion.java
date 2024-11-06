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

import java.util.function.Predicate;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;

/**
 * Common code for Provider field comparison
 */
public abstract class AbstractProviderCriterion extends AbstractCriterion {

    /**
     * Constant name used in LDAP queries
     */
    private final String ldapConstantName;

    /**
     * Tested value
     */
    private final IStringValue expectedValue;

    /**
     * LDAP comparator to use
     */
    private final LdapComparator comparator;

    /**
     * Sets up the criterion
     *
     * @param ldapConstantName Constant name used in LDAP queries
     * @param expectedValue    Tested value
     * @param comparator       LDAP comparator to use
     */
    public AbstractProviderCriterion(final String ldapConstantName, final IStringValue expectedValue,
            final LdapComparator comparator) {
        super();
        this.ldapConstantName = ldapConstantName;
        this.expectedValue = expectedValue;
        this.comparator = comparator;
    }

    /**
     * Gets the tested value from the provider
     */
    protected abstract String getProviderFieldValue(final ProviderSnapshot provider);

    @Override
    public String toString() {
        String content = String.format("(%s%s%s)", ldapConstantName, comparator, expectedValue);
        if (isNegative()) {
            content = String.format("(!%s)", content);
        }
        return content;
    }

    @Override
    public Predicate<ProviderSnapshot> getProviderFilter() {
        final boolean approxMatch = comparator == LdapComparator.APPROX;
        if (isNegative()) {
            return p -> !expectedValue.matches(getProviderFieldValue(p), approxMatch);
        } else {
            return p -> expectedValue.matches(getProviderFieldValue(p), approxMatch);
        }
    }

    @Override
    public ResourceValueFilter getResourceValueFilter() {
        return (p, rs) -> !rs.isEmpty() && getProviderFilter().test(p);
    }
}

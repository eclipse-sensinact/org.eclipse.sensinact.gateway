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
package org.eclipse.sensinact.northbound.filters.ldap.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.sensinact.prototype.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ResourceValueFilter;

/**
 * Combination filter (AND or OR)
 */
public class LdapFilter extends AbstractCriterion {

    private final List<ILdapCriterion> subCriteria = new ArrayList<>();

    private LdapOperator operator;

    public LdapFilter(final LdapOperator operator, final List<ILdapCriterion> criteria) {
        this.operator = operator;
        this.subCriteria.addAll(criteria);
    }

    @Override
    public void negate() {
        switch (operator) {
        case AND:
            operator = LdapOperator.OR;
            subCriteria.forEach(ILdapCriterion::negate);
            break;

        case OR:
            operator = LdapOperator.AND;
            subCriteria.forEach(ILdapCriterion::negate);
            break;

        default:
            break;
        }
    }

    @Override
    public String toString() {
        String op;
        switch (operator) {
        case AND:
            op = "&";
            break;

        case OR:
            op = "|";
            break;

        default:
            op = "?";
            break;
        }

        return String.format("(%s%s)", op,
                this.subCriteria.stream().map(Objects::toString).reduce("", (a, b) -> a + b));
    }

    private Predicate<ProviderSnapshot> makeProviderFilter(final List<Predicate<ProviderSnapshot>> criteria) {
        if (criteria.isEmpty()) {
            return null;
        }

        switch (operator) {
        case AND:
            return p -> criteria.stream().allMatch(c -> c.test(p));

        case OR:
            return p -> criteria.stream().anyMatch(c -> c.test(p));

        default:
            return null;
        }
    }

    public Predicate<ProviderSnapshot> getProviderFilter() {
        final List<Predicate<ProviderSnapshot>> allCriteria = subCriteria.stream()
                .map(ILdapCriterion::getProviderFilter).collect(Collectors.toList());
        final List<Predicate<ProviderSnapshot>> filteredCriteria = allCriteria.stream().filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (filteredCriteria.isEmpty() || filteredCriteria.size() != allCriteria.size()) {
            // Some branches have a provider criteria, some other don't
            return null;
        }

        return makeProviderFilter(filteredCriteria);
    }

    public ResourceValueFilter getResourceValueFilter() {
        final List<ResourceValueFilter> allCriteria = subCriteria.stream().map(ILdapCriterion::getResourceValueFilter)
                .filter(Objects::nonNull).collect(Collectors.toList());
        if (allCriteria.isEmpty()) {
            return (p, rs) -> true;
        }

        // Prepare the provider filter
        final Predicate<ProviderSnapshot> pFilter = makeProviderFilter(subCriteria.stream()
                .map(ILdapCriterion::getProviderFilter).filter(Objects::nonNull).collect(Collectors.toList()));

        switch (operator) {
        case AND:
            return (p, rs) -> !rs.isEmpty() && (pFilter == null || pFilter.test(p))
                    && allCriteria.stream().allMatch(c -> c.test(p, rs));

        case OR:
            return (p, rs) -> !rs.isEmpty()
                    && ((pFilter != null && pFilter.test(p)) || allCriteria.stream().anyMatch(c -> c.test(p, rs)));

        default:
            return null;
        }
    }
}

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

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.filters.ldap.impl.LdapFilter;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;

/**
 * Common code from LDAP criteria
 */
public abstract class AbstractCriterion implements ILdapCriterion {

    /**
     * Flag that indicates this criterion must negate its result
     */
    private final boolean negative;

    protected AbstractCriterion(boolean isNegative) {
        this.negative = isNegative;
    }

    /**
     * If true, the result of this criterion but be negated
     */
    protected boolean isNegative() {
        return negative;
    }

    @Override
    public Predicate<GeoJsonObject> getLocationFilter() {
        return null;
    }

    @Override
    public Predicate<ProviderSnapshot> getProviderFilter() {
        return null;
    }

    @Override
    public Predicate<ServiceSnapshot> getServiceFilter() {
        return null;
    }

    @Override
    public Predicate<ResourceSnapshot> getResourceFilter() {
        return null;
    }

    @Override
    public ResourceValueFilter getResourceValueFilter() {
        return null;
    }

    @Override
    public ICriterion and(ICriterion criterion) {
        if(criterion instanceof ILdapCriterion) {
            return new LdapFilter(LdapOperator.AND, List.of(this, (ILdapCriterion) criterion));
        }
        return ILdapCriterion.super.and(criterion);
    }

    @Override
    public ICriterion or(ICriterion criterion) {
        if(criterion instanceof ILdapCriterion) {
            return new LdapFilter(LdapOperator.OR, List.of(this, (ILdapCriterion) criterion));
        }
        return ILdapCriterion.super.or(criterion);
    }
}

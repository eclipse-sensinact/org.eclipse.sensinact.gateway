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

import java.util.function.Predicate;

import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.prototype.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.prototype.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.prototype.snapshot.ServiceSnapshot;

/**
 * Common code from LDAP criteria
 */
public abstract class AbstractCriterion implements ILdapCriterion {

    /**
     * Flag that indicates this criterion must negate its result
     */
    private boolean negative = false;

    /**
     * Invert the result of this criterion
     */
    public void negate() {
        negative = !negative;
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
}

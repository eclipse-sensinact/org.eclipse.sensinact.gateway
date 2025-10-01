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
package org.eclipse.sensinact.northbound.query.impl;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;

/**
 * Criterion with extra filters
 */
public class UpdatableCriterion implements ICriterion {

    /**
     * Location filter
     */
    private BiPredicate<ProviderSnapshot, GeoJsonObject> locationFilter;

    /**
     * Location filter
     */
    private Predicate<ProviderSnapshot> providerFilter;

    /**
     * Location filter
     */
    private Predicate<ServiceSnapshot> svcFilter;

    /**
     * Location filter
     */
    private Predicate<ResourceSnapshot> rcFilter;

    /**
     * Location filter
     */
    private ResourceValueFilter resourceValueFilter;

    /**
     * @param locationFilter      Location filter
     * @param providerFilter      Provider filter
     * @param serviceFilter       Service filter
     * @param resourceFilter      Resource filter
     * @param resourceValueFilter Resource value filter
     */
    public UpdatableCriterion(final BiPredicate<ProviderSnapshot, GeoJsonObject> locationFilter,
            final Predicate<ProviderSnapshot> providerFilter, final Predicate<ServiceSnapshot> serviceFilter,
            final Predicate<ResourceSnapshot> resourceFilter, final ResourceValueFilter resourceValueFilter) {
        this.locationFilter = locationFilter;
        this.providerFilter = providerFilter;
        this.svcFilter = serviceFilter;
        this.rcFilter = resourceFilter;
        this.resourceValueFilter = resourceValueFilter;
    }

    @Override
    public BiPredicate<ProviderSnapshot, GeoJsonObject> getLocationFilter() {
        return locationFilter;
    }

    @Override
    public Predicate<ProviderSnapshot> getProviderFilter() {
        return providerFilter;
    }

    @Override
    public Predicate<ServiceSnapshot> getServiceFilter() {
        return svcFilter;
    }

    @Override
    public Predicate<ResourceSnapshot> getResourceFilter() {
        return rcFilter;
    }

    @Override
    public ResourceValueFilter getResourceValueFilter() {
        return resourceValueFilter;
    }

    /**
     * @param source Parent criterion
     */
    public UpdatableCriterion(final ICriterion source) {
        this(source.getLocationFilter(), source.getProviderFilter(), source.getServiceFilter(),
                source.getResourceFilter(), source.getResourceValueFilter());
    }

    /**
     * @param locationFilter the new location filter
     */
    public void setLocationFilter(final BiPredicate<ProviderSnapshot, GeoJsonObject> locationFilter) {
        this.locationFilter = locationFilter;
    }

    /**
     * @param providerFilter the new provider filter
     */
    public void setProviderFilter(final Predicate<ProviderSnapshot> providerFilter) {
        this.providerFilter = providerFilter;
    }

    /**
     * @param svcFilter the new service filter
     */
    public void setSvcFilter(final Predicate<ServiceSnapshot> svcFilter) {
        this.svcFilter = svcFilter;
    }

    /**
     * @param rcFilter the new resource filter
     */
    public void setRcFilter(final Predicate<ResourceSnapshot> rcFilter) {
        this.rcFilter = rcFilter;
    }

    /**
     * @param resourceValueFilter The new resource value filter
     */
    public void setResourceValueFilter(final ResourceValueFilter resourceValueFilter) {
        this.resourceValueFilter = resourceValueFilter;
    }

    /**
     * @param locationFilter the location filter to add (AND operator)
     */
    public void addLocationFilter(final BiPredicate<ProviderSnapshot, GeoJsonObject> locationFilter) {
        if (this.locationFilter == null) {
            this.locationFilter = locationFilter;
        } else if (locationFilter != null) {
            this.locationFilter = this.locationFilter.and(locationFilter);
        }
    }

    /**
     * @param providerFilter the provider filter to add (AND operator)
     */
    public void addProviderFilter(final Predicate<ProviderSnapshot> providerFilter) {
        if (this.providerFilter == null) {
            this.providerFilter = providerFilter;
        } else if (providerFilter != null) {
            this.providerFilter = this.providerFilter.and(providerFilter);
        }
    }

    /**
     * @param svcFilter the service filter to add (AND operator)
     */
    public void addSvcFilter(final Predicate<ServiceSnapshot> svcFilter) {
        if (this.svcFilter == null) {
            this.svcFilter = svcFilter;
        } else if (svcFilter != null) {
            this.svcFilter = this.svcFilter.and(svcFilter);
        }
    }

    /**
     * @param rcFilter the resource filter to add (AND operator)
     */
    public void addRcFilter(final Predicate<ResourceSnapshot> rcFilter) {
        if (this.rcFilter == null) {
            this.rcFilter = rcFilter;
        } else if (rcFilter != null) {
            this.rcFilter = this.rcFilter.and(rcFilter);
        }
    }
}

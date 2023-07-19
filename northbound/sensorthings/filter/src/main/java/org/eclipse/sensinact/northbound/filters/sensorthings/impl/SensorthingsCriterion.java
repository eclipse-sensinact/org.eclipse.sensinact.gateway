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
package org.eclipse.sensinact.northbound.filters.sensorthings.impl;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.northbound.filters.sensorthings.EFilterContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.ResourceValueFilterInputHolder;

public class SensorthingsCriterion implements ICriterion {

    private final EFilterContext context;
    private final Predicate<ResourceValueFilterInputHolder> predicate;

    public SensorthingsCriterion(final EFilterContext context,
            final Predicate<ResourceValueFilterInputHolder> predicate) {
        this.context = context;
        this.predicate = predicate;
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
        switch (context) {
        case FEATURES_OF_INTEREST:
        case HISTORICAL_LOCATIONS:
        case LOCATIONS:
        case THINGS:
            return new ResourceValueFilter() {
                @Override
                public boolean test(final ProviderSnapshot provider, final List<ResourceSnapshot> resources) {
                    return predicate.test(new ResourceValueFilterInputHolder(context, provider, resources));
                }
            };

        case DATASTREAMS:
        case OBSERVATIONS:
        case OBSERVED_PROPERTIES:
        case SENSORS:
            return new ResourceValueFilter() {
                @Override
                public boolean test(final ProviderSnapshot provider, final List<ResourceSnapshot> resources) {
                    return resources.stream().map(r -> new ResourceValueFilterInputHolder(context, provider, r))
                            .anyMatch(predicate);
                }
            };

        default:
            throw new IllegalArgumentException("Filters are not supported in context of " + context);
        }
    }
}

/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.filters.resource.selector.impl;

import static org.eclipse.sensinact.filters.resource.selector.impl.ResourceSelectorCriterion.ALWAYS;
import static org.eclipse.sensinact.filters.resource.selector.impl.ResourceSelectorCriterion.always;
import static org.eclipse.sensinact.filters.resource.selector.impl.ResourceSelectorCriterion.fromSelection;
import static org.eclipse.sensinact.filters.resource.selector.impl.ResourceSelectorCriterion.never;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector.ProviderSelection;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;

public class ProviderSelectionCriterion {

    private final ProviderSelection ps;

    private final Predicate<ProviderSnapshot> providerFilter;
    private final List<ResourceSelectionCriterion> resources;
    private final List<LocationSelectionCriterion> locations;

    private final Predicate<ServiceSnapshot> serviceFilter;
    private final Predicate<ResourceSnapshot> resourceFilter;

    private final ResourceValueFilter valueFilter;
    private final BiPredicate<ProviderSnapshot, GeoJsonObject> locationFilter;

    public ProviderSelectionCriterion(ProviderSelection ps) {
        this.ps = ps;
        this.providerFilter = toProviderFilter(ps);
        this.resources = ps.resources().stream()
                .map(ResourceSelectionCriterion::new)
                .toList();
        this.locations = ps.location().stream()
                .map(LocationSelectionCriterion::new)
                .toList();

        if(resources.isEmpty()) {
            this.serviceFilter = never();
            this.resourceFilter = never();
            this.valueFilter = (x,y) -> Boolean.TRUE;
        } else {
            this.serviceFilter = combineServiceCheck(providerFilter, resources.stream()
                            .map(ResourceSelectionCriterion::serviceFilter)
                            .reduce(ResourceSelectorCriterion::combineFilters)
                            .orElse(always()));

            this.resourceFilter = combineResourceCheck(providerFilter, resources.stream()
                    .map(ResourceSelectionCriterion::resourceFilter)
                    .reduce(ResourceSelectorCriterion::combineFilters)
                    .orElse(always()));

            this.valueFilter = this::checkResourceValues;
        }
        locationFilter = combineLocationCheck(this.providerFilter, locations.stream()
                    .map(LocationSelectionCriterion::locationFilter)
                    .reduce(Predicate::and)
                    .orElse(always()));
    }

    public Predicate<ProviderSnapshot> providerFilter() {
        return providerFilter;
    }

    public Predicate<ServiceSnapshot> serviceFilter() {
        return serviceFilter;
    }

    public Predicate<ResourceSnapshot> resourceFilter() {
        return resourceFilter;
    }

    public ResourceValueFilter resourceValueFilter() {
        return valueFilter;
    }

    public BiPredicate<ProviderSnapshot, GeoJsonObject> locationFilter() {
        return locationFilter;
    }

    public String exactModel() {
        return ResourceSelectionCriterion.exactSelection(ps.model());
    }

    public String exactProvider() {
        return ResourceSelectionCriterion.exactSelection(ps.provider());
    }

    public List<ResourceSelectionCriterion> getResources() {
        return resources;
    }

    private static Predicate<ProviderSnapshot> toProviderFilter(ProviderSelection ps) {

        if(ps.modelUri() == null) {
            if(ps.model() == null) {
                return fromSelection(ProviderSnapshot::getName, ps.provider());
            } else {
                Predicate<ProviderSnapshot> modelCheck = fromSelection(ProviderSnapshot::getModelName, ps.model());
                if(ps.provider() == null) {
                    return modelCheck;
                } else {
                    return modelCheck.and(fromSelection(ProviderSnapshot::getName, ps.provider()));
                }
            }
        } else {
            Predicate<ProviderSnapshot> modelUriCheck = fromSelection(ProviderSnapshot::getModelPackageUri, ps.modelUri());
            if(ps.model() == null) {
                return ps.provider() == null ? modelUriCheck :
                    modelUriCheck.and(fromSelection(ProviderSnapshot::getName, ps.provider()));
            } else {
                Predicate<ProviderSnapshot> modelCheck = fromSelection(ProviderSnapshot::getModelName, ps.model());
                if(ps.provider() == null) {
                    return modelUriCheck.and(modelCheck);
                } else {
                    return modelUriCheck.and(modelCheck).and(fromSelection(ProviderSnapshot::getName, ps.provider()));
                }
            }
        }
    }

    private static Predicate<ServiceSnapshot> combineServiceCheck(Predicate<ProviderSnapshot> p, Predicate<ServiceSnapshot> s) {
        if(p == ALWAYS) {
            return s;
        } else {
            Predicate<ServiceSnapshot> pCheck = ss -> p.test(ss.getProvider());
            return s == ALWAYS ? pCheck : pCheck.and(s);
        }
    }

    private static Predicate<ResourceSnapshot> combineResourceCheck(Predicate<ProviderSnapshot> p, Predicate<ResourceSnapshot> r) {
        if(p == ALWAYS) {
            return r;
        } else {
            Predicate<ResourceSnapshot> pCheck = rs -> p.test(rs.getService().getProvider());
            return r == ALWAYS ? pCheck : pCheck.and(r);
        }
    }

    private static BiPredicate<ProviderSnapshot, GeoJsonObject> combineLocationCheck(Predicate<ProviderSnapshot> p, Predicate<GeoJsonObject> l) {
        if(p == ALWAYS) {
            return (x,g) -> l.test(g);
        } else {
            return l == ALWAYS ? (ps, x) -> p.test(ps) : (ps, g) -> p.test(ps) && l.test(g);
        }
    }

    private boolean checkResourceValues(ProviderSnapshot p, List<ResourceSnapshot> resources) {
        // It must be for this provider
        if(providerFilter != ALWAYS && !providerFilter.test(p)) {
            return false;
        }

        // At least one resource must match every resource requirement
        for(ResourceSelectionCriterion rsc : this.resources) {
            if(!resources.stream().anyMatch(rsc.resourceValueFilter())) {
                return false;
            }
        }
        return true;
    }
}

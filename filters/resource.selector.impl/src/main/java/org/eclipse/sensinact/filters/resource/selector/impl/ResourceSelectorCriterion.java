/*********************************************************************
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.Selection;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceSelectorCriterion implements ICriterion {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceSelectorCriterion.class);

    static final Predicate<?> ALWAYS = x -> Boolean.TRUE;
    static final Predicate<?> NEVER = x -> Boolean.FALSE;

    private final ResourceSelector rs;

    private final boolean allowSingleLevelWildcards;

    private final List<ProviderSelectionCriterion> providerSelections;

    private final List<ResourceSelectionCriterion> additionalResources;

    private final Predicate<ProviderSnapshot> providerFilter;
    private final Predicate<ServiceSnapshot> serviceFilter;
    private final Predicate<ResourceSnapshot> resourceFilter;

    private final ResourceValueFilter valueFilter;

    public ResourceSelectorCriterion(ResourceSelector rs, boolean allowSingleLevelWildcards) {
        this.rs = rs;
        this.allowSingleLevelWildcards = allowSingleLevelWildcards;

        this.providerSelections = rs.providers().stream().map(ProviderSelectionCriterion::new).toList();
        this.additionalResources = rs.resources().stream().map(ResourceSelectionCriterion::new).toList();

        this.providerFilter = providerSelections.stream()
                .map(ProviderSelectionCriterion::providerFilter)
                .reduce(ResourceSelectorCriterion::combineFilters)
                .orElse(null);

        Stream<Predicate<ServiceSnapshot>> services = Stream.concat(
                providerSelections.stream().map(ProviderSelectionCriterion::serviceFilter),
                additionalResources.stream().map(ResourceSelectionCriterion::serviceFilter));

        this.serviceFilter = services
                .reduce(ResourceSelectorCriterion::combineFilters)
                .orElse(null);

        Stream<Predicate<ResourceSnapshot>> resources = Stream.concat(
                providerSelections.stream().map(ProviderSelectionCriterion::resourceFilter),
                additionalResources.stream().map(ResourceSelectionCriterion::resourceFilter));

        this.resourceFilter = resources
                .reduce(ResourceSelectorCriterion::combineFilters)
                .orElse(null);

        this.valueFilter = (p, rl) -> providerSelections.stream()
                .anyMatch(ps -> ps.resourceValueFilter().test(p, rl));
    }

    static <T> Predicate<T> fromSelection(Function<T,String> nameExtractor, Selection s) {
        if(s == null) return always();
        Predicate<String> test = s.asPredicate();
        return t -> test.test(nameExtractor.apply(t));
    }

    static <T> Predicate<T> combineFilters(Predicate<T> a, Predicate<T> b) {
        return a == ALWAYS ? a : b == ALWAYS ? b : a.or(b);
    }

    @SuppressWarnings("unchecked")
    static <T> Predicate<T> always() {
        return (Predicate<T>) ALWAYS;
    }

    @SuppressWarnings("unchecked")
    static <T> Predicate<T> never() {
        return (Predicate<T>) NEVER;
    }

    @Override
    public BiPredicate<ProviderSnapshot, GeoJsonObject> getLocationFilter() {
        if(rs.providers().stream().flatMap(p -> p.location().stream()).findAny().isPresent()) {
            LOG.warn("Location filtering is not yet implemented for Resource Selectors.");
        }
        return null;
    }

    @Override
    public Predicate<ProviderSnapshot> getProviderFilter() {
        return providerFilter == ALWAYS ? null : providerFilter;
    }

    @Override
    public Predicate<ServiceSnapshot> getServiceFilter() {
        return serviceFilter == ALWAYS ? null : serviceFilter;
    }

    @Override
    public Predicate<ResourceSnapshot> getResourceFilter() {
        return resourceFilter == ALWAYS ? null : resourceFilter;
    }

    @Override
    public ResourceValueFilter getResourceValueFilter() {
        return valueFilter;
    }

    @Override
    public ICriterion negate() {

        // We used to override this, but I'm not convinced that it is worth
        // optimising further. It can be reinstated if that turns out to be
        // incorrect

        return ICriterion.super.negate();
    }

    @Override
    public List<String> dataTopics() {
        Set<String> found = new HashSet<String>();
        for(ProviderSelectionCriterion ps : providerSelections) {
            Set<String> locallyFound = new HashSet<String>();
            String exactModel = ps.exactModel();
            String exactProvider = ps.exactProvider();
            if(exactModel == null) {
                if(allowSingleLevelWildcards) {
                    exactModel = "+";
                } else {
                    // Once we have a root glob there's no need to calculate further
                    found.add("*");
                    break;
                }
            }
            if(exactProvider == null) {
                if(allowSingleLevelWildcards) {
                    exactProvider = "+";
                } else {
                    // Once we have a trailing glob there's no need to continue with resources
                    found.add(String.format("%s/*", exactModel));
                    continue;
                }
            }

            for(ResourceSelectionCriterion rsc : Stream.concat(ps.getResources().stream(), additionalResources.stream()).toList()) {
                String exactService = rsc.exactService();
                String exactResource = rsc.exactResource();
                if(exactService == null) {
                    if(allowSingleLevelWildcards) {
                        exactService = "+";
                    } else {
                        // Once we have a trailing glob there's no need to calculate further for this provider
                        locallyFound.clear();
                        locallyFound.add(String.format("%s/%s/*", exactModel, exactProvider));
                        break;
                    }
                }
                if(exactResource == null) {
                    if(allowSingleLevelWildcards) {
                        exactResource = "+";
                    } else {
                        exactResource = "*";
                    }
                }
                locallyFound.add(String.format("%s/%s/%s/%s", exactModel, exactProvider, exactService, exactResource));
            }
            found.addAll(locallyFound);
            locallyFound.clear();
        }

        if(found.contains("*")) {
            return List.of("DATA/*");
        } else if(found.contains("+/+/+/+")) {
            return List.of("DATA/+/+/+/+");
        } else {
            return found.stream().map("DATA/"::concat).toList();
        }
    }

    @Override
    public Predicate<ResourceDataNotification> dataEventFilter() {
        // TODO Auto-generated method stub
        return ICriterion.super.dataEventFilter();
    }
}

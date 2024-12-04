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

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrResourceSelectorCriterion implements ICriterion {

    private static final Logger LOG = LoggerFactory.getLogger(OrResourceSelectorCriterion.class);

    private final List<ResourceSelectorCriterion> criteria;

    public OrResourceSelectorCriterion(Stream<ResourceSelector> selectors, boolean allowSingleLevelWildcards) {
        this(selectors.map(rs -> new ResourceSelectorCriterion(rs, allowSingleLevelWildcards)));
    }

    OrResourceSelectorCriterion(OrResourceSelectorCriterion orsc, ResourceSelectorCriterion rsc) {
        this(Stream.concat(Stream.of(rsc), orsc.criteria.stream()));
    }

    private OrResourceSelectorCriterion(OrResourceSelectorCriterion orsc, OrResourceSelectorCriterion orsc2) {
        this(Stream.concat(orsc.criteria.stream(), orsc2.criteria.stream()));
    }

    OrResourceSelectorCriterion(Stream<ResourceSelectorCriterion> criteria) {
        this.criteria = criteria.collect(toList());
    }

    @Override
    public Predicate<GeoJsonObject> getLocationFilter() {
        return g -> criteria.stream().map(ICriterion::getLocationFilter)
                .filter(Objects::nonNull)
                .anyMatch(f -> f.test(g));
    }

    @Override
    public Predicate<ProviderSnapshot> getProviderFilter() {
        return p -> criteria.stream().map(ICriterion::getProviderFilter)
                .anyMatch(f -> f == null || f.test(p));
    }

    @Override
    public Predicate<ServiceSnapshot> getServiceFilter() {
        return s -> criteria.stream().map(ICriterion::getServiceFilter)
                .anyMatch(f -> f == null || f.test(s));
    }

    @Override
    public Predicate<ResourceSnapshot> getResourceFilter() {
        return r -> criteria.stream().map(ICriterion::getResourceFilter)
                .anyMatch(f -> f == null || f.test(r));
    }

    @Override
    public ResourceValueFilter getResourceValueFilter() {
        return (p,rs) -> criteria.stream().map(ICriterion::getResourceValueFilter)
                .anyMatch(f -> f == null || f.test(p,rs));
    }

    @Override
    public ICriterion or(ICriterion criterion) {
        if(criterion instanceof ResourceSelectorCriterion) {
            return new OrResourceSelectorCriterion(this, (ResourceSelectorCriterion) criterion);
        } else if (criterion instanceof OrResourceSelectorCriterion) {
            return new OrResourceSelectorCriterion(this, (OrResourceSelectorCriterion) criterion);
        }
        return ICriterion.super.or(criterion);
    }

    @Override
    public List<String> dataTopics() {
        // TODO deduplicate further using wildcard matching

        List<String> topics = criteria.stream().map(ICriterion::dataTopics)
                .flatMap(Collection::stream)
                .distinct()
                .collect(toList());
        if(LOG.isDebugEnabled()) {
            LOG.debug("Calculated topic list is {}", topics);
        }

        return topics;
    }
}

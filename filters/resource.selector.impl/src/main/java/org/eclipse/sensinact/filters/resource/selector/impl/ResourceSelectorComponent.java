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

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.filters.propertytypes.FiltersSupported;
import org.eclipse.sensinact.filters.resource.selector.api.LocationSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelectorFilterFactory;
import org.eclipse.sensinact.filters.resource.selector.api.Selection;
import org.eclipse.sensinact.filters.resource.selector.api.ValueSelection;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * Provides the ResourceSelector filter creation service
 */
@Component(configurationPid = "sensinact.resource.selector")
@FiltersSupported("resource.selector")
public class ResourceSelectorComponent implements ResourceSelectorFilterFactory {

    @interface Config {
        // This is temporary until we can use a released Typed Event
        // implementation with wildcard support
        boolean single_level_wildcard_enabled() default false;
    }

    @Activate
    Config config;

    @Override
    public ICriterion parseResourceSelector(ResourceSelector selector) {
        return new ResourceSelectorCriterion(copy(selector), config.single_level_wildcard_enabled());
    }

    private ResourceSelector copy(ResourceSelector rs) {
        ResourceSelector copy = new ResourceSelector();
        copy.model = negateSelection(rs.model);
        copy.provider = negateSelection(rs.provider);
        copy.service = negateSelection(rs.service);
        copy.resource = negateSelection(rs.resource);
        copy.value =  rs.value == null ? null :
            rs.value.stream().map(this::negateValueSelection)
                .collect(Collectors.toList());
        copy.location =  rs.location == null ? null :
            rs.location.stream().map(this::negateLocationSelection)
            .collect(Collectors.toList());
        return copy;
    }

    private Selection negateSelection(Selection s) {
        if(s == null) return null;
        Selection neg = new Selection();
        neg.type = s.type;
        neg.value = s.value;
        neg.negate = s.negate;
        return neg;
    }

    private ValueSelection negateValueSelection(ValueSelection s) {
        ValueSelection neg = new ValueSelection();
        neg.operation = s.operation;
        neg.value = s.value;
        neg.checkType = s.checkType;
        neg.negate = s.negate;
        return neg;
    }

    private LocationSelection negateLocationSelection(LocationSelection s) {
        LocationSelection neg = new LocationSelection();
        neg.type = s.type;
        neg.value = s.value;
        neg.radius = s.radius;
        neg.negate = s.negate;
        return neg;
    }

    @Override
    public ICriterion parseResourceSelector(Stream<ResourceSelector> selectors) {
        Optional<ICriterion> or = selectors.map(this::parseResourceSelector).reduce(ICriterion::or);
        return or.orElseThrow(() -> new IllegalArgumentException("No selectors defined"));
    }

}

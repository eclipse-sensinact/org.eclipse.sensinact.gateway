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
package org.eclipse.sensinact.filters.resource.selector.api;

import java.util.List;

import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector.ProviderSelection;
import org.eclipse.sensinact.filters.resource.selector.api.ResourceSelector.ResourceSelection;
import org.eclipse.sensinact.filters.resource.selector.api.Selection.MatchType;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Feature;

/**
 * Describes the selection of resources based on their model, provider, service
 * or name. Members can be null (any). Selections set in a single
 * {@link CompactResourceSelector} are combined with an AND semantic.
 * 
 * <p>
 * A {@link CompactResourceSelector} is not used directly for creating filters,
 * and should be converted into a {@link ResourceSelector} using the
 * {@link #toResourceSelector()} method.
 * 
 * <p>
 * Note that the primary goal of a Compact Resource Selector is to select a *single* resource
 * across one or more providers. They are most efficient when used with
 * {@link MatchType#EXACT} {@link Selection}s, particularly when subscribing to data
 * notifications. Common use cases include:
 *
 * <ul>
 *   <li>Setting exact matches for the model, service and resource, selecting resource
 *   value data across certain types of provider</li>
 *   <li>Gathering all resources for a specific provider</li>
 *   <li>Gathering all providers where the value of a particular resource has a certain value</li>
 * </ul>
 */
public record CompactResourceSelector(Selection modelUri, Selection model, Selection provider, Selection service, Selection resource,
        @JsonFormat(with = Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) List<ValueSelection> value,
        @JsonFormat(with = Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) List<LocationSelection> location) {

    public CompactResourceSelector {
        if(value == null) {
            value = List.of();
        } else {
            value = List.copyOf(value);
        }
        if(location == null) {
            location = List.of();
        } else {
            location = List.copyOf(location);
        }
    }
    
    public ResourceSelector toResourceSelector() {
        ResourceSelection rs = new ResourceSelection(service, resource, value);
        ProviderSelection ps = new ProviderSelection(modelUri, model, provider, value.isEmpty() ? List.of() : List.of(rs), location);
        return new ResourceSelector(List.of(ps), value().isEmpty() ? List.of(rs) : List.of());
    }
}

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
package org.eclipse.sensinact.filters.resource.selector.api;

import java.util.List;

import org.eclipse.sensinact.core.snapshot.ResourceValueFilter;
import org.eclipse.sensinact.filters.resource.selector.api.Selection.MatchType;
import org.eclipse.sensinact.filters.resource.selector.jackson.ResourceSelectorDeserializer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Feature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Describes the selection of resources based on their attributes:
 * 
 * <ul>
 *   <li>The providers section determines which providers are selected. Any resources
 *   used in this section will be included in the output. The list of providers has an
 *   <emphasis>OR</emphasis> semantic. Providers are selected if they match any of the
 *   supplied {@link ProviderSelection} filters.</li>
 *   <li>The resources section selects additional resource values to include in the 
 *   selection. It is not permitted to use a {@link ResourceValueFilter} in the resources.
 *   If a {@link ResourceSelection} in the resources section is not found in a selected
 *   provider then it will be silently ignored for that provider.</li>
 * </ul>
 * 
 * Note that the primary goal of a Resource Selector is to gather a filtered snapshot
 * across one or more providers. They are most efficient when used with a single
 * {@link ProviderSelection} with {@link MatchType#EXACT} {@link Selection}s, particularly
 * when subscribing to data notifications. Common use cases include:
 *
 * <ul>
 *   <li>Setting exact matches for the model, service and resource, selecting resource
 *   value data across certain types of provider</li>
 *   <li>Gathering all resources for a specific provider</li>
 *   <li>Gathering all providers where the value of a particular resource has a certain value</li>
 * </ul>
 * 
 * To gather all resources for a provider add a {@link ResourceSelection} with a <code>null</code>
 * service and resource {@link Selection}.
 */
@JsonDeserialize(using = ResourceSelectorDeserializer.class)
public record ResourceSelector(
        /**
         * The providers that should be selected by this {@link ResourceSelector}
         */
        List<ProviderSelection> providers,
        
        /**
         * The additional resources that should be selected by this {@link ResourceSelector}
         */
        List<ResourceSelection> resources) {
    
    public ResourceSelector {
        if(providers == null) {
            providers = List.of();
        } else {
            providers = List.copyOf(providers);
        }
        if(resources == null) {
            resources = List.of();
        } else {
            resources = List.copyOf(resources);
        }
        if(resources.stream().anyMatch(r -> !r.value.isEmpty())) {
            throw new IllegalArgumentException("Filters in the \"resources\" section must not declare any value filters");
        }
    }

    /**
     * A {@link ResourceSelection} represents a filter on a resource and value, and is either used in:
     * 
     * <ul>
     *   <li>A {@link ProviderSelection} to select one or more providers for inclusion in the result</li>
     *   <li>A {@link ResourceSelector} to select one or more additional resources. In this case the
     *   list of filters must be empty.</li>
     * </ul>
     * 
     * Selections set in a single {@link ResourceSelection} are combined with an AND semantic.
     * If you want to set up an OR semantic then you can do this by creating a
     * {@link ResourceSelector} with multiple {@link ProviderSelection} entries.
     * 
     * If either {@link Selection} entry is null then that is treated as a global match.
     */
    public record ResourceSelection(
            /**
             * A selection for the service. If <code>null</code> then any service will match
             */
            Selection service,
            /**
             * A selection for the resource. If <code>null</code> then any resource will match
             */
            Selection resource,
            /**
             * A selection based on the value of the resource. Multiple matches are combined using
             * an <emphasis>AND</emphasis> semantic.
             */
            @JsonFormat(with = Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) List<ValueSelection> value) {
        
        public ResourceSelection {
            if(value == null) {
                value = List.of();
            } else {
                value = List.copyOf(value);
            }
        }
    }
    
    /**
     * A {@link ProviderSelection} represents a filter selecting providers for inclusion in the result.
     * <p>
     * Selections set in a single {@link ProviderSelection} are combined with an AND semantic.
     * If you want to set up an OR semantic then you can do this by creating a
     * {@link ResourceSelector} with multiple {@link ProviderSelection} entries.
     */
    public record ProviderSelection(
            /**
             * A selection for the model URI. If <code>null</code> then any model URI will match
             */
            Selection modelUri,
            /**
             * A selection for the model. If <code>null</code> then any model will match
             */
            Selection model,
            /**
             * A selection for the provider. If <code>null</code> then any provider will match
             */
            Selection provider, 
            /**
             * A selection for the resources. If <code>null</code> or empty then no resources are included
             * with the selection. If set then any matching resources will be included in the snapshot.
             */
            @JsonFormat(with = Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) List<ResourceSelection> resources, 
            /**
             * A selection for the location. If <code>null</code> or empty then no location filtering
             * will occur. If set then the provider location will be included in the snapshot.
             */
            @JsonFormat(with = Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY) List<LocationSelection> location) {
        
        public ProviderSelection {
            if(resources == null) {
                resources = List.of();
            } else {
                resources = List.copyOf(resources);
            }
            if(location == null) {
                location = List.of();
            } else {
                location = List.copyOf(location);
            }
        }
    }
}

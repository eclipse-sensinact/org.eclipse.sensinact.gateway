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

import org.eclipse.sensinact.filters.resource.selector.api.Selection.MatchType;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Feature;

/**
 * Describes the selection of resources based on their model, provider, service
 * or name. Members can be null (any). Selections set in a single
 * {@link ResourceSelector} are combined with an AND semantic.
 * If you want to set up an OR semantic then you can do this by creating multiple
 * {@link ResourceSelector} instances and passing them to a {@link ResourceSelectorFilterFactory}
 * <p>
 * Note that the primary goal of a Resource Selector is to select a *single* resource
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
public class ResourceSelector {

    /**
     * Selection based on the model name
     */
    public Selection model;

    /**
     * Selection based on the provider name
     */
    public Selection provider;

    /**
     * Selection based on the provider service name
     */
    public Selection service;

    /**
     * Selection based on the resource name
     */
    public Selection resource;

    /**
     * Selection based on the resource value
     */
    @JsonFormat(with = Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public List<ValueSelection> value;

    /**
     * Selection based on the resource value
     */
    @JsonFormat(with = Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    public List<LocationSelection> location;

    @Override
    public String toString() {
        return "ResourceSelector [model=" + model + ", provider=" + provider + ", service=" + service + ", resource="
                + resource + ", value=" + value + ", location=" + location + "]";
    }
}

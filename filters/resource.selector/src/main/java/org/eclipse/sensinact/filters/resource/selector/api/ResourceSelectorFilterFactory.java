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

import java.util.stream.Stream;

import org.eclipse.sensinact.core.snapshot.ICriterion;

/**
 * A factory for creating {@link ICriterion} instances based on
 * {@link ResourceSelector} definitions
 */
public interface ResourceSelectorFilterFactory {

    /**
     * Create a filter based on the supplied resource selector
     * @param selector
     * @return
     */
    public ICriterion parseResourceSelector(ResourceSelector selector);

    /**
     * Create an aggregate filter based on multiple resource selectors.
     * The aggregate filter will be the logical OR of the supplied
     * selectors, selecting all resources which match the {@link ResourceSelector}
     * @param selectors
     * @return
     */
    public ICriterion parseResourceSelector(Stream<ResourceSelector> selectors);
}

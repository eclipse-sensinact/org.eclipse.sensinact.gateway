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

import org.eclipse.sensinact.gateway.geojson.Geometry;

/**
 * Defines a selection for the location of provider
 */
public record LocationSelection(
        /**
         * The Geometry to match against
         */
        Geometry value,
        /**
         * The allowable distance around {@link #value}
         * to still count as a match.
         * <p>
         * If value is a {@link Point} then this
         * can be used to define a circle
         */
        double radius,
        /**
         * If true then the result of the test will be negated
         */
        boolean negate,
        /**
         * The type of matching to use
         */
        MatchType type) {

    public static enum MatchType {
        /**
         * The location must be fully contained inside the supplied value
         */
        CONTAINS,
        /**
         * The location must overlap with the supplied value
         */
        INTERSECTS,
        /**
         * The location must not overlap with the supplied value
         */
        DISJOINT;
    }
}

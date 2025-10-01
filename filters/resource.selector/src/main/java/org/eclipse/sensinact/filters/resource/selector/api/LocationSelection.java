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
         * The allowable distance, in metres, around {@link #value}
         * to still count as a match.
         * <p>
         * If value is a {@link Point} then this
         * can be used to define a circle.
         * <p>
         * This has the effect of making {@link #value}
         * bigger, making CONTAINS and INTERSECTS more
         * likely to return true, while DISJOINT and
         * WITHIN will be less likely to be true.
         */
        Double radius,
        /**
         * If true then the result of the test will be negated
         */
        boolean negate,
        /**
         * The type of matching to use
         */
        MatchType type) {

    public LocationSelection {
        if(type == null) {
            type = MatchType.WITHIN;
        }
        if(value == null) {
            throw new IllegalArgumentException("The location value must not be null");
        }
        if(radius != null && (radius.isInfinite() || radius.isNaN())) {
            throw new IllegalArgumentException("The supplied radius must be a finite value");
        }
    }

    public static enum MatchType {
        /**
         * The provider location must fully contain the supplied value
         */
        CONTAINS,
        /**
         * The provider location must be fully contained within the supplied value
         */
        WITHIN,
        /**
         * The provider location must overlap with or touch the supplied value
         */
        INTERSECTS,
        /**
         * The provider location must not overlap with the supplied value
         */
        DISJOINT;
    }
}

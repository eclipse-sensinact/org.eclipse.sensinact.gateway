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


import java.util.List;
import java.util.function.Predicate;

import org.eclipse.sensinact.filters.location.api.LocationMatchFactory;
import org.eclipse.sensinact.filters.resource.selector.api.LocationSelection;
import org.eclipse.sensinact.filters.resource.selector.api.LocationSelection.MatchType;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class uses the ESRI Geometry API to compute whether the location filter matches the location of a provider
 */
public class LocationSelectionCriterion {

    private static final Logger LOG = LoggerFactory.getLogger(LocationSelectionCriterion.class);

    /**
     * In the following matrices A is the location of the provider, and B is the
     * value of the location filter.
     */

    /**
     * The DE-9IM matrix for "Contains". Note that our contains means that the provider location
     * contains our value, and we count sitting on the boundary as containing (e.g. the filter
     * value is on the edge of the polygon defining the provider location, but not outside it).
     * This is why we do not require that they share an internal point.
     *
     * No part of the exterior of A is within the inside or boundary of B (i.e. A contains B)
     */
    private static final String CONTAINS = "******FF*";

    /**
     * The DE-9IM matrix for "Within". Note that our within means that the provider location
     * is contained by our value, and we count sitting on the boundary as containing (e.g. the
     * provider is on the edge of the geofence, but not outside it). This is why we do not
     * require that they share an internal point.
     *
     * No part of the exterior of B is within the inside or boundary of A (i.e. A is within B)
     */
    private static final String WITHIN = "**F**F***";

    /**
     * The DE-9IM matrices for "Intersects". Note that our intersects means that the provider location
     * and our value touch, cross or overlap in some way.
     *
     * Some part of the interiors is shared
     */
    private static final String INTERSECTS_1 = "T********";
    /**
     * The DE-9IM matrices for "Intersects". Note that our intersects means that the provider location
     * and our value touch, cross or overlap in some way.
     *
     * Some part of the interior of A is in the boundary of B.
     */
    private static final String INTERSECTS_2 = "*T*******";
    /**
     * The DE-9IM matrices for "Intersects". Note that our intersects means that the provider location
     * and our value touch, cross or overlap in some way.
     *
     * Some part of the boundary of A is in the interior of B.
     */
    private static final String INTERSECTS_3 = "***T*****";
    /**
     * The DE-9IM matrices for "Intersects". Note that our intersects means that the provider location
     * and our value touch, cross or overlap in some way.
     *
     * Some part of the boundary of A is in the boundary of B.
     */
    private static final String INTERSECTS_4 = "****T****";

    /**
     * The DE-9IM matrix for "DISJOINT". Note that our intersects means that the provider location
     * and our do not touch, cross or overlap in any way.
     */
    private static final String DISJOINT = "FF*FF****";

    private final LocationSelection ls;

    public LocationSelectionCriterion(LocationSelection ls) {
        this.ls = ls;
    }

    public Predicate<GeoJsonObject> locationFilter(LocationMatchFactory factory) {
        if(factory == null) {
            LOG.error("Unable to create a location match for {} as no factory is available", ls);
            throw new IllegalArgumentException("Location filtering is not enabled. Please provide a LocationMatchFactory service");
        }
        if(LOG.isDebugEnabled()) {
            LOG.debug("Creating location match for {} using factory {}", ls, factory);
        }
        return factory.toMatchPredicate(ls.value(), ls.radius(), getDE_9IM(ls.type()));
    }

    private static List<String> getDE_9IM(MatchType type) {
        return switch(type) {
            case CONTAINS:
                yield List.of(CONTAINS);
            case DISJOINT:
                yield List.of(DISJOINT);
            case INTERSECTS:
                yield List.of(INTERSECTS_1, INTERSECTS_2, INTERSECTS_3, INTERSECTS_4);
            case WITHIN:
                yield List.of(WITHIN);
            default:
                LOG.error("Unable to create matrices for match type {}", type);
                throw new IllegalArgumentException("Unknown match type " + type);
        };
    }

}

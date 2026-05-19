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

import static com.esri.core.geometry.Geometry.GeometryAccelerationDegree.enumMedium;
import static com.esri.core.geometry.Operator.Type.Relate;
import static org.eclipse.sensinact.gateway.filters.esri.geometry.EsriUtils.WGS84_COORDS;

import java.util.Objects;
import java.util.function.Predicate;

import org.eclipse.sensinact.filters.resource.selector.api.LocationSelection;
import org.eclipse.sensinact.filters.resource.selector.api.LocationSelection.MatchType;
import org.eclipse.sensinact.gateway.filters.esri.geometry.EsriUtils;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.OperatorFactoryLocal;
import com.esri.core.geometry.OperatorRelate;

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

    private static final OperatorRelate RELATE_OPERATOR = (OperatorRelate) OperatorFactoryLocal.getInstance().getOperator(Relate);

    private final Predicate<GeoJsonObject> locationFilter;

    public LocationSelectionCriterion(LocationSelection ls) {
        this.locationFilter = toLocationFilter(ls);
    }

    public Predicate<GeoJsonObject> locationFilter() {
        return locationFilter;
    }

    private static Predicate<GeoJsonObject> toLocationFilter(LocationSelection ls) {

        Geometry target = EsriUtils.toEsriGeometry(ls.value());

        Double radius = ls.radius();
        if(radius != null) {
            target = EsriUtils.bufferGeometry(target, radius);
        }
        // We accelerate the value as it will potentially be called for every provider
        // in the gateway, and for every subsequent update
        RELATE_OPERATOR.accelerateGeometry(target, null, enumMedium);

        Predicate<GeoJsonObject> filter = Objects::nonNull;
        return filter.and(getGeometryFilter(ls.type(), target));
    }

    private static Predicate<GeoJsonObject> getGeometryFilter(MatchType type, final Geometry targetValue) {
        Predicate<Geometry> locationCheck = switch(type) {
            case CONTAINS:
                yield l -> RELATE_OPERATOR.execute(l, targetValue, WGS84_COORDS, CONTAINS, null);
            case DISJOINT:
                yield l -> RELATE_OPERATOR.execute(l, targetValue, WGS84_COORDS, DISJOINT, null);
            case INTERSECTS:
                yield l -> {
                    return RELATE_OPERATOR.execute(l, targetValue, WGS84_COORDS, INTERSECTS_1, null) ||
                            RELATE_OPERATOR.execute(l, targetValue, WGS84_COORDS, INTERSECTS_2, null) ||
                            RELATE_OPERATOR.execute(l, targetValue, WGS84_COORDS, INTERSECTS_3, null) ||
                            RELATE_OPERATOR.execute(l, targetValue, WGS84_COORDS, INTERSECTS_4, null);
                };
            case WITHIN:
                yield l -> RELATE_OPERATOR.execute(l, targetValue, WGS84_COORDS, WITHIN, null);
            default:
                throw new IllegalArgumentException("Unknown match type " + type);
        };
        return l -> {
            Geometry g = EsriUtils.toEsriGeometry(l);
            return !g.isEmpty() && locationCheck.test(g);
        };
    }

}

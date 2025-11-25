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
import static com.esri.core.geometry.Operator.Type.Buffer;
import static com.esri.core.geometry.Operator.Type.Relate;
import static com.esri.core.geometry.Operator.Type.Union;
import static java.util.stream.Collectors.toList;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.sensinact.filters.resource.selector.api.LocationSelection;
import org.eclipse.sensinact.filters.resource.selector.api.LocationSelection.MatchType;
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.Feature;
import org.eclipse.sensinact.gateway.geojson.FeatureCollection;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.GeometryCollection;
import org.eclipse.sensinact.gateway.geojson.LineString;
import org.eclipse.sensinact.gateway.geojson.MultiLineString;
import org.eclipse.sensinact.gateway.geojson.MultiPolygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esri.core.geometry.Envelope2D;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryCursor;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.MultiPoint;
import com.esri.core.geometry.OperatorBuffer;
import com.esri.core.geometry.OperatorFactoryLocal;
import com.esri.core.geometry.OperatorRelate;
import com.esri.core.geometry.OperatorUnion;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SimpleGeometryCursor;
import com.esri.core.geometry.SpatialReference;

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

    /**
     * Used to combine multiple geometries into one
     */
    private static final OperatorUnion UNION_OPERATOR = (OperatorUnion) OperatorFactoryLocal.getInstance().getOperator(Union);

    /**
     * Geodesic buffering is not implemented, so we use "normal buffering" and warn if the distortion is likely to be big
     */
    private static final OperatorBuffer BUFFER_OPERATOR = (OperatorBuffer) OperatorFactoryLocal.getInstance().getOperator(Buffer);

    /**
     * The coordinate system used in GeoJSON
     */
    private static final SpatialReference WGS84_COORDS = SpatialReference.create("4326");

    /**
     * The approximate radius of the earth. Used in calculating the angular distance for buffering
     */
    private static final double EARTH_RADIUS_METRES = 6_371_000d;

    private static final double RADS_TO_DEGS = 180 / Math.PI;

    private final Predicate<GeoJsonObject> locationFilter;

    public LocationSelectionCriterion(LocationSelection ls) {
        this.locationFilter = toLocationFilter(ls);
    }

    public Predicate<GeoJsonObject> locationFilter() {
        return locationFilter;
    }

    private static Predicate<GeoJsonObject> toLocationFilter(LocationSelection ls) {

        Geometry target = toEsriGeometry(ls.value());

        Double radius = ls.radius();
        if(radius != null) {
            target = bufferGeometry(target, radius);
        }
        // We accelerate the value as it will potentially be called for every provider
        // in the gateway, and for every subsequent update
        RELATE_OPERATOR.accelerateGeometry(target, null, enumMedium);

        Predicate<GeoJsonObject> filter = Objects::nonNull;
        return filter.and(getGeometryFilter(ls.type(), target));
    }

    private static Predicate<GeoJsonObject> getGeometryFilter(MatchType type, final Geometry targetValue) {
        return switch(type) {
            case CONTAINS:
                yield l -> RELATE_OPERATOR.execute(toEsriGeometry(l), targetValue, WGS84_COORDS, CONTAINS, null);
            case DISJOINT:
                yield l -> RELATE_OPERATOR.execute(toEsriGeometry(l), targetValue, WGS84_COORDS, DISJOINT, null);
            case INTERSECTS:
                yield l -> {
                    Geometry g = toEsriGeometry(l);
                    return RELATE_OPERATOR.execute(g, targetValue, WGS84_COORDS, INTERSECTS_1, null) ||
                            RELATE_OPERATOR.execute(g, targetValue, WGS84_COORDS, INTERSECTS_2, null) ||
                            RELATE_OPERATOR.execute(g, targetValue, WGS84_COORDS, INTERSECTS_3, null) ||
                            RELATE_OPERATOR.execute(g, targetValue, WGS84_COORDS, INTERSECTS_4, null);
                };
            case WITHIN:
                yield l -> RELATE_OPERATOR.execute(toEsriGeometry(l), targetValue, WGS84_COORDS, WITHIN, null);
            default:
                throw new IllegalArgumentException("Unknown match type " + type);
        };
    }

    private static Geometry toEsriGeometry(GeoJsonObject value) {
        if(value == null) {
            return new Point();
        }
        return switch(value.type()) {
            case Point:
                yield toPoint(((org.eclipse.sensinact.gateway.geojson.Point) value).coordinates());
            case MultiPoint:
                MultiPoint mp = new MultiPoint();
                ((org.eclipse.sensinact.gateway.geojson.MultiPoint)value).coordinates().stream()
                .map(LocationSelectionCriterion::toPoint)
                .forEach(mp::add);
                yield mp;
            case LineString:
                Polyline line = new Polyline();
                addLine(line, ((LineString)value).coordinates());
                yield line;
            case MultiLineString:
                Polyline multiLine = new Polyline();
                List<List<Coordinates>> lines = ((MultiLineString)value).coordinates();
                for(List<Coordinates> l : lines) {
                    addLine(multiLine, l);
                }
                yield multiLine;
            case Polygon:
                List<List<Coordinates>> polyCoords = ((org.eclipse.sensinact.gateway.geojson.Polygon)value).coordinates();
                yield toPolygon(polyCoords);
            case MultiPolygon:
                MultiPolygon multiPolygon = (MultiPolygon)value;
                yield gatherUnionGeometry(multiPolygon.coordinates().stream()
                    .<Geometry>map(LocationSelectionCriterion::toPolygon)
                    .collect(toList()));
            case GeometryCollection:
                GeometryCollection gc = (GeometryCollection) value;
                yield gatherUnionGeometry(gc.geometries().stream()
                    .map(LocationSelectionCriterion::toEsriGeometry)
                    .collect(toList()));
            case Feature:
                Feature f = (Feature) value;
                yield f.geometry() == null ? new Point() : toEsriGeometry(f.geometry());
            case FeatureCollection:
                FeatureCollection fc = (FeatureCollection) value;
                yield gatherUnionGeometry(fc.features().stream()
                    .map(LocationSelectionCriterion::toEsriGeometry)
                    .collect(Collectors.toList()));
            default:
                throw new IllegalArgumentException("Unknown geometry type " + value.type());
        };
    }

    private static Point toPoint(Coordinates c) {
        return new Point(c.longitude(), c.latitude());
    }

    private static void addLine(MultiPath path, List<Coordinates> coordinates) {
        Iterator<Coordinates> iterator = coordinates.iterator();
        if(iterator.hasNext()) {
            path.startPath(toPoint(iterator.next()));
            while(iterator.hasNext()) {
                path.lineTo(toPoint(iterator.next()));
            }
        }
    }

    private static Polygon toPolygon(List<List<Coordinates>> polyCoords) {
        Polygon poly = new Polygon();
        for(List<Coordinates> l : polyCoords) {
            addLine(poly, l);
        }
        return poly;
    }

    /**
     * Gather a set of geometries into a union. This method takes into account the
     * fact that the union operator will ignore lower dimension shapes and
     * ensures that all shapes are included in the resultant geometry
     * @param geometries
     * @return
     */
    private static Geometry gatherUnionGeometry(List<Geometry> geometries) {
        if(geometries.isEmpty()) {
            return new Point();
        } else {
            int dimension = geometries.get(0).getDimension();
            GeometryCursor cursor = new SimpleGeometryCursor(geometries);
            if(geometries.stream().allMatch(g -> g.getDimension() == dimension)) {
                // All geometries have the same dimension, so we can union directly
                cursor = UNION_OPERATOR.execute(cursor, WGS84_COORDS, null);
            } else {
                // Do a minimal buffer to make all the constituents all 2d. This buffer
                // is approximately 0.1mm at the equator, reaching 1m at +/- 89.9 degrees
                cursor = BUFFER_OPERATOR.execute(cursor, WGS84_COORDS, new double[] {0.000000001d}, true, null);
            }
            return cursor.next();
        }
    }

    /**
     * Apply the required buffer radius to the geometry. Note that this is done using a basic
     * algorithm which does not take into account the distortion near the poles. Buffers will
     * become increasingly stretched along the east/west axis at extreme latitudes.
     * @param target
     * @param bufferRadius
     * @return
     */
    private static Geometry bufferGeometry(Geometry target, Double bufferRadius) {
        Envelope2D env = new Envelope2D();
        target.queryLooseEnvelope2D(env);
        if(env.ymax > 60 || env.ymin < 60 || env.getHeight() > 5) {
            LOG.debug("Location queries at high latitudes, or with large bounding boxes, are prone to distortion when adding a radius. Attempting to add radius {} to a shape from latitude {} to {}",
                    bufferRadius, env.ymin, env.ymax);
        }
        double angular_dist_degs = bufferRadius * RADS_TO_DEGS / EARTH_RADIUS_METRES;
        return BUFFER_OPERATOR.execute(target, WGS84_COORDS, angular_dist_degs, null);
    }
}

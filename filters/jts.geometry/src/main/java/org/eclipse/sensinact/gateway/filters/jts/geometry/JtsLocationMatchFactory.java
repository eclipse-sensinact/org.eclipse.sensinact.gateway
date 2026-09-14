/*********************************************************************
* Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.gateway.filters.jts.geometry;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.sensinact.filters.location.api.LocationMatchFactory;
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.Feature;
import org.eclipse.sensinact.gateway.geojson.FeatureCollection;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.GeometryCollection;
import org.eclipse.sensinact.gateway.geojson.LineString;
import org.eclipse.sensinact.gateway.geojson.MultiLineString;
import org.eclipse.sensinact.gateway.geojson.MultiPoint;
import org.eclipse.sensinact.gateway.geojson.MultiPolygon;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.geojson.Polygon;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;
import org.locationtech.jts.operation.relateng.RelateNG;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JtsLocationMatchFactory implements LocationMatchFactory {

    private static final Logger LOG = LoggerFactory.getLogger(JtsLocationMatchFactory.class);

    /**
     * The coordinate system used in GeoJSON
     */
    private static final int WGS84_COORDS = 4326;

    /**
     * The approximate radius of the earth. Used in calculating the angular distance for buffering
     */
    private static final double EARTH_RADIUS_METRES = 6_371_000d;

    private static final double RADS_TO_DEGS = 180 / Math.PI;

    /**
     * We use WGS84 coordinates with a precision of 9 decimal places to ensure good accuracy throughout
     * the world
     */
    private static final GeometryFactory GEO_FACTORY = new GeometryFactory(
            new PrecisionModel(1_000_000_000.0d), WGS84_COORDS);

    private static Geometry toJtsGeometry(GeoJsonObject value) {
        if(value == null) {
            return GEO_FACTORY.createPoint();
        }
        switch(value.type()) {
            case Point:
                return toJtsPoint(((Point) value).coordinates());
            case MultiPoint:
                return GEO_FACTORY.createMultiPoint(toCoordinateSequence(((MultiPoint) value).coordinates()));
            case LineString:
                return GEO_FACTORY.createLineString(toCoordinateSequence(((LineString) value).coordinates()));
            case MultiLineString:
                return GEO_FACTORY.createMultiLineString(((MultiLineString)value).coordinates().stream()
                        .map(l -> GEO_FACTORY.createLineString(toCoordinateSequence(l)))
                        .toArray(org.locationtech.jts.geom.LineString[]::new));
            case Polygon:
                return toPolygon(((Polygon) value).coordinates());
            case MultiPolygon:
                MultiPolygon multiPolygon = (MultiPolygon)value;
                return gatherUnionGeometry(multiPolygon.coordinates().stream()
                    .<Geometry>map(JtsLocationMatchFactory::toPolygon)
                    .collect(toList()));
            case GeometryCollection:
                GeometryCollection gc = (GeometryCollection) value;
                return gatherUnionGeometry(gc.geometries().stream()
                    .map(JtsLocationMatchFactory::toJtsGeometry)
                    .collect(toList()));
            case Feature:
                Feature f = (Feature) value;
                return f.geometry() == null ? GEO_FACTORY.createPoint() : toJtsGeometry(f.geometry());
            case FeatureCollection:
                FeatureCollection fc = (FeatureCollection) value;
                return gatherUnionGeometry(fc.features().stream()
                    .map(JtsLocationMatchFactory::toJtsGeometry)
                    .collect(Collectors.toList()));
            default:
                throw new IllegalArgumentException("Unknown geometry type " + value.type());
        }
    }

    private static Geometry toJtsPoint(Coordinates c) {
        return c.isEmpty() ? GEO_FACTORY.createPoint() :
                GEO_FACTORY.createPoint(toJtsCoordinate(c));
    }

    private static Coordinate toJtsCoordinate(Coordinates c) {
        return Double.isNaN(c.elevation()) ? new Coordinate(c.longitude(), c.latitude()) :
            new Coordinate(c.longitude(), c.latitude(), c.elevation());
    }

    private static Geometry toPolygon(List<List<Coordinates>> polyCoords) {
        if (polyCoords.isEmpty()) {
            return GEO_FACTORY.createPolygon();
        }

        List<LinearRing> rings = polyCoords.stream()
            .map(l -> GEO_FACTORY.createLinearRing(toCoordinateSequence(l)))
            .toList();
        return GEO_FACTORY.createPolygon(rings.get(0), rings.subList(1, rings.size()).toArray(LinearRing[]::new));
    }

    private static CoordinateSequence toCoordinateSequence(List<Coordinates> sequence) {
        return GEO_FACTORY.getCoordinateSequenceFactory().create(
                sequence.stream()
                    .map(JtsLocationMatchFactory::toJtsCoordinate)
                    .toArray(Coordinate[]::new));
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
            return GEO_FACTORY.createPoint();
        } else {
            return OverlayNGRobust.union(geometries, GEO_FACTORY);
        }
    }

    /**
     * Apply the required buffer radius to the geometry. Note that this is done using a basic
     * algorithm which does not take into account the distortion near the poles. Buffers will
     * become increasingly stretched along the east/west axis at extreme latitudes.
     * @param target the geometry to buffer
     * @param bufferRadius radius of buffer in metres
     * @return the bufferd geometry
     */
    private static Geometry bufferGeometry(GeoJsonObject target, Double bufferRadius) {
        Geometry jtsTarget = toJtsGeometry(target);
        Geometry env = jtsTarget.getEnvelope();
        for(Coordinate c : env.getCoordinates()) {
            if(c.y > 60 || c.y < -60 || c.z > 5) {
                LOG.debug("Location queries at high latitudes, or with large bounding boxes, are prone to distortion when adding a radius. Attempting to add radius {} to a shape with latitude {}",
                        bufferRadius, c.y);
            }
        }
        double angular_dist_degs = bufferRadius * RADS_TO_DEGS / EARTH_RADIUS_METRES;
        return jtsTarget.buffer(angular_dist_degs);
    }

    @Override
    public Predicate<GeoJsonObject> toMatchPredicate(GeoJsonObject target, Double bufferRadius, List<String> DE_9IM) {
        final Geometry jtsTarget = bufferRadius == null ? toJtsGeometry(target) :
            bufferGeometry(target, bufferRadius);

        final RelateNG prepared = RelateNG.prepare(jtsTarget);

        Predicate<GeoJsonObject> nullFilter = Objects::nonNull;
        Predicate<Geometry> geomFilter = DE_9IM.stream()
                .map(im -> getGeometryFilter(im, prepared))
                .reduce(Predicate::or)
                .orElse(x -> false);
        return nullFilter.and(l -> {
                Geometry jts = toJtsGeometry(l);
                return !jts.isEmpty() && geomFilter.test(jts);
            });
    }

    private static Predicate<Geometry> getGeometryFilter(String DE_9IM, RelateNG prepared) {
        // We transpose as the arguments are reversed in optimised JTS
        return g -> prepared.evaluate(g).transpose().matches(DE_9IM);
    }
}

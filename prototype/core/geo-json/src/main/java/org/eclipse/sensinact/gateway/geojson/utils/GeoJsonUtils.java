/*********************************************************************
* Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.gateway.geojson.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.Feature;
import org.eclipse.sensinact.gateway.geojson.FeatureCollection;
import org.eclipse.sensinact.gateway.geojson.Geometry;
import org.eclipse.sensinact.gateway.geojson.GeometryCollection;
import org.eclipse.sensinact.gateway.geojson.LineString;
import org.eclipse.sensinact.gateway.geojson.MultiLineString;
import org.eclipse.sensinact.gateway.geojson.MultiPoint;
import org.eclipse.sensinact.gateway.geojson.MultiPolygon;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.geojson.Polygon;

/**
 * Utility methods to create GeoJSON objects.
 *
 * The inputs should be valid as null values are not checked.
 */
public class GeoJsonUtils {

    /**
     * Prepares a coordinates member of a geometry object
     *
     * @param longitude Point longitude
     * @param latitude  Point latitude
     * @return The coordinates member of a geometry object
     */
    public static Coordinates coords(double longitude, double latitude) {
        return coords(longitude, latitude, Double.NaN);
    }

    /**
     * Prepares a coordinates member of a geometry object
     *
     * @param longitude Point longitude
     * @param latitude  Point latitude
     * @param elevation Point elevation (can be {@link Double#NaN})
     * @return The coordinates member of a geometry object
     */
    public static Coordinates coords(double longitude, double latitude, double elevation) {
        final Coordinates coordinates = new Coordinates();
        coordinates.longitude = longitude;
        coordinates.latitude = latitude;
        coordinates.elevation = elevation;
        return coordinates;
    }

    /**
     * Makes a point with the given coordinates
     *
     * @param coords Point coordinates
     * @return A GeoJSON point
     */
    public static Point point(Coordinates coords) {
        final Point point = new Point();
        point.coordinates = coords;
        return point;
    }

    /**
     * Makes a point without elevation
     *
     * @param longitude Point longitude
     * @param latitude  Point latitude
     * @return A GeoJSON point
     */
    public static Point point(double longitude, double latitude) {
        return point(coords(longitude, latitude));
    }

    /**
     * Makes a point with an elevation
     *
     * @param longitude Point longitude
     * @param latitude  Point latitude
     * @param elevation Point elevation (can be {@link Double#NaN})
     * @return A GeoJSON point
     */
    public static Point point(double longitude, double latitude, double elevation) {
        return point(coords(longitude, latitude, elevation));
    }

    /**
     * Makes a multi point from a list of coordinates
     *
     * @param points List of points
     * @return A GeoJSON MultiPoint
     */
    public static MultiPoint multiPoint(Coordinates... points) {
        final MultiPoint multipoint = new MultiPoint();
        multipoint.coordinates = Arrays.asList(points);
        return multipoint;
    }

    /**
     * Makes a multi point from a list of points
     *
     * @param points List of points
     * @return A GeoJSON MultiPoint
     */
    public static MultiPoint multiPoint(Point... points) {
        final MultiPoint multipoint = new MultiPoint();
        multipoint.coordinates = new ArrayList<>();
        for (Point point : points) {
            multipoint.coordinates.add(point.coordinates);
        }
        return multipoint;
    }

    /**
     * Makes a line string from a list of coordinates
     *
     * @param firstPoint  First point of the line string
     * @param secondPoint Second point of the line string
     * @param otherPoints Other points of the line string
     * @return A GeoJSON line string
     */
    public static LineString lineString(Coordinates firstPoint, Coordinates secondPoint, Coordinates... otherPoints) {
        final LineString lineString = new LineString();
        lineString.coordinates = new ArrayList<>();
        lineString.coordinates.add(firstPoint);
        lineString.coordinates.add(secondPoint);
        if (otherPoints != null) {
            lineString.coordinates.addAll(Arrays.asList(otherPoints));
        }
        return lineString;
    }

    /**
     * Makes a multi line string from a list of line strings
     *
     * @param lineStrings List of line strings
     * @return A GeoJSON MultiLineString
     */
    public static MultiLineString multiLineString(LineString... lineStrings) {
        final MultiLineString multiLineString = new MultiLineString();
        multiLineString.coordinates = new ArrayList<>();
        for (LineString lineString : lineStrings) {
            multiLineString.coordinates.add(lineString.coordinates);
        }
        return multiLineString;
    }

    /**
     * Makes a polygon from a list of coordinates.
     *
     * If the coordinates of the first and last point are different, the first point
     * is added as final point of the linear ring.
     *
     * @param firstPoint  First point of the polygon
     * @param secondPoint Second point of the polygon
     * @param thirdPoint  Third point of the polygon
     * @param otherPoints Other points of the line string
     * @return A GeoJSON polygon
     */
    public static Polygon polygon(Coordinates firstPoint, Coordinates secondPoint, Coordinates thirdPoint,
            Coordinates... otherPoints) {
        final List<Coordinates> exteriorRing = new ArrayList<>();
        exteriorRing.add(firstPoint);
        exteriorRing.add(secondPoint);
        exteriorRing.add(thirdPoint);
        if (otherPoints != null && otherPoints.length > 0) {
            exteriorRing.addAll(Arrays.asList(otherPoints));
            final Coordinates lastPoint = otherPoints[otherPoints.length - 1];
            if (!Objects.equals(firstPoint, lastPoint)) {
                exteriorRing.add(firstPoint);
            }
        } else {
            // Fourth point not given: close the loop
            exteriorRing.add(firstPoint);
        }

        final Polygon polygon = new Polygon();
        polygon.coordinates = List.of(exteriorRing);
        return polygon;
    }

    /**
     * Makes a polygon from a list of points.
     *
     * If the coordinates of the first and last point are different, the first point
     * is added as final point of the linear ring.
     *
     * @param firstPoint  First point of the polygon
     * @param secondPoint Second point of the polygon
     * @param thirdPoint  Third point of the polygon
     * @param otherPoints Other points of the line string
     * @return A GeoJSON polygon
     */
    public static Polygon polygon(Point firstPoint, Point secondPoint, Point thirdPoint, Point... otherPoints) {
        return polygon(firstPoint.coordinates, secondPoint.coordinates, thirdPoint.coordinates,
                Arrays.stream(otherPoints).map(p -> p.coordinates).toArray(Coordinates[]::new));
    }

    /**
     * Makes a polygon from a list of coordinates.
     *
     * If the coordinates of the first and last point are different, the first point
     * is added as final point of the linear ring.
     *
     * @param exteriorRing  Description of the exterior ring (the {@link LineString}
     *                      must have at least 4 points)
     * @param interiorRings Description of the interior rings (each
     *                      {@link LineString} must have at least 4 points)
     * @return A GeoJSON polygon
     */
    public static Polygon polygon(LineString exteriorRing, LineString... interiorRings) {
        final Polygon polygon = new Polygon();
        polygon.coordinates = new ArrayList<>();
        polygon.coordinates.addAll(List.of(exteriorRing.coordinates));

        // Share the bounding box if it was set
        polygon.bbox = exteriorRing.bbox;

        if (interiorRings != null) {
            for (LineString interiorRing : interiorRings) {
                polygon.coordinates.addAll(List.of(interiorRing.coordinates));
            }
        }
        return polygon;
    }

    /**
     * Makes a multi polygon from a list of polygons
     *
     * @param polygons List of polygons
     * @return A GeoJSON MultiPolygon
     */
    public static MultiPolygon multiPolygon(Polygon... polygons) {
        final MultiPolygon multiPolygon = new MultiPolygon();
        multiPolygon.coordinates = new ArrayList<>();
        for (Polygon polygon : polygons) {
            multiPolygon.coordinates.add(polygon.coordinates);
        }
        return multiPolygon;
    }

    /**
     * Makes a collection of geometries
     *
     * @param geometries List of geometries
     * @return A GeoJSON GeometryCollection
     */
    public static GeometryCollection geometryCollection(Geometry... geometries) {
        final GeometryCollection collection = new GeometryCollection();
        collection.geometries = Arrays.asList(geometries);
        return collection;
    }

    /**
     * Makes a feature.
     *
     * The Feature properties field will be empty but not null.
     *
     * @param id       Feature ID
     * @param geometry Feature geometry
     * @return A GeoJSON Feature
     */
    public static Feature feature(String id, Geometry geometry) {
        final Feature feature = new Feature();
        feature.id = id;
        feature.geometry = geometry;
        return feature;
    }

    public static FeatureCollection featureCollection(Feature... features) {
        final FeatureCollection collection = new FeatureCollection();
        collection.features = Arrays.asList(features);
        return collection;
    }
}

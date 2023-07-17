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
package org.eclipse.sensinact.gateway.geojson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.eclipse.sensinact.gateway.geojson.utils.GeoJsonUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests the creation of objects with {@link GeoJsonUtils}
 */
public class GeoJsonUtilsTest {

    @Test
    void testCoordinates() {
        final double epsilon = 0.0000001d;

        final Coordinates testedNoElevation = GeoJsonUtils.coords(2.3480934384505394, 48.853566537211556);
        assertEquals(2.3480934, testedNoElevation.longitude, epsilon);
        assertEquals(48.8535665, testedNoElevation.latitude, epsilon);
        assertTrue(Double.isNaN(testedNoElevation.elevation));

        final Coordinates testedWithElevation = GeoJsonUtils.coords(2.3480934384505394, 48.853566537211556, 36);
        assertEquals(2.3480934, testedWithElevation.longitude, epsilon);
        assertEquals(48.8535665, testedWithElevation.latitude, epsilon);
        assertEquals(36d, testedWithElevation.elevation, epsilon);

        // Test different instance
        final Coordinates testedWithElevation2 = GeoJsonUtils.coords(2.3480934384505394, 48.853566537211556, 36);
        assertTrue(testedWithElevation != testedWithElevation2);
        assertEquals(testedWithElevation.hashCode(), testedWithElevation2.hashCode());
        assertEquals(testedWithElevation, testedWithElevation2);

        // Check comparison with and without elevation
        assertNotEquals(testedWithElevation, testedNoElevation);
        assertNotEquals(testedNoElevation, testedWithElevation);

        final Coordinates testedNoElevation2 = GeoJsonUtils.coords(2.3480934384505394, 48.853566537211556);
        assertTrue(testedNoElevation != testedNoElevation2);
        assertEquals(testedNoElevation.hashCode(), testedNoElevation2.hashCode());
        assertEquals(testedNoElevation, testedNoElevation2);

        assertEquals(testedNoElevation, GeoJsonUtils.coords(2.3480934384505394, 48.853566537211556, Double.NaN));
    }

    @Test
    void testPoint() {
        double lon = 2.295052d;
        double lat = 48.873771;
        double elev = 59d;

        final Coordinates coords = GeoJsonUtils.coords(lon, lat, elev);
        final Coordinates coordsNoElev = GeoJsonUtils.coords(lon, lat);

        assertEquals(Map.of(), GeoJsonUtils.point(coords).foreignMembers);
        assertEquals(coords, GeoJsonUtils.point(coords).coordinates);
        assertEquals(coords, GeoJsonUtils.point(lon, lat, elev).coordinates);
        assertEquals(coordsNoElev, GeoJsonUtils.point(lon, lat).coordinates);
        assertEquals(coordsNoElev, GeoJsonUtils.point(lon, lat, Double.NaN).coordinates);
    }

    @Test
    void testLineString() {
        double lon1 = 2.27968d;
        double lat1 = 48.85002d;
        double elev1 = 29d;
        final Coordinates coords1 = GeoJsonUtils.coords(lon1, lat1, elev1);

        double lon2 = -74.0444947d;
        double lat2 = 40.6892558d;
        double elev2 = 5d;
        final Coordinates coords2 = GeoJsonUtils.coords(lon2, lat2, elev2);
        assertNotEquals(coords1, coords2);

        final LineString line = GeoJsonUtils.lineString(coords1, coords2);
        assertEquals(Map.of(), line.foreignMembers);
        assertEquals(2, line.coordinates.size());
        assertEquals(coords1, line.coordinates.get(0));
        assertEquals(coords2, line.coordinates.get(1));

        final LineString reverseLine = GeoJsonUtils.lineString(coords2, coords1);
        assertNotEquals(line.hashCode(), reverseLine.hashCode());
        assertNotEquals(line, reverseLine);
        assertNotEquals(reverseLine, line);

        double lon3 = 7.299085d;
        double lat3 = 43.727424d;
        double elev3 = 369.86d;
        final Coordinates coords3 = GeoJsonUtils.coords(lon3, lat3, elev3);

        final LineString threePoints = GeoJsonUtils.lineString(coords1, coords2, coords3);
        assertEquals(3, threePoints.coordinates.size());
        assertEquals(coords1, threePoints.coordinates.get(0));
        assertEquals(coords2, threePoints.coordinates.get(1));
        assertEquals(coords3, threePoints.coordinates.get(2));
        assertNotEquals(line, threePoints);
    }

    @Test
    void testMultiPoint() {
        double lon1 = 2.27968d;
        double lat1 = 48.85002d;
        double elev1 = 29d;
        final Coordinates coords1 = GeoJsonUtils.coords(lon1, lat1, elev1);

        double lon2 = -74.0444947d;
        double lat2 = 40.6892558d;
        double elev2 = 5d;
        final Coordinates coords2 = GeoJsonUtils.coords(lon2, lat2, elev2);

        final MultiPoint points = GeoJsonUtils.multiPoint(coords1, coords2);
        assertEquals(Map.of(), points.foreignMembers);
        assertEquals(2, points.coordinates.size());
        assertEquals(coords1, points.coordinates.get(0));
        assertEquals(coords2, points.coordinates.get(1));

        final MultiPoint reversePoints = GeoJsonUtils.multiPoint(coords2, coords1);
        assertNotEquals(points.hashCode(), reversePoints.hashCode());
        assertNotEquals(points, reversePoints);
        assertNotEquals(reversePoints, points);

        double lon3 = 7.299085d;
        double lat3 = 43.727424d;
        double elev3 = 369.86d;
        final Coordinates coords3 = GeoJsonUtils.coords(lon3, lat3, elev3);

        final MultiPoint threePoints = GeoJsonUtils.multiPoint(coords1, coords2, coords3);
        assertEquals(3, threePoints.coordinates.size());
        assertEquals(coords1, threePoints.coordinates.get(0));
        assertEquals(coords2, threePoints.coordinates.get(1));
        assertEquals(coords3, threePoints.coordinates.get(2));
        assertNotEquals(points, threePoints);
    }

    @Test
    void testPolygon() {
        double lon1 = 2.27968d;
        double lat1 = 48.85002d;
        double elev1 = 29d;
        final Coordinates coords1 = GeoJsonUtils.coords(lon1, lat1, elev1);

        double lon2 = -74.0444947d;
        double lat2 = 40.6892558d;
        double elev2 = 5d;
        final Coordinates coords2 = GeoJsonUtils.coords(lon2, lat2, elev2);

        double lon3 = 7.299085d;
        double lat3 = 43.727424d;
        double elev3 = 369.86d;
        final Coordinates coords3 = GeoJsonUtils.coords(lon3, lat3, elev3);

        double lon4 = 2.29449d;
        double lat4 = 48.858255;
        double elev4 = 312.99d;
        final Coordinates coords4 = GeoJsonUtils.coords(lon4, lat4, elev4);

        final Polygon polygon1Opened = GeoJsonUtils.polygon(coords1, coords2, coords3);
        assertEquals(Map.of(), polygon1Opened.foreignMembers);
        assertEquals(1, polygon1Opened.coordinates.size());
        assertEquals(4, polygon1Opened.coordinates.get(0).size());
        assertEquals(coords1, polygon1Opened.coordinates.get(0).get(0));
        assertEquals(coords2, polygon1Opened.coordinates.get(0).get(1));
        assertEquals(coords3, polygon1Opened.coordinates.get(0).get(2));
        assertEquals(coords1, polygon1Opened.coordinates.get(0).get(3));

        final Polygon polygon1Closed = GeoJsonUtils.polygon(coords1, coords2, coords3, coords1);
        assertEquals(1, polygon1Closed.coordinates.size());
        assertEquals(4, polygon1Closed.coordinates.get(0).size());
        assertEquals(coords1, polygon1Closed.coordinates.get(0).get(0));
        assertEquals(coords2, polygon1Closed.coordinates.get(0).get(1));
        assertEquals(coords3, polygon1Closed.coordinates.get(0).get(2));
        assertEquals(coords1, polygon1Closed.coordinates.get(0).get(3));
        assertEquals(polygon1Closed, polygon1Opened);

        final Polygon polygon2Opened = GeoJsonUtils.polygon(coords1, coords2, coords3, coords4);
        assertEquals(1, polygon2Opened.coordinates.size());
        assertEquals(5, polygon2Opened.coordinates.get(0).size());
        assertEquals(coords1, polygon2Opened.coordinates.get(0).get(0));
        assertEquals(coords2, polygon2Opened.coordinates.get(0).get(1));
        assertEquals(coords3, polygon2Opened.coordinates.get(0).get(2));
        assertEquals(coords4, polygon2Opened.coordinates.get(0).get(3));
        assertEquals(coords1, polygon2Opened.coordinates.get(0).get(4));

        final Polygon polygon2Closed = GeoJsonUtils.polygon(coords1, coords2, coords3, coords4, coords1);
        assertEquals(1, polygon2Closed.coordinates.size());
        assertEquals(5, polygon2Closed.coordinates.get(0).size());
        assertEquals(coords1, polygon2Closed.coordinates.get(0).get(0));
        assertEquals(coords2, polygon2Closed.coordinates.get(0).get(1));
        assertEquals(coords3, polygon2Closed.coordinates.get(0).get(2));
        assertEquals(coords4, polygon2Closed.coordinates.get(0).get(3));
        assertEquals(coords1, polygon2Closed.coordinates.get(0).get(4));
        assertEquals(polygon2Closed, polygon2Opened);
    }

    @Test
    void testFeature() {
        double lat = 45.184957;
        double lon = 5.735451;
        final Feature feature = GeoJsonUtils.feature("someId", GeoJsonUtils.point(lat, lon));
        assertEquals("someId", feature.id);
        assertNotNull(feature.properties);
        assertEquals(Map.of(), feature.foreignMembers);

        final FeatureCollection collection = GeoJsonUtils.featureCollection(feature);
        assertEquals(1, collection.features.size());
        assertEquals(feature, collection.features.get(0));
        assertEquals(Map.of(), collection.foreignMembers);
    }
}

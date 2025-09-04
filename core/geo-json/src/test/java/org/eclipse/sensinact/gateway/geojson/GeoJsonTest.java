/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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

import static org.eclipse.sensinact.gateway.geojson.GeoJsonType.Feature;
import static org.eclipse.sensinact.gateway.geojson.GeoJsonType.FeatureCollection;
import static org.eclipse.sensinact.gateway.geojson.GeoJsonType.GeometryCollection;
import static org.eclipse.sensinact.gateway.geojson.GeoJsonType.LineString;
import static org.eclipse.sensinact.gateway.geojson.GeoJsonType.MultiLineString;
import static org.eclipse.sensinact.gateway.geojson.GeoJsonType.MultiPoint;
import static org.eclipse.sensinact.gateway.geojson.GeoJsonType.MultiPolygon;
import static org.eclipse.sensinact.gateway.geojson.GeoJsonType.Point;
import static org.eclipse.sensinact.gateway.geojson.GeoJsonType.Polygon;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

public class GeoJsonTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        JsonFactory factory = JsonFactory.builder()
                .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
                .build();
        mapper = new ObjectMapper(factory);
    }

    private URL getFileResource(String name) {
        return getClass().getResource("/" + name);
    }

    @Test
    void testGeometries() throws Exception {
        List<Geometry> geometries = mapper.readValue(getFileResource("test-geometries.json"),
                new TypeReference<List<Geometry>>() {
                });

        assertEquals(9, geometries.size());
        assertEquals(Point, geometries.get(0).type);
        assertEquals(LineString, geometries.get(1).type);
        assertEquals(Polygon, geometries.get(2).type);
        assertEquals(Polygon, geometries.get(3).type);
        assertEquals(MultiPoint, geometries.get(4).type);
        assertEquals(MultiLineString, geometries.get(5).type);
        assertEquals(MultiPolygon, geometries.get(6).type);
        assertEquals(GeometryCollection, geometries.get(7).type);
        assertEquals(MultiPolygon, geometries.get(8).type);

        Polygon p = (Polygon) geometries.get(2);
        assertEquals(1, p.coordinates.size());
        assertEquals(5, p.coordinates.get(0).size());

        p = (Polygon) geometries.get(3);
        assertEquals(2, p.coordinates.size());
        assertEquals(5, p.coordinates.get(0).size());
        assertEquals(5, p.coordinates.get(1).size());

        GeometryCollection gc = (GeometryCollection) geometries.get(7);

        assertEquals(2, gc.geometries.size());
        assertEquals(Point, gc.geometries.get(0).type);
        assertEquals(LineString, gc.geometries.get(1).type);
    }

    @Test
    void testBoundingBox() throws Exception {
        List<GeoJsonObject> geoObjects = mapper.readValue(getFileResource("test-bbox.json"),
                new TypeReference<List<GeoJsonObject>>() {
                });

        assertEquals(4, geoObjects.size());
        assertEquals(FeatureCollection, geoObjects.get(0).type);
        assertEquals(FeatureCollection, geoObjects.get(1).type);
        assertEquals(Feature, geoObjects.get(2).type);
        assertEquals(Polygon, geoObjects.get(3).type);

        for (GeoJsonObject go : geoObjects) {
            assertNotNull(go.bbox);
            assertFalse(go.bbox.isEmpty());
        }
    }

    @Test
    void testFeatures() throws Exception {
        List<GeoJsonObject> geoObjects = mapper.readValue(getFileResource("test-features.json"),
                new TypeReference<List<GeoJsonObject>>() {
                });

        assertEquals(1, geoObjects.size());
        assertEquals(FeatureCollection, geoObjects.get(0).type);

        FeatureCollection fc = (FeatureCollection) geoObjects.get(0);

        assertEquals(3, fc.features.size());

        Feature f = fc.features.get(0);

        assertEquals("value0", f.properties.get("prop0"));
        assertEquals(Point, f.geometry.type);

        f = fc.features.get(1);

        assertEquals("value0", f.properties.get("prop0"));
        assertEquals(0.0d, f.properties.get("prop1"));
        assertEquals(LineString, f.geometry.type);

        f = fc.features.get(2);

        assertEquals("value0", f.properties.get("prop0"));
        assertEquals(Map.of("this", "that"), f.properties.get("prop1"));
        assertEquals(Polygon, f.geometry.type);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testForeignMembers() throws Exception {
        List<GeoJsonObject> geoObjects = mapper.readValue(getFileResource("test-foreignMembers.json"),
                new TypeReference<List<GeoJsonObject>>() {
                });

        assertEquals(3, geoObjects.size());

        assertEquals(Point, geoObjects.get(0).type);
        assertEquals(Feature, geoObjects.get(1).type);
        assertEquals(FeatureCollection, geoObjects.get(2).type);

        assertEquals("foo", geoObjects.get(0).foreignMembers.get("extra"));
        assertEquals("LineString",
                ((Map<String, Object>) geoObjects.get(1).foreignMembers.get("centerline")).get("type"));
        assertEquals(Map.of("bar", "baz"), geoObjects.get(2).foreignMembers.get("extra"));
    }

    @Test
    void testNaN() throws Exception {
    	 try {
             mapper.readValue(getFileResource("test-pointNaN.json"), new TypeReference<List<Geometry>>() {
             });
             fail("MismatchedInputException expected");
         } catch (Exception e) {
             assertTrue(e instanceof MismatchedInputException);
             assertEquals("GeoJSON coordinates cannot have NaN as latitude or longitude\n"
                     + " at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 7, column: 9] (through reference chain: java.util.ArrayList[0]->org.eclipse.sensinact.gateway.geojson.Point[\"coordinates\"])", e.getMessage());
         }
    }

    @Test
    void testEmpty() throws Exception {
        try {
            mapper.readValue(getFileResource("test-pointEmpty.json"), new TypeReference<List<Geometry>>() {
            });
            fail("MismatchedInputException expected");
        } catch (Exception e) {
            assertTrue(e instanceof MismatchedInputException);
            assertEquals("GeoJSON coordinates must always be a list of at least two elements\n"
                    + " at [Source: REDACTED (`StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION` disabled); line: 5, column: 9] (through reference chain: java.util.ArrayList[0]->org.eclipse.sensinact.gateway.geojson.Point[\"coordinates\"])", e.getMessage());
        }
    }

}

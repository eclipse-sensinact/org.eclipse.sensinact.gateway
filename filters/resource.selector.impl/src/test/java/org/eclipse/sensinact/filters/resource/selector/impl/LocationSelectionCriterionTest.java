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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.eclipse.sensinact.filters.resource.selector.api.LocationSelection;
import org.eclipse.sensinact.filters.resource.selector.api.LocationSelection.MatchType;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * This class tests a subset of the location selection function.
 * It can be extended if we wish to test filter values that are not
 * polygons or multi-polygons.
 */
public class LocationSelectionCriterionTest {

    // All polygons
    private static final String BUSHY_PARK = "Bushy Park";
    private static final String RICHMOND_PARK = "Richmond Park";
    private static final String GREENWICH_PARK = "Greenwich Park";
    private static final String PRIMROSE_HILL = "Primrose Hill";
    private static final String REGENT_S_PARK = "Regent's Park";
    private static final String GREEN_PARK = "Green Park";
    private static final String ST_JAMES_S_PARK = "St James's Park";
    private static final String KENSINGTON_GARDENS = "Kensington Gardens";
    private static final String HYDE_PARK = "Hyde Park";

    @SuppressWarnings("unused")
    private static final Set<String> ALL_PARKS = Set.of(HYDE_PARK, KENSINGTON_GARDENS,
            ST_JAMES_S_PARK, GREEN_PARK, REGENT_S_PARK, PRIMROSE_HILL, GREENWICH_PARK, RICHMOND_PARK,
            BUSHY_PARK);

    /**
     * Point
     */
    private static final String ALBERT_MEMORIAL = "Albert Memorial";
    /**
     * Point
     */
    private static final String ROYAL_ALBERT_HALL = "Royal Albert Hall";
    /**
     * Polygon
     */
    private static final String KENSINGTON_GARDENS_ROUND_POND = "Round Pond";
    /**
     * Polygon
     */
    private static final String SERPENTINE = "Serpentine";
    /**
     * LineString (inside Hyde Park, crosses the Serpentine)
     */
    private static final String SERPENTINE_BRIDGE = "Serpentine Bridge";
    /**
     * Point (inside the serpentine)
     */
    private static final String SERPENTINE_ISLAND = "Serpentine Island";
    /**
     * Point (on the boundary of the serpentine)
     */
    private static final String SERPENTINE_BOATHOUSE = "Serpentine Boathouse";
    /**
     * Point
     */
    private static final String GREENWICH_OBSERVATORY = "Greenwich Observatory";
    /**
     * LineString in Regent's Park
     */
    private static final String THE_BROADWALK = "The Broadwalk";
    /**
     * Polygon in Regent's Park
     */
    private static final String LONDON_ZOO = "London Zoo";
    /**
     * LineString crossing from Hyde Park into Kensington Gardens
     */
    private static final String BUDGE_S_WALK = "Budge's Walk";
    /**
     * LineString entirely in Kensington Gardens, crosses Budge's Walk
     */
    private static final String LANCASTER_WALK = "Lancaster Walk";
    /**
     * Polygon partially in Kensington Gardens
     */
    private static final String KENSINGTON_PALACE = "Kensington Palace";
    /**
     * LineString partially in Kensington Gardens and partially in Hyde Park
     */
    private static final String SOUTH_CARRIAGE_DRIVE = "South Carriage Drive";
    /**
     * LineString completely in Kensington Gardens
     */
    private static final String JUBILEE_WALK = "Jubilee Walk";
    /**
     * Polygon completely in Regent's Park
     */
    private static final String HANOVER_ISLAND = "Hanover Island";
    /**
     * Polygon partially in Regent's Park
     */
    private static final String LONDON_CENTRAL_MOSQUE = "London Central Mosque";
    /**
     * Polygon partially in Regent's Park
     */
    private static final String CUMBERLAND_TURN_BASIN = "Cumberland Turn Basin";

    private FeatureCollection locations;

    @BeforeEach
    public void loadJSON() throws Exception {
        locations = JsonMapper.builder().build()
                .readValue(getClass().getResource("/geojson/london-parks-landmarks.json"), FeatureCollection.class);
    }

    private Feature getFeature(String id) {
        return locations.features().stream().filter(f -> id.equals(f.id())).findFirst().get();
    }

    private MultiPoint getMultiPoint(String pointA, String pointB) {
        Point a = (Point) getFeature(pointA).geometry();
        Point b = (Point) getFeature(pointB).geometry();
        MultiPoint mp = new MultiPoint(List.of(a.coordinates(), b.coordinates()), null, null);
        return mp;
    }

    private MultiLineString getMultiLine(String lineA, String lineB) {
        LineString a = (LineString) getFeature(lineA).geometry();
        LineString b = (LineString) getFeature(lineB).geometry();
        MultiLineString ml = new MultiLineString(List.of(a.coordinates(), b.coordinates()), null, null);
        return ml;
    }

    private MultiPolygon getMultiPolygon(String polyA, String polyB) {
        Polygon a = (Polygon) getFeature(polyA).geometry();
        Polygon b = (Polygon) getFeature(polyB).geometry();
        MultiPolygon mp = new MultiPolygon(List.of(a.coordinates(), b.coordinates()), null, null);
        return mp;
    }

    /**
     * Test using a polygon as a value
     */
    @Nested
    class PolygonValues {
        /**
         * Albert Memorial is inside Kensington Gardens, Royal Albert Hall is not
         *
         * @param pointId
         * @param inside
         * @param match
         */
        @ParameterizedTest
        @CsvSource(value = {
                /* Within */ ALBERT_MEMORIAL + ",true,WITHIN", ROYAL_ALBERT_HALL + ",false,WITHIN",
                /* Contains */ ALBERT_MEMORIAL + ",false,CONTAINS", ROYAL_ALBERT_HALL + ",false,CONTAINS",
                /* Intersects */ ALBERT_MEMORIAL + ",true,INTERSECTS", ROYAL_ALBERT_HALL + ",false,INTERSECTS",
                /* Disjoint */ ALBERT_MEMORIAL + ",false,DISJOINT", ROYAL_ALBERT_HALL + ",true,DISJOINT"})
        void point(String pointId, boolean inside, MatchType match) {
            Polygon poly = (Polygon) getFeature(KENSINGTON_GARDENS).geometry();
            Point point = (Point) getFeature(pointId).geometry();

            LocationSelection ls = new LocationSelection(poly, null, false, match);

            assertEquals(inside, new LocationSelectionCriterion(ls).locationFilter().test(point));
        }

        /**
         * Budge's Walk extends through the boundary of Kensington Gardens,
         * Lancaster Walk is inside Kensington Gardens, The Broadwalk is in a different park.
         * @param pointId
         * @param inside
         * @param match
         */
        @ParameterizedTest
        @CsvSource(value = {
                /* Within */ BUDGE_S_WALK + ",false,WITHIN", LANCASTER_WALK + ",true,WITHIN", THE_BROADWALK + ",false,WITHIN",
                /* Contains */ BUDGE_S_WALK + ",false,CONTAINS", LANCASTER_WALK + ",false,CONTAINS",  THE_BROADWALK + ",false,CONTAINS",
                /* Intersects */ BUDGE_S_WALK + ",true,INTERSECTS", LANCASTER_WALK + ",true,INTERSECTS", THE_BROADWALK + ",false,INTERSECTS",
                /* Disjoint */ BUDGE_S_WALK + ",false,DISJOINT", LANCASTER_WALK + ",false,DISJOINT", THE_BROADWALK + ",true,DISJOINT"})
        void line(String lineId, boolean inside, MatchType match) {
            Polygon poly = (Polygon) getFeature(KENSINGTON_GARDENS).geometry();
            LineString line = (LineString) getFeature(lineId).geometry();

            LocationSelection ls = new LocationSelection(poly, null, false, match);

            assertEquals(inside, new LocationSelectionCriterion(ls).locationFilter().test(line));
        }

        /**
         * Kensington Palace is partly in Kensington Gardens,
         * The Round Pond is inside Kensington Gardens, Bushy Park is outside Kensington Gardens,
         * The Broadwalk is in a different park
         * @param polyId
         * @param inside
         * @param match
         */
        @ParameterizedTest
        @CsvSource(value = {
                /* Within */ KENSINGTON_PALACE + ",false,WITHIN", KENSINGTON_GARDENS_ROUND_POND + ",true,WITHIN", BUSHY_PARK + ",false,WITHIN",
                /* Contains */ KENSINGTON_PALACE + ",false,CONTAINS", KENSINGTON_GARDENS_ROUND_POND + ",false,CONTAINS",  BUSHY_PARK + ",false,CONTAINS",
                /* Intersects */ KENSINGTON_PALACE + ",true,INTERSECTS", KENSINGTON_GARDENS_ROUND_POND + ",true,INTERSECTS", BUSHY_PARK + ",false,INTERSECTS",
                /* Disjoint */ KENSINGTON_PALACE + ",false,DISJOINT", KENSINGTON_GARDENS_ROUND_POND + ",false,DISJOINT", BUSHY_PARK + ",true,DISJOINT"})
        void polygon(String polyId, boolean inside, MatchType match) {
            Polygon poly = (Polygon) getFeature(KENSINGTON_GARDENS).geometry();
            Polygon location = (Polygon) getFeature(polyId).geometry();

            LocationSelection ls = new LocationSelection(poly, null, false, match);

            assertEquals(inside, new LocationSelectionCriterion(ls).locationFilter().test(location));
        }

        @Test
        void polygonContainsTrue() {
            Polygon hyde = (Polygon) getFeature(HYDE_PARK).geometry();
            Polygon serpentine = (Polygon) getFeature(SERPENTINE).geometry();

            LocationSelection ls = new LocationSelection(serpentine, null, false, MatchType.CONTAINS);

            assertTrue(new LocationSelectionCriterion(ls).locationFilter().test(hyde));
        }

        /**
         * Serpentine Island is inside Hyde Park, Serpentine Boathouse is inside Hyde Park, Royal Albert Hall is not,
         * Greenwich Observatory is Not
         *
         * @param pointId
         * @param inside
         * @param match
         */
        @ParameterizedTest
        @CsvSource(value = {
                /* Within */
                SERPENTINE_ISLAND + "," + SERPENTINE_BOATHOUSE + ",true,WITHIN",
                SERPENTINE_ISLAND + "," + ROYAL_ALBERT_HALL + ",false,WITHIN",
                ROYAL_ALBERT_HALL+ "," + GREENWICH_OBSERVATORY + ",false,WITHIN",
                /* Contains */
                SERPENTINE_ISLAND + "," + SERPENTINE_BOATHOUSE + ",false,CONTAINS",
                SERPENTINE_ISLAND + "," + ROYAL_ALBERT_HALL + ",false,CONTAINS",
                ROYAL_ALBERT_HALL+ "," + GREENWICH_OBSERVATORY + ",false,CONTAINS",
                /* Intersects */
                SERPENTINE_ISLAND + "," + SERPENTINE_BOATHOUSE + ",true,INTERSECTS",
                SERPENTINE_ISLAND + "," + ROYAL_ALBERT_HALL + ",true,INTERSECTS",
                ROYAL_ALBERT_HALL+ "," + GREENWICH_OBSERVATORY + ",false,INTERSECTS",
                /* Disjoint */
                SERPENTINE_ISLAND + "," + SERPENTINE_BOATHOUSE + ",false,DISJOINT",
                SERPENTINE_ISLAND + "," + ROYAL_ALBERT_HALL + ",false,DISJOINT",
                ROYAL_ALBERT_HALL+ "," + GREENWICH_OBSERVATORY + ",true,DISJOINT"})
        void multiPoint(String pointA, String pointB, boolean inside, MatchType match) {
            Polygon poly = (Polygon) getFeature(HYDE_PARK).geometry();
            MultiPoint mp = getMultiPoint(pointA, pointB);

            LocationSelection ls = new LocationSelection(poly, null, false, match);

            assertEquals(inside, new LocationSelectionCriterion(ls).locationFilter().test(mp));
        }

        /**
         * Budge's Walk and South Carriage Drive extend through the boundary of Kensington Gardens,
         * Lancaster Walk and Jubilee Walk are inside Kensington Gardens,
         * The Broadwalk and Serpentine Bridge are in different parks
         *
         * @param pointId
         * @param inside
         * @param match
         */
        @ParameterizedTest
        @CsvSource(value = {
                /* Within */
                LANCASTER_WALK + "," + JUBILEE_WALK + ",true,WITHIN",
                LANCASTER_WALK + "," + BUDGE_S_WALK + ",false,WITHIN",
                LANCASTER_WALK + "," + SERPENTINE_BRIDGE + ",false,WITHIN",
                SOUTH_CARRIAGE_DRIVE + "," + JUBILEE_WALK + ",false,WITHIN",
                SOUTH_CARRIAGE_DRIVE + "," + BUDGE_S_WALK + ",false,WITHIN",
                SOUTH_CARRIAGE_DRIVE + "," + SERPENTINE_BRIDGE + ",false,WITHIN",
                THE_BROADWALK + "," + JUBILEE_WALK + ",false,WITHIN",
                THE_BROADWALK + "," + BUDGE_S_WALK + ",false,WITHIN",
                THE_BROADWALK + "," + SERPENTINE_BRIDGE + ",false,WITHIN",
                /* Contains */
                LANCASTER_WALK + "," + JUBILEE_WALK + ",false,CONTAINS",
                LANCASTER_WALK + "," + BUDGE_S_WALK + ",false,CONTAINS",
                LANCASTER_WALK + "," + SERPENTINE_BRIDGE + ",false,CONTAINS",
                SOUTH_CARRIAGE_DRIVE + "," + JUBILEE_WALK + ",false,CONTAINS",
                SOUTH_CARRIAGE_DRIVE + "," + BUDGE_S_WALK + ",false,CONTAINS",
                SOUTH_CARRIAGE_DRIVE + "," + SERPENTINE_BRIDGE + ",false,CONTAINS",
                THE_BROADWALK + "," + JUBILEE_WALK + ",false,CONTAINS",
                THE_BROADWALK + "," + BUDGE_S_WALK + ",false,CONTAINS",
                THE_BROADWALK + "," + SERPENTINE_BRIDGE + ",false,CONTAINS",
                /* Intersects */
                LANCASTER_WALK + "," + JUBILEE_WALK + ",true,INTERSECTS",
                LANCASTER_WALK + "," + BUDGE_S_WALK + ",true,INTERSECTS",
                LANCASTER_WALK + "," + SERPENTINE_BRIDGE + ",true,INTERSECTS",
                SOUTH_CARRIAGE_DRIVE + "," + JUBILEE_WALK + ",true,INTERSECTS",
                SOUTH_CARRIAGE_DRIVE + "," + BUDGE_S_WALK + ",true,INTERSECTS",
                SOUTH_CARRIAGE_DRIVE + "," + SERPENTINE_BRIDGE + ",true,INTERSECTS",
                THE_BROADWALK + "," + JUBILEE_WALK + ",true,INTERSECTS",
                THE_BROADWALK + "," + BUDGE_S_WALK + ",true,INTERSECTS",
                THE_BROADWALK + "," + SERPENTINE_BRIDGE + ",false,INTERSECTS",
                /* Disjoint */
                LANCASTER_WALK + "," + JUBILEE_WALK + ",false,DISJOINT",
                LANCASTER_WALK + "," + BUDGE_S_WALK + ",false,DISJOINT",
                LANCASTER_WALK + "," + SERPENTINE_BRIDGE + ",false,DISJOINT",
                SOUTH_CARRIAGE_DRIVE + "," + JUBILEE_WALK + ",false,DISJOINT",
                SOUTH_CARRIAGE_DRIVE + "," + BUDGE_S_WALK + ",false,DISJOINT",
                SOUTH_CARRIAGE_DRIVE + "," + SERPENTINE_BRIDGE + ",false,DISJOINT",
                THE_BROADWALK + "," + JUBILEE_WALK + ",false,DISJOINT",
                THE_BROADWALK + "," + BUDGE_S_WALK + ",false,DISJOINT",
                THE_BROADWALK + "," + SERPENTINE_BRIDGE + ",true,DISJOINT"})
        void multiLine(String lineA, String lineB, boolean inside, MatchType match) {
            Polygon poly = (Polygon) getFeature(KENSINGTON_GARDENS).geometry();
            MultiLineString ml = getMultiLine(lineA, lineB);

            LocationSelection ls = new LocationSelection(poly, null, false, match);

            assertEquals(inside, new LocationSelectionCriterion(ls).locationFilter().test(ml));
        }

        /**
         * London Central Mosque and Cumberland Turn Basin extend through the boundary of Regent's Park,
         * London Zoo and Hanover Island are inside Regent's Park,
         * The Round Pond and Serpentine are in different parks
         *
         * @param pointId
         * @param inside
         * @param match
         */
        @ParameterizedTest
        @CsvSource(value = {
                /* Within */
                LONDON_ZOO + "," + HANOVER_ISLAND + ",true,WITHIN",
                LONDON_ZOO + "," + CUMBERLAND_TURN_BASIN + ",false,WITHIN",
                LONDON_ZOO + "," + SERPENTINE + ",false,WITHIN",
                LONDON_CENTRAL_MOSQUE + "," + HANOVER_ISLAND + ",false,WITHIN",
                LONDON_CENTRAL_MOSQUE + "," + CUMBERLAND_TURN_BASIN + ",false,WITHIN",
                LONDON_CENTRAL_MOSQUE + "," + SERPENTINE + ",false,WITHIN",
                KENSINGTON_GARDENS_ROUND_POND + "," + HANOVER_ISLAND + ",false,WITHIN",
                KENSINGTON_GARDENS_ROUND_POND + "," + CUMBERLAND_TURN_BASIN + ",false,WITHIN",
                KENSINGTON_GARDENS_ROUND_POND + "," + SERPENTINE + ",false,WITHIN",
                /* Contains */
                LONDON_ZOO + "," + HANOVER_ISLAND + ",false,CONTAINS",
                LONDON_ZOO + "," + CUMBERLAND_TURN_BASIN + ",false,CONTAINS",
                LONDON_ZOO + "," + SERPENTINE + ",false,CONTAINS",
                LONDON_CENTRAL_MOSQUE + "," + HANOVER_ISLAND + ",false,CONTAINS",
                LONDON_CENTRAL_MOSQUE + "," + CUMBERLAND_TURN_BASIN + ",false,CONTAINS",
                LONDON_CENTRAL_MOSQUE + "," + SERPENTINE + ",false,CONTAINS",
                KENSINGTON_GARDENS_ROUND_POND + "," + HANOVER_ISLAND + ",false,CONTAINS",
                KENSINGTON_GARDENS_ROUND_POND + "," + CUMBERLAND_TURN_BASIN + ",false,CONTAINS",
                KENSINGTON_GARDENS_ROUND_POND + "," + SERPENTINE + ",false,CONTAINS",
                /* Intersects */
                LONDON_ZOO + "," + HANOVER_ISLAND + ",true,INTERSECTS",
                LONDON_ZOO + "," + CUMBERLAND_TURN_BASIN + ",true,INTERSECTS",
                LONDON_ZOO + "," + SERPENTINE + ",true,INTERSECTS",
                LONDON_CENTRAL_MOSQUE + "," + HANOVER_ISLAND + ",true,INTERSECTS",
                LONDON_CENTRAL_MOSQUE + "," + CUMBERLAND_TURN_BASIN + ",true,INTERSECTS",
                LONDON_CENTRAL_MOSQUE + "," + SERPENTINE + ",true,INTERSECTS",
                KENSINGTON_GARDENS_ROUND_POND + "," + HANOVER_ISLAND + ",true,INTERSECTS",
                KENSINGTON_GARDENS_ROUND_POND + "," + CUMBERLAND_TURN_BASIN + ",true,INTERSECTS",
                KENSINGTON_GARDENS_ROUND_POND + "," + SERPENTINE + ",false,INTERSECTS",
                /* Disjoint */
                LONDON_ZOO + "," + HANOVER_ISLAND + ",false,DISJOINT",
                LONDON_ZOO + "," + CUMBERLAND_TURN_BASIN + ",false,DISJOINT",
                LONDON_ZOO + "," + SERPENTINE + ",false,DISJOINT",
                LONDON_CENTRAL_MOSQUE + "," + HANOVER_ISLAND + ",false,DISJOINT",
                LONDON_CENTRAL_MOSQUE + "," + CUMBERLAND_TURN_BASIN + ",false,DISJOINT",
                LONDON_CENTRAL_MOSQUE + "," + SERPENTINE + ",false,DISJOINT",
                KENSINGTON_GARDENS_ROUND_POND + "," + HANOVER_ISLAND + ",false,DISJOINT",
                KENSINGTON_GARDENS_ROUND_POND + "," + CUMBERLAND_TURN_BASIN + ",false,DISJOINT",
                KENSINGTON_GARDENS_ROUND_POND + "," + SERPENTINE + ",true,DISJOINT"})
        void multiPolygon(String polyA, String polyB, boolean inside, MatchType match) {
            Polygon poly = (Polygon) getFeature(REGENT_S_PARK).geometry();
            MultiPolygon mp = getMultiPolygon(polyA, polyB);

            LocationSelection ls = new LocationSelection(poly, null, false, match);

            assertEquals(inside, new LocationSelectionCriterion(ls).locationFilter().test(mp));
        }

        /**
         * Serpentine Island is a point inside Hyde Park, Serpentine is a polygon inside Hyde Park,
         * Royal Albert Hall is a point not in Hyde Park, London Zoo is a polgon not in hyde park.
         *
         * @param pointId
         * @param inside
         * @param match
         */
        @ParameterizedTest
        @CsvSource(value = {
                /* Within */
                SERPENTINE_ISLAND + "," + SERPENTINE + ",true,WITHIN",
                SERPENTINE_ISLAND + "," + LONDON_ZOO + ",false,WITHIN",
                ROYAL_ALBERT_HALL+ "," + LONDON_ZOO + ",false,WITHIN",
                /* Contains */
                SERPENTINE_ISLAND + "," + SERPENTINE + ",false,CONTAINS",
                SERPENTINE_ISLAND + "," + LONDON_ZOO + ",false,CONTAINS",
                ROYAL_ALBERT_HALL+ "," + LONDON_ZOO + ",false,CONTAINS",
                /* Intersects */
                SERPENTINE_ISLAND + "," + SERPENTINE + ",true,INTERSECTS",
                SERPENTINE_ISLAND + "," + LONDON_ZOO + ",true,INTERSECTS",
                ROYAL_ALBERT_HALL+ "," + LONDON_ZOO + ",false,INTERSECTS",
                /* Disjoint */
                SERPENTINE_ISLAND + "," + SERPENTINE + ",false,DISJOINT",
                SERPENTINE_ISLAND + "," + LONDON_ZOO + ",false,DISJOINT",
                ROYAL_ALBERT_HALL+ "," + LONDON_ZOO + ",true,DISJOINT"})
        void geometryCollection(String geomA, String geomB, boolean inside, MatchType match) {
            Polygon poly = (Polygon) getFeature(HYDE_PARK).geometry();
            Geometry a = getFeature(geomA).geometry();
            Geometry b = getFeature(geomB).geometry();
            GeometryCollection gc = new GeometryCollection(List.of(a, b), null, null);

            LocationSelection ls = new LocationSelection(poly, null, false, match);

            assertEquals(inside, new LocationSelectionCriterion(ls).locationFilter().test(gc));
        }
    }

    /**
     * Test using a MultiPolygon as a value
     */
    @Nested
    class MultiPolygonValues {
        /**
         * Albert Memorial is inside Kensington Gardens,
         * Serpentine Boathouse is in Hyde Park, Royal Albert Hall is in neither
         *
         * @param pointId
         * @param inside
         * @param match
         */
        @ParameterizedTest
        @CsvSource(value = {
                /* Within */ ALBERT_MEMORIAL + ",true,WITHIN", SERPENTINE_BOATHOUSE + ",true,WITHIN", ROYAL_ALBERT_HALL + ",false,WITHIN",
                /* Contains */ ALBERT_MEMORIAL + ",false,CONTAINS", SERPENTINE_BOATHOUSE + ",false,CONTAINS", ROYAL_ALBERT_HALL + ",false,CONTAINS",
                /* Intersects */ ALBERT_MEMORIAL + ",true,INTERSECTS", SERPENTINE_BOATHOUSE + ",true,INTERSECTS", ROYAL_ALBERT_HALL + ",false,INTERSECTS",
                /* Disjoint */ ALBERT_MEMORIAL + ",false,DISJOINT", SERPENTINE_BOATHOUSE + ",false,DISJOINT", ROYAL_ALBERT_HALL + ",true,DISJOINT"})
        void point(String pointId, boolean inside, MatchType match) {
            MultiPolygon mp = getMultiPolygon(KENSINGTON_GARDENS, HYDE_PARK);
            Point point = (Point) getFeature(pointId).geometry();

            LocationSelection ls = new LocationSelection(mp, null, false, match);

            assertEquals(inside, new LocationSelectionCriterion(ls).locationFilter().test(point));
        }

        /**
         * Budge's Walk extends through the boundary of Kensington Gardens and Hyde Park,
         * Lancaster Walk is inside Kensington Gardens, Serpentine Bridge is in Hyde Park,
         * The Broadwalk is in a different park.
         * @param pointId
         * @param inside
         * @param match
         */
        @ParameterizedTest
        @CsvSource(value = {
                /* Within */ BUDGE_S_WALK + ",false,WITHIN", LANCASTER_WALK + ",true,WITHIN", SERPENTINE_BRIDGE + ",true,WITHIN", THE_BROADWALK + ",false,WITHIN",
                /* Contains */ BUDGE_S_WALK + ",false,CONTAINS", LANCASTER_WALK + ",false,CONTAINS", SERPENTINE_BRIDGE + ",false,CONTAINS", THE_BROADWALK + ",false,CONTAINS",
                /* Intersects */ BUDGE_S_WALK + ",true,INTERSECTS", LANCASTER_WALK + ",true,INTERSECTS", SERPENTINE_BRIDGE + ",true,INTERSECTS", THE_BROADWALK + ",false,INTERSECTS",
                /* Disjoint */ BUDGE_S_WALK + ",false,DISJOINT", LANCASTER_WALK + ",false,DISJOINT", SERPENTINE_BRIDGE + ",false,DISJOINT", THE_BROADWALK + ",true,DISJOINT"})
        void line(String lineId, boolean inside, MatchType match) {
            MultiPolygon mp = getMultiPolygon(KENSINGTON_GARDENS, HYDE_PARK);
            LineString line = (LineString) getFeature(lineId).geometry();

            LocationSelection ls = new LocationSelection(mp, null, false, match);

            assertEquals(inside, new LocationSelectionCriterion(ls).locationFilter().test(line));
        }

        /**
         * Kensington Palace is partly in Kensington Gardens, Central Mosque is partly in Regent's Park
         * The Round Pond is inside Kensington Gardens, London Zoo is inside Regent's Park
         * Bushy Park is a different park
         * @param polyId
         * @param inside
         * @param match
         */
        @ParameterizedTest
        @CsvSource(value = {
                /* Within */
                KENSINGTON_PALACE + ",false,WITHIN",
                LONDON_CENTRAL_MOSQUE + ",false,WITHIN",
                KENSINGTON_GARDENS_ROUND_POND + ",true,WITHIN",
                LONDON_ZOO + ",true,WITHIN",
                BUSHY_PARK + ",false,WITHIN",
                /* Contains */
                KENSINGTON_PALACE + ",false,CONTAINS",
                LONDON_CENTRAL_MOSQUE + ",false,CONTAINS",
                KENSINGTON_GARDENS_ROUND_POND + ",false,CONTAINS",
                LONDON_ZOO + ",false,CONTAINS",
                BUSHY_PARK + ",false,CONTAINS",
                /* Intersects */ KENSINGTON_PALACE + ",true,INTERSECTS", KENSINGTON_GARDENS_ROUND_POND + ",true,INTERSECTS", BUSHY_PARK + ",false,INTERSECTS",
                /* Disjoint */ KENSINGTON_PALACE + ",false,DISJOINT", KENSINGTON_GARDENS_ROUND_POND + ",false,DISJOINT", BUSHY_PARK + ",true,DISJOINT"})
        void polygon(String polyId, boolean inside, MatchType match) {
            MultiPolygon mp = getMultiPolygon(KENSINGTON_GARDENS, REGENT_S_PARK);
            Polygon location = (Polygon) getFeature(polyId).geometry();

            LocationSelection ls = new LocationSelection(mp, null, false, match);

            assertEquals(inside, new LocationSelectionCriterion(ls).locationFilter().test(location));
        }

        @Test
        void polygonContainsTrue() {
            MultiPolygon mp = getMultiPolygon(RICHMOND_PARK, HYDE_PARK);
            Polygon serpentine = (Polygon) getFeature(SERPENTINE).geometry();

            LocationSelection ls = new LocationSelection(serpentine, null, false, MatchType.CONTAINS);

            assertTrue(new LocationSelectionCriterion(ls).locationFilter().test(mp));
        }
    }

    @Nested
    class EmptyValues {
        private static final Point EMPTY_POINT = new Point(Coordinates.EMPTY, null, null);
        private static final LineString EMPTY_LINE = new LineString(List.of(), null, null);
        private static final Polygon EMPTY_POLY = new Polygon(List.of(List.of()), null, null);

        @Test
        void polygonAndEmptyPoint() {
            Polygon serpentine = (Polygon) getFeature(SERPENTINE).geometry();

            LocationSelection ls = new LocationSelection(serpentine, null, false, MatchType.CONTAINS);
            assertFalse(new LocationSelectionCriterion(ls).locationFilter().test(EMPTY_POINT));
            ls = new LocationSelection(serpentine, null, false, MatchType.WITHIN);
            assertFalse(new LocationSelectionCriterion(ls).locationFilter().test(EMPTY_POINT));
            ls = new LocationSelection(serpentine, null, false, MatchType.INTERSECTS);
            assertFalse(new LocationSelectionCriterion(ls).locationFilter().test(EMPTY_POINT));
            ls = new LocationSelection(serpentine, null, false, MatchType.DISJOINT);
            assertFalse(new LocationSelectionCriterion(ls).locationFilter().test(EMPTY_POINT));
        }

        @Test
        void polygonAndEmptyLine() {
            Polygon serpentine = (Polygon) getFeature(SERPENTINE).geometry();

            LocationSelection ls = new LocationSelection(serpentine, null, false, MatchType.CONTAINS);
            assertFalse(new LocationSelectionCriterion(ls).locationFilter().test(EMPTY_LINE));
            ls = new LocationSelection(serpentine, null, false, MatchType.WITHIN);
            assertFalse(new LocationSelectionCriterion(ls).locationFilter().test(EMPTY_LINE));
            ls = new LocationSelection(serpentine, null, false, MatchType.INTERSECTS);
            assertFalse(new LocationSelectionCriterion(ls).locationFilter().test(EMPTY_LINE));
            ls = new LocationSelection(serpentine, null, false, MatchType.DISJOINT);
            assertFalse(new LocationSelectionCriterion(ls).locationFilter().test(EMPTY_LINE));
        }

        @Test
        void polygonAndEmptyPoly() {
            Polygon serpentine = (Polygon) getFeature(SERPENTINE).geometry();

            LocationSelection ls = new LocationSelection(serpentine, null, false, MatchType.CONTAINS);
            assertFalse(new LocationSelectionCriterion(ls).locationFilter().test(EMPTY_POLY));
            ls = new LocationSelection(serpentine, null, false, MatchType.WITHIN);
            assertFalse(new LocationSelectionCriterion(ls).locationFilter().test(EMPTY_POLY));
            ls = new LocationSelection(serpentine, null, false, MatchType.INTERSECTS);
            assertFalse(new LocationSelectionCriterion(ls).locationFilter().test(EMPTY_POLY));
            ls = new LocationSelection(serpentine, null, false, MatchType.DISJOINT);
            assertFalse(new LocationSelectionCriterion(ls).locationFilter().test(EMPTY_POLY));
        }
    }
}

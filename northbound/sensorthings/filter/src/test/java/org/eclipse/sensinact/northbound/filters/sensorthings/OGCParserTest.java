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
package org.eclipse.sensinact.northbound.filters.sensorthings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.gateway.geojson.Polygon;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterLexer;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.ODataFilterParser.BoolcommonexprContext;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.BoolCommonExprVisitor;
import org.eclipse.sensinact.northbound.filters.sensorthings.antlr.impl.ResourceValueFilterInputHolder;
import org.junit.jupiter.api.Test;

public class OGCParserTest {

    private void assertQuery(final boolean expected, final String query,
            final ResourceValueFilterInputHolder testProvider) throws Exception {
        CodePointCharStream inStream = CharStreams.fromString(query);
        ODataFilterLexer markupLexer = new ODataFilterLexer(inStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(markupLexer);
        ODataFilterParser parser = new ODataFilterParser(commonTokenStream);
        BoolcommonexprContext context = parser.boolcommonexpr();

        try {
            BoolCommonExprVisitor visitor = new BoolCommonExprVisitor(parser);
            final Predicate<ResourceValueFilterInputHolder> predicate = visitor.visit(context);
            if (predicate != null) {
                assertEquals(expected, predicate.test(testProvider),
                        String.format("Expected %s for query: %s", expected, query));
            } else {
                fail("Coudln't parse '" + query + "'");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println(context.toStringTree(parser));
            throw e;
        }
    }

    private void assertQueries(final Map<String, Boolean> expectations, final ResourceValueFilterInputHolder testObject)
            throws Exception {
        for (Entry<String, Boolean> expectation : expectations.entrySet()) {
            assertQuery(expectation.getValue(), expectation.getKey(), testObject);
        }
    }

    private void assertQueries(final Map<String, Boolean> expectations) throws Exception {
        assertQueries(expectations, null);
    }

    private ResourceSnapshot makeLocatedResource(double[] lonlat) {
        Point location = new Point(new Coordinates(lonlat[0], lonlat[1]), null, null);
        return makeLocatedResource(location);
    }

    private ResourceSnapshot makeLocatedResource(GeoJsonObject location) {
        ProviderSnapshot provider = RcUtils.makeProvider("testProvider");
        ServiceSnapshot svc = RcUtils.addService(provider, "admin");
        ResourceSnapshot rc = RcUtils.addResource(svc, "location", location);
        return rc;
    }

    @Test
    void testGeography() throws Exception {
        final Map<String, Boolean> expectations = new LinkedHashMap<>();
        // Length
        expectations.put("floor(geo.length(geography'LINESTRING (30 10, 10 30, 40 40)')) eq 5973069", true);
        expectations.put("geo.length(geography'LINESTRING (5.69773 45.12477, 5.72047 45.19225)') gt 7000", true);
        expectations.put("geo.length(geography'LINESTRING (-0.4478 51.4649, 0.05523 51.5052)') lt 36000.0", true);
        // Distance
        expectations.put("geo.distance(geography'POINT(-0.4478 51.4649)', geography'POINT(0.05523 51.5052)') lt 36000",
                true);

        assertQueries(expectations);

        // Compare SensiNact GeoJSON location
        double[] outOfCircle = { 4.95444599, 47.1763052 };
        double[] inCircle = { 4.9544520269, 47.176310264 };
        ResourceSnapshot rc = makeLocatedResource(inCircle);
        ResourceValueFilterInputHolder holder = new ResourceValueFilterInputHolder(EFilterContext.THINGS,
                RcUtils.getSession(), rc.getService().getProvider(), List.of(rc));
        assertQuery(true, "geo.distance(Locations/location, geography'POINT(4.954450501 47.17631149)') lt 0.3", holder);

        rc = makeLocatedResource(outOfCircle);
        holder = new ResourceValueFilterInputHolder(EFilterContext.THINGS, RcUtils.getSession(),
                rc.getService().getProvider(), List.of(rc));
        assertQuery(true, "geo.distance(Locations/location, geography'POINT(4.954450501 47.17631149)') lt 0.3", holder);
    }

    private Coordinates makeCoors(double lon, double lat) {
        return new Coordinates(lon, lat);
    }

    @Test
    void testSpatial() throws Exception {
        final String point1 = "geography'POINT (30 10)'";
        final String point2 = "geography'POINT (50 10)'";
        final Polygon rect1 = new Polygon(List
                .of(List.of(makeCoors(0, 0), makeCoors(50, 0), makeCoors(50, 50), makeCoors(00, 50), makeCoors(0, 0))),
                null, null);

        ResourceSnapshot rc = makeLocatedResource(rect1);
        ResourceValueFilterInputHolder holder = new ResourceValueFilterInputHolder(EFilterContext.THINGS,
                RcUtils.getSession(), rc.getService().getProvider(), List.of(rc));

        final Map<String, Boolean> expectations = new LinkedHashMap<>();
        expectations.put(String.format("st_equals(%s, %s)", point1, point1), true);
        expectations.put(String.format("st_equals(%s, %s)", point1, point2), false);
        expectations.put(String.format("st_within(%s, %s)", point1, point1), false);
        expectations.put(String.format("st_relate(%s, %s, 'WITHIN')", point1, point1), false);
        expectations.put(String.format("st_relate(%s, %s, 'INTERSECTS')", point1, point1), true);
        expectations.put(String.format("st_contains(Locations/location, %s)", point1), true);
        expectations.put(String.format("st_within(%s, Locations/location)", point1), true);
        assertQueries(expectations, holder);
    }

    @Test
    void testNumbersComparison() throws Exception {

        final Map<String, Boolean> expectations = new LinkedHashMap<>();
        // Equal
        expectations.put("1 eq 1", true);
        expectations.put("1 eq 2", false);
        expectations.put("1 eq -1", false);
        // Not equal
        expectations.put("1 ne 1", false);
        expectations.put("1 ne 2", true);
        // Less or equal
        expectations.put("1 le 1", true);
        expectations.put("1 le 2", true);
        expectations.put("2 le 1", false);
        // Less than
        expectations.put("1 lt 1", false);
        expectations.put("1 lt 2", true);
        expectations.put("2 lt 1", false);
        // Greater or equal
        expectations.put("1 ge 1", true);
        expectations.put("1 ge 2", false);
        expectations.put("2 ge 1", true);
        // Greater than
        expectations.put("1 gt 1", false);
        expectations.put("1 gt 2", false);
        expectations.put("2 gt 1", true);

        // Complex
        expectations.put("42 ge 21 and 45 ge 42", true);
        expectations.put("42 ge 21 and 21 ge 42", false);
        expectations.put("42 ge 21 or 21 ge 42", true);
        assertQueries(expectations);
    }

    @Test
    void testNumbersOperations() throws Exception {
        final Map<String, Boolean> expectations = new LinkedHashMap<>();
        expectations.put("10 add 3 eq 13", true);
        expectations.put("10 add 4 ge 13", true);
        expectations.put("10 sub 3 eq 7", true);
        expectations.put("10 mul 3 eq 30", true);
        expectations.put("10 div 2 eq 5", true);
        expectations.put("15 div 2 eq 7.5", true);
        expectations.put("12 mod 10 eq 2", true);
        assertQueries(expectations);
    }

    @Test
    void testStringOperations() throws Exception {
        final Map<String, Boolean> expectations = new LinkedHashMap<>();
        expectations.put("length('hello') eq 5", true);
        expectations.put("indexof(' Sensor Things','Sensor') eq 1", true);

        expectations.put("substring('Sensor Things',1) eq 'ensor Things'", true);
        expectations.put("substring('Sensor Things',2,4) eq 'nsor'", true);

        expectations.put("tolower('Sensor Things') eq 'sensor things'", true);
        expectations.put("toupper('Sensor Things') eq 'SENSOR THINGS'", true);

        expectations.put("trim('  Sensor Things  ') eq 'Sensor Things'", true);
        expectations.put("concat(concat('degree', ', '), 'Celsius') eq 'degree, Celsius'", true);

        assertQueries(expectations);
    }

    @Test
    void testStringBoolOps() throws Exception {
        final Map<String, Boolean> expectations = new LinkedHashMap<>();
        expectations.put("startswith('SensorThings', 'Sensor')", true);
        expectations.put("endswith('SensorThings', 'Things')", true);
        expectations.put("contains('SensorThings', 'Things')", true);
        expectations.put("substringof('SensorThings', 'Things')", true);
        assertQueries(expectations);
    }

    @Test
    void testThingsPath() throws Exception {
        final Map<String, Boolean> expectations = new LinkedHashMap<>();
        expectations.put("id eq 'testProvider'", true);
        expectations.put("Datastreams/Observations/FeatureOfInterest/id eq 'testProvider'", true);
        expectations.put("Datastreams/Observations/result gt 10", true);
        expectations.put("Datastreams/Observations/result gt 20", false);
        expectations.put("Datastreams/Observations/result lt 5", false);
        expectations.put("Datastreams/Observations/result le 5", true);

        ProviderSnapshot provider = RcUtils.makeProvider("testProvider");
        ServiceSnapshot svc = RcUtils.addService(provider, "test");
        ResourceSnapshot rc1 = RcUtils.addResource(svc, "value1", 5);
        ResourceSnapshot rc2 = RcUtils.addResource(svc, "value2", 15.2);

        ResourceValueFilterInputHolder holder = new ResourceValueFilterInputHolder(EFilterContext.THINGS,
                RcUtils.getSession(), provider, List.of(rc1, rc2));
        assertQueries(expectations, holder);
    }

    @Test
    void testObservationsPath() throws Exception {
        final Map<String, Boolean> expectations = new LinkedHashMap<>();
        expectations.put("result lt 10.00", true);
        expectations.put("Datastream/id eq 'testProvider~test~value'", true);
        expectations.put("FeatureOfInterest/id eq 'testProvider'", true);

        ProviderSnapshot provider = RcUtils.makeProvider("testProvider");
        ServiceSnapshot svc = RcUtils.addService(provider, "test");
        ResourceSnapshot rc = RcUtils.addResource(svc, "value", 5.0);

        ResourceValueFilterInputHolder holder = new ResourceValueFilterInputHolder(EFilterContext.OBSERVATIONS,
                RcUtils.getSession(), provider, rc);
        assertQueries(expectations, holder);
    }

    @Test
    void testDateComparison() throws Exception {
        final Map<String, Boolean> expectations = new LinkedHashMap<>();
        // Date time offset
        expectations.put("resultTime eq 2023-02-07T15:40:30Z", true);
        expectations.put("resultTime ge 2010-06-01T00:00:00Z", true);
        expectations.put("resultTime gt 2010-06-01T00:00:00Z", true);
        expectations.put("resultTime lt 2023-02-07T16:00:00Z", true);
        expectations.put("resultTime le 2023-02-07T16:00:00Z", true);

        // Date
        expectations.put("date(resultTime) eq 2023-02-07", true);
        expectations.put("date(resultTime) ge 2010-06-01", true);
        expectations.put("date(resultTime) gt 2010-06-01", true);
        expectations.put("date(resultTime) lt 2023-02-08", true);
        expectations.put("date(resultTime) le 2023-02-07", true);

        // Time
        expectations.put("time(resultTime) eq 15:40:30", true);
        expectations.put("time(resultTime) ge 01:00:00", true);
        expectations.put("time(resultTime) gt 01:00:00", true);
        expectations.put("time(resultTime) lt 16:00:00", true);
        expectations.put("time(resultTime) le 16:00:00", true);

        ProviderSnapshot provider = RcUtils.makeProvider("provider");
        ServiceSnapshot svc = RcUtils.addService(provider, "service");
        ResourceSnapshot rc = RcUtils.addResource(svc, "value", 5.0,
                ZonedDateTime.of(2023, 2, 7, 15, 40, 30, 0, ZoneId.of("UTC")).toInstant());

        ResourceValueFilterInputHolder holder = new ResourceValueFilterInputHolder(EFilterContext.OBSERVATIONS,
                RcUtils.getSession(), provider, rc);
        assertQueries(expectations, holder);
    }

    @Test
    void testDuration() throws Exception {
        final Map<String, Boolean> expectations = new LinkedHashMap<>();
        // Date time offset
        expectations.put("2023-02-07T15:40:30Z add duration'PT10M' 2023-02-07T15:50:30Z", true);
        expectations.put("2023-02-07T15:40:30Z sub duration'PT10M' 2023-02-07T15:30:30Z", true);
        expectations.put("2023-02-07T15:40:30Z add duration'-PT10M' 2023-02-07T15:30:30Z", true);
        expectations.put("2023-02-07T15:40:30Z sub duration'-PT10M' 2023-02-07T15:50:30Z", true);
        expectations.put("2023-02-07T15:50:30Z sub 2023-02-07T15:40:00Z eq duration'PT10M30S'", true);

        ProviderSnapshot provider = RcUtils.makeProvider("provider");
        ServiceSnapshot svc = RcUtils.addService(provider, "service");
        ResourceSnapshot rc = RcUtils.addResource(svc, "value", 5.0,
                ZonedDateTime.of(2023, 2, 7, 15, 40, 30, 0, ZoneId.of("UTC")).toInstant());

        ResourceValueFilterInputHolder holder = new ResourceValueFilterInputHolder(EFilterContext.OBSERVATIONS,
                RcUtils.getSession(), provider, rc);
        assertQueries(expectations, holder);
    }

    @Test
    void testThingsComplex() throws Exception {
        final Map<String, Boolean> expectations = new LinkedHashMap<>();
        expectations.put("Datastreams/Observations/FeatureOfInterest/id eq 'FOI_1' "
                + "and Datastreams/Observations/resultTime ge 2010-06-01T00:00:00Z "
                + "and Datastreams/Observations/resultTime le 2010-07-01T00:00:00Z", true);

        ProviderSnapshot provider = RcUtils.makeProvider("FOI_1");
        ServiceSnapshot svc = RcUtils.addService(provider, "sensor");
        ResourceSnapshot rc = RcUtils.addResource(svc, "value", 5.0,
                ZonedDateTime.of(2010, 6, 15, 21, 42, 0, 0, ZoneId.of("UTC")).toInstant());

        ResourceValueFilterInputHolder holder = new ResourceValueFilterInputHolder(EFilterContext.THINGS,
                RcUtils.getSession(), provider, List.of(rc));
        assertQueries(expectations, holder);
    }
}

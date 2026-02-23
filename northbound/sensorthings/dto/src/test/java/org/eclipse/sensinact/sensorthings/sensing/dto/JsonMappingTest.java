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
package org.eclipse.sensinact.sensorthings.sensing.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.sensorthings.sensing.dto.RootResponse.NameUrl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class JsonMappingTest {

    ObjectMapper getObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    File getFile(String path) {
        return new File("src/test/resources/toronto-bike-snapshot.sensorup.com", path);
    }

    @Nested
    class ResourceParsing {

        @Test
        void testRootResponse() throws IOException {
            RootResponse response = getObjectMapper().readValue(getFile("v1.0.json"), RootResponse.class);

            assertEquals(8, response.value().size());

            List<String> expected = List.of("Things", "https://toronto-bike-snapshot.sensorup.com/v1.0/Things",
                    "Locations", "https://toronto-bike-snapshot.sensorup.com/v1.0/Locations", "HistoricalLocations",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/HistoricalLocations", "Datastreams",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams", "Sensors",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Sensors", "Observations",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Observations", "ObservedProperties",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/ObservedProperties", "FeaturesOfInterest",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/FeaturesOfInterest");
            Iterator<String> it = expected.iterator();
            for (NameUrl nu : response.value()) {
                assertEquals(it.next(), nu.name());
                assertEquals(it.next(), nu.url());
            }
        }

        @Test
        void testThings() throws IOException {
            ResultList<Thing> things = getObjectMapper().readValue(getFile("Things.json"),
                    new TypeReference<ResultList<Thing>>() {
                    });

            assertEquals(199, things.count());
            assertEquals("https://toronto-bike-snapshot.sensorup.com/v1.0/Things?$top=100&$skip=100",
                    things.nextLink());
            assertEquals(100, things.value().size());

            assertThing(things.value().get(0), 206047, "https://toronto-bike-snapshot.sensorup.com/v1.0/Things(206047)",
                    "Bloor St / Brunswick Ave Toronto bike share station with data of available bikes and available docks",
                    "7061:Bloor St / Brunswick Ave",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Things(206047)/Datastreams",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Things(206047)/HistoricalLocations",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Things(206047)/Locations");
            assertThing(things.value().get(1), 1581, "https://toronto-bike-snapshot.sensorup.com/v1.0/Things(1581)",
                    "Wellington Dog Park Toronto bike share station with data of available bikes and available docks",
                    "7216:Wellington Dog Park",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Things(1581)/Datastreams",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Things(1581)/HistoricalLocations",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Things(1581)/Locations");
            assertThing(things.value().get(2), 1573, "https://toronto-bike-snapshot.sensorup.com/v1.0/Things(1573)",
                    "Fort York/Garrison Toronto bike share station with data of available bikes and available docks",
                    "7211:Fort York/Garrison",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Things(1573)/Datastreams",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Things(1573)/HistoricalLocations",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Things(1573)/Locations");
        }

        void assertThing(Thing thing, Integer id, String selfLink, String description, String name, String datastreams,
                String historicalLocations, String locations) {
            assertEquals(id, thing.id());
            assertEquals(selfLink, thing.selfLink());
            assertEquals(description, thing.description());
            assertEquals(name, thing.name());
            assertEquals(Map.of(), thing.properties());
            assertEquals(datastreams, thing.datastreamsLink());
            assertEquals(historicalLocations, thing.historicalLocationsLink());
            assertEquals(locations, thing.locationsLink());
        }

        @Test
        void testLocations() throws IOException {
            ResultList<Location> locations = getObjectMapper().readValue(getFile("Locations.json"),
                    new TypeReference<ResultList<Location>>() {
                    });

            assertEquals(199, locations.count());
            assertEquals("https://toronto-bike-snapshot.sensorup.com/v1.0/Locations?$top=100&$skip=100",
                    locations.nextLink());
            assertEquals(100, locations.value().size());

            assertLocation(locations.value().get(0), 206048,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Locations(206048)",
                    "The geographic location with coordinates for the Toronto bike share station Bloor St / Brunswick Ave",
                    "7061:Bloor St / Brunswick Ave", -79.407224, 43.665876,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Locations(206048)/Things",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Locations(206048)/HistoricalLocations");
            assertLocation(locations.value().get(1), 1582,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Locations(1582)",
                    "The geographic location with coordinates for the Toronto bike share station Wellington Dog Park",
                    "7216:Wellington Dog Park", -79.409339, 43.641281,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Locations(1582)/Things",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Locations(1582)/HistoricalLocations");
            assertLocation(locations.value().get(2), 1574,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Locations(1574)",
                    "The geographic location with coordinates for the Toronto bike share station Fort York/Garrison",
                    "7211:Fort York/Garrison", -79.4061111111111, 43.6375,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Locations(1574)/Things",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Locations(1574)/HistoricalLocations");
        }

        void assertLocation(Location location, Integer id, String selfLink, String description, String name,
                double longitude, double latitude, String things, String historicalLocations) {
            assertEquals(id, location.id());
            assertEquals(selfLink, location.selfLink());
            assertEquals(description, location.description());
            assertEquals(name, location.name());
            assertEquals("application/vnd.geo+json", location.encodingType());
            assertInstanceOf(Point.class, location.location());
            assertEquals(longitude, ((Point) location.location()).coordinates().longitude());
            assertEquals(latitude, ((Point) location.location()).coordinates().latitude());
            assertEquals(things, location.thingsLink());
            assertEquals(historicalLocations, location.historicalLocationsLink());
        }

        @Test
        void testHistoricalLocations() throws IOException {
            ResultList<HistoricalLocation> locations = getObjectMapper().readValue(getFile("HistoricalLocations.json"),
                    new TypeReference<ResultList<HistoricalLocation>>() {
                    });

            assertEquals(199, locations.count());
            assertEquals("https://toronto-bike-snapshot.sensorup.com/v1.0/HistoricalLocations?$top=100&$skip=100",
                    locations.nextLink());
            assertEquals(100, locations.value().size());

            assertHistoricalLocation(locations.value().get(0), 206049,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/HistoricalLocations(206049)",
                    "2017-02-04T15:50:10.489Z",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/HistoricalLocations(206049)/Locations",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/HistoricalLocations(206049)/Thing");
            assertHistoricalLocation(locations.value().get(1), 1583,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/HistoricalLocations(1583)",
                    "2017-02-02T20:45:24.966Z",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/HistoricalLocations(1583)/Locations",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/HistoricalLocations(1583)/Thing");
            assertHistoricalLocation(locations.value().get(2), 1575,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/HistoricalLocations(1575)",
                    "2017-02-02T20:45:24.906Z",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/HistoricalLocations(1575)/Locations",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/HistoricalLocations(1575)/Thing");
        }

        void assertHistoricalLocation(HistoricalLocation historicalLocation, Integer id, String selfLink, String time,
                String locations, String thing) {
            assertEquals(id, historicalLocation.id());
            assertEquals(selfLink, historicalLocation.selfLink());
            assertEquals(Instant.parse(time), historicalLocation.time());
            assertEquals(locations, historicalLocation.locationsLink());
            assertEquals(thing, historicalLocation.thingLink());
        }

        @Test
        void testDatastreams() throws IOException {
            ResultList<Datastream> streams = getObjectMapper().readValue(getFile("Datastreams.json"),
                    new TypeReference<ResultList<Datastream>>() {
                    });

            assertEquals(398, streams.count());
            assertEquals("https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams?$top=100&$skip=100",
                    streams.nextLink());
            assertEquals(100, streams.value().size());

            assertDatastream(streams.value().get(0), 206051,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(206051)",
                    "7061:Bloor St / Brunswick Ave:available_docks",
                    "The datastream of available docks count for the Toronto bike share station Bloor St / Brunswick Ave",
                    "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CountObservation", "dock count",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(206051)/Observations",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(206051)/ObservedProperty",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(206051)/Sensor",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(206051)/Thing");
            assertDatastream(streams.value().get(1), 206050,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(206050)",
                    "7061:Bloor St / Brunswick Ave:available_bikes",
                    "The datastream of available bikes count for the Toronto bike share station Bloor St / Brunswick Ave",
                    "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CountObservation", "bike count",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(206050)/Observations",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(206050)/ObservedProperty",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(206050)/Sensor",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(206050)/Thing");
            assertDatastream(streams.value().get(2), 1585,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(1585)",
                    "7216:Wellington Dog Park:available_docks",
                    "The datastream of available docks count for the Toronto bike share station Wellington Dog Park",
                    "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CountObservation", "dock count",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(1585)/Observations",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(1585)/ObservedProperty",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(1585)/Sensor",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(1585)/Thing");
        }

        void assertDatastream(Datastream datastream, Integer id, String selfLink, String name, String description,
                String observationType, String unitName, String observations, String observedProperty, String sensor,
                String thing) {
            assertEquals(id, datastream.id());
            assertEquals(selfLink, datastream.selfLink());
            assertEquals(name, datastream.name());
            assertEquals(description, datastream.description());
            assertEquals(observationType, datastream.observationType());
            assertNull(datastream.observedArea());
            assertEquals("{TOT}", datastream.unitOfMeasurement().symbol());
            assertEquals(unitName, datastream.unitOfMeasurement().name());
            assertEquals("http://unitsofmeasure.org/ucum.html#para-50", datastream.unitOfMeasurement().definition());
            assertEquals(observations, datastream.observationsLink());
            assertEquals(observedProperty, datastream.observedPropertyLink());
            assertEquals(sensor, datastream.sensorLink());
            assertEquals(thing, datastream.thingLink());
        }

        @Test
        void testSensors() throws IOException {
            ResultList<Sensor> streams = getObjectMapper().readValue(getFile("Sensors.json"),
                    new TypeReference<ResultList<Sensor>>() {
                    });

            assertEquals(2, streams.count());
            assertNull(streams.nextLink());
            assertEquals(2, streams.value().size());

            assertSensor(streams.value().get(0), 4, "https://toronto-bike-snapshot.sensorup.com/v1.0/Sensors(4)",
                    "available_docks", "A sensor for tracking how many docks are available in a bike station",
                    "text/plan", "https://member.bikesharetoronto.com/stations",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Sensors(4)/Datastreams");
            assertSensor(streams.value().get(1), 3, "https://toronto-bike-snapshot.sensorup.com/v1.0/Sensors(3)",
                    "available_bikes", "A sensor for tracking how many bikes are available in a bike station",
                    "text/plan", "https://member.bikesharetoronto.com/stations",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Sensors(3)/Datastreams");

        }

        void assertSensor(Sensor datastream, Integer id, String selfLink, String name, String description,
                String encodingType, String metadata, String datastreams) {
            assertEquals(id, datastream.id());
            assertEquals(selfLink, datastream.selfLink());
            assertEquals(name, datastream.name());
            assertEquals(description, datastream.description());
            assertEquals(encodingType, datastream.encodingType());
            assertEquals(metadata, datastream.metadata());
            assertEquals(datastreams, datastream.datastreamsLink());
        }

        @Test
        void testObservations() throws IOException {
            ResultList<Observation> observations = getObjectMapper().readValue(getFile("Observations.json"),
                    new TypeReference<ResultList<Observation>>() {
                    });

            assertEquals(1594349, observations.count());
            assertEquals("https://toronto-bike-snapshot.sensorup.com/v1.0/Observations?$top=100&$skip=100",
                    observations.nextLink());
            assertEquals(100, observations.value().size());

            assertObservation(observations.value().get(0), 1595550,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Observations(1595550)", "2017-02-16T21:55:12.841Z",
                    "7", "https://toronto-bike-snapshot.sensorup.com/v1.0/Observations(1595550)/Datastream",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Observations(1595550)/FeatureOfInterest");
            assertObservation(observations.value().get(1), 1595551,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Observations(1595551)", "2017-02-16T21:55:12.841Z",
                    "4", "https://toronto-bike-snapshot.sensorup.com/v1.0/Observations(1595551)/Datastream",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Observations(1595551)/FeatureOfInterest");
            assertObservation(observations.value().get(2), 1595549,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Observations(1595549)", "2017-02-16T21:55:12.830Z",
                    "8", "https://toronto-bike-snapshot.sensorup.com/v1.0/Observations(1595549)/Datastream",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Observations(1595549)/FeatureOfInterest");
        }

        void assertObservation(Observation observation, Integer id, String selfLink, String time, String result,
                String datastream, String featureOfInterest) {
            assertEquals(id, observation.id());
            assertEquals(selfLink, observation.selfLink());
            assertEquals(Instant.parse(time), observation.phenomenonTime());
            assertEquals(result, observation.result());
            assertNull(observation.resultTime());
            assertNull(observation.resultQuality());
            assertEquals(datastream, observation.datastreamLink());
            assertEquals(featureOfInterest, observation.featureOfInterestLink());
        }

        @Test
        void testObservedProperties() throws IOException {
            ResultList<ObservedProperty> observedProps = getObjectMapper().readValue(getFile("ObservedProperties.json"),
                    new TypeReference<ResultList<ObservedProperty>>() {
                    });

            assertEquals(2, observedProps.count());
            assertNull(observedProps.nextLink());
            assertEquals(2, observedProps.value().size());

            assertObservedProperty(observedProps.value().get(0), 2,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/ObservedProperties(2)", "available_docks",
                    "The total number count of available docks in a bike station",
                    "https://member.bikesharetoronto.com/stations",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/ObservedProperties(2)/Datastreams");
            assertObservedProperty(observedProps.value().get(1), 1,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/ObservedProperties(1)", "available_bikes",
                    "The total number count of available bikes in a bike station",
                    "https://member.bikesharetoronto.com/stations",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/ObservedProperties(1)/Datastreams");
        }

        void assertObservedProperty(ObservedProperty observation, Integer id, String selfLink, String name,
                String description, String definition, String datastreams) {
            assertEquals(id, observation.id());
            assertEquals(selfLink, observation.selfLink());
            assertEquals(name, observation.name());
            assertEquals(description, observation.description());
            assertEquals(definition, observation.definition());
            assertEquals(datastreams, observation.datastreamsLink());
        }

        @Test
        void testFeaturesOfInterest() throws IOException {
            ResultList<FeatureOfInterest> observedProps = getObjectMapper()
                    .readValue(getFile("FeaturesOfInterest.json"), new TypeReference<ResultList<FeatureOfInterest>>() {
                    });

            assertEquals(199, observedProps.count());
            assertEquals("https://toronto-bike-snapshot.sensorup.com/v1.0/FeaturesOfInterest?$top=100&$skip=100",
                    observedProps.nextLink());
            assertEquals(100, observedProps.value().size());

            assertFeatureOfInterest(observedProps.value().get(0), 206052,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/FeaturesOfInterest(206052)",
                    "7061:Bloor St / Brunswick Ave",
                    "Generated using location details: The geographic location with coordinates for the Toronto bike share station Bloor St / Brunswick Ave",
                    -79.407224, 43.665876,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/FeaturesOfInterest(206052)/Observations");
            assertFeatureOfInterest(observedProps.value().get(1), 1586,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/FeaturesOfInterest(1586)",
                    "7216:Wellington Dog Park",
                    "Generated using location details: The geographic location with coordinates for the Toronto bike share station Wellington Dog Park",
                    -79.409339, 43.641281,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/FeaturesOfInterest(1586)/Observations");
            assertFeatureOfInterest(observedProps.value().get(2), 1578,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/FeaturesOfInterest(1578)",
                    "7211:Fort York/Garrison",
                    "Generated using location details: The geographic location with coordinates for the Toronto bike share station Fort York/Garrison",
                    -79.4061111111111, 43.6375,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/FeaturesOfInterest(1578)/Observations");
        }

        void assertFeatureOfInterest(FeatureOfInterest featureOfInterest, Integer id, String selfLink, String name,
                String description, double longitude, double latitude, String observations) {
            assertEquals(id, featureOfInterest.id());
            assertEquals(selfLink, featureOfInterest.selfLink());
            assertEquals(name, featureOfInterest.name());
            assertEquals(description, featureOfInterest.description());
            assertEquals("application/vnd.geo+json", featureOfInterest.encodingType());
            assertInstanceOf(Point.class, featureOfInterest.feature());
            assertEquals(longitude, ((Point) featureOfInterest.feature()).coordinates().longitude());
            assertEquals(latitude, ((Point) featureOfInterest.feature()).coordinates().latitude());
            assertEquals(observations, featureOfInterest.observationsLink());
        }
    }

    @Nested
    class ResourceSerialization {

        @Test
        void testThing() throws Exception {
            Thing thing = new Thing("https://toronto-bike-snapshot.sensorup.com/v1.0/Things(206047)", 206047,
                    "7061:Bloor St / Brunswick Ave",
                    "Bloor St / Brunswick Ave Toronto bike share station with data of available bikes and available docks",
                    Map.of(), "https://toronto-bike-snapshot.sensorup.com/v1.0/Things(206047)/Datastreams",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Things(206047)/HistoricalLocations",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Things(206047)/Locations");

            assertEquals(getObjectMapper().readTree(getFile("Datastreams(206051)/Thing.json")),
                    getObjectMapper().valueToTree(thing));
        }

        @Test
        void testLocation() throws Exception {
            Location location = new Location("https://toronto-bike-snapshot.sensorup.com/v1.0/Locations(206048)",
                    206048, "7061:Bloor St / Brunswick Ave",
                    "The geographic location with coordinates for the Toronto bike share station Bloor St / Brunswick Ave",
                    "application/vnd.geo+json", new Point(new Coordinates(-79.407224, 43.665876), null, null), Map.of(),
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Locations(206048)/Things",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Locations(206048)/HistoricalLocations");

            ResultList<Location> list = new ResultList<>(1, null, List.of(location));

            assertEquals(getObjectMapper().readTree(getFile("HistoricalLocations(206049)/Locations.json")),
                    getObjectMapper().valueToTree(list));
        }

        @Test
        void testHistoricalLocation() throws Exception {
            HistoricalLocation historicalLocation = new HistoricalLocation(
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/HistoricalLocations(206049)", 206049,
                    Instant.parse("2017-02-04T15:50:10.489Z"),
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/HistoricalLocations(206049)/Locations",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/HistoricalLocations(206049)/Thing");

            ResultList<HistoricalLocation> list = new ResultList<>(1, null, List.of(historicalLocation));

            assertEquals(getObjectMapper().readTree(getFile("Locations(206048)/HistoricalLocations.json")),
                    getObjectMapper().valueToTree(list));
        }

        @Test
        void testDatastream() throws Exception {
            Datastream datastream = new Datastream("https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(1512)",
                    1512, "7203:Harrison/Dovercourt:available_bikes",
                    "The datastream of available bikes count for the Toronto bike share station Harrison/Dovercourt",
                    "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_CountObservation",
                    new UnitOfMeasurement("bike count", "{TOT}", "http://unitsofmeasure.org/ucum.html#para-50"), null,
                    null, null, null, "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(1512)/Observations",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(1512)/ObservedProperty",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(1512)/Sensor",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Datastreams(1512)/Thing");

            assertEquals(getObjectMapper().readTree(getFile("Observations(1595550)/Datastream.json")),
                    getObjectMapper().valueToTree(datastream));
        }

        @Test
        void testSensor() throws Exception {
            Sensor sensor = new Sensor("https://toronto-bike-snapshot.sensorup.com/v1.0/Sensors(4)", 4,
                    "available_docks", "A sensor for tracking how many docks are available in a bike station",
                    "text/plan", "https://member.bikesharetoronto.com/stations", null,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Sensors(4)/Datastreams");

            assertEquals(getObjectMapper().readTree(getFile("Datastreams(206051)/Sensor.json")),
                    getObjectMapper().valueToTree(sensor));
        }

        @Test
        void testObservation() throws Exception {
            Observation observation = new Observation(
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Observations(1595550)", 1595550,
                    Instant.parse("2017-02-16T21:55:12.841Z"), null, "7", null, null, null,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Observations(1595550)/Datastream",
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/Observations(1595550)/FeatureOfInterest");

            assertEquals(getObjectMapper().readTree(getFile("Observations(1595550)/root.json")),
                    getObjectMapper().valueToTree(observation));
        }

        @Test
        void testObservedProperty() throws Exception {
            ObservedProperty observedProperty = new ObservedProperty(
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/ObservedProperties(2)", 2, "available_docks",
                    "The total number count of available docks in a bike station",
                    "https://member.bikesharetoronto.com/stations", null,
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/ObservedProperties(2)/Datastreams");

            assertEquals(getObjectMapper().readTree(getFile("Datastreams(206051)/ObservedProperty.json")),
                    getObjectMapper().valueToTree(observedProperty));
        }

        @Test
        void testFeatureOfInterest() throws Exception {
            FeatureOfInterest featureOfInterest = new FeatureOfInterest(
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/FeaturesOfInterest(1514)", 1514,
                    "7203:Harrison/Dovercourt",
                    "Generated using location details: The geographic location with coordinates for the Toronto bike share station Harrison/Dovercourt",
                    "application/vnd.geo+json", new Point(new Coordinates(-79.424557, 43.650978), null, null), Map.of(),
                    "https://toronto-bike-snapshot.sensorup.com/v1.0/FeaturesOfInterest(1514)/Observations");

            assertEquals(getObjectMapper().readTree(getFile("Observations(1595550)/FeatureOfInterest.json")),
                    getObjectMapper().valueToTree(featureOfInterest));
        }
    }
}

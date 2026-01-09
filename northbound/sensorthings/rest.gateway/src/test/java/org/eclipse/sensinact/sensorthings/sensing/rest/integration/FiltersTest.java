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
package org.eclipse.sensinact.sensorthings.sensing.rest.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.RootResponse;
import org.eclipse.sensinact.sensorthings.sensing.dto.RootResponse.NameUrl;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Tests the ref, count, ... filters
 */
public class FiltersTest extends AbstractIntegrationTest {

    final Random random = new Random();

    @Test
    void testCountFilter() throws IOException, InterruptedException {
        // Create providers
        int nbProviders = 4;
        for (int i = 0; i < nbProviders; i++) {
            createResource("countTester_" + (i + 1), "sensor", "rc", random.nextInt());
            createResource("countTester_" + (i + 1), "admin", "location", new Point(Coordinates.EMPTY, null, null));
        }

        final RootResponse rootResponse = utils.queryJson("/", RootResponse.class);
        for (NameUrl url : rootResponse.value()) {
            // No count by default
            ResultList<AnyIdDTO> resultList = utils.queryJson(url.url(), RESULT_ANY);
            assertNull(resultList.count());
            assertNotNull(resultList.value());

            // Explicit no count
            resultList = utils.queryJson(url.url() + "?$count=false", RESULT_ANY);
            assertNull(resultList.count());
            assertNotNull(resultList.value());

            // Explicit count
            resultList = utils.queryJson(url.url() + "?$count=true", RESULT_ANY);
            assertTrue(resultList.value().size() >= nbProviders,
                    "Expected " + resultList.value().size() + " >= " + nbProviders + " at " + url.url());
            assertEquals(resultList.value().size(), resultList.count());

            // Invalid value
            utils.assertURLStatus(url.url() + "?$count=yes", 400);
            utils.assertURLStatus(url.url() + "?$count=no", 400);
            utils.assertURLStatus(url.url() + "?$count=1", 400);
            utils.assertURLStatus(url.url() + "?$count=0", 400);
            utils.assertURLStatus(url.url() + "?$count=", 400);
        }
    }

    private List<String> extractProviderIds(ResultList<AnyIdDTO> resultList, Predicate<String> filter) {
        return resultList.value().stream().map(item -> {
            String rawId = item.id();
            if (rawId == null) {
                return null;
            } else {
                return rawId.split("~")[0];
            }
        }).distinct().filter(filter).collect(Collectors.toList());
    }

    @Test
    void testOrderBy() throws IOException, InterruptedException {
        final String prefix = "orderTester_";
        final List<String> sortedProviderIds = IntStream.rangeClosed(0, 9).boxed().map(id -> prefix + id)
                .sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        final int nbProviders = sortedProviderIds.size();

        final List<String> reversedProviderIds = new ArrayList<>(sortedProviderIds);
        Collections.reverse(reversedProviderIds);

        sortedProviderIds.stream().filter(id -> id.startsWith(prefix)).forEach(id -> {
            String thingId = id + "-thing";
            String locationId = id + "-location";
            createDatastrem(id + "datastreamA", thingId);
            createDatastrem(id + "datastreamA" + 256, thingId);
            createDatastrem(id + "datastreamB", thingId);
            createDatastrem(id + "datastreamB" + 256, thingId);
            createLocation(locationId);
            createThing(thingId, List.of(locationId), List.of(id + "datastreamA", id + "datastreamA" + 256,
                    id + "datastreamB", id + "datastreamB" + 256));

        });

        final Predicate<String> filter = sortedProviderIds::contains;

        final RootResponse rootResponse = utils.queryJson("/", RootResponse.class);
        for (NameUrl url : rootResponse.value()) {
            // Order by ID by default
            ResultList<AnyIdDTO> resultList = utils.queryJson(url.url(), RESULT_ANY);
            assertNull(resultList.count());
            assertEquals(sortedProviderIds, extractProviderIds(resultList, filter));

            // Explicit order by ID
            resultList = utils.queryJson(url.url() + "?$orderby=id", RESULT_ANY);
            assertNull(resultList.count());
            assertEquals(sortedProviderIds, extractProviderIds(resultList, filter));

            // Explicit ascending order by ID
            resultList = utils.queryJson(url.url() + "?$orderby=id%20asc", RESULT_ANY);
            assertNull(resultList.count());
            assertEquals(sortedProviderIds, extractProviderIds(resultList, filter));

            // Order + count
            resultList = utils.queryJson(url.url() + "?$orderby=id&$count=true", RESULT_ANY);
            assertTrue(resultList.count() >= nbProviders);
            assertEquals(sortedProviderIds, extractProviderIds(resultList, filter));

            // Reverse order by ID
            resultList = utils.queryJson(url.url() + "?$orderby=id%20desc", RESULT_ANY);
            assertNull(resultList.count());
            assertEquals(reversedProviderIds, extractProviderIds(resultList, filter));

            // Reverse order by ID, with multiple space characters
            resultList = utils.queryJson(url.url() + "?$orderby=id%20%20%20desc", RESULT_ANY);
            assertNull(resultList.count());
            assertEquals(reversedProviderIds, extractProviderIds(resultList, filter));

            // Reverse order + count
            resultList = utils.queryJson(url.url() + "?$orderby=id%20desc&$count=true", RESULT_ANY);
            assertTrue(resultList.count() >= nbProviders);
            assertEquals(reversedProviderIds, extractProviderIds(resultList, filter));
        }
    }

    @Test
    void testRef() throws IOException, InterruptedException {
        final String provider = "expandTesterThing";
        final String providerDatastream = "expandTesterDatastream";

        createThing(provider, List.of(), List.of(providerDatastream));
        createDatastrem(providerDatastream, provider, 42);

        // Parsing will fail if there is any other JSON property
        ResultList<? extends Self> resultList = utils.queryJson(String.format("/Things(%s)/Datastreams/$ref", provider),
                RESULT_SELF);
        assertNull(resultList.count());

        List<Self> references = resultList.value().stream()
                .filter(s -> s.selfLink().endsWith(String.format("Datastreams(%s)", providerDatastream)))
                .collect(Collectors.toList());
        assertEquals(1, references.size());

        // Add the count
        resultList = utils.queryJson(String.format("/Things(%s)/Datastreams/$ref?$count=true", provider), RESULT_SELF);
        assertEquals(resultList.value().size(), resultList.count());
        references = resultList.value().stream()
                .filter(s -> s.selfLink().endsWith(String.format("Datastreams(%s)", providerDatastream)))
                .collect(Collectors.toList());
        assertEquals(1, references.size());
    }

    @Test
    void testProp() throws IOException, InterruptedException {
        final Instant creationTime = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        final int value = random.nextInt();
        final String provider = "propTester";
        final String providerDatastream = "propTesterDatastream";
        createThing(provider, List.of(), List.of(providerDatastream));
        createDatastrem(providerDatastream, provider, 42);

        ResultList<? extends Self> observations = utils
                .queryJson(String.format("/FeaturesOfInterest(%s)/Observations/$ref", provider), RESULT_SELF);
        String baseUrl = observations.value().stream()
                .filter(s -> s.selfLink().contains(String.join("~", providerDatastream, "test", "test"))).findFirst()
                .get().selfLink();

        String property = "resultTime";
        Map<?, ?> rawResult = utils.queryJson(baseUrl + "/" + property, Map.class);
        assertEquals(Set.of(property), rawResult.keySet());
        assertEquals(creationTime.toString(), rawResult.get(property));

        property = "result";
        rawResult = utils.queryJson(baseUrl + "/" + property, Map.class);
        assertEquals(Set.of(property), rawResult.keySet());
        assertEquals(value, rawResult.get(property));
    }

    @Test
    void testSelect() throws IOException, InterruptedException {
        final String provider = "selectTester";
        final String svc = "sensor";
        final String rc = "rc";
        createResource(provider, svc, rc, 42);

        Set<String> selectedFields = Set.of("result", "resultTime");
        Map<?, ?> rawResultList = utils.queryJson("/Observations/?$select=" + String.join(",", selectedFields),
                Map.class);
        List<?> items = (List<?>) rawResultList.get("value");
        assertFalse(items.isEmpty());
        for (Object rawItem : items) {
            Map<?, ?> item = utils.getMapper().convertValue(rawItem, Map.class);
            assertEquals(selectedFields, item.keySet());
        }
    }

    @Test
    void testSkipTop() throws IOException, InterruptedException {
        // Register the resources
        final String provider = "skipTopFilter";
        final String svc = "sensor";
        final String rcPrefix = "rc";
        final int nbRc = 5;
        for (int i = 0; i < nbRc; i++) {
            createResource(provider, svc, rcPrefix + i, i);
        }

        // List all datastreams (should be more or as many as our resources)
        final ResultList<AnyIdDTO> allStreams = utils
                .queryJson(String.format("/Things(%s)/Datastreams?$count=true", provider), RESULT_ANY);
        final int nbIds = allStreams.count();
        final List<String> allIds = allStreams.value().stream().map(s -> s.id()).collect(Collectors.toList());
        assertTrue(allIds.size() >= nbRc);
        assertEquals(allIds.size(), nbIds);

        // Test top
        int topVal = 2;
        ResultList<AnyIdDTO> subStreams = utils
                .queryJson(String.format("/Things(%s)/Datastreams?$top=%d", provider, topVal), RESULT_ANY);
        List<String> ids = subStreams.value().stream().map(s -> s.id()).collect(Collectors.toList());
        assertNull(subStreams.count());
        assertEquals(topVal, ids.size());
        assertEquals(allIds.subList(0, topVal), ids);

        subStreams = utils.queryJson(String.format("/Things(%s)/Datastreams?$top=%d&$count=true", provider, topVal),
                RESULT_ANY);
        assertEquals(ids, subStreams.value().stream().map(s -> s.id()).collect(Collectors.toList()));
        assertEquals(nbIds, subStreams.count());

        // Test skip
        int skipVal = 2;
        subStreams = utils.queryJson(String.format("/Things(%s)/Datastreams?$skip=%d", provider, skipVal), RESULT_ANY);
        ids = subStreams.value().stream().map(s -> s.id()).collect(Collectors.toList());
        assertNull(subStreams.count());
        assertEquals(nbIds - skipVal, ids.size());
        assertEquals(allIds.subList(skipVal, allIds.size()), ids);

        subStreams = utils.queryJson(String.format("/Things(%s)/Datastreams?$skip=%d&$count=true", provider, skipVal),
                RESULT_ANY);
        assertEquals(ids, subStreams.value().stream().map(s -> s.id()).collect(Collectors.toList()));
        assertEquals(nbIds, subStreams.count());

        // Test both
        subStreams = utils.queryJson(
                String.format("/Things(%s)/Datastreams?$top=%d&$skip=%d", provider, topVal, skipVal), RESULT_ANY);
        ids = subStreams.value().stream().map(s -> s.id()).collect(Collectors.toList());
        assertNull(subStreams.count());
        assertEquals(topVal, ids.size());
        assertEquals(allIds.subList(skipVal, skipVal + topVal), ids);

        subStreams = utils.queryJson(
                String.format("/Things(%s)/Datastreams?$top=%d&$skip=%d&$count=true", provider, topVal, skipVal),
                RESULT_ANY);
        assertEquals(ids, subStreams.value().stream().map(s -> s.id()).collect(Collectors.toList()));
        assertEquals(nbIds, subStreams.count());
    }

    @Nested
    class FilterFilterTest {

        final List<String> above40 = new ArrayList<>();
        final List<String> below40 = new ArrayList<>();

        final int nbRc = 5;
        String provider1 = null;
        String provider2 = null;
        final String svc = "sensor";
        final String rcPrefix = "rc";

        @BeforeEach
        void setup(TestInfo testInfo) {
            // Register the resources
            final String testMethodName = testInfo.getTestMethod().get().getName();
            provider1 = testMethodName + "_1";
            provider2 = testMethodName + "_2";
            for (int i = 0; i < nbRc; i++) {
                final String rcName = rcPrefix + i;
                createResource(provider1, svc, rcName, i);
                createResource(provider2, svc, rcName, 40 + i);
                below40.add(String.join("~", provider1, svc, rcName));
                above40.add(String.join("~", provider2, svc, rcName));
            }

            createResource(provider1, "admin", "location", new Point(Coordinates.EMPTY, null, null));
            createResource(provider2, "admin", "location", new Point(Coordinates.EMPTY, null, null));
        }

        @AfterEach
        void cleanup() {
            provider1 = null;
            provider2 = null;
            above40.clear();
            below40.clear();
        }

        @Test
        void testFilterThings() throws Exception {
            final TypeReference<ResultList<Thing>> RESULT_THINGS = new TypeReference<>() {
            };

            // Return providers with a resources values less than 30
            ResultList<Thing> things = utils.queryJson(
                    String.format("/Things?$filter=%s",
                            URLEncoder.encode("Datastreams/Observations/result lt 30", StandardCharsets.UTF_8)),
                    RESULT_THINGS);
            List<String> allIds = things.value().stream().map(s -> (String) s.id()).collect(Collectors.toList());
            assertTrue(allIds.size() >= 1, "Not enough things returned");
            assertTrue(allIds.contains(provider1), provider1 + " not in result");
            assertFalse(allIds.contains(provider2), provider2 + " in result");

            // Loop back on provider ID
            things = utils.queryJson(String.format("/Things?$filter=%s", URLEncoder.encode(
                    "Datastreams/Observations/FeatureOfInterest/id eq '" + provider2 + "'", StandardCharsets.UTF_8)),
                    RESULT_THINGS);
            allIds = things.value().stream().map(s -> (String) s.id()).collect(Collectors.toList());
            assertTrue(allIds.size() >= 1, "Not enough things returned");
            assertTrue(allIds.contains(provider2), provider2 + " not in result");
            assertFalse(allIds.contains(provider1), provider1 + " in result");

            // Sample query from the specifications
            createResource("filterFOI_1", "some-service", "some-resource", 42,
                    ZonedDateTime.of(2010, 6, 15, 21, 42, 0, 0, ZoneOffset.UTC).toInstant());
            things = utils.queryJson(String.format("/Things?$filter=%s",
                    URLEncoder.encode(
                            "Datastreams/Observations/FeatureOfInterest/id eq 'filterFOI_1' "
                                    + "and Datastreams/Observations/resultTime ge 2010-06-01T00:00:00Z "
                                    + "and Datastreams/Observations/resultTime le 2010-07-01T00:00:00Z",
                            StandardCharsets.UTF_8)),
                    RESULT_THINGS);
            assertEquals(1, things.value().size(), "Not enough things returned");
            assertEquals("filterFOI_1", things.value().get(0).id());
        }

        @Test
        void testFilterLocations() throws Exception {
            final TypeReference<ResultList<Location>> RESULT_LOCATIONS = new TypeReference<>() {
            };

            // Loop back on provider ID
            ResultList<Location> items = utils.queryJson(
                    String.format("/Locations?$filter=%s",
                            URLEncoder.encode("Things/id eq '" + provider2 + "'", StandardCharsets.UTF_8)),
                    RESULT_LOCATIONS);
            List<String> allIds = items.value().stream().map(s -> (String) s.id()).collect(Collectors.toList());
            assertTrue(allIds.size() >= 1, "Not enough locations returned");
            for (String id : allIds) {
                assertTrue(id.startsWith(provider2 + "~"), "Found: " + id);
            }
        }

        @Test
        void testFilterHistoricalLocations() throws Exception {
            final TypeReference<ResultList<HistoricalLocation>> RESULT_HISTORICAL_LOCATIONS = new TypeReference<>() {
            };

            // Loop back on provider ID
            ResultList<HistoricalLocation> items = utils.queryJson(
                    String.format("/HistoricalLocations?$filter=%s",
                            URLEncoder.encode("Things/id eq '" + provider2 + "'", StandardCharsets.UTF_8)),
                    RESULT_HISTORICAL_LOCATIONS);
            List<String> allIds = items.value().stream().map(s -> (String) s.id()).collect(Collectors.toList());
            assertTrue(allIds.size() >= 1, "Not enough historical locations returned");
            for (String id : allIds) {
                assertTrue(id.startsWith(provider2 + "~"), "Found: " + id);
            }
        }

        @Test
        void testFilterDatastreams() throws Exception {
            final TypeReference<ResultList<Datastream>> RESULT_DATASTREAMS = new TypeReference<>() {
            };

            // Return providers with a resources values less than 30
            ResultList<Datastream> items = utils.queryJson(
                    String.format("/Datastreams?$filter=%s",
                            URLEncoder.encode("Observations/result ge 40", StandardCharsets.UTF_8)),
                    RESULT_DATASTREAMS);
            List<String> allIds = items.value().stream().map(s -> (String) s.id()).collect(Collectors.toList());
            assertTrue(allIds.size() >= nbRc, "Not enough datastreams returned");
            for (String below : below40) {
                assertFalse(allIds.contains(below), below + " in result");
            }
            for (String above : above40) {
                assertTrue(allIds.contains(above), above + " not in result");
            }

            // Loop back on provider ID
            items = utils.queryJson(
                    String.format("/Datastreams?$filter=%s", URLEncoder.encode(
                            "Observations/FeatureOfInterest/id eq '" + provider1 + "'", StandardCharsets.UTF_8)),
                    RESULT_DATASTREAMS);
            allIds = items.value().stream().map(s -> (String) s.id()).collect(Collectors.toList());
            assertTrue(allIds.size() >= nbRc, "Not enough datastreams returned");
            for (String id : allIds) {
                assertTrue(id.startsWith(provider1 + "~"), "Found: " + id);
            }

            // Loop back on resource ID
            final String expectedId = String.join("~", provider1, svc, rcPrefix + 2);
            items = utils.queryJson(
                    String.format("/Datastreams?$filter=%s",
                            URLEncoder.encode("id eq '" + expectedId + "'", StandardCharsets.UTF_8)),
                    RESULT_DATASTREAMS);
            allIds = items.value().stream().map(s -> (String) s.id()).collect(Collectors.toList());
            assertTrue(allIds.size() >= 1, "Not enough datastreams returned");
            for (String id : allIds) {
                assertEquals(expectedId, id, "Found: " + id);
            }
        }

        @Test
        void testFilterSensors() throws Exception {
            final TypeReference<ResultList<Sensor>> RESULT_SENSORS = new TypeReference<>() {
            };

            // Return providers with a resources values less than 30
            ResultList<Sensor> items = utils.queryJson(
                    String.format("/Sensors?$filter=%s",
                            URLEncoder.encode("Datastreams/Observations/result ge 40", StandardCharsets.UTF_8)),
                    RESULT_SENSORS);
            List<String> allIds = items.value().stream().map(s -> (String) s.id()).collect(Collectors.toList());
            assertTrue(allIds.size() >= nbRc, "Not enough sensors returned");
            for (String below : below40) {
                assertFalse(allIds.contains(below), below + " in result");
            }
            for (String above : above40) {
                assertTrue(allIds.contains(above), above + " not in result");
            }

            // Loop back on provider ID
            items = utils.queryJson(String.format("/Sensors?$filter=%s", URLEncoder.encode(
                    "Datastreams/Observations/FeatureOfInterest/id eq '" + provider1 + "'", StandardCharsets.UTF_8)),
                    RESULT_SENSORS);
            allIds = items.value().stream().map(s -> (String) s.id()).collect(Collectors.toList());
            assertTrue(allIds.size() >= nbRc, "Not enough sensors returned");
            for (String id : allIds) {
                assertTrue(id.startsWith(provider1 + "~"), "Found: " + id);
            }

            // Loop back on resource ID
            final String expectedId = String.join("~", provider1, svc, rcPrefix + 2);
            items = utils.queryJson(String.format("/Sensors?$filter=%s",
                    URLEncoder.encode("id eq '" + expectedId + "'", StandardCharsets.UTF_8)), RESULT_SENSORS);
            allIds = items.value().stream().map(s -> (String) s.id()).collect(Collectors.toList());
            assertTrue(allIds.size() >= 1, "Not enough sensors returned");
            for (String id : allIds) {
                assertEquals(expectedId, id, "Found: " + id);
            }
        }

        @Test
        void testFilterObservedProperties() throws Exception {
            final TypeReference<ResultList<ObservedProperty>> RESULT_OBS_PROPS = new TypeReference<>() {
            };

            // Return providers with a resources values less than 30
            ResultList<ObservedProperty> items = utils.queryJson(
                    String.format("/ObservedProperties?$filter=%s",
                            URLEncoder.encode("Datastreams/Observations/result ge 40", StandardCharsets.UTF_8)),
                    RESULT_OBS_PROPS);
            List<String> allIds = items.value().stream().map(s -> (String) s.id()).collect(Collectors.toList());
            assertTrue(allIds.size() >= nbRc, "Not enough ObservedProperties returned");
            for (String below : below40) {
                assertFalse(allIds.contains(below), below + " in result");
            }
            for (String above : above40) {
                assertTrue(allIds.contains(above), above + " not in result");
            }

            // Loop back on provider ID
            items = utils.queryJson(String.format("/ObservedProperties?$filter=%s", URLEncoder.encode(
                    "Datastreams/Observations/FeatureOfInterest/id eq '" + provider1 + "'", StandardCharsets.UTF_8)),
                    RESULT_OBS_PROPS);
            allIds = items.value().stream().map(s -> (String) s.id()).collect(Collectors.toList());
            assertTrue(allIds.size() >= nbRc, "Not enough ObservedProperties returned");
            for (String id : allIds) {
                assertTrue(id.startsWith(provider1 + "~"), "Found: " + id);
            }

            // Loop back on resource ID
            final String expectedId = String.join("~", provider1, svc, rcPrefix + 2);
            items = utils.queryJson(String.format("/ObservedProperties?$filter=%s",
                    URLEncoder.encode("id eq '" + expectedId + "'", StandardCharsets.UTF_8)), RESULT_OBS_PROPS);
            allIds = items.value().stream().map(s -> (String) s.id()).collect(Collectors.toList());
            assertTrue(allIds.size() >= 1, "Not enough ObservedProperties returned");
            for (String id : allIds) {
                assertEquals(expectedId, id, "Found: " + id);
            }
        }

        @Test
        void testFilterObservations() throws Exception {
            final TypeReference<ResultList<Observation>> RESULT_OBSERVATIONS = new TypeReference<>() {
            };

            // Return providers with a resources values less than 30
            ResultList<Observation> obs = utils.queryJson(String.format("/Observations?$filter=%s",
                    URLEncoder.encode("result ge 40", StandardCharsets.UTF_8)), RESULT_OBSERVATIONS);
            List<String> allIds = obs.value().stream().map(s -> (String) s.id())
                    .map(s -> s.substring(0, s.lastIndexOf('~'))).collect(Collectors.toList());
            assertTrue(allIds.size() >= nbRc, "Not enough observations returned");
            for (String below : below40) {
                assertFalse(allIds.contains(below), below + " in result");
            }
            for (String above : above40) {
                assertTrue(allIds.contains(above), above + " not in result");
            }

            // Loop back on provider ID
            obs = utils.queryJson(
                    String.format("/Observations?$filter=%s",
                            URLEncoder.encode("FeatureOfInterest/id eq '" + provider1 + "'", StandardCharsets.UTF_8)),
                    RESULT_OBSERVATIONS);
            allIds = obs.value().stream().map(s -> (String) s.id()).collect(Collectors.toList());
            assertTrue(allIds.size() >= nbRc, "Not enough observations returned");
            for (String id : allIds) {
                assertTrue(id.startsWith(provider1 + "~"), "Found: " + id);
            }

            // Loop back on resource ID
            final String expectedId = String.join("~", provider1, svc, rcPrefix + 2);
            obs = utils.queryJson(
                    String.format("/Observations?$filter=%s",
                            URLEncoder.encode("Datastream/id eq '" + expectedId + "'", StandardCharsets.UTF_8)),
                    RESULT_OBSERVATIONS);
            allIds = obs.value().stream().map(s -> (String) s.id()).collect(Collectors.toList());
            assertTrue(allIds.size() >= 1, "Not enough observations returned");
            for (String id : allIds) {
                // Ignore the timestamp
                assertTrue(id.startsWith(expectedId + "~"), "Found: " + id);
            }
        }
    }

    @Nested
    class ExpandFilterTest {

        @Test
        void testExpand() throws IOException, InterruptedException {
            final String provider = "expandTesterThing";
            final String providerDatastream = "expandTesterDatastream";
            final String providerDatastream2 = "expandTesterDatastream2";

            createThing(provider, List.of(), List.of(providerDatastream, providerDatastream2));
            createDatastrem(providerDatastream, provider);
            createDatastrem(providerDatastream2, provider);

            Set<String> expandedFields = Set.of("Datastreams/Observations", "Locations");
            Map<?, ?> rawResultList = utils.queryJson("/Things/?$expand=" + String.join(",", expandedFields),
                    Map.class);

            // One Thing
            List<?> items = (List<?>) rawResultList.get("value");
            assertFalse(items.isEmpty());
            assertEquals(1, items.size());

            Map<?, ?> rawThing = items.stream().map(Map.class::cast).filter(m -> !"sensiNact".equals(m.get("name")))
                    .findFirst().get();

            // Two data streams (one per resource)
            List<?> rawDatastreamsList = (List<?>) rawThing.get("Datastreams");

            assertNotNull(rawDatastreamsList);
            assertEquals(7, rawDatastreamsList.size());

            // One observation with the value
            Map<?, ?> rawDatastream = rawDatastreamsList.stream().map(Map.class::cast)
                    .filter(m -> providerDatastream.equals(m.get("@iot.id"))).findFirst().get();

            List<?> rawObservationsList = (List<?>) rawDatastream.get("Observations");

            assertNotNull(rawObservationsList);
            assertEquals(1, rawObservationsList.size());

            Map<?, ?> rawObservation = (Map<?, ?>) rawObservationsList.get(0);
            assertNotNull(rawObservation);
            assertEquals(42, rawObservation.get("result"));

            // Check the second value
            rawDatastream = rawDatastreamsList.stream().map(Map.class::cast)
                    .filter(m -> "expandTester~sensor~rc_2".equals(m.get("@iot.id"))).findFirst().get();

            rawObservationsList = (List<?>) rawDatastream.get("Observations");

            assertNotNull(rawObservationsList);
            assertEquals(1, rawObservationsList.size());

            rawObservation = (Map<?, ?>) rawObservationsList.get(0);
            assertNotNull(rawObservation);
            assertEquals(24, rawObservation.get("result"));
        }

        @Test
        void testExpandSingle() throws IOException, InterruptedException {
            final String provider = "expandTesterThing";
            final String providerDatastream = "expandTesterDatastream";

            createThing(provider, List.of(), List.of(providerDatastream));
            createDatastrem(providerDatastream, provider);

            Set<String> expandedFields = Set.of("Thing", "Sensor");
            Map<?, ?> rawDatastream = utils.queryJson(
                    "/Datastreams(" + providerDatastream + ")/?$expand=" + String.join(",", expandedFields), Map.class);

            Map<?, ?> rawThing = (Map<?, ?>) rawDatastream.get("Thing");

            assertNotNull(rawThing);
            assertEquals(provider, rawThing.get("@iot.id"));

            Map<?, ?> rawSensor = (Map<?, ?>) rawDatastream.get("Sensor");

            assertNotNull(rawSensor);
            assertEquals("expandTesterDatastream~test", rawSensor.get("@iot.id"));
        }
    }
}

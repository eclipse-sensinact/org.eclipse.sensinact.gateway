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
package org.eclipse.sensinact.sensorthings.sensing.rest.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.sensinact.prototype.PrototypePush;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.eclipse.sensinact.prototype.generic.dto.GenericDto;
import org.eclipse.sensinact.prototype.notification.ResourceDataNotification;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.RootResponse;
import org.eclipse.sensinact.sensorthings.sensing.dto.RootResponse.NameUrl;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests that all links related to a thing are valid
 */
public class LinksTest {

    private static final TypeReference<ResultList<Datastream>> RESULT_DATASTREAMS = new TypeReference<ResultList<Datastream>>() {
    };

    private static final TypeReference<ResultList<HistoricalLocation>> RESULT_HISTORICAL_LOCATIONS = new TypeReference<ResultList<HistoricalLocation>>() {
    };

    private static final TypeReference<ResultList<Location>> RESULT_LOCATIONS = new TypeReference<ResultList<Location>>() {
    };

    private static final TypeReference<ResultList<Observation>> RESULT_OBSERVATIONS = new TypeReference<ResultList<Observation>>() {
    };

    private static final TypeReference<ResultList<Sensor>> RESULT_SENSORS = new TypeReference<ResultList<Sensor>>() {
    };

    private static final TypeReference<ResultList<Thing>> RESULT_THINGS = new TypeReference<ResultList<Thing>>() {
    };

    private static final String USER = "user";

    private static final String PROVIDER = "linkTester";
    private static final String PROVIDER_TOPIC = PROVIDER + "/*";

    @InjectService
    SensiNactSessionManager sessionManager;

    @InjectService
    PrototypePush push;

    final TestUtils utils = new TestUtils();

    BlockingQueue<ResourceDataNotification> queue;

    @BeforeEach
    void start() throws InterruptedException {
        queue = new ArrayBlockingQueue<>(32);
        SensiNactSession session = sessionManager.getDefaultSession(USER);
        session.addListener(List.of(PROVIDER_TOPIC), (t, e) -> queue.offer(e), null, null, null);
        assertNull(queue.poll(500, TimeUnit.MILLISECONDS));
    }

    @AfterEach
    void stop() {
        SensiNactSession session = sessionManager.getDefaultSession(USER);
        session.activeListeners().keySet().forEach(session::removeListener);
    }

    /**
     * Check if the mirror access works
     */
    private <S extends Id> void checkMirror(String mirrorBaseUrl, S srcObject, Class<S> srcType)
            throws IOException, InterruptedException {
        // Single element
        String mirrorUrl = String.format("%s/%s", mirrorBaseUrl, srcType.getSimpleName());
        try {
            S mirrorAccess = utils.queryJson(mirrorUrl, srcType);
            utils.assertDtoEquals(srcObject, mirrorAccess, srcType);
            return;
        } catch (IOException ex) {
            // ... ignore HTTP error
        }

        // List of elements
        mirrorUrl = String.format("%s/%s", mirrorBaseUrl, srcType.getSimpleName() + "s");
        Map<?, ?> base = utils.queryJson(mirrorUrl, Map.class);
        List<?> mirrorAccess = utils.getMapper().convertValue(base.get("value"), List.class);

        boolean found = false;
        for (Object rawItem : mirrorAccess) {
            S item = utils.getMapper().convertValue(rawItem, srcType);
            if (srcObject.id.equals(item.id)) {
                found = true;
                utils.assertDtoEquals(srcObject, item, srcType);
                break;
            }
        }
        assertTrue(found, "Source object not found in mirror list");
        return;
    }

    private String toPluralLink(final String kindOfLink) {
        switch (kindOfLink) {
        case "FeatureOfInterest":
            return "FeaturesOfInterest";

        default:
            return kindOfLink.replaceFirst("y$", "ie") + "s";
        }
    }

    private String fromPluralLink(final String kindOfLink) {
        switch (kindOfLink) {
        case "FeaturesOfInterest":
            return "FeatureOfInterest";

        default:
            return kindOfLink.replaceFirst("ies$", "y").replaceFirst("s$", "");
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Id> Class<T> getDTOType(final String simpleName) throws ClassNotFoundException {
        return (Class<T>) Class.forName(Id.class.getPackageName() + "." + simpleName);
    }

    private <T extends Id> Set<Object> listIdsFromURL(final String url, Class<T> linkType)
            throws IOException, InterruptedException {

        // Result List root
        final ObjectMapper mapper = utils.getMapper();
        List<?> values = (List<?>) utils.queryJson(url, Map.class).get("value");
        return values.stream().map(o -> mapper.convertValue(o, linkType).id).collect(Collectors.toSet());
    }

    /**
     * Checks if the access to the data stream works
     */
    private <S extends Id, T extends Id> void checkSubLinks(S srcObject, String listUrl,
            TypeReference<ResultList<T>> resultListType, Class<T> resultType) throws IOException, InterruptedException {
        ResultList<T> results = utils.queryJson(listUrl, resultListType);
        assertNotNull(results);
        assertFalse(results.value.isEmpty(), "No " + resultType.getSimpleName() + " found");

        @SuppressWarnings("unchecked")
        Class<S> srcType = (Class<S>) srcObject.getClass();

        final ObjectMapper mapper = utils.getMapper();

        for (T item : results.value) {
            // Check access from source object
            String directAccessItemUrl = String.format("%s(%s)", listUrl, item.id);
            T directAccessItem = utils.queryJson(directAccessItemUrl, resultType);
            utils.assertDtoEquals(item, directAccessItem, resultType);

            // Check mirror
            checkMirror(String.format("%s(%s)", listUrl, item.id), srcObject, srcType);

            // Check deeper access
            for (Field field : item.getClass().getFields()) {
                String fieldName = field.getName();
                if (!fieldName.endsWith("Link") || "selfLink".equals(fieldName)) {
                    continue;
                }

                String kindOfLink = fieldName.substring(0, fieldName.length() - "Link".length());
                kindOfLink = Character.toUpperCase(kindOfLink.charAt(0)) + kindOfLink.substring(1);
                try {
                    if (kindOfLink.endsWith("s") || kindOfLink.startsWith("Features")) {
                        // Returns a result list
                        String singularLink = fromPluralLink(kindOfLink);
                        Class<? extends Id> linkType = getDTOType(singularLink);
                        Set<Object> linkedItemsIds = listIdsFromURL(
                                String.format("%s/%s", directAccessItemUrl, kindOfLink), linkType);
                        Set<Object> allItemsIds = listIdsFromURL(kindOfLink, linkType);
                        assertTrue(allItemsIds.containsAll(linkedItemsIds),
                                linkedItemsIds + " not a subset of " + allItemsIds);
                    } else {
                        // Returns a single item
                        Class<? extends Id> linkType = getDTOType(kindOfLink);
                        Id linkedDto = mapper.convertValue(
                                utils.queryJson(String.format("%s/%s", directAccessItemUrl, kindOfLink), Map.class),
                                linkType);

                        Id directDto = mapper.convertValue(utils.queryJson(
                                String.format("%s(%s)", toPluralLink(kindOfLink), linkedDto.id), Map.class), linkType);

                        utils.assertDtoEquals(directDto, linkedDto, linkType);
                    }
                } catch (ClassNotFoundException | ClassCastException e) {
                    fail(e);
                }
            }
        }
    }

    /**
     * Check links returned by the root endpoint
     */
    @Test
    void testLinksFromRoot() throws IOException, InterruptedException {
        RootResponse root = utils.queryJson("/", RootResponse.class);
        assertNotNull(root);

        for (final NameUrl nameUrl : root.value) {
            assertNotNull(nameUrl);
            assertNotNull(nameUrl.name, "Null link name");
            assertFalse(nameUrl.name.isEmpty(), "Empty name");
            assertNotNull(nameUrl.url, "Null link URL");
            assertFalse(nameUrl.url.isEmpty(), "Empty URL");
            utils.assertURLStatus(nameUrl.url, 200);
        }
    }

    /**
     * Check links returned by the Things endpoint
     */
    @Test
    void testLinksFromThings() throws IOException, InterruptedException {
        // Add a resource
        GenericDto dto = utils.makeDto(PROVIDER, "sensor", "data", 42, Integer.class);
        push.pushUpdate(dto);
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Get the new things
        ResultList<Thing> things = utils.queryJson("/Things", RESULT_THINGS);
        assertNotNull(things);
        assertFalse(things.value.isEmpty(), "No thing found");

        for (final Thing thing : things.value) {
            assertNotNull(thing);
            assertNotNull(thing.id);
            assertNotNull(thing.name);

            // Check self link
            utils.assertURLStatus(thing.selfLink);
            utils.assertDtoEquals(thing, utils.queryJson(thing.selfLink, Thing.class), Thing.class);

            // Check sub-links existence
            checkSubLinks(thing, thing.datastreamsLink, RESULT_DATASTREAMS, Datastream.class);
            checkSubLinks(thing, thing.historicalLocationsLink, RESULT_HISTORICAL_LOCATIONS, HistoricalLocation.class);
            checkSubLinks(thing, thing.locationsLink, RESULT_LOCATIONS, Location.class);
        }
    }

    /**
     * Check links returned by the Locations endpoint
     */
    @Test
    void testLinksFromLocations() throws IOException, InterruptedException {
        // Add a resource
        GenericDto dto = utils.makeDto(PROVIDER, "sensor", "data", 42, Integer.class);
        push.pushUpdate(dto);
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Get the new locations
        ResultList<Location> locations = utils.queryJson("/Locations", RESULT_LOCATIONS);
        assertNotNull(locations);
        assertFalse(locations.value.isEmpty(), "No location found");

        for (final Location location : locations.value) {
            assertNotNull(location);
            assertNotNull(location.id);
            assertNotNull(location.name);

            // Check self link
            utils.assertURLStatus(location.selfLink);
            utils.assertDtoEquals(location, utils.queryJson(location.selfLink, Location.class), Location.class);

            // Check sub-links existence
            checkSubLinks(location, location.historicalLocationsLink, RESULT_HISTORICAL_LOCATIONS,
                    HistoricalLocation.class);
            checkSubLinks(location, location.thingsLink, RESULT_THINGS, Thing.class);
        }
    }

    /**
     * Check links returned by the HistoricalLocations endpoint
     */
    @Test
    void testLinksFromHistoricalLocations() throws IOException, InterruptedException {
        // Add a resource
        GenericDto dto = utils.makeDto(PROVIDER, "sensor", "data", 42, Integer.class);
        push.pushUpdate(dto);
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));
        final SensiNactSession session = sessionManager.getDefaultSession(USER);
        session.setResourceValue(PROVIDER, "admin", "location",
                "{\"coordinates\": [5.7685,45.192],\"type\": \"Point\"}");

        // Get the new locations
        ResultList<HistoricalLocation> historicalLocations = utils.queryJson("/HistoricalLocations",
                RESULT_HISTORICAL_LOCATIONS);
        assertNotNull(historicalLocations);
        assertFalse(historicalLocations.value.isEmpty(), "No historical location found");

        for (final HistoricalLocation historicalLocation : historicalLocations.value) {
            assertNotNull(historicalLocation);
            assertNotNull(historicalLocation.id);
            assertNotNull(historicalLocation.time);

            // Check self link
            utils.assertURLStatus(historicalLocation.selfLink);
            utils.assertDtoEquals(historicalLocation,
                    utils.queryJson(historicalLocation.selfLink, HistoricalLocation.class), HistoricalLocation.class);

            // Check sub-links existence
            checkSubLinks(historicalLocation, historicalLocation.locationsLink, RESULT_LOCATIONS, Location.class);
            utils.assertURLStatus(historicalLocation.thingLink);
        }
    }

    /**
     * Check links returned by the Datastreams endpoint
     */
    @Test
    void testLinksFromDatastreams() throws IOException, InterruptedException {
        // Add a resource
        GenericDto dto = utils.makeDto(PROVIDER, "sensor", "data", 42, Integer.class);
        push.pushUpdate(dto);
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Get the new locations
        ResultList<Datastream> datastreams = utils.queryJson("/Datastreams", RESULT_DATASTREAMS);
        assertNotNull(datastreams);
        assertFalse(datastreams.value.isEmpty(), "No datastream found");

        for (final Datastream datastream : datastreams.value) {
            assertNotNull(datastream);
            assertNotNull(datastream.id);
            assertNotNull(datastream.name);
            assertNotNull(datastream.unitOfMeasurement);

            // Check self link
            utils.assertURLStatus(datastream.selfLink);
            utils.assertDtoEquals(datastream, utils.queryJson(datastream.selfLink, Datastream.class), Datastream.class);

            // Check sub-links existence
            checkSubLinks(datastream, datastream.observationsLink, RESULT_OBSERVATIONS, Observation.class);
            utils.assertURLStatus(datastream.observedPropertyLink);
            utils.assertURLStatus(datastream.sensorLink);
            utils.assertURLStatus(datastream.thingLink);
        }
    }

    /**
     * Check links returned by the Sensors endpoint
     */
    @Test
    void testLinksFromSensors() throws IOException, InterruptedException {
        // Add a resource
        GenericDto dto = utils.makeDto(PROVIDER, "sensor", "data", 42, Integer.class);
        push.pushUpdate(dto);
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Get the new locations
        ResultList<Sensor> sensors = utils.queryJson("/Sensors", RESULT_SENSORS);
        assertNotNull(sensors);
        assertFalse(sensors.value.isEmpty(), "No sensor found");

        for (final Sensor sensor : sensors.value) {
            assertNotNull(sensor);
            assertNotNull(sensor.id);
            assertNotNull(sensor.name);

            // Check self link
            utils.assertURLStatus(sensor.selfLink);
            utils.assertDtoEquals(sensor, utils.queryJson(sensor.selfLink, Sensor.class), Sensor.class);

            // Check sub-links existence
            checkSubLinks(sensor, sensor.datastreamsLink, RESULT_DATASTREAMS, Datastream.class);
        }
    }

    /**
     * Check links returned by the Observations endpoint
     */
    @Test
    void testLinksFromObservations() throws IOException, InterruptedException {
        // Add a resource
        GenericDto dto = utils.makeDto(PROVIDER, "sensor", "data", 42, Integer.class);
        push.pushUpdate(dto);
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Get the new locations
        ResultList<Observation> observations = utils.queryJson("/Observations", RESULT_OBSERVATIONS);
        assertNotNull(observations);
        assertFalse(observations.value.isEmpty(), "No observation found");

        for (final Observation observation : observations.value) {
            assertNotNull(observation);
            assertNotNull(observation.id);
            assertNotNull(observation.phenomenonTime);
            assertNotNull(observation.resultTime);

            // Check self link
            utils.assertURLStatus(observation.selfLink);
            utils.assertDtoEquals(observation, utils.queryJson(observation.selfLink, Observation.class),
                    Observation.class);

            // Check sub-links existence
            utils.assertURLStatus(observation.datastreamLink);
            utils.assertURLStatus(observation.featureOfInterestLink);
        }
    }

    /**
     * Check links returned by the ObservedProperties endpoint
     */
    @Test
    void testLinksFromObservedProperties() throws IOException, InterruptedException {
        // Add a resource
        GenericDto dto = utils.makeDto(PROVIDER, "sensor", "data", 42, Integer.class);
        push.pushUpdate(dto);
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Get the new locations
        ResultList<ObservedProperty> observedProperties = utils.queryJson("/ObservedProperties",
                new TypeReference<ResultList<ObservedProperty>>() {
                });
        assertNotNull(observedProperties);
        assertFalse(observedProperties.value.isEmpty(), "No observed property found");

        for (final ObservedProperty observed : observedProperties.value) {
            assertNotNull(observed);
            assertNotNull(observed.id);
            assertNotNull(observed.name);

            // Check self link
            utils.assertURLStatus(observed.selfLink);
            utils.assertDtoEquals(observed, utils.queryJson(observed.selfLink, ObservedProperty.class),
                    ObservedProperty.class);

            // Check sub-links existence
            checkSubLinks(observed, observed.datastreamsLink, RESULT_DATASTREAMS, Datastream.class);
        }
    }

    /**
     * Check links returned by the FeaturesOfInterest endpoint
     */
    @Test
    void testLinksFromFeaturesOfInterest() throws IOException, InterruptedException {
        // Add a resource
        GenericDto dto = utils.makeDto(PROVIDER, "sensor", "data", 42, Integer.class);
        push.pushUpdate(dto);
        utils.assertNotification(dto, queue.poll(1, TimeUnit.SECONDS));

        // Get the new locations
        ResultList<FeatureOfInterest> features = utils.queryJson("/FeaturesOfInterest",
                new TypeReference<ResultList<FeatureOfInterest>>() {
                });
        assertNotNull(features);
        assertFalse(features.value.isEmpty(), "No features found");

        for (final FeatureOfInterest feature : features.value) {
            assertNotNull(feature);
            assertNotNull(feature.id);
            assertNotNull(feature.name);

            // Check self link
            utils.assertURLStatus(feature.selfLink);
            utils.assertDtoEquals(feature, utils.queryJson(feature.selfLink, FeatureOfInterest.class),
                    FeatureOfInterest.class);

            // FIXME: observations have a timestamp, but not the feature of interest
            checkSubLinks(feature, feature.observationsLink, RESULT_OBSERVATIONS, Observation.class);
        }
    }
}

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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.sensinact.prototype.PrototypePush;
import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.eclipse.sensinact.prototype.generic.dto.GenericDto;
import org.eclipse.sensinact.prototype.notification.ResourceDataNotification;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
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

/**
 * Tests that all links related to a thing are valid
 */
public class LinksTest {
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
        ResultList<Thing> things = utils.queryJson("/Things", new TypeReference<ResultList<Thing>>() {
        });
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
            utils.assertURLStatus(thing.datastreamsLink);
            utils.assertURLStatus(thing.historicalLocationsLink);
            utils.assertURLStatus(thing.locationsLink);
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
        ResultList<Location> locations = utils.queryJson("/Locations", new TypeReference<ResultList<Location>>() {
        });
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
            utils.assertURLStatus(location.historicalLocationsLink);
            utils.assertURLStatus(location.thingsLink);
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
                new TypeReference<ResultList<HistoricalLocation>>() {
                });
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
            utils.assertURLStatus(historicalLocation.locationsLink);
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
        ResultList<Datastream> datastreams = utils.queryJson("/Datastreams",
                new TypeReference<ResultList<Datastream>>() {
                });
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
            utils.assertURLStatus(datastream.observationsLink);
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
        ResultList<Sensor> sensors = utils.queryJson("/Sensors", new TypeReference<ResultList<Sensor>>() {
        });
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
            utils.assertURLStatus(sensor.datastreamsLink);
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
        ResultList<Observation> observations = utils.queryJson("/Observations",
                new TypeReference<ResultList<Observation>>() {
                });
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
            utils.assertURLStatus(observed.datastreamsLink);
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

            // Check sub-links existence
            utils.assertURLStatus(feature.observationsLink);
        }
    }
}

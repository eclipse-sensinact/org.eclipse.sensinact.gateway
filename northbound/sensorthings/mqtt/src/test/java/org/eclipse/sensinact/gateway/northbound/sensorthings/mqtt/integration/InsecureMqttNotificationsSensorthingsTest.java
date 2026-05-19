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
package org.eclipse.sensinact.gateway.northbound.sensorthings.mqtt.integration;

import static org.eclipse.sensinact.sensorthings.models.extended.ExtendedPackage.eNS_URI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.sensinact.core.command.AbstractSensinactCommand;
import org.eclipse.sensinact.core.command.GatewayThread;
import org.eclipse.sensinact.core.model.SensinactModelManager;
import org.eclipse.sensinact.core.push.DataUpdate;
import org.eclipse.sensinact.core.push.dto.GenericDto;
import org.eclipse.sensinact.core.twin.SensinactDigitalTwin;
import org.eclipse.sensinact.core.twin.SensinactProvider;
import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.Feature;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.GeoJsonType;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

@WithFactoryConfiguration(factoryPid = "sensiNact.northbound.sensorthings.mqtt", properties = {
        @Property(key = "port", value = "13579"), @Property(key = "websocket.enable", value = "false") })
public class InsecureMqttNotificationsSensorthingsTest {

    @InjectService
    DataUpdate push;

    @InjectService
    GatewayThread thread;

    private IMqttAsyncClient client;

    private BlockingQueue<String> messages = new ArrayBlockingQueue<>(64);

    private IMqttMessageListener listener;

    private ObjectMapper mapper;

    @BeforeEach
    void start() throws Exception {
        client = new MqttAsyncClient(getMqttURL(), "test-client");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(500);
        boolean connected = false;
        for (int i = 0; i < 5; i++) {
            try {
                client.connect(options).waitForCompletion(1000);
                connected = true;
            } catch (MqttException e) {
                e.printStackTrace();
                if (e.getCause() instanceof ConnectException) {
                    Thread.sleep(500);
                    continue;
                }
                throw e;
            }
            break;
        }

        if (!connected) {
            fail("Not connected to the MQTT broker");
        }

        listener = (t, m) -> messages.put(new String(m.getPayload(), StandardCharsets.UTF_8));

        mapper = JsonMapper.builder()
                .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, true).build();
    }

    protected String getMqttURL() {
        return "tcp://127.0.0.1:13579";
    }

    @AfterEach
    void stop() throws Exception {
        try {
            client.disconnect(500).waitForCompletion(1000);
        } catch (MqttException e) {
            // Swallow it
        }
        messages.clear();

        thread.execute(new AbstractSensinactCommand<Void>() {
            @Override
            protected Promise<Void> call(final SensinactDigitalTwin twin, final SensinactModelManager modelMgr,
                    final PromiseFactory promiseFactory) {
                twin.getProviders().forEach(SensinactProvider::delete);
                return promiseFactory.resolved(null);
            }
        }).getValue();
    }

    protected void createResourceWithPackageUri(String provider, String modelPackageUri, String service,
            String resource, Object value) {
        createOrUpdateResourceWithPackageUri(provider, modelPackageUri, service, resource, value, null);
    }

    protected void createResource(String provider, String service, String resource, Object value) {
        createOrUpdateResourceWithPackageUri(provider, "sensinact", service, resource, value, null);
    }

    protected void createDatastream(String provider, String thingId, String sensorId, String opId) {
        createDatastream(provider, thingId, sensorId, opId, 42);
    }

    protected FeatureOfInterest getFeatureOfInterest(String foiRefId, String name, GeoJsonObject geo) {
        return new FeatureOfInterest(null, foiRefId, name, null, "test", geo, null, null);
    }

    protected FeatureOfInterest getFeatureOfInterest(String foiRefId) {
        return getFeatureOfInterest(foiRefId, "test", new Point(0, 0));
    }

    protected void createDatastream(String provider, String name, String thingId, int value) {
        createDatastream(provider, name, thingId, "sensor", "op", null, value, Instant.now());
    }

    protected void createDatastream(String provider, String name, String thingId, int value, Instant stamp) {
        createDatastream(provider, name, thingId, "sensor", "op", null, value, stamp);
    }

    protected void createDatastream(String provider, String thingId, String sensorId, String opId, int value) {
        createDatastream(provider, thingId, sensorId, opId, value, Instant.now());
    }

    protected void createDatastream(String provider, String name, String thingId, String sensorId, String opId,
            int value) {
        createDatastream(provider, name, thingId, sensorId, opId, null, value, Instant.now());
    }

    protected void createDatastream(String provider, String name, String thingId, Object val, GeoJsonObject geo) {
        createDatastream(provider, name, thingId, val, geo, Instant.now());
    }

    protected void createDatastream(String provider, String name, String thingId, Object val, GeoJsonObject geo,
            Instant stamp) {
        createDatastream(provider, name, thingId, "sensor", "op", geo, val, stamp);
    }

    protected void createObservation(String provider, String thingId, Object value, Instant valueInstant) {
        FeatureOfInterest foi = getFeatureOfInterest(provider + "test");
        createFoi(foi.id().toString(), valueInstant, foi);
        createOrUpdateResourceWithPackageUri(provider, eNS_URI, DtoMapperSimple.SERVICE_DATASTREAM, "lastObservation",
                getObservation(provider + "~test", value, foi, valueInstant), valueInstant);

    }

    private void createFoi(String datastreamprov, Instant valueInstant, FeatureOfInterest foi) {
        createOrUpdateResourceWithPackageUri((String) foi.id(), eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "friendlyName",
                foi.name(), valueInstant);
        createOrUpdateResourceWithPackageUri((String) foi.id(), eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "description",
                foi.name(), valueInstant);
        createOrUpdateResourceWithPackageUri((String) foi.id(), eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "location",
                foi.feature(), valueInstant);

        createOrUpdateResourceWithPackageUri((String) foi.id(), eNS_URI, DtoMapperSimple.SERVICE_FOI, "encodingType",
                foi.name(), valueInstant);
        createOrUpdateResourceWithPackageUri((String) foi.id(), eNS_URI, DtoMapperSimple.SERVICE_FOI, "datastreamIds",
                List.of(datastreamprov), valueInstant);
    }

    protected void createDatastream(String provider, String name, String thingId, String sensorId, String opId,
            GeoJsonObject geo, Object value, Instant valueInstant) {

        createOrUpdateResourceWithPackageUri(provider, eNS_URI, DtoMapperSimple.SERVICE_DATASTREAM, "thingId", thingId,
                valueInstant);
        createOrUpdateResourceWithPackageUri(provider, eNS_URI, DtoMapperSimple.SERVICE_DATASTREAM, "id", provider,
                valueInstant);
        createOrUpdateResourceWithPackageUri(provider, eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "friendlyName", name,
                valueInstant);
        createOrUpdateResourceWithPackageUri(provider, eNS_URI, DtoMapperSimple.SERVICE_DATASTREAM, "observationType",
                "test", valueInstant);
        createOrUpdateResourceWithPackageUri(provider, eNS_URI, DtoMapperSimple.SERVICE_DATASTREAM, "sensorId",
                sensorId, valueInstant);
        createOrUpdateResourceWithPackageUri(provider, eNS_URI, DtoMapperSimple.SERVICE_DATASTREAM,
                "observedPropertyId", opId, valueInstant);
        if (geo != null)
            createOrUpdateResourceWithPackageUri(provider, eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "location", geo,
                    valueInstant);
        createSensor(provider, sensorId, sensorId, valueInstant);

        createObservedProperty(provider, opId, opId, valueInstant);

        createOrUpdateResourceWithPackageUri(provider, eNS_URI, DtoMapperSimple.SERVICE_DATASTREAM, "unitName", "test",
                valueInstant);
        createOrUpdateResourceWithPackageUri(provider, eNS_URI, DtoMapperSimple.SERVICE_DATASTREAM, "unitSymbol",
                "test", valueInstant);
        createOrUpdateResourceWithPackageUri(provider, eNS_URI, DtoMapperSimple.SERVICE_DATASTREAM, "unitDefinition",
                "test", valueInstant);

        createObservation(provider, thingId, value, valueInstant);
    }

    protected void createDatastream(String provider, String thingId, String sensorId, String opId, Object value,
            Instant valueInstant) {
        createDatastream(provider, provider, thingId, sensorId, opId, null, value, valueInstant);
    }

    private void createSensor(String provider, String sensorId, String name, Instant valueInstant) {
        createOrUpdateResourceWithPackageUri(sensorId, eNS_URI, DtoMapperSimple.SERVICE_SENSOR, "id", provider,
                valueInstant);
        createOrUpdateResourceWithPackageUri(sensorId, eNS_URI, DtoMapperSimple.SERVICE_SENSOR, "datastreamIds",
                List.of(provider), valueInstant);

        createOrUpdateResourceWithPackageUri(sensorId, eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "friendlyName", name,
                valueInstant);
        createOrUpdateResourceWithPackageUri(sensorId, eNS_URI, DtoMapperSimple.SERVICE_SENSOR, "sensorEncodingType",
                "test", valueInstant);
    }

    private void createObservedProperty(String provider, String opId, String name, Instant valueInstant) {
        createOrUpdateResourceWithPackageUri(opId, eNS_URI, DtoMapperSimple.SERVICE_OBSERVED_PROPERTY, "id", opId,
                valueInstant);
        createOrUpdateResourceWithPackageUri(opId, eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "friendlyName", name,
                valueInstant);
        createOrUpdateResourceWithPackageUri(opId, eNS_URI, DtoMapperSimple.SERVICE_OBSERVED_PROPERTY,
                "observedPropertyDefinition", "test", valueInstant);
        createOrUpdateResourceWithPackageUri(opId, eNS_URI, DtoMapperSimple.SERVICE_OBSERVED_PROPERTY, "datastreamIds",
                List.of(provider), valueInstant);
    }

    public static String getObservation(String name, Object result, FeatureOfInterest foi) {
        return getObservation(name, result, foi, null);
    }

    public static String toString(ExpandedObservation obs) {
        ObjectMapper mapper = JsonMapper.builder().build();
        try {
            return mapper.writeValueAsString(obs);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }

    }

    public static String getObservation(String id, Object result, FeatureOfInterest foi, Instant instant) {
        ExpandedObservation obs = new ExpandedObservation(null, id,
                instant != null ? instant : Instant.now().truncatedTo(ChronoUnit.SECONDS),
                instant != null ? instant : Instant.now().truncatedTo(ChronoUnit.SECONDS), result, "test", null, null,
                null, null, null, null, foi, false);
        return toString(obs);
    }

    protected void createLocation(String provider) {
        createLocation(provider, provider, new Point(Coordinates.EMPTY, null, null), Instant.now());
    }

    protected void createLocation(String provider, String name, GeoJsonObject location) {
        createLocation(provider, name, location, Instant.now());

    }

    protected void createLocation(String provider, GeoJsonObject location) {
        createLocation(provider, provider, location, Instant.now());

    }

    protected void createLocation(String provider, String name, GeoJsonObject location, Instant instant) {
        createOrUpdateResourceWithPackageUri(provider, eNS_URI, DtoMapperSimple.SERVICE_LOCATION, "id", provider,
                instant);
        createOrUpdateResourceWithPackageUri(provider, eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "location", location,
                instant);
        createOrUpdateResourceWithPackageUri(provider, eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "friendlyName", name,
                instant);

    }

    protected void createThing(String provider, String name) {
        createThing(provider, name, List.of(), List.of(), null, Instant.now());
    }

    protected void createThing(String provider) {
        createThing(provider, "test");
    }

    protected void createThing(String provider, Instant stamp) {
        createThing(provider, "test", List.of(), List.of(), new Point(0, 0), stamp);
    }

    protected void createThing(String provider, String name, List<String> locationIds, List<String> datastreamIds) {
        createThing(provider, name, locationIds, datastreamIds, new Point(0, 0), Instant.now());
    }

    protected void createThing(String provider, String name, List<String> locationIds, List<String> datastreamIds,
            Instant instant) {
        createThing(provider, name, locationIds, datastreamIds, new Point(0, 0), instant);
    }

    protected void createThing(String provider, String name, List<String> locationIds, List<String> datastreamIds,
            GeoJsonObject location, Instant instant) {
        createOrUpdateResourceWithPackageUri(provider, eNS_URI, DtoMapperSimple.SERVICE_THING, "id", provider, instant);
        createOrUpdateResourceWithPackageUri(provider, eNS_URI, DtoMapperSimple.SERVICE_THING, "name", name, instant);
        createOrUpdateResourceWithPackageUri(provider, eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "location",
                location != null ? location : new Point(Coordinates.EMPTY, null, null), instant);
        createOrUpdateResourceWithPackageUri(provider, eNS_URI, DtoMapperSimple.SERVICE_THING, "locationIds",
                locationIds, instant);
        createOrUpdateResourceWithPackageUri(provider, eNS_URI, DtoMapperSimple.SERVICE_THING, "datastreamIds",
                datastreamIds, instant);
    }

    protected void createResource(String provider, String service, String resource, Object value, Instant instant) {
        createOrUpdateResourceWithPackageUri(provider, "sensinact", service, resource, value, instant);
    }

    protected void createOrUpdateResourceWithPackageUri(String provider, String modelPackageUri, String service,
            String resource, Object value, Instant instant) {
        GenericDto dto = new GenericDto();
        if (value instanceof Collection<?>) {
            dto.upperBound = -1;
        }
        dto.modelPackageUri = modelPackageUri;
        dto.provider = provider;
        dto.service = service;
        dto.resource = resource;
        if (value instanceof Collection<?>) {
            dto.type = String.class;

        } else {
            dto.type = value.getClass();
        }
        dto.value = value;
        dto.timestamp = instant;
        try {
            push.pushUpdate(dto).getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> readMessages(Class<T> type)
            throws InterruptedException, JacksonException, DatabindException {
        List<T> streams = new ArrayList<>();

        String message = messages.poll(1500, TimeUnit.MILLISECONDS);
        while (message != null) {
            assertNotNull(message, () -> "Received " + streams.size() + " messages");
            streams.add(mapper.readValue(message, type));
            message = messages.poll(500, TimeUnit.MILLISECONDS);
        }
        assertNull(message);

        return streams;
    }

    @Nested
    public class DatastreamsTests {

        @Test
        public void testDatastreamsCollection() throws Exception {

            client.subscribe("v1.1/Datastreams", 0, listener).waitForCompletion(5000);

            // We must use a different model as otherwise the other tests interfere
            // by including other resources
            createDatastream("foobar", "thing", "sensor", "op", 17);
            createDatastream("fizzbuzz", "thing", "sensor", "op", 42);

            List<Datastream> streams = readMessages(Datastream.class);

            // Creation in sorted event order (p/s/r uri)
            assertEquals("foobar", streams.get(0).name());

        }

        @Test
        public void testDatastreamsWithId() throws Exception {

            client.subscribe("v1.1/Datastreams(fizzbuzz)", 0, listener).waitForCompletion(5000);
            createDatastream("foobar", "thing", "sensor", "op", 17);
            createDatastream("fizzbuzz", "thing", "sensor", "op", 42);

            List<Datastream> streams = readMessages(Datastream.class);

            // Creation, metadata update, location update
            assertEquals("fizzbuzz", streams.get(0).name());
            createOrUpdateResourceWithPackageUri("fizzbuzz", eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "friendlyName",
                    "fizzbuzz2", Instant.now());
            streams = readMessages(Datastream.class);

            assertEquals("fizzbuzz2", streams.get(0).name());

        }

        @Test
        public void testDatastreamsWithIdAndSelection() throws Exception {

            client.subscribe("v1.1/Datastreams(fizzbuzz)?$select=@iot.id,name", 0, listener).waitForCompletion(5000);

            createDatastream("foobar", "thing", "sensor", "op", 17);
            createDatastream("fizzbuzz", "thing", "sensor", "op", 42);

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(Map.class);

            // Creation, metadata update, location update
            for (@SuppressWarnings("rawtypes")
            Map m : streams) {
                assertEquals(Set.of("@iot.id", "name"), m.keySet());
                assertEquals("fizzbuzz", m.get("name"));
                assertEquals("fizzbuzz", m.get("@iot.id"));
            }
        }

        @Test
        public void testDatastreamsWithIdAndProperty() throws Exception {

            client.subscribe("v1.1/Datastreams(fizzbuzz2)/name", 0, listener).waitForCompletion(5000);
            createDatastream("fizzbuzz", "fizzbuzz", "fizz", "sensor", "op", 17);

            // Ensure the datastream is linked to the 'fizz' Thing (was using 'foo' which is
            // wrong)
            createDatastream("fizzbuzz2", "fizzbuzz", "fizz", "sensor", "op", 42);

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(Map.class);

            assertEquals(Map.of("name", "fizzbuzz"), streams.get(0));
            assertEquals(Map.of("name", "fizzbuzz"), streams.get(1));
            assertEquals(Map.of("name", "fizzbuzz"), streams.get(2));
            assertEquals(Map.of("name", "fizzbuzz"), streams.get(3));
            assertEquals(Map.of("name", "fizzbuzz"), streams.get(4));
            assertEquals(Map.of("name", "fizzbuzz"), streams.get(streams.size() - 1));

            createOrUpdateResourceWithPackageUri("fizzbuzz2", eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "friendlyName",
                    "foobar", Instant.now());

            streams.addAll(readMessages(Map.class));

            assertEquals(Map.of("name", "foobar"), streams.get(streams.size() - 1));

        }
    }

    @Nested
    public class FeaturesOfInterestTests {

        @Test
        public void testFeaturesOfInterestCollection() throws Exception {

            client.subscribe("v1.1/FeaturesOfInterest", 0, listener).waitForCompletion(5000);

            Point p = new Point(34d, 12d);

            createFoi("bar", Instant.now(), getFeatureOfInterest("bar", "fa", p));

            List<FeatureOfInterest> fois = readMessages(FeatureOfInterest.class);

            assertEquals("fa", fois.get(fois.size() - 1).name());
            assertEquals("bar", fois.get(fois.size() - 1).id());
        }

        @Test
        public void testFeaturesOfInterestWithId() throws Exception {

            client.subscribe("v1.1/FeaturesOfInterest(bar)", 0, listener).waitForCompletion(5000);

            Point p = new Point(34d, 12d);

            createFoi("foo", Instant.now(), getFeatureOfInterest("foo", "fe", p));
            createFoi("bar", Instant.now(), getFeatureOfInterest("bar", "fa", p));

            List<FeatureOfInterest> fois = readMessages(FeatureOfInterest.class);

            assertEquals("fa", fois.get(fois.size() - 1).name());
            assertEquals("bar", fois.get(fois.size() - 1).id());

        }

        @Test
        public void testFeaturesOfInterestWithIdAndSelection() throws Exception {

            client.subscribe("v1.1/FeaturesOfInterest(bar)?$select=@iot.id,name", 0, listener).waitForCompletion(5000);

            Point p = new Point(34d, 12d);

            createFoi("foo", Instant.now(), getFeatureOfInterest("foo", "fe", p));
            createFoi("bar", Instant.now(), getFeatureOfInterest("bar", "fa", p));

            @SuppressWarnings("rawtypes")
            List<Map> fois = readMessages(Map.class);

            // Creation, value setting, metadata update
            for (@SuppressWarnings("rawtypes")
            Map m : fois) {
                assertEquals(Set.of("@iot.id", "name"), m.keySet());
                assertEquals("fa", m.get("name"));
                assertEquals("bar", m.get("@iot.id"));
            }
        }

        @Test
        public void testFeaturesOfInterestWithIdAndProperty() throws Exception {

            client.subscribe("v1.1/FeaturesOfInterest(bar)/name", 0, listener).waitForCompletion(5000);

            Point p = new Point(34d, 12d);
            Feature f = new Feature("test", p, Map.of("name", "fizzbuzz"), null, null);
            FeatureOfInterest foi = getFeatureOfInterest("bar", "test", f);
            createFoi("bar", Instant.now(), foi);
            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(Map.class);
            assertEquals(Map.of("name", "test"), streams.get(streams.size() - 1));

            createOrUpdateResourceWithPackageUri("bar", eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "friendlyName",
                    "foobar", Instant.now());
            streams.addAll(readMessages(Map.class));

            assertEquals(Map.of("name", "foobar"), streams.get(streams.size() - 1));

        }
    }

    @Nested
    public class HistoricalLocationsTests {

        Instant testTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        @Test
        public void testHistoricalLocationsCollection() throws Exception {

            client.subscribe("v1.1/HistoricalLocations", 0, listener).waitForCompletion(5000);

            createThing("fooThing", testTime);

            List<HistoricalLocation> hls = readMessages(HistoricalLocation.class);

            assertEquals(testTime, hls.get(0).time());
            assertEquals("fooThing~", String.valueOf(hls.get(0).id()).substring(0, "fooThing".length() + 1));
        }

        @Test
        public void testHistoricalLocationsWithId() throws Exception {

            client.subscribe("v1.1/HistoricalLocations(bar)", 0, listener).waitForCompletion(5000);

            createThing("foo", testTime);
            createThing("bar", testTime);

            List<HistoricalLocation> hls = readMessages(HistoricalLocation.class);

            assertEquals(testTime, hls.get(0).time());
            assertEquals("bar~", String.valueOf(hls.get(0).id()).substring(0, 4));

        }

        @Test
        public void testHistoricalLocationsWithIdAndSelection() throws Exception {

            client.subscribe("v1.1/HistoricalLocations(bar)?$select=@iot.id,time", 0, listener).waitForCompletion(5000);

            createThing("foo", testTime);
            createThing("bar", testTime);

            @SuppressWarnings("rawtypes")
            List<Map> hls = readMessages(Map.class);

            // Creation, value setting, metadata update
            for (@SuppressWarnings("rawtypes")
            Map m : hls) {
                assertEquals(Set.of("@iot.id", "time"), m.keySet());
                assertEquals(testTime.toString(), m.get("time"));
                assertEquals("bar~", String.valueOf(m.get("@iot.id")).substring(0, 4));
            }
        }

    }

    @Nested
    public class LocationsTests {

        @Test
        public void testLocationsCollection() throws Exception {

            client.subscribe("v1.1/Locations", 0, listener).waitForCompletion(5000);

            Point p = new Point(34d, 12d);

            createLocation("foo", p);

            List<Location> hls = readMessages(Location.class);

            assertEquals(GeoJsonType.Point, hls.get(0).location().type());
            assertEquals("foo", hls.get(0).id());
        }

        @Test
        public void testLocationsWithId() throws Exception {

            client.subscribe("v1.1/Locations(bar)", 0, listener).waitForCompletion(5000);

            Point p = new Point(34d, 12d);
            createLocation("foo", p);
            createLocation("bar", p);

            List<Location> hls = readMessages(Location.class);

            assertEquals(GeoJsonType.Point, hls.get(0).location().type());
            assertEquals("bar", hls.get(0).id());

        }

        @SuppressWarnings("unchecked")
        @Test
        public void testLocationsWithIdAndSelection() throws Exception {

            client.subscribe("v1.1/Locations(bar)?$select=@iot.id,location", 0, listener).waitForCompletion(5000);

            Point p = new Point(34d, 12d);
            createLocation("foo", p);
            createLocation("bar", p);

            @SuppressWarnings("rawtypes")
            List<Map> hls = readMessages(Map.class);

            // Creation, value setting, metadata update
            for (@SuppressWarnings("rawtypes")
            Map m : hls) {
                assertEquals(Set.of("@iot.id", ProviderPackage.Literals.ADMIN__LOCATION.getName()), m.keySet());
                assertEquals(GeoJsonType.Point.name(),
                        ((Map<String, Object>) m.get(ProviderPackage.Literals.ADMIN__LOCATION.getName())).get("type"));
                assertEquals("bar", m.get("@iot.id"));
            }
        }

        @Test
        public void testLocationsWithIdAndProperty() throws Exception {

            client.subscribe("v1.1/Locations(bar)/name", 0, listener).waitForCompletion(5000);

            Point p = new Point(34d, 12d);
            Feature f = new Feature("test", p, Map.of("name", "fizzbuzz"), null, null);
            createLocation("bar", "foobar", f);

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(Map.class);

            assertEquals(Map.of("name", "foobar"), streams.get(streams.size() - 1));

            createOrUpdateResourceWithPackageUri("bar", eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "friendlyName",
                    "fizzbuzz", Instant.now());
            streams.addAll(readMessages(Map.class));

            assertEquals(Map.of("name", "fizzbuzz"), streams.get(streams.size() - 1));

        }
    }

    @Nested
    public class ObservationsTests {

        Instant testTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        @Test
        public void testObservationsCollection() throws Exception {

            client.subscribe("v1.1/Observations", 0, listener).waitForCompletion(5000);

            createDatastream("foo", "fizz", "fizz", 42, testTime);

            List<Observation> obs = readMessages(Observation.class);

            assertEquals(testTime, obs.get(obs.size() - 1).resultTime());

            assertEquals(42, obs.get(obs.size() - 1).result());
            assertEquals(testTime, obs.get(obs.size() - 1).resultTime());
            assertTrue(obs.get(obs.size() - 1).id().toString().startsWith("foo~test"));
        }

        @Test
        public void testObservationsWithId() throws Exception {

            client.subscribe("v1.1/Observations(foobar~test)", 0, listener).waitForCompletion(5000);
            createDatastream("foobar", "fizz", "fizz", 42, testTime);
            createDatastream("fizzbuzz", "fizz", "fizz", 17, testTime);

            List<Observation> obs = readMessages(Observation.class);

            assertEquals(42, obs.get(0).result());
            assertEquals(testTime, obs.get(0).resultTime());
            assertTrue(obs.get(0).id().toString().startsWith("foobar~test"));

        }

        @Test
        public void testObservationsWithIdAndSelection() throws Exception {

            client.subscribe("v1.1/Observations(fizzbuzz~test)?$select=@iot.id,result", 0, listener)
                    .waitForCompletion(5000);
            createDatastream("foobar", "fizz", "fizz", 42, testTime);
            createDatastream("fizzbuzz", "fizz", "fizz", 17, testTime);

            @SuppressWarnings("rawtypes")
            List<Map> obs = readMessages(Map.class);

            // Creation, value setting, metadata update
            for (@SuppressWarnings("rawtypes")
            Map m : obs) {
                assertEquals(Set.of("@iot.id", "result"), m.keySet());
                assertEquals(17, m.get("result"));
                assertTrue(m.get("@iot.id").toString().startsWith("fizzbuzz~test"));
            }
        }

        @Test
        public void testObservationsWithIdAndProperty() throws Exception {

            client.subscribe("v1.1/Observations(foobar~test)/phenomenonTime", 0, listener).waitForCompletion(5000);

            Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);

            Instant first = now.minus(Duration.ofSeconds(30));

            createDatastream("foobar", "fizz", "fizz", 42, first);

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(Map.class);

            createDatastream("foobar", "fizz", "foo", 42, now);

            streams.addAll(readMessages(Map.class));

            assertEquals(Map.of("phenomenonTime", first.toString()), streams.get(0));
            assertEquals(Map.of("phenomenonTime", now.toString()), streams.get(1));

        }

    }

    @Nested
    public class ObservedPropertiesTests {

        Instant testTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        @Test
        public void testObservedPropertiesCollection() throws Exception {

            client.subscribe("v1.1/ObservedProperties", 0, listener).waitForCompletion(5000);

            // We must use a different model as otherwise the other tests interfere
            createObservedProperty("op", "op", "op", testTime);

            List<ObservedProperty> obs = readMessages(ObservedProperty.class);

            // Creation
            assertEquals("op", obs.get(obs.size() - 1).name());

        }

        @Test
        public void testObservedPropertiesWithId() throws Exception {

            client.subscribe("v1.1/ObservedProperties(op)", 0, listener).waitForCompletion(5000);
            createObservedProperty("op", "op", "op", testTime);
            List<ObservedProperty> obs = readMessages(ObservedProperty.class);

            // Creation, metadata update
            assertEquals("op", obs.get(obs.size() - 1).name());

            createOrUpdateResourceWithPackageUri("op", eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "friendlyName", "op2",
                    Instant.now());
            obs.addAll(readMessages(ObservedProperty.class));

            assertEquals("op2", obs.get(obs.size() - 1).name());

        }

        @Test
        public void testObservedPropertiesWithIdAndSelection() throws Exception {

            client.subscribe("v1.1/ObservedProperties(op)?$select=@iot.id,name", 0, listener).waitForCompletion(5000);
            createObservedProperty("ds", "op", "test", testTime);
            createObservedProperty("ds", "op2", "op2", testTime);

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(Map.class);

            // Creation, metadata update
            for (@SuppressWarnings("rawtypes")
            Map m : streams) {
                assertEquals(Set.of("@iot.id", "name"), m.keySet());
                assertEquals("test", m.get("name"));
                assertEquals("op", m.get("@iot.id"));
            }
        }

        @Test
        public void testObservedPropertiesWithIdAndProperty() throws Exception {

            client.subscribe("v1.1/ObservedProperties(op)/name", 0, listener).waitForCompletion(5000);
            createObservedProperty("ds", "op", "test", testTime);
            createObservedProperty("ds", "op2", "op2", testTime);

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(Map.class);

            assertEquals(Map.of("name", "test"), streams.get(streams.size() - 1));
            createOrUpdateResourceWithPackageUri("op", eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "friendlyName", "foobar",
                    Instant.now());
            streams.addAll(readMessages(Map.class));

            assertEquals(Map.of("name", "foobar"), streams.get(streams.size() - 1));

        }
    }

    @Nested
    public class SensorsTests {

        Instant testTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        @Test
        public void testSensorsCollection() throws Exception {

            client.subscribe("v1.1/Sensors", 0, listener).waitForCompletion(5000);

            // We must use a different model as otherwise the other tests interfere
            createSensor("ds", "op", "op", testTime);

            List<Sensor> obs = readMessages(Sensor.class);

            // Creation
            int i = 0;
            assertEquals("op", obs.get(i++).name());
            assertEquals("op", obs.get(i++).name());

        }

        @Test
        public void testSensorsWithId() throws Exception {

            client.subscribe("v1.1/Sensors(op)", 0, listener).waitForCompletion(5000);
            createSensor("ds", "op", "test", testTime);
            List<Sensor> obs = readMessages(Sensor.class);

            // Creation, metadata update
            assertEquals("test", obs.get(obs.size() - 1).name());

            createOrUpdateResourceWithPackageUri("op", eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "friendlyName", "op2",
                    Instant.now());
            obs.addAll(readMessages(Sensor.class));

            assertEquals("op2", obs.get(obs.size() - 1).name());

        }

        @Test
        public void testSensorsWithIdAndSelection() throws Exception {

            client.subscribe("v1.1/Sensors(op)?$select=@iot.id,name", 0, listener).waitForCompletion(5000);
            createSensor("ds", "op", "test", testTime);
            createSensor("ds", "op2", "op2", testTime);

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(Map.class);

            // Creation, metadata update
            for (@SuppressWarnings("rawtypes")
            Map m : streams) {
                assertEquals(Set.of("@iot.id", "name"), m.keySet());
                assertEquals("test", m.get("name"));
                assertEquals("op", m.get("@iot.id"));
            }
        }

        @Test
        public void testSensorsWithIdAndProperty() throws Exception {

            client.subscribe("v1.1/Sensors(op)/name", 0, listener).waitForCompletion(5000);
            createSensor("ds", "op", "op", testTime);
            createSensor("ds", "op2", "op2", testTime);

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(Map.class);

            assertEquals(Map.of("name", "op"), streams.get(streams.size() - 1));
            createOrUpdateResourceWithPackageUri("op", eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "friendlyName", "foobar",
                    Instant.now());
            streams.addAll(readMessages(Map.class));

            assertEquals(Map.of("name", "foobar"), streams.get(streams.size() - 1));

        }
    }

    @Nested
    public class ThingsTests {

        @Test
        public void testThingsCollection() throws Exception {

            client.subscribe("v1.1/Things", 0, listener).waitForCompletion(5000);

            createThing("foo");

            List<Thing> obs = readMessages(Thing.class);

            // Creation, value setting, metadata updates x 2
            assertEquals("foo", obs.get(obs.size() - 1).name());
        }

        @Test
        public void testThingsWithId() throws Exception {

            client.subscribe("v1.1/Things(foo)", 0, listener).waitForCompletion(5000);
            createThing("fizz");
            createThing("foo");

            List<Thing> obs = readMessages(Thing.class);

            // Creation, value setting, metadata update
            assertEquals("foo", obs.get(0).name());

            assertEquals("foo", obs.get(obs.size() - 1).name());

        }

        @Test
        public void testThingsWithIdAndSelection() throws Exception {

            client.subscribe("v1.1/Things(foo)?$select=@iot.id,name", 0, listener).waitForCompletion(5000);
            createThing("fizz", "foobar");
            createThing("foo", "foo");

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(Map.class);

            // Creation, value setting, metadata update
            for (@SuppressWarnings("rawtypes")
            Map m : streams) {
                assertEquals(Set.of("@iot.id", "name"), m.keySet());
                assertEquals("foo", m.get("name"));
                assertEquals("foo", m.get("@iot.id"));
            }
        }

        @Test
        public void testThingsWithIdAndProperty() throws Exception {

            client.subscribe("v1.1/Things(foo)/name", 0, listener).waitForCompletion(5000);
            createThing("fizz", "fizzbuzz");
            createThing("foo", "fizzbuzz");

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(Map.class);
            assertEquals(Map.of("name", "foo"), streams.get(0));
            assertEquals(Map.of("name", "foo"), streams.get(streams.size() - 1));

            createOrUpdateResourceWithPackageUri("foo", eNS_URI, DtoMapperSimple.SERVICE_ADMIN, "friendlyName",
                    "foobar", Instant.now());
            streams.addAll(readMessages(Map.class));
            assertEquals(Map.of("name", "foo"), streams.get(0));
            assertEquals(Map.of("name", "foobar"), streams.get(streams.size() - 1));

        }

    }
}

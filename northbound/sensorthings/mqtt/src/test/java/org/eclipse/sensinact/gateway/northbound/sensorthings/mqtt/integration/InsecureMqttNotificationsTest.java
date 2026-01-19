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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
import org.eclipse.sensinact.gateway.geojson.Feature;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@WithFactoryConfiguration(factoryPid = "sensiNact.northbound.sensorthings.mqtt", properties = {
        @Property(key = "port", value = "13579"), @Property(key = "websocket.enable", value = "false") })
public class InsecureMqttNotificationsTest {

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

        mapper = JsonMapper.builder().addModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true).build();
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

    protected void createResource(String provider, String service, String resource, Object value) {
        createResource(provider, service, resource, value, null);
    }

    protected void createResource(String provider, String service, String resource, Object value, Instant instant) {
        GenericDto dto = new GenericDto();
        dto.provider = provider;
        dto.service = service;
        dto.resource = resource;
        dto.type = value.getClass();
        dto.value = value;
        dto.timestamp = instant;
        try {
            push.pushUpdate(dto).getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void updateMetadata(String provider, String service, String resource, String key, Object value) {
        GenericDto dto = new GenericDto();
        dto.provider = provider;
        dto.service = service;
        dto.resource = resource;
        dto.metadata = Map.of(key, value);
        try {
            push.pushUpdate(dto).getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> readMessages(int expected, Class<T> type)
            throws InterruptedException, JsonProcessingException, JsonMappingException {
        List<T> streams = new ArrayList<>();

        String message = messages.poll(1500, TimeUnit.MILLISECONDS);
        for (int i = 0; i < expected; i++) {
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
            createResource("data", "bar", "foobar", 17);

            List<Datastream> streams = readMessages(10, Datastream.class);

            // Creation in sorted event order (p/s/r uri)
            int i=0;
            assertEquals(ProviderPackage.Literals.ADMIN__DESCRIPTION.getName(), streams.get(i++).name());
            assertEquals(ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), streams.get(i++).name());
            assertEquals(ProviderPackage.Literals.ADMIN__MODEL.getName(), streams.get(i++).name());
            assertEquals(ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName(), streams.get(i++).name());
            assertEquals("foobar", streams.get(i++).name());

            // metadata update in sorted event order (p/s/r uri)
            assertEquals(ProviderPackage.Literals.ADMIN__DESCRIPTION.getName(), streams.get(i++).name());
            assertEquals(ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), streams.get(i++).name());
            assertEquals(ProviderPackage.Literals.ADMIN__MODEL.getName(), streams.get(i++).name());
            assertEquals(ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName(), streams.get(i++).name());
            assertEquals("foobar", streams.get(i).name());

            Point p = new Point(34d, 12d);

            createResource("data", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), p);

            streams = readMessages(8, Datastream.class);
            streams.forEach(d -> System.err.println(d.name()));

            // location creation
            i = 0;
            assertEquals(ProviderPackage.Literals.ADMIN__LOCATION.getName(), streams.get(i++).name());

            // location metadata update
            assertEquals(ProviderPackage.Literals.ADMIN__LOCATION.getName(), streams.get(i++).name());

            // location value update
            assertEquals(ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), streams.get(i++).name());
            assertEquals(ProviderPackage.Literals.ADMIN__DESCRIPTION.getName(), streams.get(i++).name());
            assertEquals(ProviderPackage.Literals.ADMIN__LOCATION.getName(), streams.get(i++).name());
            assertEquals(ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName(), streams.get(i++).name());
            assertEquals(ProviderPackage.Literals.ADMIN__MODEL.getName(), streams.get(i++).name());
            assertEquals("foobar", streams.get(i).name());
        }

        @Test
        public void testDatastreamsWithId() throws Exception {

            client.subscribe("v1.1/Datastreams(foo~bar~fizzbuzz)", 0, listener).waitForCompletion(5000);

            createResource("foo", "bar", "foobar", 17);
            createResource("foo", "bar", "fizzbuzz", 42);
            Point p = new Point(34d, 12d);

            createResource("foo", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), p);

            List<Datastream> streams = readMessages(3, Datastream.class);

            // Creation, metadata update, location update
            assertEquals("fizzbuzz", streams.get(0).name());
            assertEquals("fizzbuzz", streams.get(1).name());
            assertEquals("fizzbuzz", streams.get(2).name());

        }

        @Test
        public void testDatastreamsWithIdAndSelection() throws Exception {

            client.subscribe("v1.1/Datastreams(foo~bar~fizzbuzz)?$select=@iot.id,name", 0, listener)
                    .waitForCompletion(5000);

            createResource("foo", "bar", "foobar", 17);
            createResource("foo", "bar", "fizzbuzz", 42);

            Point p = new Point(34d, 12d);

            createResource("foo", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), p);

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(3, Map.class);

            // Creation, metadata update, location update
            for (@SuppressWarnings("rawtypes")
            Map m : streams) {
                assertEquals(Set.of("@iot.id", "name"), m.keySet());
                assertEquals("fizzbuzz", m.get("name"));
                assertEquals("foo~bar~fizzbuzz", m.get("@iot.id"));
            }
        }

        @Test
        public void testDatastreamsWithIdAndProperty() throws Exception {

            client.subscribe("v1.1/Datastreams(foo~bar~fizzbuzz)/name", 0, listener).waitForCompletion(5000);

            createResource("fizz", "buzz", "fizzbuzz", 42);
            createResource("foo", "bar", "fizzbuzz", 42);

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(2, Map.class);

            updateMetadata("foo", "bar", "fizzbuzz", ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), "foobar");

            streams.addAll(readMessages(1, Map.class));

            assertEquals(Map.of("name", "fizzbuzz"), streams.get(0));
            assertEquals(Map.of("name", "fizzbuzz"), streams.get(1));
            assertEquals(Map.of("name", "foobar"), streams.get(2));

        }
    }

    @Nested
    public class FeaturesOfInterestTests {

        @Test
        public void testFeaturesOfInterestCollection() throws Exception {

            client.subscribe("v1.1/FeaturesOfInterest", 0, listener).waitForCompletion(5000);

            Point p = new Point(34d, 12d);

            createResource("foo", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), p);

            List<FeatureOfInterest> fois = readMessages(1, FeatureOfInterest.class);

            assertEquals("foo", fois.get(0).name());
            assertEquals("foo", fois.get(0).id());
        }

        @Test
        public void testFeaturesOfInterestWithId() throws Exception {

            client.subscribe("v1.1/FeaturesOfInterest(bar)", 0, listener).waitForCompletion(5000);

            Point p = new Point(34d, 12d);

            createResource("foo", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), p);
            createResource("bar", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), p);

            List<FeatureOfInterest> fois = readMessages(1, FeatureOfInterest.class);

            assertEquals("bar", fois.get(0).name());
            assertEquals("bar", fois.get(0).id());

        }

        @Test
        public void testFeaturesOfInterestWithIdAndSelection() throws Exception {

            client.subscribe("v1.1/FeaturesOfInterest(bar)?$select=@iot.id,name", 0, listener).waitForCompletion(5000);

            Point p = new Point(34d, 12d);

            createResource("foo", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), p);
            createResource("bar", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), p);

            @SuppressWarnings("rawtypes")
            List<Map> fois = readMessages(1, Map.class);

            // Creation, value setting, metadata update
            for (@SuppressWarnings("rawtypes")
            Map m : fois) {
                assertEquals(Set.of("@iot.id", "name"), m.keySet());
                assertEquals("bar", m.get("name"));
                assertEquals("bar", m.get("@iot.id"));
            }
        }

        @Test
        public void testFeaturesOfInterestWithIdAndProperty() throws Exception {

            client.subscribe("v1.1/FeaturesOfInterest(bar)/name", 0, listener).waitForCompletion(5000);

            Point p = new Point(34d, 12d);
            Feature f = new Feature("test", p, Map.of("name", "fizzbuzz"), null, null);

            createResource("bar", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), f);

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(1, Map.class);

            f = new Feature(f.id(), f.geometry(), Map.of("name", "foobar"), f.bbox(), f.foreignMembers());
            createResource("bar", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), f);

            streams.addAll(readMessages(1, Map.class));

            assertEquals(Map.of("name", "fizzbuzz"), streams.get(0));
            assertEquals(Map.of("name", "foobar"), streams.get(1));

        }
    }

    @Nested
    public class HistoricalLocationsTests {

        Instant testTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        @Test
        public void testHistoricalLocationsCollection() throws Exception {

            client.subscribe("v1.1/HistoricalLocations", 0, listener).waitForCompletion(5000);

            Point p = new Point(34d, 12d);

            createResource("foo", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), p, testTime);

            List<HistoricalLocation> hls = readMessages(1, HistoricalLocation.class);

            assertEquals(testTime, hls.get(0).time());
            assertEquals("foo~", String.valueOf(hls.get(0).id()).substring(0, 4));
        }

        @Test
        public void testHistoricalLocationsWithId() throws Exception {

            client.subscribe("v1.1/HistoricalLocations(bar)", 0, listener).waitForCompletion(5000);

            Point p = new Point(34d, 12d);

            createResource("foo", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), p, testTime);
            createResource("bar", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), p, testTime);

            List<HistoricalLocation> hls = readMessages(1, HistoricalLocation.class);

            assertEquals(testTime, hls.get(0).time());
            assertEquals("bar~", String.valueOf(hls.get(0).id()).substring(0, 4));

        }

        @Test
        public void testHistoricalLocationsWithIdAndSelection() throws Exception {

            client.subscribe("v1.1/HistoricalLocations(bar)?$select=@iot.id,time", 0, listener).waitForCompletion(5000);

            Point p = new Point(34d, 12d);

            createResource("foo", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), p, testTime);
            createResource("bar", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), p, testTime);

            @SuppressWarnings("rawtypes")
            List<Map> hls = readMessages(1, Map.class);

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

        Instant testTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        @Test
        public void testLocationsCollection() throws Exception {

            client.subscribe("v1.1/Locations", 0, listener).waitForCompletion(5000);

            Point p = new Point(34d, 12d);

            createResource("foo", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), p, testTime);

            List<Location> hls = readMessages(1, Location.class);

            assertEquals(GeoJsonType.Point, hls.get(0).location().type());
            assertEquals("foo~", String.valueOf(hls.get(0).id()).substring(0, 4));
        }

        @Test
        public void testLocationsWithId() throws Exception {

            client.subscribe("v1.1/Locations(bar)", 0, listener).waitForCompletion(5000);

            Point p = new Point(34d, 12d);

            createResource("foo", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), p, testTime);
            createResource("bar", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), p, testTime);

            List<Location> hls = readMessages(1, Location.class);

            assertEquals(GeoJsonType.Point, hls.get(0).location().type());
            assertEquals("bar~", String.valueOf(hls.get(0).id()).substring(0, 4));

        }

        @SuppressWarnings("unchecked")
        @Test
        public void testLocationsWithIdAndSelection() throws Exception {

            client.subscribe("v1.1/Locations(bar)?$select=@iot.id,location", 0, listener).waitForCompletion(5000);

            Point p = new Point(34d, 12d);

            createResource("foo", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), p, testTime);
            createResource("bar", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), p, testTime);

            @SuppressWarnings("rawtypes")
            List<Map> hls = readMessages(1, Map.class);

            // Creation, value setting, metadata update
            for (@SuppressWarnings("rawtypes")
            Map m : hls) {
                assertEquals(Set.of("@iot.id", ProviderPackage.Literals.ADMIN__LOCATION.getName()), m.keySet());
                assertEquals(GeoJsonType.Point.name(),
                        ((Map<String, Object>) m.get(ProviderPackage.Literals.ADMIN__LOCATION.getName())).get("type"));
                assertEquals("bar~", String.valueOf(m.get("@iot.id")).substring(0, 4));
            }
        }

        @Test
        public void testLocationsWithIdAndProperty() throws Exception {

            client.subscribe("v1.1/Locations(bar)/name", 0, listener).waitForCompletion(5000);

            Point p = new Point(34d, 12d);
            Feature f = new Feature("test", p, Map.of("name", "fizzbuzz"), null, null);

            createResource("bar", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), f);

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(1, Map.class);

            f = new Feature(f.id(), f.geometry(), Map.of("name", "foobar"), f.bbox(), f.foreignMembers());
            createResource("bar", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__LOCATION.getName(), f);

            streams.addAll(readMessages(1, Map.class));

            assertEquals(Map.of("name", "fizzbuzz"), streams.get(0));
            assertEquals(Map.of("name", "foobar"), streams.get(1));

        }
    }

    @Nested
    public class ObservationsTests {

        Instant testTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        @Test
        public void testObservationsCollection() throws Exception {

            client.subscribe("v1.1/Observations", 0, listener).waitForCompletion(5000);

            createResource("foo", "bar", "foobar", 42, testTime);

            List<Observation> obs = readMessages(5, Observation.class);

            assertNull(obs.get(0).result());
            assertEquals(testTime, obs.get(0).resultTime());

            assertEquals("foo", obs.get(1).result());
            assertEquals(testTime, obs.get(1).resultTime());
            assertEquals(
                    "foo~" + ProviderPackage.Literals.PROVIDER__ADMIN.getName() + "~"
                            + ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(),
                    String.valueOf(obs.get(1).id()).substring(0, 22));

            assertEquals(testTime, obs.get(2).resultTime());
            assertEquals(
                    "foo~" + ProviderPackage.Literals.PROVIDER__ADMIN.getName() + "~"
                            + ProviderPackage.Literals.ADMIN__MODEL.getName(),
                    String.valueOf(obs.get(2).id()).substring(0, 15));

            assertEquals(testTime, obs.get(3).resultTime());
            assertEquals(
                    "foo~" + ProviderPackage.Literals.PROVIDER__ADMIN
                            .getName() + "~" + ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName(),
                    String.valueOf(obs.get(3).id()).substring(0,
                            ("foo~" + ProviderPackage.Literals.PROVIDER__ADMIN.getName() + "~"
                                    + ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName()).length()));

            assertEquals(42, obs.get(4).result());
            assertEquals(testTime, obs.get(4).resultTime());
            assertEquals("foo~bar~foobar", String.valueOf(obs.get(4).id()).substring(0, 14));
        }

        @Test
        public void testObservationsWithId() throws Exception {

            client.subscribe("v1.1/Observations(foo~bar~fizzbuzz)", 0, listener).waitForCompletion(5000);

            createResource("foo", "bar", "foobar", 42, testTime);
            createResource("foo", "bar", "fizzbuzz", 17, testTime);

            List<Observation> obs = readMessages(1, Observation.class);

            assertEquals(17, obs.get(0).result());
            assertEquals(testTime, obs.get(0).resultTime());
            assertEquals("foo~bar~fizzbuzz", String.valueOf(obs.get(0).id()).substring(0, 16));

        }

        @Test
        public void testObservationsWithIdAndSelection() throws Exception {

            client.subscribe("v1.1/Observations(foo~bar~fizzbuzz)?$select=@iot.id,result", 0, listener)
                    .waitForCompletion(5000);

            createResource("foo", "bar", "foobar", 42, testTime);
            createResource("foo", "bar", "fizzbuzz", 17, testTime);

            @SuppressWarnings("rawtypes")
            List<Map> obs = readMessages(1, Map.class);

            // Creation, value setting, metadata update
            for (@SuppressWarnings("rawtypes")
            Map m : obs) {
                assertEquals(Set.of("@iot.id", "result"), m.keySet());
                assertEquals(17, m.get("result"));
                assertEquals("foo~bar~fizzbuzz", String.valueOf(m.get("@iot.id")).substring(0, 16));
            }
        }

        @Test
        public void testObservationsWithIdAndProperty() throws Exception {

            client.subscribe("v1.1/Observations(foo~bar~fizzbuzz)/phenomenonTime", 0, listener).waitForCompletion(5000);

            Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);

            Instant first = now.minus(Duration.ofSeconds(30));

            createResource("foo", "bar", "fizzbuzz", 42, first);

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(1, Map.class);

            createResource("foo", "bar", "fizzbuzz", 42, now);

            streams.addAll(readMessages(1, Map.class));

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
            createResource("obs", "bar", "foobar", 42, testTime);

            List<ObservedProperty> obs = readMessages(10, ObservedProperty.class);

            // Creation
            int i = 0;
            assertEquals(ProviderPackage.Literals.ADMIN__DESCRIPTION.getName(), obs.get(i++).name());
            assertEquals(ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), obs.get(i++).name());
            assertEquals(ProviderPackage.Literals.ADMIN__MODEL.getName(), obs.get(i++).name());
            assertEquals(ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName(), obs.get(i++).name());
            assertEquals("foobar", obs.get(i++).name());

            // metadata update
            assertEquals(ProviderPackage.Literals.ADMIN__DESCRIPTION.getName(), obs.get(i++).name());
            assertEquals(ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), obs.get(i++).name());
            assertEquals(ProviderPackage.Literals.ADMIN__MODEL.getName(), obs.get(i++).name());
            assertEquals(ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName(), obs.get(i++).name());
            assertEquals("foobar", obs.get(i++).name());

        }

        @Test
        public void testObservedPropertiesWithId() throws Exception {

            client.subscribe("v1.1/ObservedProperties(foo~bar~fizzbuzz)", 0, listener).waitForCompletion(5000);

            createResource("foo", "bar", "foobar", 42, testTime);
            createResource("foo", "bar", "fizzbuzz", 17, testTime);

            List<ObservedProperty> obs = readMessages(2, ObservedProperty.class);

            // Creation, metadata update
            assertEquals("fizzbuzz", obs.get(0).name());
            assertEquals("fizzbuzz", obs.get(1).name());

        }

        @Test
        public void testObservedPropertiesWithIdAndSelection() throws Exception {

            client.subscribe("v1.1/ObservedProperties(foo~bar~fizzbuzz)?$select=@iot.id,name", 0, listener)
                    .waitForCompletion(5000);

            createResource("foo", "bar", "foobar", 42, testTime);
            createResource("foo", "bar", "fizzbuzz", 17, testTime);

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(2, Map.class);

            // Creation, metadata update
            for (@SuppressWarnings("rawtypes")
            Map m : streams) {
                assertEquals(Set.of("@iot.id", "name"), m.keySet());
                assertEquals("fizzbuzz", m.get("name"));
                assertEquals("foo~bar~fizzbuzz", m.get("@iot.id"));
            }
        }

        @Test
        public void testObservedPropertiesWithIdAndProperty() throws Exception {

            client.subscribe("v1.1/ObservedProperties(foo~bar~fizzbuzz)/name", 0, listener).waitForCompletion(5000);

            createResource("fizz", "buzz", "fizzbuzz", 42);
            createResource("foo", "bar", "fizzbuzz", 42);

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(2, Map.class);

            updateMetadata("foo", "bar", "fizzbuzz", ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), "foobar");

            streams.addAll(readMessages(1, Map.class));

            assertEquals(Map.of("name", "fizzbuzz"), streams.get(0));
            assertEquals(Map.of("name", "fizzbuzz"), streams.get(1));
            assertEquals(Map.of("name", "foobar"), streams.get(2));

        }
    }

    @Nested
    public class SensorsTests {

        Instant testTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        @Test
        public void testSensorsCollection() throws Exception {

            client.subscribe("v1.1/Sensors", 0, listener).waitForCompletion(5000);

            // We must use a different model as otherwise the other tests interfere
            // by including other resources
            createResource("sens", "bar", "foobar", 42, testTime);

            List<Sensor> sens = readMessages(10, Sensor.class);

            // Creation
            assertEquals(ProviderPackage.Literals.ADMIN__DESCRIPTION.getName(), sens.get(0).name());
            assertEquals(ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), sens.get(1).name());
            assertEquals(ProviderPackage.Literals.ADMIN__MODEL.getName(), sens.get(2).name());
            assertEquals(ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName(), sens.get(3).name());
            assertEquals("foobar", sens.get(4).name());

            // metadata update
            assertEquals(ProviderPackage.Literals.ADMIN__DESCRIPTION.getName(), sens.get(5).name());
            assertEquals(ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), sens.get(6).name());
            assertEquals(ProviderPackage.Literals.ADMIN__MODEL.getName(), sens.get(7).name());
            assertEquals(ProviderPackage.Literals.ADMIN__MODEL_PACKAGE_URI.getName(), sens.get(8).name());
            assertEquals("foobar", sens.get(9).name());

        }

        @Test
        public void testSensorsWithId() throws Exception {

            client.subscribe("v1.1/Sensors(foo~bar~fizzbuzz)", 0, listener).waitForCompletion(5000);

            createResource("foo", "bar", "foobar", 42, testTime);
            createResource("foo", "bar", "fizzbuzz", 17, testTime);

            List<Sensor> obs = readMessages(2, Sensor.class);

            // Creation, metadata update
            assertEquals("fizzbuzz", obs.get(0).name());
            assertEquals("fizzbuzz", obs.get(1).name());

        }

        @Test
        public void testSensorsWithIdAndSelection() throws Exception {

            client.subscribe("v1.1/Sensors(foo~bar~fizzbuzz)?$select=@iot.id,name", 0, listener)
                    .waitForCompletion(5000);

            createResource("foo", "bar", "foobar", 42, testTime);
            createResource("foo", "bar", "fizzbuzz", 17, testTime);

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(2, Map.class);

            // Creation, value setting, metadata update
            for (@SuppressWarnings("rawtypes")
            Map m : streams) {
                assertEquals(Set.of("@iot.id", "name"), m.keySet());
                assertEquals("fizzbuzz", m.get("name"));
                assertEquals("foo~bar~fizzbuzz", m.get("@iot.id"));
            }
        }

        @Test
        public void testSensorsWithIdAndProperty() throws Exception {

            client.subscribe("v1.1/Sensors(foo~bar~fizzbuzz)/name", 0, listener).waitForCompletion(5000);

            createResource("fizz", "buzz", "fizzbuzz", 42);
            createResource("foo", "bar", "fizzbuzz", 42);

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(2, Map.class);

            updateMetadata("foo", "bar", "fizzbuzz", ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), "foobar");

            streams.addAll(readMessages(1, Map.class));

            assertEquals(Map.of("name", "fizzbuzz"), streams.get(0));
            assertEquals(Map.of("name", "fizzbuzz"), streams.get(1));
            assertEquals(Map.of("name", "foobar"), streams.get(2));

        }
    }

    @Nested
    public class ThingsTests {

        Instant testTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        @Test
        public void testThingsCollection() throws Exception {

            client.subscribe("v1.1/Things", 0, listener).waitForCompletion(5000);

            createResource("foo", "bar", "foobar", 42, testTime);

            List<Thing> obs = readMessages(3, Thing.class);

            // Creation, value setting, metadata updates x 2
            assertEquals("foo", obs.get(0).name());
            assertEquals("foo", obs.get(1).name());
        }

        @Test
        public void testThingsWithId() throws Exception {

            client.subscribe("v1.1/Things(foo)", 0, listener).waitForCompletion(5000);

            createResource("fizz", "buzz", "fizzbuzz", 17, testTime);
            createResource("foo", "bar", "foobar", 42, testTime);

            List<Thing> obs = readMessages(3, Thing.class);

            // Creation, value setting, metadata update
            assertEquals("foo", obs.get(0).name());
            assertEquals("foo", obs.get(1).name());

        }

        @Test
        public void testThingsWithIdAndSelection() throws Exception {

            client.subscribe("v1.1/Things(foo)?$select=@iot.id,name", 0, listener).waitForCompletion(5000);

            createResource("foo", "bar", "foobar", 42, testTime);
            createResource("foo", "bar", "fizzbuzz", 17, testTime);

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(3, Map.class);

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

            createResource("fizz", "buzz", "fizzbuzz", 42);
            createResource("foo", "bar", "fizzbuzz", 42);

            @SuppressWarnings("rawtypes")
            List<Map> streams = readMessages(1, Map.class);

            createResource("foo", ProviderPackage.Literals.PROVIDER__ADMIN.getName(),
                    ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName(), "foobar");

            streams.addAll(readMessages(1, Map.class));

            assertEquals(Map.of("name", "foo"), streams.get(0));
            assertEquals(Map.of("name", "foobar"), streams.get(1));

        }

    }

}

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
package org.eclipse.sensinact.gateway.southbound.sensorthings.sensing.rest.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.abort;
import static org.osgi.test.common.annotation.Property.ValueSource.SystemProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.sensinact.core.notification.ResourceDataNotification;
import org.eclipse.sensinact.core.snapshot.ICriterion;
import org.eclipse.sensinact.core.snapshot.ProviderSnapshot;
import org.eclipse.sensinact.core.snapshot.ResourceSnapshot;
import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.gateway.geojson.Feature;
import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.gateway.geojson.GeoJsonType;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.model.core.provider.ProviderPackage;
import org.eclipse.sensinact.northbound.security.api.UserInfo;
import org.eclipse.sensinact.northbound.session.SensiNactSession;
import org.eclipse.sensinact.northbound.session.SensiNactSessionManager;
import org.eclipse.sensinact.southbound.sensorthings.model.sensorthings.SensorthingsPackage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithConfiguration;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@WithConfiguration(pid = "sensinact.session.manager", properties = @Property(key = "auth.policy", value = "ALLOW_ALL"))
class GatewayProxyIntegrationTest {

    private static final String FROST_URI_PROP = "frost.uri";

    static ComposeContainer FROST_RUNTIME;

    static URI FROST_URL;

    static HttpClient CLIENT;

    @InjectService
    SensiNactSessionManager sessionManager;
    SensiNactSession session;
    BlockingQueue<ResourceDataNotification> queue;

    @SuppressWarnings("resource")
    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        Path resourcesDir = Paths.get(System.getProperty("project.basedir"),
                "src/test/resources/");

        CLIENT = HttpClient.newHttpClient();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(GatewayProxyIntegrationTest.class.getClassLoader());
        try {
            try {
                DockerClientFactory.lazyClient().versionCmd().exec();
            } catch (Throwable t) {
                abort("No docker executable on the path, so tests will be skipped");
            }
            FROST_RUNTIME =
                    new ComposeContainer(resourcesDir.resolve("frost-compose.yaml").toFile())
                    .withExposedService("web",
                            8080,
                            Wait.forLogMessage(".*org.apache.catalina.startup.Catalina.start Server startup in.*\\n",
                                    1));

            FROST_RUNTIME.start();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }

        FROST_URL = URI.create(String.format("http://%s:%d/FROST-Server/v1.1/", FROST_RUNTIME.getServiceHost("web", 8080),
                FROST_RUNTIME.getServicePort("web", 8080)));

        HttpRequest request = HttpRequest.newBuilder().uri(FROST_URL.resolve("Things"))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofFile(resourcesDir.resolve("frost-demo-entities.json")))
            .build();
        HttpResponse<String> send = CLIENT.send(request, BodyHandlers.ofString());

        assertEquals(201, send.statusCode());

        System.setProperty(FROST_URI_PROP, FROST_URL.toString());
    }

    @AfterAll
    static void tearDownAfterClass() throws Exception {
        if (FROST_RUNTIME != null) {
            FROST_RUNTIME.stop();
            FROST_RUNTIME = null;
        }
    }

    @BeforeEach
    void start() throws InterruptedException {
        session = sessionManager.getDefaultSession(UserInfo.ANONYMOUS);
        queue = new ArrayBlockingQueue<>(32);
        session.addListener(List.of("*"),
                (t,r) -> queue.offer(r), null, null, null);
    }

    @AfterEach
    void stop() {
        session.activeListeners().keySet().forEach(session::removeListener);
        session = null;
    }

    @WithConfiguration(pid = "sensinact.sensorthings.southbound.rest", properties = @Property(key = "uri", value = FROST_URI_PROP, source = SystemProperty))
    @Test
    void test() throws InterruptedException {

        ResourceDataNotification poll = queue.poll(10, TimeUnit.SECONDS);
        assertNotNull(poll);

        ICriterion criterion = Mockito.mock(ICriterion.class);
        Mockito.when(criterion.getProviderFilter()).thenReturn(p -> "Living_Room".equals(p.getName()));

        List<ProviderSnapshot> snapshot = session.filteredSnapshot(criterion);

        assertEquals(1, snapshot.size());

        // Provider level data
        ProviderSnapshot ps = snapshot.get(0);
        assertEquals(SensorthingsPackage.eNS_URI, ps.getModelPackageUri());
        assertEquals(SensorthingsPackage.Literals.SENSOR_THINGS_DEVICE.getName(), ps.getModelName());
        assertEquals("Living_Room", ps.getName());

        assertEquals(4, ps.getServices().size());

        // Admin data
        ServiceSnapshot service = ps.getService(ProviderPackage.Literals.PROVIDER__ADMIN.getName());
        assertNotNull(service);
        assertEquals("Living Room", service.getResource(ProviderPackage.Literals.ADMIN__FRIENDLY_NAME.getName())
                .getValue().getValue());

        GeoJsonObject geo = (GeoJsonObject) service.getResource(ProviderPackage.Literals.ADMIN__LOCATION.getName())
                .getValue().getValue();
        assertNotNull(geo);
        assertEquals(GeoJsonType.Feature, geo.type);
        Feature f = (Feature) geo;
        assertEquals(GeoJsonType.Point, f.geometry.type);
        assertEquals(49.015308d, ((Point)f.geometry).coordinates.latitude, 0.000001d);
        assertEquals(8.4259727d, ((Point)f.geometry).coordinates.longitude, 0.000001d);
        assertEquals("My Living Room", f.properties.get("sensorthings.location.name"));
        assertEquals("The living room of Fraunhoferstr. 1", f.properties.get("sensorthings.location.description"));

        // Thing level data
        service = ps.getService(SensorthingsPackage.Literals.SENSOR_THINGS_DEVICE__THING.getName());
        assertNotNull(service);
        ResourceSnapshot rs = service.getResource(SensorthingsPackage.Literals.SENSOR_THINGS_SERVICE__ID.getName());
        Map<String, Object> metadata = rs.getMetadata();
        assertEquals(Boolean.TRUE, metadata.get("sensorthings.thing.balcony"));
        assertEquals("Cozy", metadata.get("sensorthings.thing.style"));
        assertEquals("My Living Room", service.getResource(SensorthingsPackage.Literals.SENSOR_THINGS_SERVICE__DESCRIPTION.getName())
                .getValue().getValue());

        // First Datastream
        service = ps.getService("Temperature_Living_Room");
        assertNotNull(service);
        assertEquals("Temperature Living Room", service.getResource(SensorthingsPackage.Literals.DATA_STREAM_SERVICE__NAME.getName())
                .getValue().getValue());
        assertEquals("The temperature in my living room", service.getResource(SensorthingsPackage.Literals.DATA_STREAM_SERVICE__DESCRIPTION.getName())
                .getValue().getValue());
        rs = service.getResource(SensorthingsPackage.Literals.DATA_STREAM_SERVICE__LATEST_OBSERVATION.getName());
        assertEquals(20.0d, rs.getValue().getValue());
        assertEquals(Instant.parse("2019-03-14T10:05:00Z"), rs.getValue().getTimestamp());

        rs = service.getResource(SensorthingsPackage.Literals.DATA_STREAM_SERVICE__UNIT.getName());
        assertEquals("C", rs.getValue().getValue());
        metadata = rs.getMetadata();
        assertEquals("Centigrade", metadata.get("sensorthings.unit.name"));
        assertEquals("http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#DegreeCentigrade", metadata.get("sensorthings.unit.definition"));

        rs = service.getResource(SensorthingsPackage.Literals.DATA_STREAM_SERVICE__SENSOR.getName());
        metadata = rs.getMetadata();
        assertEquals("DHT22/Temperature", metadata.get("sensorthings.sensor.name"));
        assertEquals("Temperature sensor of a DHT22", metadata.get("sensorthings.sensor.description"));
        assertEquals("application/pdf", metadata.get("sensorthings.sensor.encodingType"));
        assertEquals("https://www.sparkfun.com/datasheets/Sensors/Temperature/DHT22.pdf", metadata.get("sensorthings.sensor.metadata"));

        rs = service.getResource(SensorthingsPackage.Literals.DATA_STREAM_SERVICE__OBSERVED_PROPERTY.getName());
        metadata = rs.getMetadata();
        assertEquals("Temperature", metadata.get("sensorthings.observedProperty.name"));
        assertEquals("The temperature.", metadata.get("sensorthings.observedProperty.description"));
        assertEquals("http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#ThermodynamicTemperature", metadata.get("sensorthings.observedProperty.definition"));

        // Second Datastream
        service = ps.getService("Humidity_Living_Room");
        assertNotNull(service);
        assertEquals("Humidity Living Room", service.getResource(SensorthingsPackage.Literals.DATA_STREAM_SERVICE__NAME.getName())
                .getValue().getValue());
        assertEquals("The humidity in my living room", service.getResource(SensorthingsPackage.Literals.DATA_STREAM_SERVICE__DESCRIPTION.getName())
                .getValue().getValue());
        rs = service.getResource(SensorthingsPackage.Literals.DATA_STREAM_SERVICE__LATEST_OBSERVATION.getName());
        assertEquals(41.0d, rs.getValue().getValue());
        assertEquals(Instant.parse("2019-03-14T10:05:00Z"), rs.getValue().getTimestamp());

        rs = service.getResource(SensorthingsPackage.Literals.DATA_STREAM_SERVICE__UNIT.getName());
        assertEquals("%", rs.getValue().getValue());
        metadata = rs.getMetadata();
        assertEquals("percentage", metadata.get("sensorthings.unit.name"));
        assertEquals("https://en.wikipedia.org/wiki/Percentage", metadata.get("sensorthings.unit.definition"));

        rs = service.getResource(SensorthingsPackage.Literals.DATA_STREAM_SERVICE__SENSOR.getName());
        metadata = rs.getMetadata();
        assertEquals("DHT22/Humidity", metadata.get("sensorthings.sensor.name"));
        assertEquals("Relative humidity sensor of a DHT22", metadata.get("sensorthings.sensor.description"));
        assertEquals("application/pdf", metadata.get("sensorthings.sensor.encodingType"));
        assertEquals("https://www.sparkfun.com/datasheets/Sensors/Temperature/DHT22.pdf", metadata.get("sensorthings.sensor.metadata"));

        rs = service.getResource(SensorthingsPackage.Literals.DATA_STREAM_SERVICE__OBSERVED_PROPERTY.getName());
        metadata = rs.getMetadata();
        assertEquals("Relative Humidity", metadata.get("sensorthings.observedProperty.name"));
        assertEquals("The relative humidity", metadata.get("sensorthings.observedProperty.description"));
        assertEquals("https://en.wikipedia.org/wiki/Relative_humidity", metadata.get("sensorthings.observedProperty.definition"));
    }

}

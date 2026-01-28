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
package org.eclipse.sensinact.sensorthings.sensing.rest.integration.sensinact;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Random;

import org.eclipse.sensinact.gateway.geojson.Coordinates;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.model.core.provider.Admin;
import org.eclipse.sensinact.model.core.provider.ProviderFactory;
import org.eclipse.sensinact.model.core.testdata.TestSensor;
import org.eclipse.sensinact.model.core.testdata.TestTemperatur;
import org.eclipse.sensinact.model.core.testdata.TestdataFactory;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Tests that resource value changes are shown correctly
 */
public class ValueSensinactTest extends AbstractIntegrationTest {

    private static final String PROVIDER = "valueTester";
    private static final String LOCATION = "{\"coordinates\": [5.7685,45.192],\"type\": \"Point\"}";

    final Random random = new Random();

    @Test
    void testValueUpdate() throws IOException, InterruptedException {
        // Create resource
        final String svcName = "sensor";
        final String rcName = "data";
        final int value = random.nextInt(1024);
        Instant valueSetInstant = Instant.now();
        createResource(PROVIDER, svcName, rcName, value, valueSetInstant);
        session.setResourceValue(PROVIDER, "admin", "location", LOCATION, valueSetInstant);
        session.setResourceValue(PROVIDER, "admin", "description", "Description", valueSetInstant);

        // Check thing direct access
        Thing thing = utils.queryJson("/Things(" + PROVIDER + ")", Thing.class);
        assertNotNull(thing, "Thing not found");
        assertEquals(PROVIDER, thing.id());

        // Check sensor direct access
        final String sensorId = String.join("~", PROVIDER, svcName, rcName);
        Sensor sensor = utils.queryJson("/Sensors(" + sensorId + ")", Sensor.class);
        assertNotNull(sensor, "Sensor not found");
        assertEquals(sensorId, sensor.id());

        // Get the data stream (should be a single one)
        ResultList<Datastream> streams = utils.queryJson(sensor.datastreamsLink(),
                new TypeReference<ResultList<Datastream>>() {
                });
        assertEquals(1, streams.value().size());
        Datastream stream = streams.value().get(0);

        // Get the observation
        ResultList<Observation> observations = utils.queryJson(stream.observationsLink(),
                new TypeReference<ResultList<Observation>>() {
                });
        assertEquals(1, observations.value().size());
        Observation obs = observations.value().get(0);

        assertEquals(value, obs.result());
        Instant firstResultTime = obs.resultTime();
        assertFalse(valueSetInstant.isAfter(firstResultTime));

        // Update the value
        final int newValue = value + random.nextInt(1024) + 1;
        Instant valueUpdateInstant = Instant.now();
        session.setResourceValue(PROVIDER, svcName, rcName, newValue);

        observations = utils.queryJson(stream.observationsLink(), new TypeReference<ResultList<Observation>>() {
        });
        assertEquals(1, observations.value().size());
        obs = observations.value().get(0);

        assertEquals(newValue, obs.result());
        assertTrue(valueUpdateInstant.isAfter(firstResultTime));
        assertFalse(valueUpdateInstant.isAfter(obs.resultTime()));
    }

    @Test
    void testObservationUnit() throws IOException, InterruptedException {
        // Create resource
        final String svcName = "sensor";
        final String rcName = "rcWithUnit";
        final int value = random.nextInt(1024);
        createResource(PROVIDER, svcName, rcName, value);

        // No unit by default
        Datastream ds = utils.queryJson(
                String.format("/Things(%s)/Datastreams(%s)", PROVIDER, String.join("~", PROVIDER, svcName, rcName)),
                Datastream.class);
        assertNull(ds.unitOfMeasurement().name());
        assertNull(ds.unitOfMeasurement().symbol());
        assertNull(ds.unitOfMeasurement().definition());

        // Set its unit
        final String unitName = "degree Celsius";
        final String unitSymbol = "°C";
        final String unitDefinition = "http://unitsofmeasure.org/ucum.html#para-30";
        session.setResourceMetadata(PROVIDER, svcName, rcName, Map.of("unit", unitSymbol, "sensorthings.unit.name",
                unitName, "sensorthings.unit.definition", unitDefinition));

        // Check in datastream
        ds = utils.queryJson(
                String.format("/Things(%s)/Datastreams(%s)", PROVIDER, String.join("~", PROVIDER, svcName, rcName)),
                Datastream.class);
        assertEquals(unitName, ds.unitOfMeasurement().name());
        assertEquals(unitSymbol, ds.unitOfMeasurement().symbol());
        assertEquals(unitDefinition, ds.unitOfMeasurement().definition());
    }

    @Test
    void testEMFValueUpdate() throws IOException, InterruptedException {
        // Create resource
        final String value = "14 °C";
        Instant valueSetInstant = Instant.now();
        String id = PROVIDER + "10";
        createResourceEMF(id, value);

        // Check thing direct access
        Thing thing = utils.queryJson("/Things(" + id + ")", Thing.class);
        assertNotNull(thing, "Thing not found");
        assertEquals(id, thing.id());
        assertEquals("Foobar", thing.description());

        // Check sensor direct access
        final String sensorId = String.join("~", id, "temp", "v1");
        Sensor sensor = utils.queryJson("/Sensors(" + sensorId + ")", Sensor.class);
        assertNotNull(sensor, "Sensor not found");
        assertEquals(sensorId, sensor.id());

        // Get the data stream (should be a single one)
        ResultList<Datastream> streams = utils.queryJson(sensor.datastreamsLink(),
                new TypeReference<ResultList<Datastream>>() {
                });
        assertEquals(1, streams.value().size());
        Datastream stream = streams.value().get(0);

        // Get the observation
        ResultList<Observation> observations = utils.queryJson(stream.observationsLink(),
                new TypeReference<ResultList<Observation>>() {
                });
        assertEquals(1, observations.value().size());
        Observation obs = observations.value().get(0);

        assertEquals(value, obs.result());
        Instant firstResultTime = obs.resultTime();
        assertFalse(valueSetInstant.isAfter(firstResultTime));

        // Update the value
        final String newValue = "15 °C";
        Instant valueUpdateInstant = Instant.now();
        createResourceEMF(id, newValue);

        observations = utils.queryJson(stream.observationsLink(), new TypeReference<ResultList<Observation>>() {
        });
        assertEquals(1, observations.value().size());
        obs = observations.value().get(0);

        assertEquals(newValue, obs.result());
        assertTrue(valueUpdateInstant.isAfter(firstResultTime));
        assertFalse(valueUpdateInstant.isAfter(obs.resultTime()));
    }

    protected TestSensor createResourceEMF(String provider, String value) {
        TestSensor sensor = TestdataFactory.eINSTANCE.createTestSensor();
        TestTemperatur temp = TestdataFactory.eINSTANCE.createTestTemperatur();
        Admin admin = ProviderFactory.eINSTANCE.createAdmin();
        temp.setV1(value);
        sensor.setTemp(temp);
        sensor.setId(provider);
        sensor.setAdmin(admin);
        Point p = new Point(Coordinates.EMPTY, null, null);
        admin.setLocation(p);
        admin.setDescription("Foobar");
        try {
            push.pushUpdate(sensor).getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return sensor;
    }
}

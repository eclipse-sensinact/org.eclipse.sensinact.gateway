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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Random;

import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Tests that resource value changes are shown correctly
 */
public class ValueTest extends AbstractIntegrationTest {

    private static final String PROVIDER = "valueTester";
    private static final String LOCATION = "{\"coordinates\": [5.7685,45.192],\"type\": \"Point\"}";

    final Random random = new Random();

    @Test
    void testValueUpdate() throws IOException, InterruptedException {
        // Create resource
        final String rcName = "data";
        final int value = random.nextInt(1024);
        Instant valueSetInstant = Instant.now();
        final String provider = "expandTesterThing";
        final String providerDatastream = "expandTesterDatastream";
        final String providerLocation = "expandTesterLocation";

        createThing(provider, List.of(providerLocation), List.of(providerDatastream), valueSetInstant);
        createDatastream(providerDatastream, provider, value, valueSetInstant);
        createLocation(providerLocation);

        // Check thing direct access
        Thing thing = utils.queryJson("/Things(" + provider + ")", Thing.class);
        assertNotNull(thing, "Thing not found");
        assertEquals(provider, thing.id());

        // Check sensor direct access
        final String sensorId = String.join("~", providerDatastream, "test1");
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
        assertFalse(valueSetInstant.isAfter(firstResultTime), String.format("firstResult %s, setInstant %s",
                firstResultTime.toEpochMilli(), valueSetInstant.toEpochMilli()));

        // Update the value
        final int newValue = value + random.nextInt(1024) + 1;
        Instant valueUpdateInstant = Instant.now();
        session.setResourceValue(providerDatastream, UtilDto.SERVICE_DATASTREAM, "lastObservation",
                getObservation("test", newValue, getFeatureOfInterest("test")));

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
        final String provider = "expandTesterThing";
        final String providerDatastream = "expandTesterDatastream";
        final String providerLocation = "expandTesterLocation";

        createThing(provider, List.of(providerLocation), List.of(providerDatastream));
        createDatastream(providerDatastream, provider, value);
        createLocation(providerLocation);

        // No unit by default
        Datastream ds = utils.queryJson(
                String.format("/Things(%s)/Datastreams(%s)", provider, String.join("~", providerDatastream)),
                Datastream.class);
        assertEquals("test", ds.unitOfMeasurement().name());
        assertEquals("test", ds.unitOfMeasurement().symbol());
        assertEquals("test", ds.unitOfMeasurement().definition());

        // Set its unit
        final String unitName = "degree Celsius";
        final String unitSymbol = "°C";
        final String unitDefinition = "http://unitsofmeasure.org/ucum.html#para-30";
        session.setResourceValue(providerDatastream, UtilDto.SERVICE_DATASTREAM, "unitName", unitName);
        session.setResourceValue(providerDatastream, UtilDto.SERVICE_DATASTREAM, "unitSymbol", unitSymbol);
        session.setResourceValue(providerDatastream, UtilDto.SERVICE_DATASTREAM, "unitDefinition", unitDefinition);

        // Check in datastream
        ds = utils.queryJson(String.format("/Things(%s)/Datastreams(%s)", provider, providerDatastream),
                Datastream.class);
        assertEquals(unitName, ds.unitOfMeasurement().name());
        assertEquals(unitSymbol, ds.unitOfMeasurement().symbol());
        assertEquals(unitDefinition, ds.unitOfMeasurement().definition());
    }

}

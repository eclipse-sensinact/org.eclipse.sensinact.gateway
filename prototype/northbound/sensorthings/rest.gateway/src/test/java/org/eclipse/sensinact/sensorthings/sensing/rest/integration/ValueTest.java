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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.util.Random;

import org.eclipse.sensinact.prototype.SensiNactSession;
import org.eclipse.sensinact.prototype.SensiNactSessionManager;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ResultList;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Tests that resource value changes are shown correctly
 */
public class ValueTest {

    private static final String USER = "user";

    private static final String PROVIDER = "valueTester";
    private static final String LOCATION = "{\"coordinates\": [5.7685,45.192],\"type\": \"Point\"}";

    @InjectService
    SensiNactSessionManager sessionManager;
    SensiNactSession session;

    final Random random = new Random();
    final TestUtils utils = new TestUtils();

    @BeforeEach
    void start() throws InterruptedException {
        session = sessionManager.getDefaultSession(USER);
    }

    @AfterEach
    void stop() {
        session = null;
    }

    @Test
    void testValueUpdate() throws IOException, InterruptedException {
        // Create resource
        final String svcName = "sensor";
        final String rcName = "data";
        final int value = random.nextInt(1024);
        Instant valueSetInstant = Instant.now();
        session.setResourceValue(PROVIDER, svcName, rcName, value);
        session.setResourceValue(PROVIDER, "admin", "location", LOCATION);

        // Check thing direct access
        Thing thing = utils.queryJson("/Things(" + PROVIDER + ")", Thing.class);
        assertNotNull(thing, "Thing not found");
        assertEquals(PROVIDER, thing.id);

        // Check sensor direct access
        final String sensorId = String.join("~", PROVIDER, svcName, rcName);
        Sensor sensor = utils.queryJson("/Sensors(" + sensorId + ")", Sensor.class);
        assertNotNull(sensor, "Sensor not found");
        assertEquals(sensorId, sensor.id);

        // Get the data stream (should be a single one)
        ResultList<Datastream> streams = utils.queryJson(sensor.datastreamsLink,
                new TypeReference<ResultList<Datastream>>() {
                });
        assertEquals(1, streams.value.size());
        Datastream stream = streams.value.get(0);

        // Get the observation
        ResultList<Observation> observations = utils.queryJson(stream.observationsLink,
                new TypeReference<ResultList<Observation>>() {
                });
        assertEquals(1, observations.value.size());
        Observation obs = observations.value.get(0);

        assertEquals(value, obs.result);
        Instant firstResultTime = obs.resultTime;
        assertFalse(valueSetInstant.isAfter(firstResultTime));

        // Update the value
        final int newValue = value + random.nextInt(1024) + 1;
        Instant valueUpdateInstant = Instant.now();
        session.setResourceValue(PROVIDER, svcName, rcName, newValue);

        observations = utils.queryJson(stream.observationsLink, new TypeReference<ResultList<Observation>>() {
        });
        assertEquals(1, observations.value.size());
        obs = observations.value.get(0);

        assertEquals(newValue, obs.result);
        assertTrue(valueUpdateInstant.isAfter(firstResultTime));
        assertFalse(valueUpdateInstant.isAfter(obs.resultTime));
    }
}

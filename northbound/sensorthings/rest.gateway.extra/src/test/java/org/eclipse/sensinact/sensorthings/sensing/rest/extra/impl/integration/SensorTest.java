package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Unit test for simple App.
 */
public class SensorTest extends AbstractIntegrationTest {

    @Test
    public void testCreateSensor() throws Exception {
        // given
        String name = "testCreateSensor";
        Sensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);

        UtilsAssert.assertSensor(sensor, json);
        json = getJsonResponseFromGet(String.format("sensors(%s)", getIdFromJson(json)));
        UtilsAssert.assertSensor(sensor, json);

    }
}

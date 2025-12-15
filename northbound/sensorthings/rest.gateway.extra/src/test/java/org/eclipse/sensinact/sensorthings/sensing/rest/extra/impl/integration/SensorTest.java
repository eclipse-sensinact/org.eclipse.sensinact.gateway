package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
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
        ExpandedSensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);

        UtilsAssert.assertSensor(sensor, json);

    }

    @Test
    public void testCreateDatastreamLinkSensor() throws Exception {
        // given
        String name = "testCreateDatastreamLinkSensor";
        ExpandedSensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        String sensorId = getIdFromJson(json);
        UtilsAssert.assertSensor(sensor, json);

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkSensor(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(sensorId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=Sensor", 201);
        ExpandedDataStream expectedDatastream = DtoFactory.getDatastreamMinimalWithThingObervedPropertySensor(
                name + "1", DtoFactory.getRefId(thingId), sensor, null);
        UtilsAssert.assertDatastream(expectedDatastream, json, true);

    }
}

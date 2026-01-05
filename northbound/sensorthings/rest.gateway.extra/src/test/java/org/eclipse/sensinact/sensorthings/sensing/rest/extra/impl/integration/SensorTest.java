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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
        assertNotNull(sensorUseCase.getInMemorySensor(sensorId));

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkSensor(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(sensorId));
        // when
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=Sensor", 201);
        ExpandedDataStream expectedDatastream = DtoFactory.getDatastreamMinimalWithThingObervedPropertySensor(
                name + "1", DtoFactory.getRefId(thingId), sensor, null);
        // then
        UtilsAssert.assertDatastream(expectedDatastream, json, true);
        assertNull(sensorUseCase.getInMemorySensor(sensorId));

    }

    public void testUpdateSensor() throws Exception {
        // given
        String name = "testCreateSensor";
        ExpandedSensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        String idSensor = getIdFromJson(json);
        UtilsAssert.assertSensor(sensor, json);
        ExpandedSensor sensorUpdate = DtoFactory.getSensor(name + "2");
        // when
        json = getJsonResponseFromPut(sensorUpdate, String.format("Sensors(%s)", idSensor), 204);

    }

    @Test
    public void testUpdateDatastreamLinkSensor() throws Exception {
        // given
        String name = "testUpdateDatastreamLinkSensor";
        ExpandedSensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        String sensorId = getIdFromJson(json);
        UtilsAssert.assertSensor(sensor, json);
        assertNotNull(sensorUseCase.getInMemorySensor(sensorId));

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkSensor(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(sensorId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=Sensor", 201);
        ExpandedDataStream expectedDatastream = DtoFactory.getDatastreamMinimalWithThingObervedPropertySensor(
                name + "1", DtoFactory.getRefId(thingId), sensor, null);
        String sensorIdDatastream = getIdFromJson(json.get("Sensor"));
        UtilsAssert.assertDatastream(expectedDatastream, json, true);
        assertNull(sensorUseCase.getInMemorySensor(sensorId));
        // when
        ExpandedSensor sensorUpdate = DtoFactory.getSensor(name + "2");
        json = getJsonResponseFromPut(sensorUpdate, String.format("Sensor(%s)", sensorIdDatastream), 204);
        assertNull(sensorUseCase.getInMemorySensor(sensorId));

    }

    public void testUpdatePatchSensor() throws Exception {
        // given
        String name = "testUpdatePatchSensor";
        ExpandedSensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        String idSensor = getIdFromJson(json);
        UtilsAssert.assertSensor(sensor, json);
        ExpandedSensor sensorUpdate = DtoFactory.getSensor(name + "2");
        // when
        json = getJsonResponseFromPatch(sensorUpdate, String.format("Sensors(%s)", idSensor), 204);
        // then
    }

    @Test
    public void testUpdatePatchDatastreamLinkSensor() throws Exception {
        // given
        String name = "testUpdatePatchDatastreamLinkSensor";
        ExpandedSensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        String sensorId = getIdFromJson(json);
        UtilsAssert.assertSensor(sensor, json);
        assertNotNull(sensorUseCase.getInMemorySensor(sensorId));

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkSensor(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(sensorId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=Sensor", 201);
        ExpandedDataStream expectedDatastream = DtoFactory.getDatastreamMinimalWithThingObervedPropertySensor(
                name + "1", DtoFactory.getRefId(thingId), sensor, null);
        String sensorIdDatastream = getIdFromJson(json.get("Sensor"));
        UtilsAssert.assertDatastream(expectedDatastream, json, true);
        assertNull(sensorUseCase.getInMemorySensor(sensorId));
        // when
        ExpandedSensor sensorUpdate = DtoFactory.getSensor(name + "2", null, "testencodingType");
        json = getJsonResponseFromPatch(sensorUpdate, String.format("Sensor(%s)", sensorIdDatastream), 204);
        assertNull(sensorUseCase.getInMemorySensor(sensorId));
        // then
    }
}

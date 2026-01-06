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
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Map;

import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.rest.UtilDto;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Unit test for simple App.
 */
public class SensorTest extends AbstractIntegrationTest {

    /**
     * test create sensor that will be in memory as not link to a datastream
     *
     * @throws Exception
     */
    @Test
    public void testCreateSensor() throws Exception {
        // given
        String name = "testCreateSensor";
        ExpandedSensor sensor = DtoFactory.getSensor(name);
        // when
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        // then
        UtilsAssert.assertSensor(sensor, json);

    }

    /**
     * test create sensor that will be in memory as not link to a datastream
     *
     * @throws Exception
     */
    @Test
    public void testCreateSensorWithMissingField() throws Exception {
        // given
        String name = "testCreateSensor";
        // when empty name
        ExpandedSensor sensor = DtoFactory.getSensor(null, "test description", "test encodingType", "test metadata");
        getJsonResponseFromPost(sensor, "Sensors", 400);

        // when empty encoding type
        sensor = DtoFactory.getSensor(name, "test description", null, "test metadata");
        getJsonResponseFromPost(sensor, "Sensors", 400);
        // when empty metadata
        sensor = DtoFactory.getSensor(name, "test description", "test encodingType", null);
        getJsonResponseFromPost(sensor, "Sensors", 400);

    }

    /**
     * test create sensor link to a datastream
     *
     * @throws Exception
     */
    @Test
    public void testDeleteSensor() throws Exception {
        // given
        String name = "testDeleteSensor";
        ExpandedSensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        String sensorId = getIdFromJson(json);
        UtilsAssert.assertSensor(sensor, json);
        // when
        getJsonResponseFromDelete(String.format("Sensors(%s)", sensorId), 204);
        // then
        assertNull(sensorCache.getDto(sensorId));
    }

    @Test
    public void testDeleteDatastreamLinkSensor() throws Exception {
        // given
        String name = "testDeleteDatastreamLinkSensor";
        ExpandedSensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        String sensorId = getIdFromJson(json);
        UtilsAssert.assertSensor(sensor, json);
        assertNotNull(sensorCache.getDto(sensorId));

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkSensor(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(sensorId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=Sensor", 201);
        ExpandedDataStream expectedDatastream = DtoFactory.getDatastreamMinimalWithThingObervedPropertySensor(
                name + "1", DtoFactory.getRefId(thingId), sensor, null);
        String idDatastream = getIdFromJson(json);
        JsonNode jsonSensor = json.get("Sensor");
        String isSensorDatastream = getIdFromJson(jsonSensor);
        UtilsAssert.assertDatastream(expectedDatastream, json, true);
        assertNull(sensorCache.getDto(sensorId));
        // when
        getJsonResponseFromDelete(String.format("Sensors(%s)", isSensorDatastream), 400);
        // then
        ServiceSnapshot service = serviceUseCase.read(session, idDatastream, "datastream");
        assertNotNull(UtilDto.getResourceField(service, "sensorId", String.class));
        assertNotNull(UtilDto.getResourceField(service, "sensorName", String.class));
        assertNotNull(UtilDto.getResourceField(service, "sensorDescription", String.class));
        assertNotNull(UtilDto.getResourceField(service, "sensorEncodingType", String.class));
        assertNotNull(UtilDto.getResourceField(service, "sensorMetadata", Object.class));
        assertNotNull(UtilDto.getResourceField(service, "sensorProperties", Map.class));

    }

    @Test
    public void testCreateDatastreamLinkSensor() throws Exception {
        // given
        String name = "testCreateDatastreamLinkSensor";
        ExpandedSensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        String sensorId = getIdFromJson(json);
        UtilsAssert.assertSensor(sensor, json);
        assertNotNull(sensorCache.getDto(sensorId));

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
        assertNull(sensorCache.getDto(sensorId));

    }

    /**
     * Tests that <code>PUT</code> can be used to update a Sensor
     */
    /**
     * test update sensor cached in memory
     *
     * @throws Exception
     */
    @Test
    public void testUpdateSensor() throws Exception {
        // given
        String name = "testCreateSensor";
        ExpandedSensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        String idSensor = getIdFromJson(json);
        UtilsAssert.assertSensor(sensor, json);
        ExpandedSensor sensorUpdate = DtoFactory.getSensor(name + "2");
        // when
        getJsonResponseFromPut(sensorUpdate, String.format("Sensors(%s)", idSensor), 204);
        // then
        assertEquals(name + "2", sensorCache.getDto(idSensor).name());
    }

    /**
     * test update sensor that are link to a datastream
     *
     * @throws Exception
     */
    @Test
    public void testUpdateDatastreamLinkSensor() throws Exception {
        // given
        String name = "testUpdateDatastreamLinkSensor";
        ExpandedSensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        String sensorId = getIdFromJson(json);
        UtilsAssert.assertSensor(sensor, json);
        assertNotNull(sensorCache.getDto(sensorId));

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkSensor(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(sensorId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=Sensor", 201);
        String idDatastream = getIdFromJson(json);
        ExpandedDataStream expectedDatastream = DtoFactory.getDatastreamMinimalWithThingObervedPropertySensor(
                name + "1", DtoFactory.getRefId(thingId), sensor, null);
        String sensorIdDatastream = getIdFromJson(json.get("Sensor"));
        UtilsAssert.assertDatastream(expectedDatastream, json, true);
        assertNull(sensorCache.getDto(sensorId));
        // when
        ExpandedSensor sensorUpdate = DtoFactory.getSensor(name + "2");
        json = getJsonResponseFromPut(sensorUpdate, String.format("Sensors(%s)", sensorIdDatastream), 204);
        assertNull(sensorCache.getDto(sensorId));
        // then
        ServiceSnapshot service = serviceUseCase.read(session, idDatastream, "datastream");
        assertEquals(name + "2", UtilDto.getResourceField(service, "sensorName", String.class));

    }

    /**
     * Tests that <code>PATCH</code> can be used to update a Sensor
     */
    /**
     * test patch sensor cached in memory
     *
     * @throws Exception
     */
    @Test
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
        assertEquals(name + "2", sensorCache.getDto(idSensor).name());

    }

    /**
     * test patch sensor that are link to a datastream
     *
     * @throws Exception
     */
    @Test
    public void testUpdatePatchDatastreamLinkSensor() throws Exception {
        // given
        String name = "testUpdatePatchDatastreamLinkSensor";
        ExpandedSensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        String sensorId = getIdFromJson(json);
        UtilsAssert.assertSensor(sensor, json);
        assertNotNull(sensorCache.getDto(sensorId));

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkSensor(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(sensorId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=Sensor", 201);
        String idDatastream = getIdFromJson(json);
        ExpandedDataStream expectedDatastream = DtoFactory.getDatastreamMinimalWithThingObervedPropertySensor(
                name + "1", DtoFactory.getRefId(thingId), sensor, null);
        String sensorIdDatastream = getIdFromJson(json.get("Sensor"));
        UtilsAssert.assertDatastream(expectedDatastream, json, true);
        assertNull(sensorCache.getDto(sensorId));
        // when
        ExpandedSensor sensorUpdate = DtoFactory.getSensor(name + "2", null, "testencodingType");
        json = getJsonResponseFromPatch(sensorUpdate, String.format("Sensors(%s)", sensorIdDatastream), 204);
        assertNull(sensorCache.getDto(sensorId));
        // then
        ServiceSnapshot service = serviceUseCase.read(session, idDatastream, "datastream");
        assertEquals(name + "2", UtilDto.getResourceField(service, "sensorName", String.class));

    }
}

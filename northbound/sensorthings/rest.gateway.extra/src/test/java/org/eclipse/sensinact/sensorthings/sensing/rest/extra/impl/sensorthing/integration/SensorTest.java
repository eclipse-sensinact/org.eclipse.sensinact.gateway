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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.sensorthing.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.ws.rs.NotFoundException;

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
        Sensor sensor = DtoFactory.getSensor(name);
        // when
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        // then
        UtilsAssert.assertSensor(sensor, json);

    }

    @Test
    public void testCreateSensorAssignTo2Datastream() throws Exception {
        // given
        String name = "testCreateSensorAssignTo2Datastream";
        Sensor sensor = DtoFactory.getSensor(name);
        // when
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        UtilsAssert.assertSensor(sensor, json);
        String sensorId = getIdFromJson(json);

        ExpandedThing thing = DtoFactory.getExpandedThing("testCreateDatastreamLinkObservedPropertyAlreadyExists",
                "testThing existing Location", Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkSensor(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(sensorId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=Sensor,Observations", 201);
        ExpandedDataStream datastream2 = DtoFactory.getDatastreamMinimalLinkThingLinkSensor(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(sensorId));
        json = getJsonResponseFromPost(datastream2, "Datastreams?$expand=Sensor,Observations", 201);

        ServiceSnapshot service = serviceUseCase.read(session, sensorId, "sensor");
        assertEquals(2, DtoMapperSimple.getResourceField(service, "datastreamIds", List.class).size());
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
        Sensor sensor = DtoFactory.getSensor(null, "test description", "test encodingType", "test metadata");
        getJsonResponseFromPost(sensor, "Sensors", 400);

        // when empty encoding type
        sensor = DtoFactory.getSensor(name, "test description", null, "test metadata");
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
        Sensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        String sensorId = getIdFromJson(json);
        UtilsAssert.assertSensor(sensor, json);
        // when
        getJsonResponseFromDelete(String.format("Sensors(%s)", sensorId), 200);
        // then
    }

    /**
     * test delete sensor using Datastream endpoint - return 409
     */
    @Test
    public void testDeleteDatastreamLinkSensor() throws Exception {
        // given
        String name = "testDeleteDatastreamLinkSensor";
        Sensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        String sensorId = getIdFromJson(json);
        UtilsAssert.assertSensor(sensor, json);

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkSensor(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(sensorId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=Sensor,Observations", 201);
        ExpandedDataStream expectedDatastream = DtoFactory.getDatastreamMinimalWithThingObervedPropertySensor(
                name + "1", DtoFactory.getRefId(thingId), sensor, null);
        String idDatastream = getIdFromJson(json);
        JsonNode jsonSensor = json.get("Sensor");
        String isSensorDatastream = getIdFromJson(jsonSensor);
        UtilsAssert.assertDatastream(expectedDatastream, json, true);
        // when
        getJsonResponseFromDelete(String.format("Sensors(%s)", isSensorDatastream), 200);
        // then
        assertThrows(NotFoundException.class, () -> {
            serviceUseCase.read(session, idDatastream, "datastream");
        });
    }

    /**
     * test create sensor link to a datastream
     */
    @Test
    public void testCreateDatastreamLinkSensor() throws Exception {
        // given
        String name = "testCreateDatastreamLinkSensor";
        Sensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        String sensorId = getIdFromJson(json);
        UtilsAssert.assertSensor(sensor, json);

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkSensor(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(sensorId));
        // when
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=Sensor,Observations", 201);
        ExpandedDataStream expectedDatastream = DtoFactory.getDatastreamMinimalWithThingObervedPropertySensor(
                name + "1", DtoFactory.getRefId(thingId), sensor, null);
        // then
        UtilsAssert.assertDatastream(expectedDatastream, json, true);

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
        Sensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        String idSensor = getIdFromJson(json);
        UtilsAssert.assertSensor(sensor, json);
        Sensor sensorUpdate = DtoFactory.getSensor(name + "2");
        // when
        getJsonResponseFromPut(sensorUpdate, String.format("Sensors(%s)", idSensor), 200);
        // then
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
        Sensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        String sensorId = getIdFromJson(json);
        UtilsAssert.assertSensor(sensor, json);

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkSensor(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(sensorId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=Sensor,Observations", 201);
        String idDatastream = getIdFromJson(json);
        ExpandedDataStream expectedDatastream = DtoFactory.getDatastreamMinimalWithThingObervedPropertySensor(
                name + "1", DtoFactory.getRefId(thingId), sensor, null);
        String sensorIdDatastream = getIdFromJson(json.get("Sensor"));
        UtilsAssert.assertDatastream(expectedDatastream, json, true);
        // when
        Sensor sensorUpdate = DtoFactory.getSensor(name + "2");
        json = getJsonResponseFromPut(sensorUpdate, String.format("Sensors(%s)", sensorIdDatastream), 200);
        // then
        ServiceSnapshot service = serviceUseCase.read(session, sensorIdDatastream, "admin");
        assertEquals(name + "2", DtoMapperSimple.getResourceField(service, "friendlyName", String.class));

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
        Sensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        String idSensor = getIdFromJson(json);
        UtilsAssert.assertSensor(sensor, json);
        Sensor sensorUpdate = DtoFactory.getSensor(name + "2");
        // when
        json = getJsonResponseFromPatch(sensorUpdate, String.format("Sensors(%s)", idSensor), 200);
        // then

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
        Sensor sensor = DtoFactory.getSensor(name);
        JsonNode json = getJsonResponseFromPost(sensor, "Sensors", 201);
        String sensorId = getIdFromJson(json);
        UtilsAssert.assertSensor(sensor, json);

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkSensor(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(sensorId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=Sensor,Observations", 201);
        String idDatastream = getIdFromJson(json);
        ExpandedDataStream expectedDatastream = DtoFactory.getDatastreamMinimalWithThingObervedPropertySensor(
                name + "1", DtoFactory.getRefId(thingId), sensor, null);
        String sensorIdDatastream = getIdFromJson(json.get("Sensor"));
        UtilsAssert.assertDatastream(expectedDatastream, json, true);
        // when
        Sensor sensorUpdate = DtoFactory.getSensor(name + "2", null, "testencodingType");
        json = getJsonResponseFromPatch(sensorUpdate, String.format("Sensors(%s)", sensorIdDatastream), 200);
        // then
        ServiceSnapshot service = serviceUseCase.read(session, sensorIdDatastream, "admin");
        assertEquals(name + "2", DtoMapperSimple.getResourceField(service, "friendlyName", String.class));

    }
}

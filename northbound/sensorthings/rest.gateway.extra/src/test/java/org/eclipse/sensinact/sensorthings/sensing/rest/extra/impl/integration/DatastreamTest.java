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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.ws.rs.NotFoundException;

/**
 * Unit test for simple App.
 */
public class DatastreamTest extends AbstractIntegrationTest {

    /**
     * test simple datastream
     *
     * @throws Exception
     */
    @Test
    public void testCreateDatastream() throws Exception {
        // given
        String nameThing = "testCreateDatastreamThing";
        String name = "testCreateDatastream";

        ExpandedThing thing = DtoFactory.getExpandedThing(nameThing, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String idJson = getIdFromJson(json);
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamMinimalLinkThing(name, DtoFactory.getRefId(idJson));

        // when
        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 201);
        UtilsAssert.assertDatastream(dtoDatastream, json);

    }

    /**
     * test datastream with missing field
     *
     * @throws Exception
     */
    @Test
    public void testCreateMissingFieldDatastream() throws Exception {
        // given
        String nameThing = "testCreateMissingFieldDatastreamThing";

        ExpandedThing thing = DtoFactory.getExpandedThing(nameThing, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        RefId refId = DtoFactory.getRefId(getIdFromJson(json));
        // name
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastream(null, "test", DtoFactory.getUnitOfMeasure("test"),
                "obsType", refId, DtoFactory.getSensor("test"), DtoFactory.getObservedProperty("test"), null);
        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 400);

        // uom
        dtoDatastream = DtoFactory.getDatastream("test", "test", null, "obsType", refId, DtoFactory.getSensor("test"),
                DtoFactory.getObservedProperty("test"), null);
        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 400);

        // observationType
        dtoDatastream = DtoFactory.getDatastream("test", "test", DtoFactory.getUnitOfMeasure("test"), null, refId,
                DtoFactory.getSensor("test"), DtoFactory.getObservedProperty("test"), null);
        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 400);

        // thingId
        dtoDatastream = DtoFactory.getDatastream("test", "test", DtoFactory.getUnitOfMeasure("test"), "obsType", null,
                DtoFactory.getSensor("test"), DtoFactory.getObservedProperty("test"), null);
        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 400);

        // sensor
        dtoDatastream = DtoFactory.getDatastream("test", "test", DtoFactory.getUnitOfMeasure("test"), "obsType", refId,
                null, DtoFactory.getObservedProperty("test"), null);
        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 400);

        // observedProperty
        dtoDatastream = DtoFactory.getDatastream("test", "test", DtoFactory.getUnitOfMeasure("test"), "obsType", refId,
                DtoFactory.getSensor("test"), null, null);
        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 400);

    }

    /**
     * test create datastream using thing/datastream endpoint
     *
     * @throws Exception
     */
    @Test
    public void testCreateDatastreamThroughThing() throws Exception {
        // given
        String name = "testCreateDatastreamThroughThing";
        String nameThing = "testCreateDatastreamThroughThingThing";

        ExpandedThing thing = DtoFactory.getExpandedThing(nameThing, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamMinimalLinkThing(name, DtoFactory.getRefId(thingId));

        // when
        json = getJsonResponseFromPost(dtoDatastream, String.format("Things(%s)/Datastreams", name), 201);
        UtilsAssert.assertDatastream(dtoDatastream, json);

    }

    /**
     * test create datastream with sennsor and observed property in payload
     *
     * @throws Exception
     */
    @Test
    public void testCreateDatastreamWithSensorAndObservedProperty() throws Exception {
        // given
        String name = "testCreateDatastreamWithSensorAndObservedProperty";
        String nameThing = "testCreateDatastreamWithSensorAndObservedPropertyThing";

        ExpandedThing thing = DtoFactory.getExpandedThing(nameThing, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamLinkThingWithSensorObservedProperty(name,
                DtoFactory.getRefId(getIdFromJson(json)));

        // when
        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 201);
        UtilsAssert.assertDatastream(dtoDatastream, json);

    }

    /**
     * test create datastream with expand result expected
     *
     * @throws Exception
     */
    @Test
    public void testCreateDatastreamWithExpand() throws Exception {
        // given
        String name = "testCreateDatastreamWithExpand";
        String nameThing = "testCreateDatastreamWithExpandThing";

        ExpandedThing thing = DtoFactory.getExpandedThing(nameThing, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamLinkThingWithSensorObservedProperty(name,
                DtoFactory.getRefId(getIdFromJson(json)));

        // when
        json = getJsonResponseFromPost(dtoDatastream, "Datastreams?$expand=Sensor,ObservedProperty,Observations", 201);
        UtilsAssert.assertDatastream(dtoDatastream, json, true);

    }

    /**
     * Tests that <code>PUT</code> can be used to update a Datastream
     */
    /**
     * test simple update datastream
     *
     * @throws Exception
     */
    @Test
    public void testUpdateDatastream() throws Exception {
        // given
        String nameThing = "testUpdateDatastreamThing";
        String name = "testUpdateDatastream";

        ExpandedThing thing = DtoFactory.getExpandedThing(nameThing, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String idThing = getIdFromJson(json);
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamMinimalLinkThing(name, DtoFactory.getRefId(idThing));

        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 201);
        String idDatastream = getIdFromJson(json);

        UtilsAssert.assertDatastream(dtoDatastream, json);
        // when
        ExpandedDataStream dtoDatastreamUpdate = DtoFactory.getDatastreamMinimal(name + " Update", "Update", "Update");

        getJsonResponseFromPut(dtoDatastreamUpdate, String.format("Datastreams(%s)", idDatastream), 204);
        // then
        ServiceSnapshot service = serviceUseCase.read(session, idDatastream, "datastream");
        ServiceSnapshot serviceAdmin = serviceUseCase.read(session, idDatastream, "admin");

        assertEquals(name + " Update", DtoMapperSimple.getResourceField(serviceAdmin, "friendlyName", String.class));
        assertNotNull(DtoMapperSimple.getResourceField(service, "lastObservation", String.class));

    }

    /**
     * Tests that <code>PUT</code> can be used to update a Datastream
     */
    /**
     * test simple update datastream
     *
     * @throws Exception
     */
    @Test
    public void testPatchDatastream() throws Exception {
        // given
        String nameThing = "testPatchDatastream";
        String name = "testPatchDatastream";

        ExpandedThing thing = DtoFactory.getExpandedThing(nameThing, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String idThing = getIdFromJson(json);
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamMinimalLinkThing(name, DtoFactory.getRefId(idThing));

        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 201);
        String idDatastream = getIdFromJson(json);

        UtilsAssert.assertDatastream(dtoDatastream, json);
        // when
        ExpandedDataStream dtoDatastreamUpdate = DtoFactory.getDatastreamMinimal(null, "Update", "Update");

        getJsonResponseFromPatch(dtoDatastreamUpdate, String.format("Datastreams(%s)", idDatastream), 204);
        // then
        ServiceSnapshot service = serviceUseCase.read(session, idDatastream, "datastream");
        ServiceSnapshot serviceAdmin = serviceUseCase.read(session, idDatastream, "admin");

        assertEquals("Update", DtoMapperSimple.getResourceField(serviceAdmin, "description", String.class));
        assertNotNull(DtoMapperSimple.getResourceField(service, "lastObservation", String.class));

    }

    /**
     * Tests that a Datastream can use id references to refer to existing other
     * resources and will be linked to them
     */
    @Test
    public void testUpdateDatastreamRefs() throws Exception {
        // given
        String nameThing = "testUpdateDatastreamThing";
        String name = "testUpdateDatastream";

        ExpandedThing thing = DtoFactory.getExpandedThing(nameThing, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String idThing = getIdFromJson(json);

        ExpandedThing thingUpdate = DtoFactory.getExpandedThing(nameThing + "update", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thingUpdate, "Things", 201);
        String idThingUpdate = getIdFromJson(json);

        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamMinimalLinkThing(name, DtoFactory.getRefId(idThing));

        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 201);
        String idDatastream = getIdFromJson(json);

        UtilsAssert.assertDatastream(dtoDatastream, json);

        Sensor sensor = DtoFactory.getSensor(name);
        json = getJsonResponseFromPost(sensor, "Sensors", 201);
        UtilsAssert.assertSensor(sensor, json);

        String idSensor = getIdFromJson(json);
        ObservedProperty ObservedProperty = DtoFactory.getObservedProperty(name);
        json = getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 201);
        UtilsAssert.assertObservedProperty(ObservedProperty, json);
        String idObservedProperty = getIdFromJson(json);

        // when
        // thing

        json = getJsonResponseFromPut(new RefId(idThingUpdate),
                String.format("Datastreams(%s)/Thing/$ref", idDatastream), 204);
        // then
        ServiceSnapshot service = serviceUseCase.read(session, idDatastream, "datastream");
        assertEquals(idThingUpdate, DtoMapperSimple.getResourceField(service, "thingId", String.class));

        // sensor
        json = getJsonResponseFromPut(new RefId(idSensor), String.format("Datastreams(%s)/Sensor/$ref", idDatastream),
                204);
        // then
        service = serviceUseCase.read(session, idDatastream, "datastream");
        assertEquals(idSensor, DtoMapperSimple.getResourceField(service, "sensorId", String.class));

        // observed property
        json = getJsonResponseFromPut(new RefId(idObservedProperty),
                String.format("Datastreams(%s)/ObservedProperty/$ref", idDatastream), 204);
        // then
        service = serviceUseCase.read(session, idDatastream, "datastream");
        assertEquals(idObservedProperty, DtoMapperSimple.getResourceField(service, "observedPropertyId", String.class));

    }

    /**
     * delete daqtastream
     *
     * @throws Exception
     */
    @Test
    public void testDeleteDatastream() throws Exception {
        // given
        String nameThing = "testDeleteDatastreamThing";
        String name = "testDeleteDatastream";

        ExpandedThing thing = DtoFactory.getExpandedThing(nameThing, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);

        String idJson = getIdFromJson(json);
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamMinimalLinkThing(name, DtoFactory.getRefId(idJson));
        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 201);
        UtilsAssert.assertDatastream(dtoDatastream, json);

        ExpandedDataStream dtoDatastream2 = DtoFactory.getDatastreamMinimalLinkThing(name + "2",
                DtoFactory.getRefId(idJson));
        json = getJsonResponseFromPost(dtoDatastream2, "Datastreams", 201);
        String idDatastream = getIdFromJson(json);
        UtilsAssert.assertDatastream(dtoDatastream2, json);

        // when

        getJsonResponseFromDelete(String.format("Datastreams(%s)", idDatastream), 204);
        // then
        assertThrows(NotFoundException.class, () -> {
            serviceUseCase.read(session, idDatastream, "datastream");
        });
        ServiceSnapshot service = serviceUseCase.read(session, idJson, "thing");
        @SuppressWarnings("unchecked")
        List<String> datastreamIds = (List<String>) DtoMapperSimple.getResourceField(service, "datastreamIds",
                Object.class);
        assertFalse(datastreamIds.contains(idDatastream));
    }

    /**
     * delete sensor $ref to datastream should return 409
     *
     * @throws Exception
     */
    @Test
    public void testDeleteDatastreamSensorRef() throws Exception {
        // given
        String nameThing = "testDeleteDatastreamSenosorRef";
        String name = "testDeleteDatastreamSenosorRef";

        ExpandedThing thing = DtoFactory.getExpandedThing(nameThing, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);

        String idJson = getIdFromJson(json);
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamMinimalLinkThing(name, DtoFactory.getRefId(idJson));
        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 201);
        UtilsAssert.assertDatastream(dtoDatastream, json);

        ExpandedDataStream dtoDatastream2 = DtoFactory.getDatastreamMinimalLinkThing(name + "2",
                DtoFactory.getRefId(idJson));
        json = getJsonResponseFromPost(dtoDatastream2, "Datastreams", 201);
        String idDatastream = getIdFromJson(json);
        UtilsAssert.assertDatastream(dtoDatastream2, json);

        // when

        getJsonResponseFromDelete(String.format("Datastreams(%s)/Sensor/$ref", idDatastream), 409);
        // then

        ServiceSnapshot service = serviceUseCase.read(session, idDatastream, "datastream");
        assertNotNull(DtoMapperSimple.getResourceField(service, "sensorId", String.class));
        assertNotNull(DtoMapperSimple.getResourceField(service, "sensorName", String.class));
        assertNotNull(DtoMapperSimple.getResourceField(service, "sensorMetadata", Object.class));

    }

    /**
     * delete observedProperty $ref to datastream should return 409
     *
     * @throws Exception
     */
    @Test
    public void testDeleteDatastreamObservedPropertyRef() throws Exception {
        // given
        String nameThing = "testDeleteDatastreamObservedPropertyRef";
        String name = "testDeleteDatastreamObservedPropertyRef";

        ExpandedThing thing = DtoFactory.getExpandedThing(nameThing, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);

        String idJson = getIdFromJson(json);
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamMinimalLinkThing(name, DtoFactory.getRefId(idJson));
        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 201);
        UtilsAssert.assertDatastream(dtoDatastream, json);

        ExpandedDataStream dtoDatastream2 = DtoFactory.getDatastreamMinimalLinkThing(name + "2",
                DtoFactory.getRefId(idJson));
        json = getJsonResponseFromPost(dtoDatastream2, "Datastreams", 201);
        String idDatastream = getIdFromJson(json);
        UtilsAssert.assertDatastream(dtoDatastream2, json);

        // when

        getJsonResponseFromDelete(String.format("/Datastreams(%s)/ObservedProperty/$ref", idDatastream), 409);
        // then

        ServiceSnapshot service = serviceUseCase.read(session, idDatastream, "datastream");
        assertNotNull(DtoMapperSimple.getResourceField(service, "observedPropertyId", String.class));
        assertNotNull(DtoMapperSimple.getResourceField(service, "observedPropertyName", String.class));
        assertNotNull(DtoMapperSimple.getResourceField(service, "observedPropertyDefinition", String.class));

    }

}

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
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.ws.rs.NotFoundException;

/**
 * Unit test for simple App.
 */
public class ObservedPropertyTest extends AbstractIntegrationTest {

    /**
     * test endpoint create observed property that store the dto in cache memory as
     * not link to datastream
     *
     * @throws Exception
     */
    @Test
    public void testCreateObservedProperty() throws Exception {
        // given
        String name = "testCreateObservedProperty";
        ObservedProperty ObservedProperty = DtoFactory.getObservedProperty(name);
        // when
        JsonNode json = getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 201);
        // then
        UtilsAssert.assertObservedProperty(ObservedProperty, json);

    }

    @Test
    public void testCreateObservedPropertyAssignTo2Datastream() throws Exception {
        // given
        String name = "testCreateObservedPropertyAssignTo2Datastream";
        ObservedProperty observedProperty = DtoFactory.getObservedProperty(name);
        // when
        JsonNode json = getJsonResponseFromPost(observedProperty, "ObservedProperties", 201);
        UtilsAssert.assertObservedProperty(observedProperty, json);
        String observedPropertyId = getIdFromJson(json);

        ExpandedThing thing = DtoFactory.getExpandedThing("testCreateDatastreamLinkObservedPropertyAlreadyExists",
                "testThing existing Location", Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkObservedProperty(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(observedPropertyId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=ObservedProperty,Observations", 201);
        ExpandedDataStream datastream2 = DtoFactory.getDatastreamMinimalLinkThingLinkObservedProperty(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(observedPropertyId));
        json = getJsonResponseFromPost(datastream2, "Datastreams?$expand=ObservedProperty,Observations", 201);

        ServiceSnapshot service = serviceUseCase.read(session, observedPropertyId, "observedproperty");
        @SuppressWarnings("unchecked")
        List<String> listDatastreamIds = DtoMapperSimple.getResourceField(service, "datastreamIds", List.class);
        assertEquals(2, listDatastreamIds.size());
    }

    /**
     * test create observed with missing field
     *
     * @throws Exception
     */
    @Test
    public void testDeleteObservedProperty() throws Exception {
        // given
        String name = "testDeleteObservedProperty";
        ObservedProperty ObservedProperty = DtoFactory.getObservedProperty(name);
        JsonNode json = getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 201);
        String idObservedProperty = getIdFromJson(json);
        UtilsAssert.assertObservedProperty(ObservedProperty, json);
        // when
        getJsonResponseFromDelete(String.format("ObservedProperties(%s)", idObservedProperty), 200);
        // then
    }

    @Test
    public void testDeleteDatastreamLinkObservedProperty() throws Exception {
        // given
        String name = "testDeleteDatastreamLinkObservedProperty";
        ObservedProperty ObservedProperty = DtoFactory.getObservedProperty(name);
        JsonNode json = getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 201);
        String observedPropertyId = getIdFromJson(json);
        UtilsAssert.assertObservedProperty(ObservedProperty, json);

        ExpandedThing thing = DtoFactory.getExpandedThing("testCreateDatastreamLinkObservedPropertyAlreadyExists",
                "testThing existing Location", Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkObservedProperty(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(observedPropertyId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=ObservedProperty,Observations", 201);
        String idDatastream = getIdFromJson(json);
        ExpandedDataStream expectedDatastream = DtoFactory.getDatastreamMinimalWithThingObervedPropertySensor(
                name + "1", DtoFactory.getRefId(thingId), null, ObservedProperty);
        JsonNode opNode = json.get("ObservedProperty");
        String observedPropertyIdDatastream = getIdFromJson(opNode);
        UtilsAssert.assertDatastream(expectedDatastream, json, true);
        // when
        getJsonResponseFromDelete(String.format("ObservedProperties(%s)", observedPropertyIdDatastream), 200);
        // then
        assertThrows(NotFoundException.class, () -> {
            serviceUseCase.read(session, idDatastream, "datastream");
        });
    }

    /**
     * test create observed property with missing required field
     */
    @Test
    public void testCreateObservedPropertyMissingField() throws Exception {
        // given
        String name = "testCreateObservedPropertyMissingField";
        // name
        ObservedProperty ObservedProperty = DtoFactory.getObservedProperty(null, "test");
        getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 400);

        // definition
        ObservedProperty = DtoFactory.getObservedProperty(name, null);
        getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 400);

    }

    /**
     * test create observed property link to datastream
     *
     * @throws Exception
     */
    @Test
    public void testCreateDatastreamLinkObservedProperty() throws Exception {
        // given
        String name = "testCreateDatastreamLinkObservedProperty";
        ObservedProperty ObservedProperty = DtoFactory.getObservedProperty(name);
        JsonNode json = getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 201);
        String ObservedPropertyId = getIdFromJson(json);
        UtilsAssert.assertObservedProperty(ObservedProperty, json);

        ExpandedThing thing = DtoFactory.getExpandedThing("testCreateDatastreamLinkObservedPropertyAlreadyExists",
                "testThing existing Location", Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkObservedProperty(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(ObservedPropertyId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=ObservedProperty,Observations", 201);
        ExpandedDataStream expectedDatastream = DtoFactory.getDatastreamMinimalWithThingObervedPropertySensor(
                name + "1", DtoFactory.getRefId(thingId), null, ObservedProperty);
        UtilsAssert.assertDatastream(expectedDatastream, json, true);

    }

    /**
     * Tests that <code>PUT</code> can be used to update a ObservedPorperty
     */
    /*
     * test simple update observed property that is store in memory cache
     */
    @Test
    public void testUpdateObservedProperty() throws Exception {
        // given
        String name = "testUpdateObservedProperty";
        ObservedProperty ObservedProperty = DtoFactory.getObservedProperty(name);
        JsonNode json = getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 201);
        UtilsAssert.assertObservedProperty(ObservedProperty, json);
        String idObservedProperty = getIdFromJson(json);
        ObservedProperty ObservedPropertyUpdate = DtoFactory.getObservedProperty(name + "2");
        json = getJsonResponseFromPut(ObservedPropertyUpdate,
                String.format("ObservedProperties(%s)", idObservedProperty), 200);
        // then

    }

    /**
     * test update observed property that is store in datastream
     *
     * @throws Exception
     */
    @Test
    public void testUpdateObservedPropertyLinkDatastream() throws Exception {
        // given
        String name = "testUpdateObservedPropertyLinkDatastream";
        ObservedProperty ObservedProperty = DtoFactory.getObservedProperty(name);
        JsonNode json = getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 201);
        String ObservedPropertyId = getIdFromJson(json);
        UtilsAssert.assertObservedProperty(ObservedProperty, json);

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkObservedProperty(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(ObservedPropertyId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=ObservedProperty,Observations", 201);
        ExpandedDataStream expectedDatastream = DtoFactory.getDatastreamMinimalWithThingObervedPropertySensor(
                name + "1", DtoFactory.getRefId(thingId), null, ObservedProperty);
        UtilsAssert.assertDatastream(expectedDatastream, json, true);
        JsonNode observedPropertyNode = json.get("ObservedProperty");
        String idObservedProperty = getIdFromJson(observedPropertyNode);
        // when
        ObservedProperty ObservedPropertyUpdate = DtoFactory.getObservedProperty(name + "2");
        json = getJsonResponseFromPut(ObservedPropertyUpdate,
                String.format("ObservedProperties(%s)", idObservedProperty), 200);
        // then
        ServiceSnapshot service = serviceUseCase.read(session, idObservedProperty, "admin");
        assertEquals(name + "2", DtoMapperSimple.getResourceField(service, "friendlyName", String.class));
    }

    /**
     * Tests that <code>PATCH</code> can be used to update a ObservedPorperty
     */
    /**
     * test patch observed property that is store in memory cache
     *
     * @throws Exception
     */
    @Test
    public void testUpdatePatchObservedProperty() throws Exception {
        // given
        String name = "testUpdateObservedProperty";
        ObservedProperty ObservedProperty = DtoFactory.getObservedProperty(name);
        JsonNode json = getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 201);
        UtilsAssert.assertObservedProperty(ObservedProperty, json);
        String idObservedProperty = getIdFromJson(json);
        ObservedProperty ObservedPropertyUpdate = DtoFactory.getObservedProperty(null, "test");
        json = getJsonResponseFromPatch(ObservedPropertyUpdate,
                String.format("ObservedProperties(%s)", idObservedProperty), 200);
        // then

    }

    /**
     * test patch observed property that is link to a datastream
     *
     * @throws Exception
     */
    @Test
    public void testUpdatePatchObservedPropertyLinkDatastream() throws Exception {
        // given
        String name = "testUpdateObservedPropertyLinkDatastream";
        ObservedProperty ObservedProperty = DtoFactory.getObservedProperty(name);
        JsonNode json = getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 201);
        String ObservedPropertyId = getIdFromJson(json);
        UtilsAssert.assertObservedProperty(ObservedProperty, json);

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkObservedProperty(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(ObservedPropertyId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=ObservedProperty,Observations", 201);
        ExpandedDataStream expectedDatastream = DtoFactory.getDatastreamMinimalWithThingObervedPropertySensor(
                name + "1", DtoFactory.getRefId(thingId), null, ObservedProperty);
        UtilsAssert.assertDatastream(expectedDatastream, json, true);
        JsonNode observedPropertyNode = json.get("ObservedProperty");
        String idObservedProperty = getIdFromJson(observedPropertyNode);
        // when
        ObservedProperty ObservedPropertyUpdate = DtoFactory.getObservedProperty(null, "test");
        json = getJsonResponseFromPatch(ObservedPropertyUpdate,
                String.format("ObservedProperties(%s)", idObservedProperty), 200);
        // then
        ServiceSnapshot service = serviceUseCase.read(session, idObservedProperty, "observedproperty");
        assertEquals("test", DtoMapperSimple.getResourceField(service, "observedPropertyDefinition", String.class));

    }
}

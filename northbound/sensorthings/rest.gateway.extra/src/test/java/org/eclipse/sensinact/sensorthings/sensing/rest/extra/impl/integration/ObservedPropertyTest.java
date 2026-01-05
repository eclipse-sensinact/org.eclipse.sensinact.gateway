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
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Unit test for simple App.
 */
public class ObservedPropertyTest extends AbstractIntegrationTest {

    @Test
    public void testCreateObservedProperty() throws Exception {
        // given
        String name = "testCreateObservedProperty";
        ExpandedObservedProperty ObservedProperty = DtoFactory.getObservedProperty(name);
        JsonNode json = getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 201);

        UtilsAssert.assertObservedProperty(ObservedProperty, json);

    }

    @Test
    public void testCreateObservedPropertyMissingField() throws Exception {
        // given
        String name = "testCreateObservedPropertyMissingField";
        // name
        ExpandedObservedProperty ObservedProperty = DtoFactory.getObservedProperty(null, "test");
        getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 400);

        // definition
        ObservedProperty = DtoFactory.getObservedProperty(name, null);
        getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 400);

    }

    @Test
    public void testCreateDatastreamLinkObservedProperty() throws Exception {
        // given
        String name = "testCreateDatastreamLinkObservedProperty";
        ExpandedObservedProperty ObservedProperty = DtoFactory.getObservedProperty(name);
        JsonNode json = getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 201);
        String ObservedPropertyId = getIdFromJson(json);
        UtilsAssert.assertObservedProperty(ObservedProperty, json);
        assertNotNull(observedPropertyCache.getDto(ObservedPropertyId));

        ExpandedThing thing = DtoFactory.getExpandedThing("testCreateDatastreamLinkObservedPropertyAlreadyExists",
                "testThing existing Location", Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkObservedProperty(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(ObservedPropertyId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=ObservedProperty", 201);
        ExpandedDataStream expectedDatastream = DtoFactory.getDatastreamMinimalWithThingObervedPropertySensor(
                name + "1", DtoFactory.getRefId(thingId), null, ObservedProperty);
        UtilsAssert.assertDatastream(expectedDatastream, json, true);
        assertNull(observedPropertyCache.getDto(ObservedPropertyId));

    }

    // update
    @Test
    public void testUpdateObservedProperty() throws Exception {
        // given
        String name = "testUpdateObservedProperty";
        ExpandedObservedProperty ObservedProperty = DtoFactory.getObservedProperty(name);
        JsonNode json = getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 201);
        UtilsAssert.assertObservedProperty(ObservedProperty, json);
        String idObservedProperty = getIdFromJson(json);
        ExpandedObservedProperty ObservedPropertyUpdate = DtoFactory.getObservedProperty(name);
        json = getJsonResponseFromPut(ObservedPropertyUpdate,
                String.format("ObservedProperties(%s)", idObservedProperty), 204);
        // then
        json = getJsonResponseFromGet(String.format("/ObservedProperties(%s)", idObservedProperty), 200);
        UtilsAssert.assertObservedProperty(ObservedPropertyUpdate, json);

    }

    @Test
    public void testUpdateObservedPropertyLinkDatastream() throws Exception {
        // given
        String name = "testUpdateObservedPropertyLinkDatastream";
        ExpandedObservedProperty ObservedProperty = DtoFactory.getObservedProperty(name);
        JsonNode json = getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 201);
        String ObservedPropertyId = getIdFromJson(json);
        UtilsAssert.assertObservedProperty(ObservedProperty, json);
        assertNotNull(observedPropertyCache.getDto(ObservedPropertyId));

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkObservedProperty(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(ObservedPropertyId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=ObservedProperty", 201);
        ExpandedDataStream expectedDatastream = DtoFactory.getDatastreamMinimalWithThingObervedPropertySensor(
                name + "1", DtoFactory.getRefId(thingId), null, ObservedProperty);
        UtilsAssert.assertDatastream(expectedDatastream, json, true);
        assertNull(observedPropertyCache.getDto(ObservedPropertyId));
        JsonNode observedPropertyNode = json.get("ObservedProperty");
        String idObservedProperty = getIdFromJson(observedPropertyNode);
        // when
        ExpandedObservedProperty ObservedPropertyUpdate = DtoFactory.getObservedProperty(name);
        json = getJsonResponseFromPut(ObservedPropertyUpdate, String.format("ObservedProperty(%s)", idObservedProperty),
                204);
        // then

    }

    // patch
    @Test
    public void testUpdatePatchObservedProperty() throws Exception {
        // given
        String name = "testUpdateObservedProperty";
        ExpandedObservedProperty ObservedProperty = DtoFactory.getObservedProperty(name);
        JsonNode json = getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 201);
        UtilsAssert.assertObservedProperty(ObservedProperty, json);
        String idObservedProperty = getIdFromJson(json);
        ExpandedObservedProperty ObservedPropertyUpdate = DtoFactory.getObservedProperty(null, "test");
        json = getJsonResponseFromPatch(ObservedPropertyUpdate,
                String.format("ObservedProperty(%s)", idObservedProperty), 204);
        // then

    }

    @Test
    public void testUpdatePatchObservedPropertyLinkDatastream() throws Exception {
        // given
        String name = "testUpdateObservedPropertyLinkDatastream";
        ExpandedObservedProperty ObservedProperty = DtoFactory.getObservedProperty(name);
        JsonNode json = getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 201);
        String ObservedPropertyId = getIdFromJson(json);
        UtilsAssert.assertObservedProperty(ObservedProperty, json);
        assertNotNull(observedPropertyCache.getDto(ObservedPropertyId));

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingLinkObservedProperty(name + "1",
                DtoFactory.getRefId(thingId), DtoFactory.getRefId(ObservedPropertyId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=ObservedProperty", 201);
        ExpandedDataStream expectedDatastream = DtoFactory.getDatastreamMinimalWithThingObervedPropertySensor(
                name + "1", DtoFactory.getRefId(thingId), null, ObservedProperty);
        UtilsAssert.assertDatastream(expectedDatastream, json, true);
        assertNull(observedPropertyCache.getDto(ObservedPropertyId));
        JsonNode observedPropertyNode = json.get("ObservedProperty");
        String idObservedProperty = getIdFromJson(observedPropertyNode);
        // when
        ExpandedObservedProperty ObservedPropertyUpdate = DtoFactory.getObservedProperty(null, "test");
        json = getJsonResponseFromPatch(ObservedPropertyUpdate,
                String.format("ObservedProperty(%s)", idObservedProperty), 204);
        // then

    }
}

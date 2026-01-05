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

import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedSensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Unit test for simple App.
 */
public class DatastreamTest extends AbstractIntegrationTest {

    @Test
    public void testCreateDatastream() throws Exception {
        // given
        String nameThing = "testCreateDatastreamThing";
        String name = "testCreateDatastream";

        ExpandedThing thing = DtoFactory.getExpandedThing(nameThing, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);

        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamMinimalLinkThing(name,
                DtoFactory.getRefId(nameThing));

        // when
        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 201);
        UtilsAssert.assertDatastream(dtoDatastream, json);

    }

    @Test
    public void testCreateMissingFieldDatastream() throws Exception {
        // given
        String name = "testCreateMissingFieldDatastream";

        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        RefId refId = DtoFactory.getRefId(getIdFromJson(json));
        // name
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastream(null, "test", DtoFactory.getUnitOfMeasure("test"),
                "obsType", refId, DtoFactory.getSensor("test"), DtoFactory.getObservedProperty("test"), null);
        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 400);

        // description
        dtoDatastream = DtoFactory.getDatastream("test", null, DtoFactory.getUnitOfMeasure("test"), "obsType", refId,
                DtoFactory.getSensor("test"), DtoFactory.getObservedProperty("test"), null);
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

    @Test
    public void testCreateDatastreamThroughThing() throws Exception {
        // given
        String name = "testCreateDatastreamThroughThing";

        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamMinimalLinkThing(name, DtoFactory.getRefId(thingId));

        // when
        json = getJsonResponseFromPost(dtoDatastream, String.format("Things(%s)/Datastreams", name), 201);
        UtilsAssert.assertDatastream(dtoDatastream, json);

    }

    @Test
    public void testCreateDatastreamWithSensorAndObservedProperty() throws Exception {
        // given
        String name = "testCreateDatastreamWithSensorAndObservedProperty";

        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamLinkThingWithSensorObservedProperty(name,
                DtoFactory.getRefId(getIdFromJson(json)));

        // when
        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 201);
        UtilsAssert.assertDatastream(dtoDatastream, json);

    }

    @Test
    public void testCreateDatastreamWithExpand() throws Exception {
        // given
        String name = "testCreateDatastreamWithExpand";

        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamLinkThingWithSensorObservedProperty(name,
                DtoFactory.getRefId(getIdFromJson(json)));

        // when
        json = getJsonResponseFromPost(dtoDatastream, "Datastreams?$expand=Sensor,ObservedProperty", 201);
        UtilsAssert.assertDatastream(dtoDatastream, json, true);

    }

    // update
    @Test
    public void testUpdateDatastream() throws Exception {
        // given
        String nameThing = "testUpdateDatastream";
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

        json = getJsonResponseFromPut(dtoDatastreamUpdate, String.format("Datastreams(%s)", idDatastream), 204);
        // then

    }

    @Test
    public void testUpdateDatastreamRefs() throws Exception {
        // given
        String nameThing = "testUpdateDatastream";
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

        ExpandedSensor sensor = DtoFactory.getSensor(name);
        json = getJsonResponseFromPost(sensor, "Sensors", 201);
        UtilsAssert.assertSensor(sensor, json);

        String idSensor = getIdFromJson(json);
        ExpandedObservedProperty ObservedProperty = DtoFactory.getObservedProperty(name);
        json = getJsonResponseFromPost(ObservedProperty, "ObservedProperties", 201);
        UtilsAssert.assertObservedProperty(ObservedProperty, json);
        String idObservedProperty = getIdFromJson(json);

        // when
        // thing

        json = getJsonResponseFromPut(new RefId(idThingUpdate),
                String.format("Datastreams(%s)/Thing/$ref", idDatastream), 204);
        // then

        // sensor
        json = getJsonResponseFromPut(new RefId(idSensor), String.format("Datastreams(%s)/Sensor/$ref", idDatastream),
                204);
        // then

        // observed property
        json = getJsonResponseFromPut(new RefId(idObservedProperty),
                String.format("Datastreams(%s)/ObservedProperty/$ref", idDatastream), 204);
        // then

    }
}

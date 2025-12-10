package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
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
    public void testCreateDatastreamWithObservedProperty() throws Exception {
        // given
        String name = "testCreateDatastreamLinnkThingWithObservedPropertyObservations";

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
}

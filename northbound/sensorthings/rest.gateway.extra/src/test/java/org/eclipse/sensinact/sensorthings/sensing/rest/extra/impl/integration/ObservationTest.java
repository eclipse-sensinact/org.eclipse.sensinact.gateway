package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Unit test for simple App.
 */
public class ObservationTest extends AbstractIntegrationTest {

    @Test
    public void testCreateObservation() throws Exception {
        // given
        String name = "testCreateObservation";
        ExpandedObservation observsation = DtoFactory.getObservation(name);
        JsonNode json = getJsonResponseFromPost(observsation, "ObservedPropertys", 201);

        UtilsAssert.assertObservation(observsation, json);

    }

    @Test
    public void createCreateObservationThroughDatastream() throws Exception {
        // given
        String name = "createCreateObservationThroughDatastream";

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThing(name + "1",
                DtoFactory.getRefId(thingId));
        json = getJsonResponseFromPost(thing, "Datastreams?$expand=ObservedProperty", 201);
        UtilsAssert.assertDatastream(datastream, json, true);
        String datastreamId = getIdFromJson(json);
        ExpandedObservation observsation = DtoFactory.getObservation(name);
        json = getJsonResponseFromPost(observsation, String.format("Datastream(%s)/Observsations", datastreamId), 201);

        UtilsAssert.assertObservation(observsation, json);

    }
}

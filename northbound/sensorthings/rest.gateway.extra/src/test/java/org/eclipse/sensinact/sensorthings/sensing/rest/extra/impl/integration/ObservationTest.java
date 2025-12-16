package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import java.time.Instant;
import java.util.List;
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
    public void createCreateObservationThroughDatastream() throws Exception {
        // given
        String name = "createCreateObservationThroughDatastream";

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThing(name + "1",
                DtoFactory.getRefId(thingId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=ObservedProperty", 201);
        UtilsAssert.assertDatastream(datastream, json, true);
        String datastreamId = getIdFromJson(json);
        ExpandedObservation observsation = DtoFactory.getObservation(name);
        json = getJsonResponseFromPost(observsation, String.format("Datastreams(%s)/Observations", datastreamId), 201);

        UtilsAssert.assertObservation(observsation, json);

    }

    @Test
    public void createCreateObservationMissingField() throws Exception {
        // given
        String name = "createCreateObservationMissingField";

        ExpandedThing thing = DtoFactory.getExpandedThing(name + "alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThing(name + "1",
                DtoFactory.getRefId(thingId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=ObservedProperty", 201);
        UtilsAssert.assertDatastream(datastream, json, true);
        String datastreamId = getIdFromJson(json);
        // result
        ExpandedObservation observsation = DtoFactory.getObservationLinkDatastream(name, null, Instant.now(), null,
                null);
        json = getJsonResponseFromPost(observsation, String.format("Datastreams(%s)/Observations", datastreamId), 400);

        // phenomenomTime
        observsation = DtoFactory.getObservationLinkDatastream(name, 5.0, null, null, null);
        json = getJsonResponseFromPost(observsation, String.format("Datastreams(%s)/Observations", datastreamId), 400);

    }

    @Test
    public void createCreateObservationsInDatastream() throws Exception {
        // given
        String name = "createCreateObservationsInDatastream";

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists2", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedObservation observsation1 = DtoFactory.getObservation(name + "1");
        ExpandedObservation observsation2 = DtoFactory.getObservation(name + "2");

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThingWithObservations(name + "Datastream",
                DtoFactory.getRefId(thingId), List.of(observsation1, observsation2));

        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=Observations,ObservedProperty", 201);

        UtilsAssert.assertDatastream(datastream, json, true);

    }
}

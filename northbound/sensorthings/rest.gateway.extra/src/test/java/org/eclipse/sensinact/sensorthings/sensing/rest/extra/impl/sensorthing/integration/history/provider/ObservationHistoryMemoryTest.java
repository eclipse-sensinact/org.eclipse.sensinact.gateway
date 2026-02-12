package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.sensorthing.integration.history.provider;

import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedObservation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.sensorthing.integration.DtoFactory;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.sensorthing.integration.UtilsAssert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class ObservationHistoryMemoryTest extends AstractIntegrationTestHistoryProvider {

    /**
     * test create observation through datastream observation endpoint
     *
     * @throws Exception
     */
    @Test
    public void testDeleteObservation() throws Exception {
        // given
        String name = "testDeleteObservation";

        ExpandedThing thing = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String thingId = getIdFromJson(json);

        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThing(name + "1",
                DtoFactory.getRefId(thingId));
        json = getJsonResponseFromPost(datastream, "Datastreams?$expand=ObservedProperty,Sensor,Observations", 201);
        UtilsAssert.assertDatastream(datastream, json, true);
        String datastreamId = getIdFromJson(json);
        ExpandedObservation observsation = DtoFactory.getObservation(name);
        json = getJsonResponseFromPost(observsation, String.format("Datastreams(%s)/Observations", datastreamId), 201);
        String obsId = getIdFromJson(json);
        observsation = DtoFactory.getObservation(name);
        json = getJsonResponseFromPost(observsation, String.format("Datastreams(%s)/Observations", datastreamId), 201);

        UtilsAssert.assertObservation(observsation, json);
        json = getJsonResponseFromDelete(String.format("Observations(%s)", obsId), 200);

    }
}

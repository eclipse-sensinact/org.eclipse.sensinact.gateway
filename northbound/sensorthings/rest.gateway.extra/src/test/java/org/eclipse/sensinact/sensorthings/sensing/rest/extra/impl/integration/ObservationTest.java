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
        ExpandedObservation observation = DtoFactory.getObservation(name);
        JsonNode json = getJsonResponseFromPost(observation, "Observations", 201);

        UtilsAssert.assertObservation(observation, json);

    }

    @Test
    public void testCreateObservationThroughDatastream() throws Exception {
        // given
        String name = "testCreateObservationThroughDatastream";

        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamMinimal(name);
        // when
        json = getJsonResponseFromPost(dtoDatastream, String.format("Things(%s)/Datastreams", getIdFromJson(json)),
                201);
        UtilsAssert.assertDatastream(dtoDatastream, json);

        ExpandedObservation observation = DtoFactory.getObservation(name);
        json = getJsonResponseFromPost(observation, String.format("Datastream(%s)/Observations", getIdFromJson(json)),
                201);

        UtilsAssert.assertObservation(observation, json);
        json = getJsonResponseFromGet(String.format("Observation(%s)", getIdFromJson(json)));
        UtilsAssert.assertObservation(observation, json);

    }

}

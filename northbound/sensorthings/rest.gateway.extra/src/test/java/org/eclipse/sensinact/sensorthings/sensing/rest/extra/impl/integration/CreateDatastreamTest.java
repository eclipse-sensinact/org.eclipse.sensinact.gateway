package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for simple App.
 */
public class CreateDatastreamTest extends AbstractIntegrationTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonNode getJsonResponseFromGet(String url) throws IOException, InterruptedException {
        HttpResponse<String> response = queryGet(url);
        return mapper.readTree(response.body());
    }

    private JsonNode getJsonResponseFromPost(Id dto, String SubUrl, int expectedStatus)
            throws IOException, InterruptedException, JsonProcessingException, JsonMappingException {
        HttpResponse<String> response = queryPost(SubUrl, dto);
        // Then
        assertEquals(response.statusCode(), expectedStatus);
        JsonNode json = mapper.readTree(response.body());

        return json;
    }

    @Test
    public void testCreateDatastream() throws Exception {
        // given
        String name = "testCreateDatastream";
        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);

        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamMinimalLinkThing(name, DtoFactory.getIdThing(name));

        // when
        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 201);
        UtilsAssert.assertDatastream(dtoDatastream, json);
        json = getJsonResponseFromGet(String.format("Datastreams(%s)", dtoDatastream.name()));
        UtilsAssert.assertDatastream(dtoDatastream, json, true);

    }

    @Test
    public void testCreateDatastreamThroughThikng() throws Exception {
        // given
        String name = "testCreateDatastreamWithSensor";

        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamMinimal(name);

        // when
        json = getJsonResponseFromPost(dtoDatastream, String.format("Things(%s)/Datastreams", name), 201);
        UtilsAssert.assertDatastream(dtoDatastream, json);
        json = getJsonResponseFromGet(String.format("Datastreams(%s)", dtoDatastream.name()));
        UtilsAssert.assertDatastream(dtoDatastream, json, true);

    }

    @Test
    public void testCreateDatastreamWithObservedPropertyObservations() throws Exception {
        // given
        String name = "testCreateDatastreamLinnkThingWithObservedPropertyObservations";

        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamLinkThingWithSensorObservedPropertyObservation(name,
                DtoFactory.getIdThing(name));

        // when
        json = getJsonResponseFromPost(dtoDatastream, "Datastreams", 201);
        UtilsAssert.assertDatastream(dtoDatastream, json);
        json = getJsonResponseFromGet(String.format("Datastreams(%s)", dtoDatastream.name()));
        UtilsAssert.assertDatastream(dtoDatastream, json, true);

    }

    @Test
    public void testCreateDatastreamWithExpand() throws Exception {
        // given
        String name = "testCreateDatastreamWithExpand";

        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        ExpandedDataStream dtoDatastream = DtoFactory.getDatastreamLinkThingWithSensorObservedPropertyObservation(name,
                DtoFactory.getIdThing(name));

        // when
        json = getJsonResponseFromPost(dtoDatastream, "Datastreams?$expand(Sensor,ObservedProperty,Observations", 201);
        UtilsAssert.assertDatastream(dtoDatastream, json, true);

    }
}

package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.http.HttpResponse;

import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for simple App.
 */
public class CreateLocationTest extends AbstractIntegrationTest {
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
    public void testCreateLocation() throws Exception {
        // given
        ExpandedLocation dtoLocation = new ExpandedLocation();
        dtoLocation.name = "testCreateLocation";
        dtoLocation.description = "testLocation creation";

        // when
        JsonNode json = getJsonResponseFromPost(dtoLocation, "/1.1/Locations", 201);
        UtilsAssert.assertLocation(dtoLocation, json);

    }

}

package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;
import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for simple App.
 */
public class CreateEndpointTest extends AbstractIntegrationTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    private void assertDatastream(ExpandedDataStream expectedLocation, JsonNode datastream) {
        assertEquals(expectedLocation.name, datastream.get("name"), "");
        assertEquals(expectedLocation.description, datastream.get("description"), "");

        if (expectedLocation.observedProperty != null) {
            JsonNode observedPropertyNode = datastream.get("observedProperty");
            assertNotNull(observedPropertyNode, "Observations array must be present");
            assertEquals(expectedLocation.observedProperty, observedPropertyNode, "");
        }
    }

    private void assertLocation(Location expectedLocation, JsonNode location) {
        assertEquals(expectedLocation.name, location.get("name"), "");
        assertEquals(expectedLocation.description, location.get("description"), "");
        assertEquals(expectedLocation.location, location.get("location"), "");
    }

    private void assertThing(ExpandedThing dtoThing, JsonNode json) {
        assertTrue(json.has("@iot.id"), "Response must contain @iot.id");
        assertEquals(dtoThing.name, json.get("name").asText());
        assertEquals(dtoThing.description, json.get("description").asText());
        if (dtoThing.locations != null && dtoThing.locations.size() > 0) {
            JsonNode locationsNode = json.get("Locations");
            assertNotNull(locationsNode, "Locations array must be present");
            assertEquals(dtoThing.locations.size(), locationsNode.size(), "Number of locations must match");
            assertLocation(dtoThing.locations.get(0), locationsNode.get(0));
        }
        if (dtoThing.datastreams != null && dtoThing.datastreams.size() > 0) {
            JsonNode datastreamNode = json.get("Datastreams");
            assertNotNull(datastreamNode, "Datastreams array must be present");
            assertEquals(dtoThing.datastreams.size(), datastreamNode.size(), "Number of Datastreams must match");
            for (int i = 0; i < dtoThing.datastreams.size(); i++) {
                assertDatastream(dtoThing.datastreams.get(i), datastreamNode.get(i));
            }
        }
    }

    // Thing
    @Test
    public void testCreateThingSimple() throws Exception {
        // Given
        ExpandedThing dtoThing = new ExpandedThing();
        dtoThing.name = "testExtraThing";
        dtoThing.description = "testThing";
        dtoThing.properties = Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25");
        // When
        HttpResponse<String> response = queryPost("/Things", dtoThing);
        // Then
        assertEquals(response.statusCode(), 201);
        JsonNode json = mapper.readTree(response.body());
        assertThing(dtoThing, json);

    }

//    @Test
//    public void testCreateThingWith1Location() throws Exception {
//        // Given
//        Thing dtoThing = new Thing();
//        dtoThing.name = "testExtraThingWithLocation";
//        dtoThing.description = "testThing With Location ";
//        dtoThing.properties = Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25");
//        Location location1 = DtoFactory.getLocation1();
//        dtoThing.locations = List.of(location1);
//
//        // When
//        HttpResponse<String> response = queryPost("/Things", dtoThing);
//        // Then
//        assertEquals(response.statusCode(), 201);
//        JsonNode json = mapper.readTree(response.body());
//        assertThing(dtoThing, json);
//
//    }
//
//    @Test
//    public void testCreateThingWithMultipleLocation() throws Exception {
//        // Given
//        Thing dtoThing = new Thing();
//        dtoThing.name = "testExtraThingWithLocation";
//        dtoThing.description = "testThing With Location ";
//        dtoThing.properties = Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25");
//        Location location1 = DtoFactory.getLocation1();
//        Location location2 = DtoFactory.getLocation2();
//        dtoThing.locations = List.of(location1, location2);
//
//        // When
//        HttpResponse<String> response = queryPost("/Things", dtoThing);
//        // Then
//        assertEquals(response.statusCode(), 201);
//        JsonNode json = mapper.readTree(response.body());
//        assertThing(dtoThing, json);
//
//    }
//
//    @Test
//    public void testCreateThingWithLocationAndDatastream() throws Exception {
//        // Given
//        Thing dtoThing = new Thing();
//        dtoThing.name = "testCreateThingWithLocationAndDatastream";
//        dtoThing.description = "testThing With Location and Datastream";
//        dtoThing.properties = Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25");
//        Location location1 = DtoFactory.getLocation1();
//        dtoThing.locations = List.of(location1);
//
//        Thing dtoThing2 = new Thing();
//        dtoThing2.name = "testCreateThingWithLocationAndDatastream2";
//        dtoThing2.description = "testThing With Location and Datastream 2";
//        dtoThing2.properties = Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25");
//        dtoThing2.locations = List.of(location1);
//
//        Datastream datastream1 = DtoFactory.getDatastreamMinimal();
//        Datastream datastream2 = DtoFactory.getDatastreamWithSensor();
//        Datastream datastream3 = DtoFactory.getDatastreamWithSensorObservedProperty();
//
//        dtoThing.datastreams = List.of(datastream1);
//        dtoThing2.datastreams = List.of(datastream1, datastream2, datastream3);
//        // When
//        HttpResponse<String> response = queryPost("/Things", dtoThing);
//        // Then
//        assertEquals(response.statusCode(), 201);
//        JsonNode json = mapper.readTree(response.body());
//        assertThing(dtoThing, json);
//        response = queryPost("/Things", dtoThing2);
//        json = mapper.readTree(response.body());
//        assertThing(dtoThing2, json);
//    }
//
//    @Test
//    public void testCreateLocation() throws Exception {
//        // given
//        Location dtoLocation = new Location();
//        dtoLocation.name = "testCreateLocation";
//        dtoLocation.description = "testLocation creation";
//
//        // when
//        HttpResponse<String> response = queryPost("/1.1/Locations", dtoLocation);
//        // Then
//        assertEquals(response.statusCode(), 201);
//        JsonNode json = mapper.readTree(response.body());
//
//    }
//
//    @Test
//    public void testCreateThingExistingLocation() throws Exception {
//        // Given
//
//        Thing dtoThing = new Thing();
//        dtoThing.name = "testCreateThingExistingLocation";
//        dtoThing.description = "testThing existing Location ";
//        dtoThing.properties = Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25");
//
//        Location location1 = DtoFactory.getLocation1();
//        HttpResponse<String> response = queryPost("/Locations", location1);
//        Location createdLocation = mapper.readValue(response.body(), Location.class);
//        Location IdLocation = new Location();
//        IdLocation.id = createdLocation.id;
//        dtoThing.locations = List.of(IdLocation);
//        // When
//        response = queryPost("/Things", dtoThing);
//        // Then
//        assertEquals(response.statusCode(), 201);
//        JsonNode json = mapper.readTree(response.body());
//
//        assertThing(dtoThing, json);
//
//    }
//
//    @Test
//    public void testCreateThingExistsDatastream() throws Exception {
//        // Given
//
//        Thing dtoThing = new Thing();
//        dtoThing.name = "testCreateThingExistsDatastream";
//        dtoThing.description = "testThing existing Datastream ";
//        dtoThing.properties = Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25");
//
//        Datastream datastream = DtoFactory.getDatastreamMinimal();
//        HttpResponse<String> response = queryPost("/Datastreams", datastream);
//        Datastream createdDatastream = mapper.readValue(response.body(), Datastream.class);
//        Datastream IdDatastream = new Datastream();
//        IdDatastream.id = createdDatastream.id;
//        dtoThing.datastreams = List.of(IdDatastream);
//        // When
//        response = queryPost("/Things", dtoThing);
//        // Then
//        assertEquals(response.statusCode(), 201);
//        JsonNode json = mapper.readTree(response.body());
//
//        assertThing(dtoThing, json);
//
//    }

}

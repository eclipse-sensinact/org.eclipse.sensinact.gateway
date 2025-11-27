package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit test for simple App.
 */
public class CreateEndpointTest extends AbstractIntegrationTest {
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

    // Thing
    @Test
    public void testCreateThingSimple() throws Exception {
        // Given
        ExpandedThing dtoThing = new ExpandedThing();
        dtoThing.name = "testExtraThing";
        dtoThing.description = "testThing";
        dtoThing.properties = Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25");
        // When
        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things", 201);
        // Then
        UtilsAssert.assertThing(dtoThing, json);

    }

    @Test
    public void testCreateThingWith1Location() throws Exception {
        // Given
        ExpandedThing dtoThing = new ExpandedThing();
        String thingName = "testExtraThingWithLocation";
        dtoThing.name = thingName;
        dtoThing.description = "testThing With Location ";
        dtoThing.properties = Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25");
        Location location1 = DtoFactory.getLocation1();
        dtoThing.locations = List.of(location1);

        // When

        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things", 201);

        UtilsAssert.assertThing(dtoThing, json);
        json = getJsonResponseFromGet(String.format("/Things(%s)?$expand=Locations", thingName));
        UtilsAssert.assertThing(dtoThing, json, true);
    }

    @Test
    public void testCreateThingWithLocationAndDatastream() throws Exception {
        // Given
        ExpandedThing dtoThing = new ExpandedThing();
        String thingName = "testCreateThingWithLocationAndDatastream";
        dtoThing.name = thingName;
        dtoThing.description = "testThing With Location and Datastream";
        dtoThing.properties = Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25");
        Location location1 = DtoFactory.getLocation1();
        dtoThing.locations = List.of(location1);

        ExpandedDataStream datastream1 = DtoFactory.getDatastreamMinimal();

        dtoThing.datastreams = List.of(datastream1);
        // When

        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things", 201);

        UtilsAssert.assertThing(dtoThing, json);
        json = getJsonResponseFromGet(String.format("/Things(%s)?$expand=Locations,Datastreams", thingName));
        UtilsAssert.assertThing(dtoThing, json, true);

    }

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

    public void testCreateThingWithLocationAndDatastreamIncludeSensorObservedPropertyhObservation() throws Exception {
        // Given
        ExpandedThing dtoThing = new ExpandedThing();
        String thingName = "testCreateThingWithLocationAndDatastreamIncludeSensorObservedPropertyhObservation";
        dtoThing.name = thingName;
        dtoThing.description = "testThing With Location and Datastream";
        dtoThing.properties = Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25");
        Location location1 = DtoFactory.getLocation1();
        dtoThing.locations = List.of(location1);

        ExpandedDataStream datastream1 = DtoFactory.getDatastreamMinimal();
        ExpandedDataStream datastream2 = DtoFactory.getDatastreamWithSensor();
        ExpandedDataStream datastream3 = DtoFactory.getDatastreamWithSensorObservedProperty();

        dtoThing.datastreams = List.of(datastream1, datastream2, datastream3);
        // When

        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things", 201);

        UtilsAssert.assertThing(dtoThing, json);
        json = getJsonResponseFromGet(String.format(
                "/Things(%s)?$expand=Locations,Datastreams($expand=Sensor,ObservedProperty,Observations)", thingName));
        UtilsAssert.assertThing(dtoThing, json, true);
    }

    @Test
    public void testCreateThingWithMultipleLocation() throws Exception {
        // Given
        ExpandedThing dtoThing = new ExpandedThing();
        String thingName = "testCreateThingWithMultipleLocation";
        dtoThing.name = thingName;
        dtoThing.description = "testThing With Multiple Location ";
        dtoThing.properties = Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25");
        Location location1 = DtoFactory.getLocation1();
        Location location2 = DtoFactory.getLocation2();
        dtoThing.locations = List.of(location1, location2);

        // When

        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things", 201);

        UtilsAssert.assertThing(dtoThing, json);
        json = getJsonResponseFromGet(String.format("/Things(%s)?$expand=Locations", thingName));
        UtilsAssert.assertThing(dtoThing, json, true);
    }

}

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

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Unit test for simple App.
 */
public class ThingTest extends AbstractIntegrationTest {

    @Test
    public void testCreateThingExistingLocation() throws Exception {
        // Given
        String name = "testCreateThingExistingLocation";
        ExpandedThing alreadyExists = DtoFactory.getExpandedThing("alreadyExists", "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(alreadyExists, "/Things", 201);

        List<RefId> listId = new ArrayList<RefId>();
        listId.add(DtoFactory.getRefId(getIdFromJson(json)));
        ExpandedLocation location = DtoFactory.getLocationLinkThing(name, "application/vnd.geo+json",
                new Point(-122.4194, 37.7749), listId);
        HttpResponse<String> response = queryPost("/Locations", location);
        Location createdLocation = mapper.readValue(response.body(), Location.class);
        List<ExpandedLocation> idLocations = List.of(DtoFactory.getIdLocation(createdLocation.id()));
        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithLocations("testCreateThingExistingLocationThing",
                "testThing existing Location", Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"),
                idLocations);

        // When

        json = getJsonResponseFromPost(dtoThing, "/Things", 201);

        UtilsAssert.assertThing(dtoThing, json);

    }

    @Test
    public void testCreateThingExistsDatastream() throws Exception {
        // Given
        String name = "testCreateThingExistsDatastream";
        ExpandedThing dtoExistsThingFromDataStream = DtoFactory.getExpandedThing("alreadyExists",
                "testThing existing Datastream ", Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));

        // When
        JsonNode json = getJsonResponseFromPost(dtoExistsThingFromDataStream, "/Things", 201);
        String idExistsThing = getIdFromJson(json);
        ExpandedDataStream datastream = DtoFactory.getDatastreamMinimalLinkThing(name + "1",
                DtoFactory.getRefId(idExistsThing));
        json = getJsonResponseFromPost(datastream, "/Datastreams", 201);

        ExpandedDataStream createdDatastream = mapper.readValue(json.toString(), ExpandedDataStream.class);
        List<ExpandedDataStream> idDatastream = List.of(DtoFactory.getIdDatastream(createdDatastream.id()));

        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithDatastreams(name, "testThing existing Datastream ",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), idDatastream);

        // When
        json = getJsonResponseFromPost(dtoThing, "/Things", 201);

        UtilsAssert.assertThing(dtoThing, json);

    }

    @Test
    public void testCreateThingSimple() throws Exception {
        // Given
        String name = "testCreateThingSimple";

        ExpandedThing dtoThing = DtoFactory.getExpandedThing(name, "testThing",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        // When
        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things", 201);
        // Then
        UtilsAssert.assertThing(dtoThing, json);

    }

    @Test
    public void testCreateThingSimpleMissingField() throws Exception {
        // Given
        String name = "testCreateThingSimpleMissingField";

        ExpandedThing dtoThing = DtoFactory.getExpandedThing(null, "testThing",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        // When
        getJsonResponseFromPost(dtoThing, "/Things", 400);

    }

    @Test
    public void testCreateThingWith1Location() throws Exception {
        // Given
        String name = "testCreateThingWith1Location";

        List<ExpandedLocation> locations = List.of(DtoFactory.getLocation(name));
        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithLocations(name + "Thing", "testThing With 1 Location ",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), locations);
        // When

        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things", 201);

        UtilsAssert.assertThing(dtoThing, json);

    }

    @Test
    public void testCreateThingWithLocationAndDatastream() throws Exception {
        // Given
        String name = "testCreateThingWithLocationAndDatastream";

        List<ExpandedLocation> locations = List.of(DtoFactory.getLocation(name + "1"));

        List<ExpandedDataStream> datastreams = List.of(DtoFactory.getDatastreamMinimal(name + "2"));

        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithDatastreamsLocations(name,
                "testThing With Location and Datastream",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), datastreams, locations);
        // When

        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things", 201);

        UtilsAssert.assertThing(dtoThing, json);

    }

    @Test

    public void testCreateThingWithLocationAndDatastreamIncludeSensorObservedPropertyhObservation() throws Exception {
        // Given
        String name = "testCreateThingWithLocationAndDatastreamIncludeSensorObservedPropertyhObservation";

        List<ExpandedLocation> locations = List.of(DtoFactory.getLocation(name + "location"));

        ExpandedDataStream datastream1 = DtoFactory.getDatastreamMinimal(name + "1");
        ExpandedDataStream datastream2 = DtoFactory.getDatastreamMinimal(name + "2");
        ExpandedDataStream datastream3 = DtoFactory.getDatastreamMinimal(name + "3");

        List<ExpandedDataStream> datastreams = List.of(datastream1, datastream2, datastream3);
        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithDatastreamsLocations(name,
                "testThing With Location and Datastream",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), datastreams, locations);
        // When

        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things", 201);

        UtilsAssert.assertThing(dtoThing, json);

    }

    // @Test
//TODO    public void testCreateThingWithExpandLocationAndDatastreamIncludeSensorObservedPropertyhObservation()
//            throws Exception {
//        // Given
//        String name = "testCreateThingWithExpandLocationAndDatastreamIncludeSensorObservedPropertyhObservation";
//
//        List<ExpandedLocation> locations = List.of(DtoFactory.getLocation(name + "Location"));
//
//        ExpandedDataStream datastream1 = DtoFactory.getDatastreamMinimal(name);
//        ExpandedDataStream datastream2 = DtoFactory.getDatastreamMinimal(name + "1");
//        ExpandedDataStream datastream3 = DtoFactory.getDatastreamMinimal(name + "2");
//
//        List<ExpandedDataStream> datastreams = List.of(datastream1, datastream2, datastream3);
//        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithDatastreamsLocations(name,
//                "testThing With Location and Datastream",
//                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), datastreams, locations);
//        // When
//
//        JsonNode json = getJsonResponseFromPost(dtoThing,
//                "/Things?$expand=Locations,Datastreams($expand=Sensor,ObservedProperty,Observations)", 201);
//
//        UtilsAssert.assertThing(dtoThing, json, true);
//
//    }

    @Test
    public void testCreateThingWithMultipleLocation() throws Exception {
        // Given
        String name = "testCreateThingWithMultipleLocation";

        ExpandedLocation location1 = DtoFactory.getLocation(name + "1");
        ExpandedLocation location2 = DtoFactory.getLocation(name + "2");
        List<ExpandedLocation> locations = List.of(location1, location2);
        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithLocations(name,
                "testThing With Location and Datastream",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), locations);
        // When

        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things?$expand=Locations", 201);

        UtilsAssert.assertThing(dtoThing, json, true);

    }

    // update
    @Test
    public void testUpdateThing() throws Exception {
        // Given
        String name = "testUpdateThing";

        ExpandedLocation location1 = DtoFactory.getLocation(name + "1");
        ExpandedLocation location2 = DtoFactory.getLocation(name + "2");
        List<ExpandedLocation> locations = List.of(location1, location2);
        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithLocations(name,
                "testThing With Location and Datastream",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), locations);

        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things?$expand=Locations", 201);
        String idJson = getIdFromJson(json);

        UtilsAssert.assertThing(dtoThing, json, true);
        // When
        ExpandedThing dtoThingToUpdate = DtoFactory.getExpandedThingWithLocations(name,
                "testThing With Location and Datastream update",
                Map.of("manufacturer update", "New Corp update", "installationDate update", "2025-12-25"), null);
        getJsonResponseFromPut(dtoThingToUpdate, String.format("/Things(%s)", idJson), 204);
        // then
        json = getJsonResponseFromGet(String.format("/Things(%s)", idJson), 200);
        UtilsAssert.assertThing(dtoThingToUpdate, json);
    }

    @Test
    public void testUpdateThingLocation() throws Exception {
        // Given
        String name = "testUpdateThingLocation";

        List<ExpandedLocation> locationsCreate = List.of(DtoFactory.getLocation(name));
        ExpandedLocation locationsUpdate = DtoFactory.getLocation(name + "2");

        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithLocations(name + "Thing", "testThing With 1 Location ",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), locationsCreate);
        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things?$expand=Locations", 201);
        String idThing = getIdFromJson(json);
        UtilsAssert.assertThing(dtoThing, json);
        JsonNode locationsNode = json.get("Locations");
        assertTrue(locationsNode.size() > 0);

        JsonNode locationNode = locationsNode.get(0);
        String idLocation = getIdFromJson(locationNode);

        // When

        json = getJsonResponseFromPut(locationsUpdate, String.format("/Things(%s)/Locations(%s)", idThing, idLocation),
                204);
        // then
        json = getJsonResponseFromGet(String.format("/Things(%s)/Locations(%s)", idThing, idLocation), 200);
        UtilsAssert.assertLocation(locationsUpdate, json);
    }

    @Test
    public void testUpdateThingDatastream() throws Exception {
        // Given
        String name = "testUpdateThingDatastream";

        List<ExpandedLocation> locations = List.of(DtoFactory.getLocation(name + "1"));

        List<ExpandedDataStream> datastreams = List.of(DtoFactory.getDatastreamMinimal(name + "2"));

        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithDatastreamsLocations(name,
                "testThing With Location and Datastream",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), datastreams, locations);

        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things?$expand=Datastreams", 201);
        String idThing = getIdFromJson(json);
        JsonNode datastreamNode = json.get("Datastreams");
        assertTrue(datastreamNode.size() > 0);

        JsonNode datastreamJson = datastreamNode.get(0);
        String idDatastream = getIdFromJson(datastreamJson);

        UtilsAssert.assertThing(dtoThing, json);
        // When
        ExpandedDataStream datastreamsUpdate = DtoFactory.getDatastreamMinimal(name + "3");

        json = getJsonResponseFromPut(datastreamsUpdate,
                String.format("/Things(%s)/Datastreams(%s)", idThing, idDatastream), 204);
        // then
        json = getJsonResponseFromGet(String.format("/Things(%s)/Datastreams(%s)", idThing, idDatastream), 200);
        UtilsAssert.assertDatastream(datastreamsUpdate, json);
    }

    @Test
    public void testUpdateThingLocationRef() throws Exception {
        // Given
        String name = "testUpdateThingLocationRef";

        ExpandedThing dtoThing = DtoFactory.getExpandedThing(name, "testThing With Location and Datastream",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));

        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things", 201);
        String idThing = getIdFromJson(json);
        UtilsAssert.assertThing(dtoThing, json);

        ExpandedLocation dtoLocation = DtoFactory.getLocation(name + "Location");

        // when
        json = getJsonResponseFromPost(dtoLocation, "Locations", 201);
        String idLocation = getIdFromJson(json);

        UtilsAssert.assertLocation(dtoLocation, json);
        // When

        json = getJsonResponseFromPost(new RefId(idLocation), String.format("/Things(%s)/Locations/$ref", idThing),
                204);
        // then

    }

    @Test
    public void testUpdateThingDatastreamRef() throws Exception {
        // Given
        String name = "testUpdateThingDatastreamRef";

        List<ExpandedDataStream> datastreams = List.of(DtoFactory.getDatastreamMinimal(name + "2"));

        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithDatastreams(name,
                "testThing With Location and Datastream",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), datastreams);

        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things?$expand=Datastreams", 201);
        String idThing = getIdFromJson(json);

        List<ExpandedDataStream> datastreamsUpdate = List.of(DtoFactory.getDatastreamMinimal(name + "3"));

        ExpandedThing dtoThingUpdate = DtoFactory.getExpandedThingWithDatastreams(name + "Update",
                "testThing With Location and Datastream",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), datastreamsUpdate);

        json = getJsonResponseFromPost(dtoThingUpdate, "/Things?$expand=Datastreams", 201);
        JsonNode datastreamNode = json.get("Datastreams");
        assertEquals(datastreamNode.size(), 1);
        JsonNode datastreamJson = datastreamNode.get(0);
        String idDatastreamUpdate = getIdFromJson(datastreamJson);

        UtilsAssert.assertThing(dtoThingUpdate, json);
        // When

        json = getJsonResponseFromPost(new RefId(idDatastreamUpdate),
                String.format("/Things(%s)/Datastreams/$ref", idThing), 204);
        // then
        json = getJsonResponseFromGet(String.format("/Things(%s)?$expand=Datastreams", idThing), 200);
        assertEquals(json.get("Datastreams").size(), 2);
    }

    // patch
    @Test
    public void testUpdatePatchThing() throws Exception {
        // Given
        String name = "testUpdatePatchThing";

        ExpandedLocation location1 = DtoFactory.getLocation(name + "1");
        ExpandedLocation location2 = DtoFactory.getLocation(name + "2");
        List<ExpandedLocation> locations = List.of(location1, location2);
        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithLocations(name,
                "testThing With Location and Datastream",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), locations);

        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things?$expand=Locations", 201);
        String idJson = getIdFromJson(json);

        UtilsAssert.assertThing(dtoThing, json, true);
        // When
        ExpandedThing dtoThingToUpdate = DtoFactory.getExpandedThingWithLocations(null,
                "testThing With Location and Datastream update", Map.of("installationDate update", "2025-12-25"), null);
        json = getJsonResponseFromPatch(dtoThingToUpdate, String.format("Things(%s)", idJson), 204);
        // then
    }

}

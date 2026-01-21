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
package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.sensorthing.integration;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedDataStream;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.eclipse.sensinact.sensorthings.sensing.dto.util.DtoMapperSimple;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.ws.rs.NotFoundException;

/**
 * Unit test for simple App.
 */
public class ThingTest extends AbstractIntegrationTest {

    /**
     * test create thing with existing location
     *
     * @throws Exception
     */
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

    /**
     * test create thing with existing datastream
     *
     * @throws Exception
     */
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

    /**
     * test create simple thing
     *
     * @throws Exception
     */
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

    /**
     * test create thing with missing mandatory field
     *
     * @throws Exception
     */
    @Test
    public void testCreateThingSimpleMissingField() throws Exception {
        // Given
        String name = "testCreateThingSimpleMissingField";

        ExpandedThing dtoThing = DtoFactory.getExpandedThing(null, "testThing",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        // When
        getJsonResponseFromPost(dtoThing, "/Things", 400);

    }

    /**
     * test create create thing with one location
     *
     * @throws Exception
     */
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

    /**
     * test create thing with location and datastream
     *
     * @throws Exception
     */
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

    /**
     * test create thing that include in payload location, datastream, sensor,
     * observed property
     *
     * @throws Exception
     */
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

    @Test
    public void testCreateThingWithExpandLocationAndDatastreamIncludeSensorObservedPropertyhObservation()
            throws Exception {
        // Given
        String name = "testCreateThingWithExpandLocationAndDatastreamIncludeSensorObservedPropertyhObservation";

        List<ExpandedLocation> locations = List.of(DtoFactory.getLocation(name + "Location"));

        ExpandedDataStream datastream1 = DtoFactory.getDatastreamLinkThingWithSensorObservedProperty(name + 1,
                new RefId(name));

        List<ExpandedDataStream> datastreams = List.of(datastream1);
        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithDatastreamsLocations(name,
                "testThing With Location and Datastream",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), datastreams, locations);
        // When

        JsonNode json = getJsonResponseFromPost(dtoThing,
                "/Things?$expand=Locations,Datastreams($expand=Sensor,ObservedProperty,Observations)", 201);

        assertNotNull(json.get("Datastreams"));
        assertNotNull(json.get("Datastreams").get(0).get("Sensor"));
        assertNotNull(json.get("Datastreams").get(0).get("ObservedProperty"));
        assertNotNull(json.get("Datastreams").get(0).get("Observations"));
        assertNotNull(json.get("Datastreams").get(0).get("Observations").get(0));
        assertNotNull(json.get("Locations"));
        assertNotNull(json.get("Locations").get(0));

    }

    /**
     * test create thing with multiple location
     *
     * @throws Exception
     */
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

    /**
     * Tests that <code>PATCH</code> can be used to update a Thing
     */
    /*
     * test simple update thing
     */
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
        ServiceSnapshot serviceAdmin = serviceUseCase.read(session, idJson, "admin");

        assertEquals("testThing With Location and Datastream update",
                DtoMapperSimple.getResourceField(serviceAdmin, "description", String.class));
    }

    /**
     * test update location through thing endpoint
     *
     * @throws Exception
     */
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
        ServiceSnapshot serviceAdmin = serviceUseCase.read(session, idLocation, "admin");

        assertEquals(name + "2", DtoMapperSimple.getResourceField(serviceAdmin, "friendlyName", String.class));

    }

    /**
     * test update datastream through thing endpoint
     *
     * @throws Exception
     */
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

        ServiceSnapshot serviceAdmin = serviceUseCase.read(session, idDatastream, "admin");

        assertEquals(name + "3", DtoMapperSimple.getResourceField(serviceAdmin, "friendlyName", String.class));
    }

    /**
     * test add location link between existing thing and existing location using
     * $ref endpoint the location should be linked to thing
     *
     * @throws Exception
     */
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

        ServiceSnapshot service = serviceUseCase.read(session, idThing, "thing");
        @SuppressWarnings("unchecked")
        List<String> idLocations = (List<String>) DtoMapperSimple.getResourceField(service, "locationIds",
                Object.class);
        assertTrue(idLocations.contains(idLocation));
    }

    /**
     * test create link between existing datastream and thing to have the thing be
     * link to the existing datastream. the datastream should move the one thing to
     * another using the $ref endpoint
     */
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
        ServiceSnapshot service = serviceUseCase.read(session, idThing, "thing");
        ServiceSnapshot serviceDatastream = serviceUseCase.read(session, idDatastreamUpdate, "datastream");

        @SuppressWarnings("unchecked")
        List<String> datastreamIds = DtoMapperSimple.getResourceField(service, "datastreamIds", List.class);
        assertTrue(datastreamIds.contains(idDatastreamUpdate));
        assertEquals(idThing, DtoMapperSimple.getResourceField(serviceDatastream, "thingId", String.class));

    }

    /**
     * Tests that <code>PATCH</code> can be used to update a Thing
     */
    /**
     * test simple patch thing endpoint
     *
     * @throws Exception
     */
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
        ServiceSnapshot serviceAdmin = serviceUseCase.read(session, idJson, "admin");

        assertEquals("testThing With Location and Datastream update",
                DtoMapperSimple.getResourceField(serviceAdmin, "description", String.class));

    }

    /**
     * test delete thing without link to locations and datastream
     */
    @Test
    public void testDeleteThingSimple() throws Exception {
        // Given
        String name = "testDeleteThingSimple";

        ExpandedThing dtoThing = DtoFactory.getExpandedThing(name, "testThing",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things", 201);
        String thingId = getIdFromJson(json);
        UtilsAssert.assertThing(dtoThing, json);
        // WhenIllegalArgumentException
        getJsonResponseFromDelete(String.format("Things(%s)", thingId), 204);
        // then
        assertThrows(NotFoundException.class, () -> {
            serviceUseCase.read(session, thingId, "thing");
        });

    }

    @Test
    public void testDeleteThingWithLocationAndDatastream() throws Exception {
        // Given
        String name = "testDeleteThingWithLocationAndDatastream";

        List<ExpandedLocation> locations = List.of(DtoFactory.getLocation(name + "1"));

        List<ExpandedDataStream> datastreams = List.of(DtoFactory.getDatastreamMinimal(name + "2"));

        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithDatastreamsLocations(name,
                "testThing With Location and Datastream",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), datastreams, locations);
        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things?$expand=Datastreams", 201);

        String thingId = getIdFromJson(json);
        JsonNode datastreamNode = json.get("Datastreams");
        assertEquals(datastreamNode.size(), 1);
        JsonNode datastreamJson = datastreamNode.get(0);
        String idDatastream = getIdFromJson(datastreamJson);

        UtilsAssert.assertThing(dtoThing, json);
        // When
        getJsonResponseFromDelete(String.format("Things(%s)", thingId), 204);
        // then
        assertThrows(NotFoundException.class, () -> {
            serviceUseCase.read(session, thingId, "thing");
        });
        assertThrows(NotFoundException.class, () -> {
            serviceUseCase.read(session, idDatastream, "datastream");
        });
    }

    /**
     * test delete thing datastream ref
     */
    public void testDeleteThingDatastreamRef() throws Exception {
        // given
        String name = "testDeleteThingDatastreamRef";

        List<ExpandedLocation> locations = List.of(DtoFactory.getLocation(name + "1"));

        List<ExpandedDataStream> datastreams = List.of(DtoFactory.getDatastreamMinimal(name + "2"));

        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithDatastreamsLocations(name,
                "testThing With Location and Datastream",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), datastreams, locations);
        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things?$expand=Datastreams", 201);

        String thingId = getIdFromJson(json);
        JsonNode datastreamNode = json.get("Datastreams");
        assertEquals(datastreamNode.size(), 1);
        JsonNode datastreamJson = datastreamNode.get(0);
        String idDatastream = getIdFromJson(datastreamJson);

        UtilsAssert.assertThing(dtoThing, json);
        // when
        getJsonResponseFromDelete(String.format("/Things(%s)/Datastreams/$ref?$id=%s", thingId, idDatastream), 204);
        // then
        ServiceSnapshot serviceThing = serviceUseCase.read(session, thingId, "things");
        assertFalse(DtoMapperSimple.getResourceField(serviceThing, "datastreamIds", List.class).contains(idDatastream));
        assertThrows(NotFoundException.class, () -> {
            serviceUseCase.read(session, idDatastream, "datastream");
        });
    }

    /**
     * test delete thing location ref
     */
    public void testDeleteThingLocationRef() throws Exception {
        String name = "testDeleteThingLocationRef";

        List<ExpandedLocation> locations = List.of(DtoFactory.getLocation(name + "1"));

        List<ExpandedDataStream> datastreams = List.of(DtoFactory.getDatastreamMinimal(name + "2"),
                DtoFactory.getDatastreamMinimal(name + "3"));

        ExpandedThing dtoThing = DtoFactory.getExpandedThingWithDatastreamsLocations(name,
                "testThing With Location and Datastream",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"), datastreams, locations);
        JsonNode json = getJsonResponseFromPost(dtoThing, "/Things?$expand=Datastreams,Locations", 201);

        String thingId = getIdFromJson(json);
        JsonNode locationsNode = json.get("Locations");
        assertEquals(locationsNode.size(), 1);
        JsonNode locationJson = locationsNode.get(0);
        String idLocation = getIdFromJson(locationJson);
        UtilsAssert.assertThing(dtoThing, json);
        // when
        getJsonResponseFromDelete(String.format("/Things(%s)/Locations(%s)/$ref", thingId, idLocation), 204);
        // then
        assertThrows(NotFoundException.class, () -> {
            serviceUseCase.read(session, idLocation, "datastream");
        });
        ServiceSnapshot service = serviceUseCase.read(session, thingId, "thing");
        assertFalse(DtoMapperSimple.getResourceField(service, "locationIds", List.class).contains(idLocation));

    }

    /**
     * test delete location association ($ref) to a thing
     */
    @Test
    public void testDeleteLocationThings() throws Exception {
        // given
        String name = "testDeleteLocationThings";

        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String idThing = getIdFromJson(json);

        ExpandedLocation dtoLocation = DtoFactory.getLocationLinkThing(name + "1", "application/vnd.geo+json",
                new Point(-122.4194, 37.7749), List.of(new RefId(idThing)));

        json = getJsonResponseFromPost(dtoLocation, "Locations", 201);
        dtoLocation = DtoFactory.getLocationLinkThing(name + "1", "application/vnd.geo+json",
                new Point(-122.4194, 37.7749), List.of(new RefId(idThing)));

        json = getJsonResponseFromPost(dtoLocation, "Locations", 201);
        // when
        getJsonResponseFromDelete(String.format("/Things(%s)/Locations/$ref", idThing), 204);
        // then
        ServiceSnapshot thingService1 = serviceUseCase.read(session, idThing, DtoMapperSimple.SERVICE_THING);
        assertEquals(0, DtoMapperSimple.getResourceField(thingService1, "locationIds", List.class).size());

    }

}

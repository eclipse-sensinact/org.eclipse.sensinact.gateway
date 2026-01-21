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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.core.snapshot.ServiceSnapshot;
import org.eclipse.sensinact.gateway.geojson.Point;
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
public class LocationTest extends AbstractIntegrationTest {

    /**
     * test simple create of location
     *
     * @throws Exception
     */
    @Test
    public void testCreateLocation() throws Exception {
        // given
        String name = "testCreateLocation";

        ExpandedLocation dtoLocation = DtoFactory.getLocation(name);

        // when
        JsonNode json = getJsonResponseFromPost(dtoLocation, "Locations", 201);
        UtilsAssert.assertLocation(dtoLocation, json);

    }

    /**
     * test create location with missing field
     *
     * @throws Exception
     */
    @Test
    public void testDeleteLocation() throws Exception {
        // given
        String name = "testDeleteLocation";

        ExpandedLocation dtoLocation = DtoFactory.getLocation(name);

        JsonNode json = getJsonResponseFromPost(dtoLocation, "Locations", 201);
        String idLocation = getIdFromJson(json);
        UtilsAssert.assertLocation(dtoLocation, json);
        // when
        getJsonResponseFromDelete(String.format("Locations(%s)", idLocation), 204);
        // then
        assertThrows(NotFoundException.class, () -> {
            serviceUseCase.read(session, idLocation, "location");
        });

    }

    @Test
    public void testCreateLocationMissingField() throws Exception {
        // given
        String name = "testCreateLocation";

        // name
        ExpandedLocation dtoLocation = DtoFactory.getLocationLinkThing(null, "application/vnd.geo+json",
                new Point(-122.4194, 37.7749), null);
        getJsonResponseFromPost(dtoLocation, "Locations", 400);
        // encoding type
        dtoLocation = DtoFactory.getLocationLinkThing(name + "1", null, new Point(-122.4194, 37.7749), null);
        getJsonResponseFromPost(dtoLocation, "Locations", 400);

        // location
        dtoLocation = DtoFactory.getLocationLinkThing(name + "1", "application/vnd.geo+json", null, null);
        getJsonResponseFromPost(dtoLocation, "Locations", 400);
    }

    /**
     * test create location link to a thing, the location and thing should be linked
     *
     * @throws Exception
     */
    @Test
    public void testDeleteLocationLinkedThing() throws Exception {
        // given
        String name = "testCreateLocationLinkedThing";

        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String idThing = getIdFromJson(json);
        List<RefId> listId = new ArrayList<RefId>();
        listId.add(DtoFactory.getRefId(idThing));
        ExpandedLocation dtoLocation = DtoFactory.getLocationLinkThing(name + "1", "application/vnd.geo+json",
                new Point(-122.4194, 37.7749), listId);

        json = getJsonResponseFromPost(dtoLocation, "Locations", 201);
        String idLocation = getIdFromJson(json);
        UtilsAssert.assertLocation(dtoLocation, json);
        // when
        getJsonResponseFromDelete(String.format("Locations(%s)", idLocation), 204);
        // then
        assertThrows(NotFoundException.class, () -> {
            serviceUseCase.read(session, idLocation, "location");
        });
        ServiceSnapshot service = serviceUseCase.read(session, idThing, "thing");
        assertFalse(DtoMapperSimple.getResourceField(service, "locationIds", List.class).contains(idLocation));
    }

    /**
     * test create of location using thing/location endpoint
     *
     * @throws Exception
     */
    @Test
    public void testCreateLocationLinkedThing() throws Exception {
        // given
        String name = "testCreateLocationLinkedThing";

        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String idThing = getIdFromJson(json);
        List<RefId> listId = new ArrayList<RefId>();
        listId.add(DtoFactory.getRefId(idThing));
        ExpandedLocation dtoLocation = DtoFactory.getLocationLinkThing(name + "1", "application/vnd.geo+json",
                new Point(-122.4194, 37.7749), listId);

        // when
        json = getJsonResponseFromPost(dtoLocation, "Locations", 201);
        UtilsAssert.assertLocation(dtoLocation, json);

    }

    /**
     * test delete historicalLocation
     */
    @Test
    public void testDeleteHistocalLocation() throws Exception {
        // given
        String name = "testDeleteHistocalLocation";
        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String idThing = getIdFromJson(json);
        ExpandedLocation dtoLocation = DtoFactory.getLocation(name + "1");

        json = getJsonResponseFromPost(dtoLocation, String.format("Things(%s)/Locations", idThing), 201);
        String idLocation = getIdFromJson(json);
        UtilsAssert.assertLocation(dtoLocation, json);
        // when
        getJsonResponseFromDelete(String.format("/HistoricalLocations(%s)", idLocation), 409);

    }

    /**
     * test delete things association ($ref) to a location
     */
    @Test
    public void testDeleteLocationThings() throws Exception {
        // given
        String name = "testDeleteLocationThings";

        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String idThing = getIdFromJson(json);

        ExpandedThing thing2 = DtoFactory.getExpandedThing(name + "2", "testThing existing 2 Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing2, "Things", 201);
        String idThing2 = getIdFromJson(json);

        ExpandedLocation dtoLocation = DtoFactory.getLocationLinkThing(name + "1", "application/vnd.geo+json",
                new Point(-122.4194, 37.7749), List.of(new RefId(idThing), new RefId(idThing2)));

        json = getJsonResponseFromPost(dtoLocation, "Locations", 201);
        String idLocation = getIdFromJson(json);
        // when
        getJsonResponseFromDelete(String.format("/Locations(%s)/Things/$ref", idLocation), 204);
        // then
        ServiceSnapshot thingService1 = serviceUseCase.read(session, idThing, DtoMapperSimple.SERVICE_THING);
        ServiceSnapshot thingService2 = serviceUseCase.read(session, idThing2, DtoMapperSimple.SERVICE_THING);
        assertEquals(0, DtoMapperSimple.getResourceField(thingService1, "locationIds", List.class).size());
        assertEquals(0, DtoMapperSimple.getResourceField(thingService2, "locationIds", List.class).size());

    }

    /**
     * test delete a specific thing association ($ref) to a location
     */
    @Test
    public void testDeleteLocationThing() throws Exception {
        // given
        String name = "testDeleteLocationThing";

        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String idThing = getIdFromJson(json);

        ExpandedThing thing2 = DtoFactory.getExpandedThing(name + "2", "testThing existing 2 Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        json = getJsonResponseFromPost(thing2, "Things", 201);
        String idThing2 = getIdFromJson(json);

        ExpandedLocation dtoLocation = DtoFactory.getLocationLinkThing(name + "1", "application/vnd.geo+json",
                new Point(-122.4194, 37.7749), List.of(new RefId(idThing), new RefId(idThing2)));

        json = getJsonResponseFromPost(dtoLocation, "Locations", 201);
        String idLocation = getIdFromJson(json);
        // when
        getJsonResponseFromDelete(String.format("/Locations(%s)/Things(%s)/$ref", idLocation, idThing), 204);
        // then
        ServiceSnapshot thingService1 = serviceUseCase.read(session, idThing, DtoMapperSimple.SERVICE_THING);
        ServiceSnapshot thingService2 = serviceUseCase.read(session, idThing2, DtoMapperSimple.SERVICE_THING);
        assertEquals(0, DtoMapperSimple.getResourceField(thingService1, "locationIds", List.class).size());
        assertEquals(1, DtoMapperSimple.getResourceField(thingService2, "locationIds", List.class).size());

    }

    /**
     * Tests that <code>PUT</code> can be used to update a Location
     */

    /**
     * test update of location simple
     *
     * @throws Exception
     */
    @Test
    public void testUpdateLocation() throws Exception {
        // given
        String name = "testUpdateLocation";

        ExpandedLocation dtoLocation = DtoFactory.getLocation(name + "1");

        // when
        JsonNode json = getJsonResponseFromPost(dtoLocation, "Locations", 201);
        UtilsAssert.assertLocation(dtoLocation, json);
        String idLocation = getIdFromJson(json);
        ExpandedLocation dtoLocationUpdate = DtoFactory.getLocation(name + "2");
        json = getJsonResponseFromPut(dtoLocationUpdate, String.format("Locations(%s)", idLocation), 204);
        // then
        ServiceSnapshot serviceAdmin = serviceUseCase.read(session, idLocation, "admin");

        assertEquals(name + "2", DtoMapperSimple.getResourceField(serviceAdmin, "friendlyName", String.class));

    }

    /**
     * Tests that <code>PATCH</code> can be used to update a Location
     */

    /**
     * test patch location
     *
     * @throws Exception
     */
    @Test
    public void testPatchLocation() throws Exception {
        // given
        String name = "testPatchLocation";

        ExpandedLocation dtoLocation = DtoFactory.getLocation(name + "1");

        // when
        JsonNode json = getJsonResponseFromPost(dtoLocation, "Locations", 201);
        UtilsAssert.assertLocation(dtoLocation, json);
        String idLocation = getIdFromJson(json);
        ExpandedLocation dtoLocationUpdate = DtoFactory.getLocation(name + "2");
        json = getJsonResponseFromPatch(dtoLocationUpdate, String.format("Locations(%s)", idLocation), 204);
        // then
        ServiceSnapshot serviceAdmin = serviceUseCase.read(session, idLocation, "admin");

        assertEquals(name + "2", DtoMapperSimple.getResourceField(serviceAdmin, "friendlyName", String.class));

    }
}

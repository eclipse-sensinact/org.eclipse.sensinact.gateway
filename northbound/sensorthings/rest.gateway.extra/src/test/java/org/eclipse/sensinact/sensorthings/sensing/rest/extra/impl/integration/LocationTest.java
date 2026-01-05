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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.geojson.Point;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.RefId;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Unit test for simple App.
 */
public class LocationTest extends AbstractIntegrationTest {

    @Test
    public void testCreateLocation() throws Exception {
        // given
        String name = "testCreateLocation";

        ExpandedLocation dtoLocation = DtoFactory.getLocation(name);

        // when
        JsonNode json = getJsonResponseFromPost(dtoLocation, "Locations", 201);
        UtilsAssert.assertLocation(dtoLocation, json);

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

    @Test
    public void testCreateLocationThroughThing() throws Exception {
        // given
        String name = "testCreateLocationThroughThing";
        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String idThing = getIdFromJson(json);
        ExpandedLocation dtoLocation = DtoFactory.getLocation(name + "1");

        // when
        json = getJsonResponseFromPost(dtoLocation, String.format("Things(%s)/Locations", idThing), 201);
        // then

        UtilsAssert.assertLocation(dtoLocation, json);

    }
    // update

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
        json = getJsonResponseFromGet(String.format("Locations(%s)", idLocation), 200);
        UtilsAssert.assertLocation(dtoLocationUpdate, json);
    }

}

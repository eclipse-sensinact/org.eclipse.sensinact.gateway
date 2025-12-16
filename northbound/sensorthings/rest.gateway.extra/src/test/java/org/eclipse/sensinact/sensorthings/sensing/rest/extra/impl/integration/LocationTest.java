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
        UtilsAssert.assertLocation(dtoLocation, json);

    }

}

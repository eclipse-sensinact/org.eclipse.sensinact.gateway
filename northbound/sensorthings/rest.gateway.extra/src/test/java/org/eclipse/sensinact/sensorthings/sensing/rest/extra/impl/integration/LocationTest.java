package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedLocation;
import org.eclipse.sensinact.sensorthings.sensing.dto.expand.ExpandedThing;
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
        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String idThing = getIdFromJson(json);
        List<Thing> listId = new ArrayList<Thing>();
        listId.add(DtoFactory.getIdThing(idThing));
        ExpandedLocation dtoLocation = DtoFactory.getLocationLinkThing(name, listId);

        // when
        json = getJsonResponseFromPost(dtoLocation, "Locations", 201);
        UtilsAssert.assertLocation(dtoLocation, json);

        json = getJsonResponseFromGet(String.format("Locations(%s)", getIdFromJson(json)));
        UtilsAssert.assertLocation(dtoLocation, json);

    }

    @Test
    public void testCreateLocationthroughThing() throws Exception {
        // given
        String name = "testCreateLocationthroughThing";
        ExpandedThing thing = DtoFactory.getExpandedThing(name, "testThing existing Location",
                Map.of("manufacturer", "New Corp", "installationDate", "2025-11-25"));
        JsonNode json = getJsonResponseFromPost(thing, "Things", 201);
        String idThing = getIdFromJson(json);
        ExpandedLocation dtoLocation = DtoFactory.getLocation(name);

        // when
        json = getJsonResponseFromPost(dtoLocation, String.format("Things(%s)/Locations", idThing), 201);
        UtilsAssert.assertLocation(dtoLocation, json);

    }

}

package org.eclipse.sensinact.sensorthings.sensing.rest.extra.impl.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.http.HttpResponse;

import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class CreateEndpointTest extends AbstractIntegrationTest {

    // Thing
    @Test
    public void givenNoThingWhenCreateThingNameDescriptionThenThingCreated() throws Exception {
        Thing dtoThing = new Thing();
        dtoThing.name = "testThing";
        dtoThing.description = "testThing creation";

        HttpResponse<String> response = queryPost("/1.1/Things", dtoThing);
        assertEquals(response.statusCode(), 201);
    }

    @Test
    public void givenNoThingWhenCreateLocationLinkToThingThenLocationCreated() throws Exception {
        // TODO
        Location dtoLocation = new Location();
        dtoLocation.name = "testLocation";
        dtoLocation.description = "testLocation creation";
        dtoLocation.thingsLink = "testThing";

        HttpResponse<String> response = queryPost("/1.1/Location", dtoThing);
        assertEquals(response.statusCode(), 201);
    }
}

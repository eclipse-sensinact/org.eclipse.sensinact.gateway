package org.eclipse.sensinact.sensorthings.sensing.rest.extra;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import org.eclipse.sensinact.sensorthings.sensing.dto.Location;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Produces(APPLICATION_JSON)
@Path("/v1.1/Locations")
public interface LocationsExtra {
    @POST
    public Response create(Location location);

    @PUT
    @Path("/{id}")
    public Response update(String id, Location location);

    @DELETE
    @Path("/{id}")

    public Response delete(String id);
}

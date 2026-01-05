package org.eclipse.sensinact.sensorthings.sensing.rest.extra;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Produces(APPLICATION_JSON)
@Path("/v1.1/Things({id})")
public interface ThingsDelete {

    @DELETE
    public Response deleteThing(@PathParam("id") String id);
}

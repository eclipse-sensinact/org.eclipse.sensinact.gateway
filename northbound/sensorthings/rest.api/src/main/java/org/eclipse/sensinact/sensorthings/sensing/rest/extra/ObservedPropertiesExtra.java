package org.eclipse.sensinact.sensorthings.sensing.rest.extra;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Produces(APPLICATION_JSON)
@Path("/v1.1/ObservedProperty")
public interface ObservedPropertiesExtra {
    @POST
    public Response create(ObservedProperty observedProperty);

    @PUT
    @Path("/{id}")
    public Response update(String id, ObservedProperty observedProperty);

    @DELETE
    @Path("/{id}")
    public Response delete(String id);
}

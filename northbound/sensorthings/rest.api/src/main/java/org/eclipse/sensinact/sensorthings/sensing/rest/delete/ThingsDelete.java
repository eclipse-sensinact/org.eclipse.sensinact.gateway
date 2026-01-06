package org.eclipse.sensinact.sensorthings.sensing.rest.delete;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

public interface ThingsDelete {
    @DELETE
    public Response deleteThing(@PathParam("id") String id);

    @DELETE
    @Path("/Datastreams/$ref")
    public Response deleteDatastreamRef(@PathParam("id") String id);

    @DELETE
    @Path("/Locations({id2})/$ref")
    public Response deleteLocationRef(@PathParam("id") String id, @PathParam("id2") String id2);
}

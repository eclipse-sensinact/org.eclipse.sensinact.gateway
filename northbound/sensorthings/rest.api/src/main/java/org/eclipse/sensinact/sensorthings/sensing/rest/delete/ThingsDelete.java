package org.eclipse.sensinact.sensorthings.sensing.rest.delete;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

public interface ThingsDelete {
    @DELETE
    public Response deleteThing(@QueryParam("id") String id);

    @DELETE
    @Path("/Datastreams/$ref")
    public Response deleteDatastreamRef(@QueryParam("id") String id);

    @DELETE
    @Path("/Locations({id2})/$ref")
    public Response deleteLocationRef(@QueryParam("id") String id, @QueryParam("id2") String id2);
}

package org.eclipse.sensinact.sensorthings.sensing.rest.delete;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

public interface LocationsDelete {
    @DELETE
    public Response deleteLocation(@PathParam("id") String id);

}

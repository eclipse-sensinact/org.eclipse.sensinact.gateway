package org.eclipse.sensinact.sensorthings.sensing.rest.delete;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

public interface LocationsDelete {
    @DELETE
    public Response deleteLocation(@QueryParam("id") String id);

}

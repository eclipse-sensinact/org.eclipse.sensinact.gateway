package org.eclipse.sensinact.sensorthings.sensing.rest.delete;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

public interface DatastreamsDelete {

    @DELETE
    public Response deleteDatastream(@QueryParam("id") String id);

}

package org.eclipse.sensinact.sensorthings.sensing.rest.delete;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

public interface ObservationsDelete {
    @DELETE
    public Response deleteObservation(@QueryParam("id") String id);

}

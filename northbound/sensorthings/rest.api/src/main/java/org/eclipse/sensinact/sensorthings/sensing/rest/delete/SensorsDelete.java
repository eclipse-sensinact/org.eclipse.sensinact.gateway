package org.eclipse.sensinact.sensorthings.sensing.rest.delete;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

public interface SensorsDelete {
    @DELETE
    public Response deleteSensor(@QueryParam("id") String id);

}

package org.eclipse.sensinact.sensorthings.sensing.rest.delete;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

public interface ObservedPropertiesDelete {
    @DELETE
    public Response deleteObservedProperty(@QueryParam("id") String id);

}

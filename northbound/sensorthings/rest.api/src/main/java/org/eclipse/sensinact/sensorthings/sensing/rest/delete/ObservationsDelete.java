package org.eclipse.sensinact.sensorthings.sensing.rest.delete;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

public interface ObservationsDelete {
    @DELETE
    public Response deleteObservation(@PathParam("id") String id);

    @DELETE
    @Path("/FeatureOfInterest/$ref")
    public Response deleteObservationFeatureOfInterest(@PathParam("id") String id);
}

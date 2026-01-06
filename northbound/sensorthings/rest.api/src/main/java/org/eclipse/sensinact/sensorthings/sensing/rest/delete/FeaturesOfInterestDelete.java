package org.eclipse.sensinact.sensorthings.sensing.rest.delete;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

public interface FeaturesOfInterestDelete {
    @DELETE
    public Response deleteFeatureOfInterest(@PathParam("id") String id);

}

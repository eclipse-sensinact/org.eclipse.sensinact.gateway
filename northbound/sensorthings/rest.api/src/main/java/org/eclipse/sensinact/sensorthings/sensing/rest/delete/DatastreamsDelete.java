package org.eclipse.sensinact.sensorthings.sensing.rest.delete;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

public interface DatastreamsDelete {

    @DELETE
    public Response deleteDatastream(@PathParam("id") String id);

    @DELETE
    @Path("/Sensor/$ref")
    public Response deleteDatastreamSensorRef(@PathParam("id") String id);

    @DELETE
    @Path("/ObservedProperty/$ref")
    public Response deleteDatastreamObservedPropertyRef(@PathParam("id") String id);

}

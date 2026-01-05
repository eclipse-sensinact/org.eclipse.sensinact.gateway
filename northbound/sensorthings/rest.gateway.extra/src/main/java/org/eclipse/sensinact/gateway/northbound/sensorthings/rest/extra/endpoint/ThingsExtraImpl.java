package org.eclipse.sensinact.gateway.northbound.sensorthings.rest.extra.endpoint;

import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;
import org.eclipse.sensinact.sensorthings.sensing.rest.extra.ThingsCreate;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/v1.1/Things")
public class ThingsExtraImpl implements ThingsCreate {

    @POST
    public Response createThings(Thing thing) {

    }

}

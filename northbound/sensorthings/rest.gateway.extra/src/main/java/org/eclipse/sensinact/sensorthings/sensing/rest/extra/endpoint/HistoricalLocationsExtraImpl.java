package org.eclipse.sensinact.sensorthings.sensing.rest.extra.endpoint;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import org.eclipse.sensinact.sensorthings.sensing.dto.HistoricalLocation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsApplicationSelect;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Produces(APPLICATION_JSON)
@Path("/v1.1/HistoricalLocations")
@Component(service = Object.class, immediate = true, property = { "osgi.jakartars.resource=true" })
@JakartarsApplicationSelect("(osgi.jakartars.name=sensorthings)")
public class HistoricalLocationsExtraImpl extends AbstractEndpoint<HistoricalLocation> {

}

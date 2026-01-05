package org.eclipse.sensinact.sensorthings.sensing.rest.extra.endpoint.dto;

import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ThingExtra extends Thing {

    @JsonProperty("Datastreams")
    public List<DatastreamExtra> datastreams;

    @JsonProperty("Locations")
    public List<Location> locations;

}

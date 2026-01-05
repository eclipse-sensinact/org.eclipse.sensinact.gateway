package org.eclipse.sensinact.sensorthings.sensing.rest.extra.endpoint.dto;

import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DatastreamExtra extends Datastream {

    @JsonProperty("ObservedProperty")
    public ObservedProperty observedProperty;

    @JsonProperty("Sensor")
    public Sensor sensor;

}

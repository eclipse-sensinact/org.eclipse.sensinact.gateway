package org.eclipse.sensinact.sensorthings.sensing.dto.expand;

import java.util.List;

import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.sensorthings.sensing.dto.NameDescription;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExpandedFeatureOfInterest(String selfLink, Object id, String name, String description,
        String encodingType, GeoJsonObject feature,
        @JsonProperty("Observations@iot.navigationLink") String observationsLink,
        @JsonProperty("Observations") List<Observation> observations) implements NameDescription {

}

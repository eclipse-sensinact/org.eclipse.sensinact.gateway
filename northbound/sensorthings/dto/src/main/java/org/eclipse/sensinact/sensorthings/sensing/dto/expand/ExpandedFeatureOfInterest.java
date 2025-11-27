package org.eclipse.sensinact.sensorthings.sensing.dto.expand;

import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExpandedFeatureOfInterest extends FeatureOfInterest {

    @JsonProperty("Observations")
    public List<Observation> observations;
}

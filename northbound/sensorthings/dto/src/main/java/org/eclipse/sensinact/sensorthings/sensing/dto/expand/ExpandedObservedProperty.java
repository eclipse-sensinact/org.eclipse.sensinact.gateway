package org.eclipse.sensinact.sensorthings.sensing.dto.expand;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.dto.NameDescription;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ExpandedObservedProperty(String selfLink, Object id, String name, String description, String definition,
        @JsonInclude(NON_NULL) Map<String, Object> properties,
        @JsonProperty("Datastreams@iot.navigationLink") String datastreamsLink,
        @JsonProperty("Datastream") RefId datastream) implements NameDescription {
}

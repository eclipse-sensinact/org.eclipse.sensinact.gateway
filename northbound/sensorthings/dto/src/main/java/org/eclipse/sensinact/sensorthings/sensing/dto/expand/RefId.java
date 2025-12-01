package org.eclipse.sensinact.sensorthings.sensing.dto.expand;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RefId(@JsonProperty("@iot.id") Object id) {

}

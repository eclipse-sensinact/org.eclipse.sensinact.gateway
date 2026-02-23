/*********************************************************************
* Copyright (c) 2025 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   Kentyou - initial implementation
**********************************************************************/
package org.eclipse.sensinact.gateway.southbound.sensorthings.sensing.rest.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.NameDescription;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ExpandedThing(String selfLink, Object id, String name, String description,
        @JsonInclude(NON_NULL) Map<String, Object> properties,
        @JsonProperty("Datastreams@iot.navigationLink") String datastreamsLink,
        @JsonProperty("HistoricalLocations@iot.navigationLink") String historicalLocationsLink,
        @JsonProperty("Locations@iot.navigationLink") String locationsLink,
        @JsonProperty("Datastreams") List<ExpandedDataStream> datastreams,
        @JsonProperty("Locations") List<Location> locations) implements NameDescription {
}

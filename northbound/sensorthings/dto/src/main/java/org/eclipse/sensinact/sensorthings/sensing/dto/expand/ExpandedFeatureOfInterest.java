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
package org.eclipse.sensinact.sensorthings.sensing.dto.expand;

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.geojson.GeoJsonObject;
import org.eclipse.sensinact.sensorthings.sensing.dto.NameDescription;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * record of featureOf interest for creation/update
 */
public record ExpandedFeatureOfInterest(String selfLink, Object id, String name, String description,
        String encodingType, GeoJsonObject feature,
        @JsonProperty("Observations@iot.navigationLink") String observationsLink, Map<String, Object> properties,
        @JsonProperty("Observations") List<Observation> observations) implements NameDescription {

}

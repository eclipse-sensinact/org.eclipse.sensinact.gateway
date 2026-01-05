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

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.time.Instant;
import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.dto.FeatureOfInterest;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.Self;
import org.eclipse.sensinact.sensorthings.sensing.dto.TimeInterval;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * record of observation for creation/update
 */
public record ExpandedObservation(String selfLink, Object id, @JsonFormat(shape = STRING) Instant phenomenonTime,
        @JsonFormat(shape = STRING) Instant resultTime, Object result, @JsonInclude(NON_NULL) Object resultQuality,
        @JsonInclude(NON_NULL) @JsonFormat(shape = STRING) TimeInterval validTime,
        @JsonInclude(NON_NULL) Map<String, Object> parameters, @JsonInclude(NON_NULL) Map<String, Object> properties,
        @JsonProperty("Datastream@iot.navigationLink") String datastreamLink,
        @JsonProperty("FeatureOfInterest@iot.navigationLink") String featureOfInterestLink,
        @JsonProperty("Datastream") RefId datastream,
        @JsonProperty("FeatureOfInterest") FeatureOfInterest featureOfInterest) implements Id, Self {
}

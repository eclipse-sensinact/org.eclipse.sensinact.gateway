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

import java.util.List;
import java.util.Map;

import org.eclipse.sensinact.gateway.geojson.Geometry;
import org.eclipse.sensinact.sensorthings.sensing.dto.NameDescription;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.TimeInterval;
import org.eclipse.sensinact.sensorthings.sensing.dto.UnitOfMeasurement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * record of datastream for creation/update
 */
public record ExpandedDataStream(String selfLink, Object id, String name, String description, String observationType,
        UnitOfMeasurement unitOfMeasurement, @JsonInclude(NON_NULL) Geometry observedArea,
        @JsonInclude(NON_NULL) @JsonFormat(shape = STRING) TimeInterval phenomenonTime,
        @JsonInclude(NON_NULL) @JsonFormat(shape = STRING) TimeInterval resultTime,
        @JsonInclude(NON_NULL) Map<String, Object> properties,
        @JsonProperty("Observations@iot.navigationLink") String observationsLink,
        @JsonProperty("ObservedProperty@iot.navigationLink") String observedPropertyLink,
        @JsonProperty("Sensor@iot.navigationLink") String sensorLink,
        @JsonProperty("Thing@iot.navigationLink") String thingLink,
        @JsonProperty("Observations") List<ExpandedObservation> observations,
        @JsonProperty("ObservedProperty") ObservedProperty observedProperty, @JsonProperty("Sensor") Sensor sensor,
        @JsonProperty("Observations@iot.nextLink") String obsLink, @JsonProperty("Thing") RefId thing)
        implements NameDescription {
}

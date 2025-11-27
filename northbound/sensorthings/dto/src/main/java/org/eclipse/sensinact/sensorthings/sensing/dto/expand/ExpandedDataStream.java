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
import static org.eclipse.sensinact.core.annotation.dto.DuplicateAction.UPDATE_IF_DIFFERENT;
import static org.eclipse.sensinact.core.annotation.dto.MapAction.USE_KEYS_AS_FIELDS;
import static org.eclipse.sensinact.core.annotation.dto.NullAction.UPDATE_IF_PRESENT;
import static org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage.Literals.DATA_STREAM_SERVICE;
import static org.eclipse.sensinact.sensorthings.models.sensorthings.SensorthingsPackage.Literals.SENSOR_THINGS_DEVICE;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.sensinact.core.annotation.dto.Data;
import org.eclipse.sensinact.core.annotation.dto.Metadata;
import org.eclipse.sensinact.core.annotation.dto.Model;
import org.eclipse.sensinact.core.annotation.dto.Provider;
import org.eclipse.sensinact.core.annotation.dto.Resource;
import org.eclipse.sensinact.core.annotation.dto.Service;
import org.eclipse.sensinact.core.annotation.dto.ServiceModel;
import org.eclipse.sensinact.core.annotation.dto.Timestamp;
import org.eclipse.sensinact.gateway.geojson.Geometry;
import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Id;
import org.eclipse.sensinact.sensorthings.sensing.dto.NameDescription;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;
import org.eclipse.sensinact.sensorthings.sensing.dto.TimeInterval;
import org.eclipse.sensinact.sensorthings.sensing.dto.UnitOfMeasurement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ExpandedDataStream(String selfLink, Object id, String name, String description, String observationType,
UnitOfMeasurement unitOfMeasurement, @JsonInclude(NON_NULL) Geometry observedArea,
@JsonInclude(NON_NULL) @JsonFormat(shape = STRING) TimeInterval phenomenonTime,
@JsonInclude(NON_NULL) @JsonFormat(shape = STRING) TimeInterval resultTime,
@JsonInclude(NON_NULL) Map<String, Object> properties,
@JsonProperty("Observations@iot.navigationLink") String observationsLink,
@JsonProperty("ObservedProperty@iot.navigationLink") String observedPropertyLink,
@JsonProperty("Sensor@iot.navigationLink") String sensorLink,
@JsonProperty("Thing@iot.navigationLink") String thingLink,
@JsonProperty("Observations") List<Observation> observations,
@JsonProperty("ObservedProperty") ObservedProperty observedProperty,
@JsonProperty("Sensor") Sensor sensor,
@JsonProperty("Observations@iot.nextLink") String nextObservation,
@JsonProperty("Thing") Id thing) implements NameDescription {
} 


  
    


/*********************************************************************
* Copyright (c) 2022 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.sensorthings.sensing.dto;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.time.Instant;
import java.util.Map;

import org.geojson.Polygon;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Datastream extends NameDescription {

    public String observationType;
    
    public Map<String, Object> unitOfMeasurement;
    
    @JsonInclude(NON_NULL)
    public Polygon observedArea;
    
    @JsonInclude(NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Instant phenomenonTime;
    
    @JsonInclude(NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Instant resultTime;

    @JsonProperty("Observations@iot.navigationLink")
    public String observationsLink;

    @JsonProperty("ObservedProperty@iot.navigationLink")
    public String observedPropertyLink;

    @JsonProperty("Sensor@iot.navigationLink")
    public String sensorLink;

    @JsonProperty("Thing@iot.navigationLink")
    public String thingLink;
}

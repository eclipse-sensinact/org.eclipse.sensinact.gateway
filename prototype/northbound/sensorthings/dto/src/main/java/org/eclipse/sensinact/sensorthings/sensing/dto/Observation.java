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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Observation extends IdSelf {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Instant phenomenonTime;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Instant resultTime;
    
    public Object result;
    
    @JsonInclude(NON_NULL)
    public Object resultQuality;
    
    @JsonInclude(NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Instant validTime;
    
    @JsonInclude(NON_NULL)
    public Map<String, Object> parameters;

    @JsonProperty("Datastream@iot.navigationLink")
    public String datastreamLink;

    @JsonProperty("FeatureOfInterest@iot.navigationLink")
    public String featureOfInterestLink;

}

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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Thing extends NameDescription {

    @JsonInclude(NON_NULL)
    public Map<String, Object> properties;

    @JsonProperty("Datastreams@iot.navigationLink")
    public String datastreamsLink;

    @JsonProperty("HistoricalLocations@iot.navigationLink")
    public String historicalLocationsLink;

    @JsonProperty("Locations@iot.navigationLink")
    public String locationsLink;

}

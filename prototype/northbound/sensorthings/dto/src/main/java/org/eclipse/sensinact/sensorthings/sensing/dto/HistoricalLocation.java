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

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HistoricalLocation extends IdSelf {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Instant time;

    @JsonProperty("Locations@iot.navigationLink")
    public String locationsLink;

    @JsonProperty("Thing@iot.navigationLink")
    public String thingLink;

}

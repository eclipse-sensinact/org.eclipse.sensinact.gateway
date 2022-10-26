/*********************************************************************
* Copyright (c) 2022 Contributors to the Eclipse Foundation.
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
package org.eclipse.sensinact.sensorthings.sensing.dto;

import org.geojson.GeoJsonObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FeatureOfInterest extends NameDescription {

    public String encodingType;

    public GeoJsonObject feature;

    @JsonProperty("Observations@iot.navigationLink")
    public String observationsLink;

}

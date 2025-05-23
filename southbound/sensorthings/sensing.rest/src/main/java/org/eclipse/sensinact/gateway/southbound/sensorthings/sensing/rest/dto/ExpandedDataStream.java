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

import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.Datastream;
import org.eclipse.sensinact.sensorthings.sensing.dto.Observation;
import org.eclipse.sensinact.sensorthings.sensing.dto.ObservedProperty;
import org.eclipse.sensinact.sensorthings.sensing.dto.Sensor;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExpandedDataStream extends Datastream{

    @JsonProperty("Observations")
    public List<Observation> observations;

    @JsonProperty("ObservedProperty")
    public ObservedProperty observedProperty;

    @JsonProperty("Sensor")
    public Sensor sensor;

    @JsonProperty("Observations@iot.nextLink")
    public String nextObservation;

}

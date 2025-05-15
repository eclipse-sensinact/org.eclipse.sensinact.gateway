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

import org.eclipse.sensinact.sensorthings.sensing.dto.Location;
import org.eclipse.sensinact.sensorthings.sensing.dto.Thing;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExpandedThing extends Thing {

    @JsonProperty("Datastreams")
    public List<ExpandedDataStream> datastreams;

    @JsonProperty("Locations")
    public List<Location> locations;

}

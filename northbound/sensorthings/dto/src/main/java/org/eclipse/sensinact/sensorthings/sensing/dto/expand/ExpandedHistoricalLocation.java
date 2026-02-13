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
package org.eclipse.sensinact.sensorthings.sensing.dto.expand;

import java.time.Instant;
import java.util.List;

import org.eclipse.sensinact.sensorthings.sensing.dto.IdSelf;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ExpandedHistoricalLocation(String selfLink, Object id,
        @JsonFormat(shape = JsonFormat.Shape.STRING) Instant time,
        @JsonProperty("Locations@iot.navigationLink") String locationsLink,
        @JsonProperty("Thing@iot.navigationLink") String thingLink, RefId Thing,
        @JsonProperty("Locations") List<RefId> locations) implements IdSelf {
}

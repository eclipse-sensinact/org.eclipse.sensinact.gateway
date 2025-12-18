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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.Map;

import org.eclipse.sensinact.sensorthings.sensing.dto.NameDescription;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * record of sensor for creation/update
 */
public record ExpandedSensor(String selfLink, Object id, String name, String description, String encodingType,
        Object metadata, @JsonInclude(NON_NULL) Map<String, Object> properties,
        @JsonProperty("Datastreams@iot.navigationLink") String datastreamsLink) implements NameDescription {
}

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
*   Tim Ward - refactor as records
**********************************************************************/
package org.eclipse.sensinact.gateway.geojson;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;

public record Feature(String id, @JsonInclude Geometry geometry, @JsonInclude Map<String, Object> properties, List<Double> bbox,
        @JsonAnySetter @JsonAnyGetter Map<String,Object> foreignMembers) implements GeoJsonObject {

    public Feature {
        if(bbox != null) {
            bbox = List.copyOf(bbox);
        }
        if(properties != null) {
            properties = Map.copyOf(properties);
        }
        if(foreignMembers != null) {
            foreignMembers = Map.copyOf(foreignMembers);
        } else {
            foreignMembers = Map.of();
        }
    }

    @Override
    public GeoJsonType type() {
        return GeoJsonType.Feature;
    }

    @Override
    public boolean isEmpty() {
        return geometry == null || geometry.isEmpty();
    }
}

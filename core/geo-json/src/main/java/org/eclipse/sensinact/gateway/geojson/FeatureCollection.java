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

public record FeatureCollection(List<Feature> features, List<Double> bbox,
        @JsonAnySetter @JsonAnyGetter Map<String,Object> foreignMembers) implements GeoJsonObject {

    public FeatureCollection {
        if(features != null) {
            features = List.copyOf(features);
        } else {
            // GeoJSON specification 3.3 -
            // A FeatureCollection object has a member
            // with the name "features".  The value of "features" is a JSON array.
            // Each element of the array is a Feature object as defined above.  It
            // is possible for this array to be empty.
            features = List.of();
        }
        if(bbox != null) {
            bbox = List.copyOf(bbox);
        }
        if(foreignMembers != null) {
            foreignMembers = Map.copyOf(foreignMembers);
        } else {
            foreignMembers = Map.of();
        }
    }

    @Override
    public GeoJsonType type() {
        return GeoJsonType.FeatureCollection;
    }

    @Override
    public boolean isEmpty() {
        return features.isEmpty();
    }
}

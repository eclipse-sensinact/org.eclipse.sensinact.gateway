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

/**
 * A GeoJSON multi polygon object as defined in
 * <a href="https://tools.ietf.org/html/rfc7946#section-3.1">the GeoJSON
 * specification</a>
 */
public record MultiPolygon(List<List<List<Coordinates>>> coordinates, List<Double> bbox, @JsonAnySetter @JsonAnyGetter Map<String,Object> foreignMembers) implements Geometry {

    public MultiPolygon {
        if(coordinates != null) {
            coordinates = coordinates.stream()
                    .map(l -> l.stream().map(List::copyOf).toList())
                    .toList();
        } else {
            // GeoJSON specification 3.1 - GeoJSON processors MAY interpret Geometry objects with
            // empty "coordinates" arrays as null objects.
            coordinates = List.of();
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
        return GeoJsonType.MultiPolygon;
    }

    @Override
    public boolean isEmpty() {
        return coordinates.isEmpty();
    }
}

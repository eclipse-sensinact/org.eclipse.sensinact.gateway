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
 * A GeoJSON point object as defined in
 * <a href="https://tools.ietf.org/html/rfc7946#section-3.1">the GeoJSON
 * specification</a>
 */
public record Point(Coordinates coordinates, List<Double> bbox, @JsonAnySetter @JsonAnyGetter Map<String,Object> foreignMembers) implements Geometry {

    public Point(double longitude, double latitude) {
        this(new Coordinates(longitude, latitude), null, null);
    }

    public Point {
        if(coordinates == null) {
            coordinates = Coordinates.EMPTY;
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
        return GeoJsonType.Point;
    }

    @Override
    public boolean isEmpty() {
        return coordinates.isEmpty();
    }
}

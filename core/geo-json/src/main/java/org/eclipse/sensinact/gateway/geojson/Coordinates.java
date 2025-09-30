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

import org.eclipse.sensinact.gateway.geojson.internal.CoordinatesDeserializer;
import org.eclipse.sensinact.gateway.geojson.internal.CoordinatesSerializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * A GeoJSON coordinate. We do not support additional point data beyond the
 * three entries defined in
 * <a href="https://tools.ietf.org/html/rfc7946#section-3.1.1">the GeoJSON
 * specification</a>
 */
@JsonDeserialize(using = CoordinatesDeserializer.class)
@JsonSerialize(using = CoordinatesSerializer.class)
public record Coordinates(double longitude, double latitude, double elevation) {

    /**
     * A ready made marker for an empty point using NaN for all coordinate values.
     * This will (de)serialize to/from an empty array in GeoJSON as described in
     * Section 3.1 of the GeoJSON specifcation:
     * <p>
     * <i>GeoJSON processors MAY interpret Geometry objects with
     * empty "coordinates" arrays as null objects.</i>
     */
    public static final Coordinates EMPTY = new Coordinates();

    private Coordinates() {
        this(Double.NaN, Double.NaN, Double.NaN);
    }

    public Coordinates(double longitude, double latitude) {
        this(longitude, latitude, Double.NaN);
    }

    public Coordinates {
        if(!isEmpty(longitude, latitude, elevation)) {
            if(!Double.isFinite(latitude) || !Double.isFinite(longitude)) {
                throw new IllegalArgumentException("Latitude and Longitude must be finite values");
            }
        }
    }

    public boolean isEmpty() {
        return isEmpty(longitude, latitude, elevation);
    }

    private boolean isEmpty(double longitude, double latitude, double elevation) {
        return Double.isNaN(longitude) && Double.isNaN(latitude) && Double.isNaN(elevation);
    }

    public boolean hasElevation() {
        return Double.isFinite(elevation);
    }
}

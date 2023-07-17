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
package org.eclipse.sensinact.gateway.geojson;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The nine GeoJSON types as defined in
 * <a href="https://tools.ietf.org/html/rfc7946#section-1.4">the GeoJSON
 * specification</a>
 */
public enum GeoJsonType {

    Point, LineString, Polygon, MultiPoint, MultiLineString, MultiPolygon, GeometryCollection, Feature(false),
    FeatureCollection(false);

    private final boolean isGeometryType;

    private GeoJsonType() {
        this(true);
    }

    private GeoJsonType(boolean isGeometryType) {
        this.isGeometryType = isGeometryType;
    }

    public boolean isGeometryType() {
        return isGeometryType;
    }

    /**
     * Get the type value as defined by the GeoJSON specification
     *
     * @return The string representing the type for this enum
     */
    @JsonValue
    public String getType() {
        return name();
    }
}

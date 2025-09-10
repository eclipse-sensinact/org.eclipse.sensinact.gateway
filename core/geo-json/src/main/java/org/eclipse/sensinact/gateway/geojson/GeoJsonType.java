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

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The nine GeoJSON types as defined in
 * <a href="https://tools.ietf.org/html/rfc7946#section-1.4">the GeoJSON
 * specification</a>
 */
public enum GeoJsonType {

    Point {
        public Point cast(GeoJsonObject object) {
            return Point.class.cast(object);
        }
    }, LineString {
        public LineString cast(GeoJsonObject object) {
            return LineString.class.cast(object);
        }
    }, Polygon {
        public Polygon cast(GeoJsonObject object) {
            return Polygon.class.cast(object);
        }
    }, MultiPoint {
        public MultiPoint cast(GeoJsonObject object) {
            return MultiPoint.class.cast(object);
        }
    }, MultiLineString {
        public MultiLineString cast(GeoJsonObject object) {
            return MultiLineString.class.cast(object);
        }
    }, MultiPolygon {
        public MultiPolygon cast(GeoJsonObject object) {
            return MultiPolygon.class.cast(object);
        }
    }, GeometryCollection {
        public GeometryCollection cast(GeoJsonObject object) {
            return GeometryCollection.class.cast(object);
        }
    }, Feature(false) {
        public Feature cast(GeoJsonObject object) {
            return Feature.class.cast(object);
        }
    },
    FeatureCollection(false) {
        public FeatureCollection cast(GeoJsonObject object) {
            return FeatureCollection.class.cast(object);
        }
    };

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

    public abstract GeoJsonObject cast(GeoJsonObject object);

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

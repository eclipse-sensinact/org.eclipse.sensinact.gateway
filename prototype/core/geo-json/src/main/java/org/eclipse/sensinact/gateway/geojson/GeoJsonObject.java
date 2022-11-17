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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * A GeoJSON object as defined in
 * <a href="https://tools.ietf.org/html/rfc7946#section-3">the GeoJSON
 * specification</a>
 */
@JsonSubTypes({
        // The two "feature" types
        @Type(value = Feature.class, name = "Feature"),
        @Type(value = FeatureCollection.class, name = "FeatureCollection"),
        // The seven "geometry" types
        @Type(value = Point.class, name = "Point"), @Type(value = MultiPoint.class, name = "MultiPoint"),
        @Type(value = LineString.class, name = "LineString"),
        @Type(value = MultiLineString.class, name = "MultiLineString"), @Type(value = Polygon.class, name = "Polygon"),
        @Type(value = MultiPolygon.class, name = "MultiPolygon"),
        @Type(value = GeometryCollection.class, name = "GeometryCollection"), })
@JsonTypeInfo(use = Id.NAME, include = As.EXISTING_PROPERTY, property = "type")
@JsonInclude(Include.NON_NULL)
public abstract class GeoJsonObject {

    /**
     * This field is not deserialized, but populated by the constructor This field
     * also defines the type of the object, and is used to map into a Java type when
     * deserializing
     */
    @JsonIgnoreProperties(allowGetters = true)
    public final GeoJsonType type;

    protected GeoJsonObject(GeoJsonType type) {
        this.type = type;
    }

    @JsonAnySetter
    public Map<String, Object> foreignMembers = new HashMap<>();

    public List<Double> bbox;
}

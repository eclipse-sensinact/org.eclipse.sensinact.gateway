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

import org.eclipse.sensinact.gateway.geojson.internal.JacksonHelper;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.core.JsonProcessingException;

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
public sealed interface GeoJsonObject permits Geometry, Feature, FeatureCollection {

    /**
     * This field is not deserialized, but populated by the constructor This field
     * also defines the type of the object, and is used to map into a Java type when
     * deserializing
     */
    @JsonGetter
    public GeoJsonType type();

    /**
     * Additional extension properties for this object
     * @return A {@link Map} of extension properties. May be empty, but will never be <code>null</code>
     */
    public Map<String, Object> foreignMembers();

    /**
     * The GeoJSON bounding box.
     * @return A List of bounding box coordinates. May be null according to the specification.
     */
    public List<Double> bbox();

    /**
     * Tests whether this GeoJSON object is "empty". For example it may contain no
     * coordinates.
     * @return <code>true</code> if the object is empty
     */
    @JsonIgnore
    public boolean isEmpty();

    public default String toJsonString() {
        try {
            return JacksonHelper.MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("This object could not be serialized to JSON", e);
        }
    }

    public static GeoJsonObject fromJsonString(String s) {
        try {
            return JacksonHelper.MAPPER.readValue(s, GeoJsonObject.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("This object could not be deserialized from JSON", e);
        }
    }
}

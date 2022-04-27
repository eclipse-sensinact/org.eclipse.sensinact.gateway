/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.util.location.geojson;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoJsonFeature {

	@JsonProperty("type")
	private String type;
	
	@JsonProperty("properties")
	private Map<String,?> properties;
	
	@JsonDeserialize(using = GeometryDeserializer.class)
	@JsonProperty("geometry")
	private GeoJsonGeometry geometry;
	
	/**
	 * Constructor
	 */
	public GeoJsonFeature() {}

	/**
	 * Constructor
	 * 
	 * @param properties the properties to set
	 * @param geometry the geometry to set
	 */
	public GeoJsonFeature(String type, Map<String,?> properties, GeoJsonGeometry geometry) {
		this.type = type;
		this.properties = properties;
		this.geometry = geometry;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the properties
	 */
	public Map<String,?> getProperties() {
		return properties;
	}

	/**
	 * @param properties the properties to set
	 */
	public void setProperties(Map<String,?> properties) {
		this.properties = properties;
	}

	/**
	 * @return the geometry
	 */
	public GeoJsonGeometry getGeometry() {
		return geometry;
	}

	/**
	 * @param geometry the geometry to set
	 */
	public void setGeometry(GeoJsonGeometry geometry) {
		this.geometry = geometry;
	}
	
	/**
	 * @return the Geometry of the wrapped GeoJsonGeometry 
	 */
	@JsonIgnore
	public Geometry getGeometryType() {
		return this.geometry.getType();
	}
}

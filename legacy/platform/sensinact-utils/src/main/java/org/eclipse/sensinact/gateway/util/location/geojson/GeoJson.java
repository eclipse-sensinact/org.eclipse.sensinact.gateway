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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeoJson {
	
	@JsonProperty("type")
	private String type;
	
	@JsonProperty("features")
	private List<GeoJsonFeature> features;
	
	/**
	 * Constructor
	 */
	public GeoJson() {}

	/**
	 * Constructor
	 * 
	 * @param type the type to set
	 * @param features the features to set
	 */
	public GeoJson(String type, List<GeoJsonFeature> features) {
		this.type = type;
		this.features = features;
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
	 * @return the features
	 */
	public List<GeoJsonFeature> getFeatures() {
		return features;
	}

	/**
	 * @param features the features to set
	 */
	public void setFeatures(List<GeoJsonFeature> features) {
		this.features = features;
	}
	
}

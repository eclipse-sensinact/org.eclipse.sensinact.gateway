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

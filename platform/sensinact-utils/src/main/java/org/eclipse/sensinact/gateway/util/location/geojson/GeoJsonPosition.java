package org.eclipse.sensinact.gateway.util.location.geojson;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = Shape.ARRAY)
public class GeoJsonPosition {	

	@JsonProperty(index = 0)
	private double longitude;
	
	@JsonProperty(index = 1)
	private double latitude;
	
	/**
	 * Constructor
	 */
	public GeoJsonPosition() {}

	/**
	 * Constructor
	 * 
	 * @param latitude the latitude to set
	 * @param longitude the longitude to set
	 */
	public GeoJsonPosition(double longitude,double latitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude the latitude to set
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude the longitude to set
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	@Override
	public String toString() {
		return String.format("[%s, %s]",longitude,latitude);
	}
}

package org.eclipse.sensinact.gateway.util.location.geojson;

import java.util.List;

public class GeoJsonMultiLineStringGeometry extends GeoJsonGeometry{
	
	private List<List<GeoJsonPosition>> coordinates;
	
	/**
	 * Constructor
	 */
	public GeoJsonMultiLineStringGeometry() {}

	/**
	 * Constructor
	 * 
	 * @param coordinates the coordinates to set
	 */
	public GeoJsonMultiLineStringGeometry(List<List<GeoJsonPosition>> coordinates) {
		this.coordinates = coordinates;
	}
	
	@Override
	public Geometry getType() {
		return Geometry.MultiLineString;
	}

	/**
	 * @return the coordinates
	 */
	public List<List<GeoJsonPosition>> getCoordinates() {
		return coordinates;
	}

	/**
	 * @param coordinates the coordinates to set
	 */
	public void setCoordinates(List<List<GeoJsonPosition>> coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public String toString() {
		return this.coordinates.toString();
	}
	
}

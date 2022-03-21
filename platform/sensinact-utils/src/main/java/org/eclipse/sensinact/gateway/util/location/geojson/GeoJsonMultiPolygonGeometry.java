package org.eclipse.sensinact.gateway.util.location.geojson;

import java.util.List;

public class GeoJsonMultiPolygonGeometry extends GeoJsonGeometry{
	
	private List<List<List<GeoJsonPosition>>> coordinates;
	
	/**
	 * Constructor
	 */
	public GeoJsonMultiPolygonGeometry() {}

	/**
	 * Constructor
	 * 
	 * @param coordinates the coordinates to set
	 */
	public GeoJsonMultiPolygonGeometry(List<List<List<GeoJsonPosition>>> coordinates) {
		this.coordinates = coordinates;
	}
	
	@Override
	public Geometry getType() {
		return Geometry.MultiPolygon;
	}

	/**
	 * @return the coordinates
	 */
	public List<List<List<GeoJsonPosition>>> getCoordinates() {
		return coordinates;
	}

	/**
	 * @param coordinates the coordinates to set
	 */
	public void setCoordinates(List<List<List<GeoJsonPosition>>> coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public String toString() {
		return this.coordinates.toString();
	}
}

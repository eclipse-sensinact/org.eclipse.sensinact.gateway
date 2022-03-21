package org.eclipse.sensinact.gateway.util.location.geojson;

import java.util.List;

public class GeoJsonPolygonGeometry extends GeoJsonGeometry{
	
	private List<List<GeoJsonPosition>> coordinates;
	
	/**
	 * Constructor
	 */
	public GeoJsonPolygonGeometry() {}

	/**
	 * Constructor
	 * 
	 * @param coordinates the coordinates to set
	 */
	public GeoJsonPolygonGeometry(List<List<GeoJsonPosition>> coordinates) {
		this.coordinates = coordinates;
	}
	
	@Override
	public Geometry getType() {
		return Geometry.Polygon;
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

package org.eclipse.sensinact.gateway.util.location.geojson;


public class GeoJsonPointGeometry  extends GeoJsonGeometry{
	
	private GeoJsonPosition coordinates;

	/**
	 * Constructor
	 */
	public GeoJsonPointGeometry() {}

	/**
	 * Constructor
	 * 
	 * @param coordinates the coordinates to set
	 */
	public GeoJsonPointGeometry(GeoJsonPosition coordinates) {
		this.coordinates = coordinates;
	}
	
	@Override
	public Geometry getType() {
		return Geometry.Point;
	}
	
	/**
	 * @return the coordinates
	 */
	public GeoJsonPosition getCoordinates() {
		return coordinates;
	}

	/**
	 * @param coordinates the coordinates to set
	 */
	public void setCoordinates(GeoJsonPosition coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public String toString() {
		return this.coordinates.toString();
	}
	
}

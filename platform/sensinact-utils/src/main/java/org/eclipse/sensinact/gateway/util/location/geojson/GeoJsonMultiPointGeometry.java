package org.eclipse.sensinact.gateway.util.location.geojson;

import java.util.List;

public class GeoJsonMultiPointGeometry  extends GeoJsonGeometry{
	
	private List<GeoJsonPosition> coordinates;

	/**
	 * Constructor
	 */
	public GeoJsonMultiPointGeometry() {}

	/**
	 * Constructor
	 * 
	 * @param coordinates the coordinates to set
	 */
	public GeoJsonMultiPointGeometry(List<GeoJsonPosition> coordinates) {
		this.coordinates = coordinates;
	}
	
	@Override
	public Geometry getType() {
		return Geometry.MultiPoint;
	}
	
	/**
	 * @return the coordinates
	 */
	public List<GeoJsonPosition> getCoordinates() {
		return coordinates;
	}

	/**
	 * @param coordinates the coordinates to set
	 */
	public void setCoordinates(List<GeoJsonPosition> coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public String toString() {
		return this.coordinates.toString();
	}
}

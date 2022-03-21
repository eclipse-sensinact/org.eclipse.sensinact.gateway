package org.eclipse.sensinact.gateway.util.location.geojson;

import java.util.List;

public class GeoJsonLineStringGeometry  extends GeoJsonMultiPointGeometry{
	
	/**
	 * Constructor
	 */
	public GeoJsonLineStringGeometry() {}

	/**
	 * Constructor
	 * 
	 * @param coordinates the coordinates to set
	 */
	public GeoJsonLineStringGeometry(List<GeoJsonPosition> coordinates) {
		super(coordinates);
	}
	
	@Override
	public Geometry getType() {
		return Geometry.LineString;
	}
	
	@Override
	public String toString() {
		return super.getCoordinates().toString();
	}
}

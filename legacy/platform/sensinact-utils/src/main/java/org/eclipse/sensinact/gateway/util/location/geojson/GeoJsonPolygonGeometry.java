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

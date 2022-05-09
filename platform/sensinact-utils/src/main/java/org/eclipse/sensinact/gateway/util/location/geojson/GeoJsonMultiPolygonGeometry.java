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

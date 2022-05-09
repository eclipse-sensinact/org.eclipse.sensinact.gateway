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

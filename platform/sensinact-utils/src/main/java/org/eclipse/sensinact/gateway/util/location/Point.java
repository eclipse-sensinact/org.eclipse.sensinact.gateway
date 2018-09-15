/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.util.location;

/**
 * @author christophe
 *
 */
public class Point {

	public final double latitude;
	public final double longitude;
	public final double altitude;

	/**
	 * @param latitude
	 * @param longitude
	 */
	public Point(double latitude, double longitude) {
		this(latitude,longitude, 0);
	}

	/**
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 */
	public Point(double latitude, double longitude, double altitude) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}
	
    /** 
     * @inheritDoc
     * 
     * @see java.lang.Object#toString()
     */
	@Override
    public String toString() {
    	StringBuilder builder = new StringBuilder();
    	builder.append("[");
    	builder.append(this.longitude);
    	builder.append(",");
    	builder.append(this.latitude);
    	builder.append("]");
    	return builder.toString();
    }

    public String toGeoJSON() {
   	  StringBuilder builder = new StringBuilder();
   	  builder.append("{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"Point\",\"coordinates\":[");
   	  builder.append(longitude);
   	  builder.append(",");
   	  builder.append(latitude); 
   	  builder.append("]");
   	  builder.append("]}}");
   	  return builder.toString();
    }
}

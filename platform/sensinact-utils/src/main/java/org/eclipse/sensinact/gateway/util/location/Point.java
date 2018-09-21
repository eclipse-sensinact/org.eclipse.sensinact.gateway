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
 * Location point
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Point {

	public static Point fromString(String s) {
		String[] coordinates = s.split(":");
		try {
			Point p  = new Point(Double.parseDouble(coordinates[0]),Double.parseDouble(coordinates[1]));
			return p;
		} catch(Exception e) {
			//do nothing
		}
		return null;
	}
	
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
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if(Point.class.isAssignableFrom(obj.getClass())){
			Point p = (Point) obj;
			return p.latitude == latitude && p.longitude==longitude;
			//return Math.abs(p.latitude-latitude)<1e-7 && Math.abs(p.longitude-longitude)<1e-7;
		}
		if(String.class == obj.getClass()) {
			return equals(fromString((String) obj));
		}
		return false;
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

    /**
     * Returns the GeoJSON formated string description of this Point
     * 
     * @return this Point description GeoJSON formated
     */
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

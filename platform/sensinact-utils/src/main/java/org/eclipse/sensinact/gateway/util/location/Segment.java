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
 * Line segment between two points
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Segment {
	
	private double lat1;
	private double radLat1;
    private double lng1;
	private double radLng1;
    private double lat2;
	private double radLat2;
    private double lng2;
	private double radLng2;
    private double fwAz;
	private double radFwAz;
    private double revAz;
	private double radRevAz;
    private double dist;

    public Segment(double lat1, double lng1, double lat2, double lng2, 
    		double fwAz, double revAz, double dist) {
        this.setLat1(lat1);
        this.setLng1(lng1);
        this.setLat2(lat2);
        this.setLng2(lng2);
        this.setFwAz(fwAz);
        this.setRevAz(revAz);
        this.setDist(dist);
    }

    public double getLat1() {
    	return this.lat1;
    }

    public double getRadLat1() {
    	return this.radLat1;
    }
    
    void setLat1(double lat1) {
    	this.lat1 = lat1;
    	this.radLat1 = lat1 * Constants.DEGREES_TO_RADIUS_COEF;
    }

    void setRadLat1(double radLat1) {
    	this.lat1 = radLat1 * Constants.RADIUS_TO_DEGREES_COEF;
    	this.radLat1 = radLat1;
    }
    
    public double getLng1() {
    	return this.lng1;
    }

    public double getRadLng1() {
    	return this.radLng1;
    }
    
    void setLng1(double lng1) {
        this.lng1 = lng1;
    	this.radLng1 = lng1 * Constants.DEGREES_TO_RADIUS_COEF;
    }

    void setRadLng1(double radLng1) {
    	this.radLng1 = radLng1;
    	this.lng1 = radLng1 * Constants.RADIUS_TO_DEGREES_COEF;
    }
    
    public double getLat2() {
    	return this.lat2;
    }

    public double getRadLat2() {
    	return this.radLat2;
    }
    
    void setLat2(double lat2) {
    	this.lat2 = lat2;
    	this.radLat2 = lat2 * Constants.DEGREES_TO_RADIUS_COEF;
    }

    void setRadLat2(double radLat2) {
    	this.lat2 = radLat2 * Constants.RADIUS_TO_DEGREES_COEF;
    	this.radLat2 = radLat2;
    }

    public double getLng2() {
    	return this.lng2;
    }

    public double getRadLng2() {
    	return this.radLng2;
    }
    
    void setLng2(double lng2) {
        this.lng2 = lng2;
    	this.radLng2 = lng2 * Constants.DEGREES_TO_RADIUS_COEF;
    }

    void setRadLng2(double radLng2) {
    	this.radLng2 = radLng2;
    	this.lng2 = radLng2 * Constants.RADIUS_TO_DEGREES_COEF;
    }
    
    public double getForwardAzimuth() {
    	return this.fwAz;
    }

    public double getRadForwardAzimuth() {
    	return this.radFwAz;
    }

    void setFwAz(double fwAz) {
    	 if (Math.abs(fwAz)> 360) {
    	   fwAz-=Math.floor(fwAz/360)*360; 
       }
       this.fwAz = fwAz;
       this.radFwAz = fwAz * Constants.DEGREES_TO_RADIUS_COEF;
    }

    void setRadFwAz(double radFwAz) {
   	 	if (Math.abs(radFwAz) > Constants.DOUBLE_PI) {
   	 		radFwAz-=Math.floor(radFwAz/Constants.DOUBLE_PI)*Constants.DOUBLE_PI; 
   	 	}
    	this.radFwAz = radFwAz; 
    	this.fwAz = radFwAz * Constants.RADIUS_TO_DEGREES_COEF;
    }
    
    public double getBackAzimuth() {
    	return this.revAz;
    }

    public double geRadBackAzimuth() {
    	return this.radRevAz;
    }

    void setRevAz(double revAz) {
    	 if (Math.abs(revAz)> 360) {
    		 revAz-=Math.floor(revAz/360)*360; 
       }
       this.revAz = revAz;
       this.radRevAz = revAz * Constants.DEGREES_TO_RADIUS_COEF;
    }

    void setRadRevAz(double radRevAz) {
   	 	if (Math.abs(radRevAz) > Constants.DOUBLE_PI) {
   	 		radRevAz-=Math.floor(radRevAz/Constants.DOUBLE_PI)*Constants.DOUBLE_PI; 
   	 	}
    	this.radRevAz = radRevAz; 
    	this.revAz = revAz * Constants.RADIUS_TO_DEGREES_COEF;
    }
    
    void setDist(double dist) {
    	this.dist = dist;
    }

    /**
     * The double distance between the two points of this Segment
     * 
     * @return this Segment size
     */
    public double getDistance() {
    	return this.dist;
    }
    
    /**
     * @inheritDoc
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
    	StringBuilder builder = new StringBuilder();
    	builder.append("--------------------------\n");
    	builder.append(this.lat1!=0 && this.lng1!=0 ?"point 1:["+this.lng1 +","+this.lat1 +"]\n":"");
    	builder.append(this.lat2!=0 && this.lng2!=0 ?"point 2:["+this.lng2 +","+this.lat2 +"]\n":"");
    	builder.append(this.dist!=0?"distance :"+dist+"\n":"\n");
    	builder.append("--------------------------\n");
    	return builder.toString();
    }

    /**
     * Returns the GeoJSON formated string description of this Segment
     * 
     * @return this Segment description GeoJSON formated
     */
    public String toGeoJSON() {
   	  StringBuilder builder = new StringBuilder();
   	  builder.append("{\"type\":\"Feature\",\"properties\":{},\"geometry\":{\"type\":\"LineString\",\"coordinates\":[[");
   	  builder.append(lng1);
   	  builder.append(",");
   	  builder.append(lat1); 
   	  builder.append("],[");
   	  builder.append(lng2);
   	  builder.append(",");
   	  builder.append(lat2);
   	  builder.append("]]}}");
   	  return builder.toString();
    }
}
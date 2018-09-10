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
package org.eclipse.sensinact.gateway.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.sensinact.gateway.util.location.Constants;
import org.eclipse.sensinact.gateway.util.location.Point;
import org.eclipse.sensinact.gateway.util.location.Segment;

/**
 *
 */
public class LocationUtils {

    private final static double asin(double x) {
   	 return Math.asin(Math.max(-1,Math.min(x,1)));
    }

    private final static double acos(double x){
   	 return Math.acos(Math.max(-1,Math.min(x,1)));
    }
    
    private final static double mod(double x, double y) {
   	 double m = x;
   	 if(Math.abs(m) > y) {
   		 m-= Math.floor(m/y)*y;
   	 }
   	 return m;
    }
    
    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double a = Constants.EARTH_SPHERICAL_MODEL_RADIUS * 1000D;
        double dlong = ((lng2 - lng1) * Constants.DEGREES_TO_RADIUS_COEF) / 2;
        double dlat = ((lat2 - lat1) * Constants.DEGREES_TO_RADIUS_COEF) / 2;
        double lat1_rad = lat1 * Constants.DEGREES_TO_RADIUS_COEF;
        double lat2_rad = lat2 * Constants.DEGREES_TO_RADIUS_COEF;
        double c = Math.pow(Math.sin(dlat), 2D) + Math.pow(Math.sin(dlong), 2D) * Math.cos(lat1_rad) * Math.cos(lat2_rad);
        double d = 2 * Math.atan2( Math.sqrt(c),Math.sqrt(1 - c));
        return (a * d);
    }

    public static Segment getSphericalEarthModelCoordinates(double lat, double lng, int bearing, double distance) {
        double brg = bearing * Constants.DEGREES_TO_RADIUS_COEF;
        double lat1 = lat * Constants.DEGREES_TO_RADIUS_COEF;
        double lng1 = lng * Constants.DEGREES_TO_RADIUS_COEF;

        double a = Constants.EARTH_SPHERICAL_MODEL_RADIUS * 1000D;
        double s1 = distance / a;

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(s1) + Math.cos(lat1) * Math.sin(s1) * Math.cos(brg));
        double lng2 = lng1 + Math.atan2(Math.sin(brg) * Math.sin(s1) * Math.cos(lat1), Math.cos(s1) - Math.sin(lat1) * Math.sin(lat2));

        double dLon = lng1 - lng2;
        double y = Math.sin(dLon) * Math.cos(lat1);
        double x = Math.cos(lat2) * Math.sin(lat1) - Math.sin(lat2) * Math.cos(lat1) * Math.cos(dLon);
        double d = Math.atan2(y, x);
        double finalBrg = d + Math.PI;
        double backBrg = d + Constants.DOUBLE_PI;

        lat2 = lat2 * Constants.RADIUS_TO_DEGREES_COEF;
        lng2 = lng2 * Constants.RADIUS_TO_DEGREES_COEF;
        finalBrg = finalBrg * Constants.RADIUS_TO_DEGREES_COEF;
        backBrg = backBrg * Constants.RADIUS_TO_DEGREES_COEF;
        return new Segment(lat, lng, lat2, lng2, finalBrg, backBrg , distance);
    }  

    public static Segment getElipsoidEarthModelDistance(double latdep, double lngdep, double latarr, double lngarr) {
//   	The WGS 84 datum surface is an oblate spheroid (ellipsoid)
//   	with major (equatorial) radius a = 6378137 m at the equator 
//   	and flattening f = 1/298.257223563.[6] The polar semi-minor 
//   	axis b then equals a × (1 − f) = 6356752.3142 m
//
//		Given the distance s in meters, the semi-major axis 'a' in 
//		meters, the semi-minor axis 'b' in meters and the polar flattening 'flat'.
//		Calculate the distance and bearing using Vincenty's formula. Shortened 
//		variable names are used.
        double a = Constants.EARTH_ELIPSOID_MODEL_RADIUS * 1000D;
        double f = 1 / Constants.EARTH_POLAR_FLATTENING;
        double b = a * (1 - f);

        double lat1 = latdep * Constants.DEGREES_TO_RADIUS_COEF;
        double lng1 = lngdep * Constants.DEGREES_TO_RADIUS_COEF;
        double lat2 = latarr * Constants.DEGREES_TO_RADIUS_COEF;
        double lng2 = lngarr * Constants.DEGREES_TO_RADIUS_COEF;
        
    	double L = lng2 - lng1;
    	double tanU1 = (1-f) * Math.tan(lat1);
    	double cosU1 = 1 / Math.sqrt((1 + tanU1*tanU1));
    	double sinU1 = tanU1 * cosU1;
    	double tanU2 = (1-f) * Math.tan(lat2);
    	double cosU2 = 1 / Math.sqrt((1 + tanU2*tanU2));
    	double sinU2 = tanU2 * cosU2;

    	double lambda = L, lambdaB, coslambda, sinlambda, cos2sigmaM, sigma, cossigma, cosSqalpha, sinsigma ;
    	int iterationLimit = 100;
    	
    	do {
    	    sinlambda = Math.sin(lambda);
    	    coslambda = Math.cos(lambda);
    	    double sinSqsigma = (cosU2*sinlambda) * (cosU2*sinlambda) + (cosU1*sinU2-sinU1*cosU2*coslambda) * (cosU1*sinU2-sinU1*cosU2*coslambda);
    	    sinsigma = Math.sqrt(sinSqsigma);
    	    if (sinsigma==0) 
    	    	return new Segment(latdep, lngdep, latarr, lngarr, 0, 0, 0); // co-incident points
    	    cossigma = sinU1*sinU2 + cosU1*cosU2*coslambda;
    	    sigma = Math.atan2(sinsigma, cossigma);
    	    double sinalpha = cosU1 * cosU2 * sinlambda / sinsigma;
    	    cosSqalpha = 1 - sinalpha*sinalpha;
    	    cos2sigmaM = cossigma - 2*sinU1*sinU2/cosSqalpha;
    	    if (Double.isNaN(cos2sigmaM)) 
    	    	cos2sigmaM = 0;  // equatorial line: cosSqalpha=0 (§6)
    	    double C = f/16*cosSqalpha*(4+f*(4-3*cosSqalpha));
    	    lambdaB = lambda;
    	    lambda = L + (1-C) * f * sinalpha * (sigma + C*sinsigma*(cos2sigmaM+C*cossigma*(-1+2*cos2sigmaM*cos2sigmaM)));
    	} while (Math.abs(lambda-lambdaB) > 1e-12 && --iterationLimit>0);
    	if (iterationLimit==0) {
    		System.out.println("Formula failed to converge");
    		return null;
    	}
    	double uSq = cosSqalpha * (a*a - b*b) / (b*b);
    	double A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
    	double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
    	double deltasigma = B*sinsigma*(cos2sigmaM+B/4*(cossigma*(-1+2*cos2sigmaM*cos2sigmaM)-
    	    B/6*cos2sigmaM*(-3+4*sinsigma*sinsigma)*(-3+4*cos2sigmaM*cos2sigmaM)));

    	double s = b*A*(sigma-deltasigma);

    	double fwdAz = Math.atan2(cosU2*sinlambda,cosU1*sinU2-sinU1*cosU2*coslambda);
    	double revAz = Math.atan2(cosU1*sinlambda,-sinU1*cosU2+cosU1*sinU2*coslambda);
    	
    	fwdAz =  fwdAz * Constants.RADIUS_TO_DEGREES_COEF;
    	revAz = revAz * Constants.RADIUS_TO_DEGREES_COEF;        
    	return new Segment(latdep, lngdep, latarr, lngarr, fwdAz, revAz, s);
   }
    
     public static Segment getElipsoidEarthModelCoordinates(double lat, double lng, double bearing, double distance) {
        double brg = bearing * Constants.DEGREES_TO_RADIUS_COEF;
        double lat1 = lat * Constants.DEGREES_TO_RADIUS_COEF;
        double lng1 = lng * Constants.DEGREES_TO_RADIUS_COEF;

        double a = Constants.EARTH_ELIPSOID_MODEL_RADIUS * 1000D;
        double f = 1 / Constants.EARTH_POLAR_FLATTENING;
        double b = a * (1 - f);

        double sb = Math.sin(brg);
        double cb = Math.cos(brg);
        double tu1 = (1 - f) * Math.tan(lat1);
        double cu1 = 1 / Math.sqrt((1 + tu1 * tu1));
        double su1 = tu1 * cu1;
        double s2 = Math.atan2(tu1, cb);
        double sa = cu1 * sb;
        double csa = 1 - sa * sa;
        double us = csa * (a * a - b * b) / (b * b);
        double A = 1 + us / 16384 * (4096 + us * (-768 + us * (320 - 175 * us)));
        double B = us / 1024 * (256 + us * (-128 + us * (74 - 47 * us)));
        double s1 = distance / (b * A);

        double s1p = Constants.DOUBLE_PI;
        double cs1m = 0;
        double ss1 = 0;
        double cs1 = 0;
        double ds1 = 0;
        //Loop through the following while condition is true.
        while (Math.abs(s1 - s1p) > 1e-12) {
            cs1m = Math.cos(2 * s2 + s1);
            ss1 = Math.sin(s1);
            cs1 = Math.cos(s1);
            ds1 = B * ss1 * (cs1m + B / 4 * (cs1 * (-1 + 2 * cs1m * cs1m) - B / 6 * cs1m * (-3 + 4 * ss1 * ss1) * (-3 + 4 * cs1m * cs1m)));
            s1p = s1;
            s1 = distance / (b * A) + ds1;
        }
        //Continue calculation after the loop.
        double t = su1 * ss1 - cu1 * cs1 * cb;
        double lat2 = Math.atan2(su1 * cs1 + cu1 * ss1 * cb, (1 - f) * Math.sqrt(sa * sa + t * t));
        double l2 = Math.atan2(ss1 * sb, cu1 * cs1 - su1 * ss1 * cb);
        double c = f / 16 * csa * (4 + f * (4 - 3 * csa));
        double l = l2 - (1 - c) * f * sa * (s1 + c * ss1 * (cs1m + c * cs1 * (-1 + 2 * cs1m * cs1m)));
        double d = Math.atan2(sa, -t);
        double finalBrg = d + Constants.DOUBLE_PI;
        double backBrg = d + Math.PI;
        double lng2 = lng1 + l;

        lat2 = lat2 * Constants.RADIUS_TO_DEGREES_COEF;
        lng2 = lng2 * Constants.RADIUS_TO_DEGREES_COEF;
        finalBrg = finalBrg * Constants.RADIUS_TO_DEGREES_COEF;
        backBrg = backBrg * Constants.RADIUS_TO_DEGREES_COEF;
        return new Segment(lat, lng, lat2, lng2, finalBrg, backBrg, distance );
    }
     
     public static final Segment getSegmentIntersection(Segment segment1, Segment segment2) {
    	 Segment crossSegment = getElipsoidEarthModelDistance(segment1.getLat1(),segment1.getLng1(), 
    			 segment2.getLat1(), segment2.getLng1());

         double a = Constants.EARTH_ELIPSOID_MODEL_RADIUS * 1000D;
    	 double crs12 = crossSegment.getRadForwardAzimuth();
    	 double crs21 = (crossSegment.getRadForwardAzimuth() < Math.PI)?crossSegment.getRadForwardAzimuth()+Math.PI
    		:(crossSegment.getRadForwardAzimuth() > Math.PI)?crossSegment.getRadForwardAzimuth()-Math.PI:0;
    	 double crs13 = segment1.getRadForwardAzimuth();
    	 double crs23 = segment2.getRadForwardAzimuth();

    	 double ang1 = mod(crs13-crs12+Math.PI,Constants.DOUBLE_PI);
    	 ang1-=Math.PI;
    	 double ang2= mod(crs21-crs23+Math.PI,Constants.DOUBLE_PI);
    	 ang2-=Math.PI;

		 if(Math.sin(ang1)==0 && Math.sin(ang2)==0) {
			 System.out.println("infinity of intersections");
			 return null;
		 } 
    	 else if( Math.sin(ang1)*Math.sin(ang2)<0) {
			 System.out.println("intersection ambiguous");
			 return null;
		 }
		 ang1=Math.abs(ang1);
		 ang2=Math.abs(ang2);
		 
		 double ang3=acos(-1*Math.cos(ang1)*Math.cos(ang2)+Math.sin(ang1)*Math.sin(ang2)*Math.cos(crossSegment.getDistance()/a)); 
		 double dst13=a*Math.atan2(Math.sin(crossSegment.getDistance()/a)*Math.sin(ang1)*Math.sin(ang2),Math.cos(ang2)+Math.cos(ang1)*Math.cos(ang3));
		 Segment intersection = getElipsoidEarthModelCoordinates(segment1.getLat1(), segment1.getLng1(), segment1.getForwardAzimuth(), dst13);
		 return intersection;
     }

     public static String toEPAF(double coordinate) { 
    	return  toEPAF((int)(coordinate*1e5));
     }

     private static String toEPAF(int coordinate) {     
    	 int formated = coordinate;
    	 formated<<=1;
    	 if(coordinate < 0) {
    		 formated=~formated;
    	 }
	     StringBuilder builder = new StringBuilder();
    	 do{	
    		 int car = 0;
    	 	 car = formated & 0x1F;
    		 formated>>=5;    	 
    	 	 if(formated > 0) {
    	 		 car|=0x20;
    	 	 }
    	 	 car+=63;
    	 	 builder.append(Character.toChars(car));
    	 } while(formated>0);    	 
    	 return builder.toString();
     }
     
     public static String toEPAF(Point[] points) {
    	 int lastLat=0;
    	 int lastLng=0;
	     StringBuilder builder = new StringBuilder();	     
    	 for(int index = 0;index <points.length;index++) {
    		 int lat=(int)(points[index].latitude*1e5);
    		 int lng=(int)(points[index].longitude*1e5);
    		 builder.append(toEPAF(lat-lastLat));
    		 builder.append(toEPAF(lng-lastLng));
    		 lastLat=lat;
    		 lastLng=lng;
    	 }
    	 return builder.toString();
     }
     
     public static List<Double> fromEPAF(String encoded) {
    	 int pos = 0;
    	 int last = 0;
    	 char[] cars = encoded.toCharArray();
    	 List<Double> coordinates = new ArrayList<Double>();
    	 while(pos < cars.length) {
    		 cars[pos]-=63;
    		 if(!((cars[pos] & 0x20) == 0x20)) {
	    		 int coordinate = 0;  
	    		 int i = pos;
	    		 do{
	    			 coordinate|= ((cars[i] & 0x1F) << ((i-last)*5));
	    			 i--;
	    		 }while(i>=last);	    		 
	    		 if((coordinate & 0x01) == 0x01) {
				    coordinate=~coordinate;
				 }				 
	    		 coordinate >>=1;
	    		 double val = ((double)coordinate)/1e5d;
	    		 if(coordinates.size() >= 2) {
	    			 val = coordinates.get(coordinates.size()-2)+val;
	    		 }
				 coordinates.add(val); 
	    		 last = pos+1;
		     }
    		 pos++;
    	 }
    	 return coordinates;
     }

     public static List<Point> epafToPoints(String encoded) {
    	 List<Point> points = new ArrayList<Point>();
    	 List<Double> coordinates = fromEPAF(encoded);
    	 if(coordinates.size()%2!=0) {
    		 return points;
    	 }
    	 for(int index=0;index<coordinates.size();index+=2) {
    		 points.add(new Point(coordinates.get(index),coordinates.get(index+1)));
    	 }
    	 return points;
     }
     
}

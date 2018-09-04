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
package org.eclipse.sensinact.gateway.nthbnd.filter.geojson.internal;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.Filtering;
import org.eclipse.sensinact.gateway.core.LocationResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.util.json.JSONObjectStatement;
import org.eclipse.sensinact.gateway.util.json.JSONTokenerStatement;
import org.eclipse.sensinact.gateway.util.json.JSONValidator;
import org.eclipse.sensinact.gateway.util.json.JSONValidator.JSONToken;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * {@link Filtering} implementation allowing to apply a location discrimination
 * to the result object to be filtered
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
//thanks to : http://www.geomidpoint.com/destination/calculation.html
public class GeoJSONFiltering implements Filtering {
    //********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//

    public static class Segment {
    	private double lat1;
        private double lng1;
        private double lat2;
        private double lng2;
        private double fwAz;
        private double revAz;
        private double dist;

        Segment(double lat1, double lng1, double lat2, double lng2, double fwAz, double revAz, double dist) {
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
        
        void setLat1(double lat1) {
        	this.lat1 = lat1;
        }

        public double getLng1() {
        	return this.lng1;
        }
        
        void setLng1(double lng1) {
        	if(lng1 == 0) {
        		this.lng1 = 0;        		
        	} else if (lng1 < -180) {
                 this.lng1 = lng1 + 360;
             } else if (lng1 > 180) {
                 this.lng1 = lng1 - 360;
             } else {
                 this.lng1 = lng1;
             }
        }
        
        void setLat2(double lat2) {
        	this.lat2 = lat2;
        }

        public double getLat2() {
        	return this.lat2;
        }
        
        void setLng2(double lng2) {
        	if(lng2 == 0) {
        		this.lng2 = 0;        		
        	} else if (lng2 < -180) {
                 this.lng2 = lng2 + 360;
             } else if (lng2 > 180) {
                 this.lng2 = lng2 - 360;
             } else {
                 this.lng2 = lng2;
             }
        }

        public double getLng2() {
        	return this.lng2;
        }
        
        void setFwAz(double fwAz) {
            if (fwAz < 0) {
                this.fwAz = fwAz + 360;
            } else if (fwAz > 360) {
                this.fwAz = fwAz - 360;
            } else {
                this.fwAz = fwAz;
            }
        }

        public double getForwardAzimuth() {
        	return this.fwAz;
        }
        
        void setRevAz(double revAz) {
            if (revAz < 0) {
                this.revAz = revAz + 360;
            } else if (revAz > 360) {
                this.revAz = revAz - 360;
            } else {
                this.revAz = revAz;
            }
        }

        public double getBackAzimuth() {
        	return this.revAz;
        }
        
        void setDist(double dist) {
        	this.dist = dist;
        }

        public double getDistance() {
        	return this.dist;
        }
    }
    
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//

    private static final double EARTH_SPHERICAL_MODEL_RADIUS = 6372.7976D;
    private static final double EARTH_ELIPSOID_MODEL_RADIUS = 6378.1370D;
    private static final double EARTH_POLAR_FLATTENING = 298.257223563D;
    private static final double DOUBLE_PI = Math.PI * 2D;
    private static final double DEGREES_TO_RADIUS_COEF = Math.PI / 180D;
    private static final double RADIUS_TO_DEGREES_COEF = 180D / Math.PI;

    private static final JSONObjectStatement STATEMENT = new JSONObjectStatement(new JSONTokenerStatement("{" + " \"type\": \"Feature\"," + " \"properties\": {" + "	    \"name\": $(name)" + "  }," + "  \"geometry\": {" + "     \"type\": \"Point\"," + "     \"coordinates\": [ $(longitude),$(latitude)] " + "  }" + "}"));

    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double a = EARTH_ELIPSOID_MODEL_RADIUS * 1000D;
        double dlong = ((lng2 - lng1) * DEGREES_TO_RADIUS_COEF) / 2;
        double dlat = ((lat2 - lat1) * DEGREES_TO_RADIUS_COEF) / 2;
        double lat1_rad = lat1 * DEGREES_TO_RADIUS_COEF;
        double lat2_rad = lat2 * DEGREES_TO_RADIUS_COEF;
        double c = Math.pow(Math.sin(dlat), 2D) + Math.pow(Math.sin(dlong), 2D) * Math.cos(lat1_rad) * Math.cos(lat2_rad);
        double d = 2 * Math.atan2( Math.sqrt(c),Math.sqrt(1 - c));
        return (a * d);
    }

    public static Segment getSphericalEarthModelCoordinates(double lat, double lng, int bearing, double distance) {
//		Given the distance 'dist' in miles or kilometers.
//		Let radiusEarth = 6372.7976 km or radiusEarth=3959.8728 miles
//		Convert distance to the distance in radians.
        double brg = bearing * DEGREES_TO_RADIUS_COEF;
        double lat1 = lat * DEGREES_TO_RADIUS_COEF;
        double lng1 = lng * DEGREES_TO_RADIUS_COEF;

        double a = EARTH_SPHERICAL_MODEL_RADIUS * 1000D;
        double s1 = distance / a;

//		Calculate the destination coordinates.
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(s1) + Math.cos(lat1) * Math.sin(s1) * Math.cos(brg));
        double lng2 = lng1 + Math.atan2(Math.sin(brg) * Math.sin(s1) * Math.cos(lat1), Math.cos(s1) - Math.sin(lat1) * Math.sin(lat2));
//		Calculate the final bearing and back bearing.
        double dLon = lng1 - lng2;
        double y = Math.sin(dLon) * Math.cos(lat1);
        double x = Math.cos(lat2) * Math.sin(lat1) - Math.sin(lat2) * Math.cos(lat1) * Math.cos(dLon);
        double d = Math.atan2(y, x);
        double finalBrg = d + Math.PI;
        double backBrg = d + DOUBLE_PI;
//		Convert lat2, lng2, finalBrg and backBrg to degrees
        lat2 = lat2 * RADIUS_TO_DEGREES_COEF;
        lng2 = lng2 * RADIUS_TO_DEGREES_COEF;
        finalBrg = finalBrg * RADIUS_TO_DEGREES_COEF;
        backBrg = backBrg * RADIUS_TO_DEGREES_COEF;
//		If lng2 is outside the range -180 to 180, add or subtract 360 to
//		bring it back into that range.
//		If finalBrg or backBrg is outside the range 0 to 360, add or subtract 
//		360 to bring them back into that range.
        return new Segment(lat, lng, lat2, lng2, finalBrg, backBrg , distance);
    }  

//	 The WGS 84 datum surface is an oblate spheroid (ellipsoid)
//	 with major (equatorial) radius a = 6378137 m at the equator 
//	 and flattening f = 1/298.257223563.[6] The polar semi-minor 
//	 axis b then equals a × (1 − f) = 6356752.3142 m
    public static Segment getElipsoidEarthModelDistance(double latdep, double lngdep, double latarr, double lngarr) {

//		Given the distance s in meters, the semi-major axis 'a' in 
//		meters, the semi-minor axis 'b' in meters and the polar flattening 'flat'.
//		Calculate the distance and bearing using Vincenty's formula. Shortened 
//		variable names are used.
        double a = EARTH_ELIPSOID_MODEL_RADIUS * 1000D;
        double f = 1 / EARTH_POLAR_FLATTENING;
        double b = a * (1 - f);

        double lat1 = latdep * DEGREES_TO_RADIUS_COEF;
        double lng1 = lngdep * DEGREES_TO_RADIUS_COEF;
        double lat2 = latarr * DEGREES_TO_RADIUS_COEF;
        double lng2 = lngarr * DEGREES_TO_RADIUS_COEF;
        
    	double L = lng2 - lng1;
    	double tanU1 = (1-f) * Math.tan(lat1), cosU1 = 1 / Math.sqrt((1 + tanU1*tanU1)), sinU1 = tanU1 * cosU1;
    	double tanU2 = (1-f) * Math.tan(lat2), cosU2 = 1 / Math.sqrt((1 + tanU2*tanU2)), sinU2 = tanU2 * cosU2;

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
    	if (iterationLimit==0) throw new Error("Formula failed to converge");

    	double uSq = cosSqalpha * (a*a - b*b) / (b*b);
    	double A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
    	double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
    	double deltasigma = B*sinsigma*(cos2sigmaM+B/4*(cossigma*(-1+2*cos2sigmaM*cos2sigmaM)-
    	    B/6*cos2sigmaM*(-3+4*sinsigma*sinsigma)*(-3+4*cos2sigmaM*cos2sigmaM)));

    	double s = b*A*(sigma-deltasigma);

    	double fwdAz = Math.atan2(cosU2*sinlambda,  cosU1*sinU2-sinU1*cosU2*coslambda);
    	double revAz = Math.atan2(cosU1*sinlambda, -sinU1*cosU2+cosU1*sinU2*coslambda);
    	
    	fwdAz =  fwdAz * RADIUS_TO_DEGREES_COEF;
    	revAz = revAz * RADIUS_TO_DEGREES_COEF;
        
    	return new Segment(latdep, lngdep, latarr, lngarr, fwdAz, revAz, s);
   }
 
//	 The WGS 84 datum surface is an oblate spheroid (ellipsoid)
//	 with major (equatorial) radius a = 6378137 m at the equator 
//	 and flattening f = 1/298.257223563.[6] The polar semi-minor 
//	 axis b then equals a × (1 − f) = 6356752.3142 m
     public static Segment getElipsoidEarthModelCoordinates(double lat, double lng, double bearing, double distance) {
//		Convert the starting point latitude 'lat1' (in the range -90 to 90) to radians.
//		Convert the starting point longitude 'lng1' (in the range -180 to 180) to radians.
//		Convert the bearing 'brg' (in the range 0 to 360) to radians.
        double brg = bearing * DEGREES_TO_RADIUS_COEF;
        double lat1 = lat * DEGREES_TO_RADIUS_COEF;
        double lng1 = lng * DEGREES_TO_RADIUS_COEF;

//		Given the distance s in meters, the semi-major axis 'a' in 
//		meters, the semi-minor axis 'b' in meters and the polar flattening 'flat'.
//		Calculate the destination point using Vincenty's formula. Shortened 
//		variable names are used.
        double a = EARTH_ELIPSOID_MODEL_RADIUS * 1000D;
        double f = 1 / EARTH_POLAR_FLATTENING;
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

        double s1p = DOUBLE_PI;
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
        double finalBrg = d + DOUBLE_PI;
        double backBrg = d + Math.PI;
        double lng2 = lng1 + l;

        //Convert lat2, lng2, finalBrg and backBrg to degrees
        lat2 = lat2 * RADIUS_TO_DEGREES_COEF;
        lng2 = lng2 * RADIUS_TO_DEGREES_COEF;
        finalBrg = finalBrg * RADIUS_TO_DEGREES_COEF;
        backBrg = backBrg * RADIUS_TO_DEGREES_COEF;

        //If lng2 is outside the range -180 to 180, add or
        //subtract 360 to bring it back into that range.
        //If finalBrg or backBrg is outside the range 0 to
        //360, add or subtract 360 to bring them back into
        //that range
        return new Segment(lat, lng, lat2, lng2, finalBrg, backBrg, distance );
    }

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//
    private Mediator mediator;

    /**
     * Constructor
     *
     * @param mediator the {@link Mediator} allowing the
     *                 GeoJSONFiltering to be instantiated to interact with
     *                 the OSGi host environment
     */
    public GeoJSONFiltering(Mediator mediator) {
        this.mediator = mediator;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Filtering#handle(java.lang.String)
     */
    @Override
    public boolean handle(String type) {
        return "geojson".equals(type);
    }


    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Filtering#apply(java.lang.String, java.lang.Object)
     */
    @Override
    public String apply(String definition, Object result) {
        JSONObject obj = new JSONObject(definition);
        boolean output = obj.optBoolean("output");
        if (!output) {
            return String.valueOf(result);
        }

        JSONValidator validator = new JSONValidator(String.valueOf(result));
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\": \"FeatureCollection\", \"features\": [");

        int count = 0;
        int index = 0;

        Set<String> names = new HashSet<String>();
        Map<Integer, String> locationMap = new HashMap<Integer, String>();
        Map<Integer, String> nameMap = new HashMap<Integer, String>();

        while (true) {
            JSONToken token = validator.nextToken();
            if (token == null) {
                break;
            }
            if (token.ordinal() == JSONToken.JSON_OBJECT_OPENING.ordinal()) {
                count++;
            }
            if (token.ordinal() == JSONToken.JSON_OBJECT_CLOSING.ordinal()) {
                Integer ind = new Integer(count);
                nameMap.remove(ind);
                locationMap.remove(ind);
                count--;
            }
            if (token.ordinal() == JSONToken.JSON_OBJECT_ITEM.ordinal() && token.getContext().key.equals(Resource.NAME)) {
                Integer ind = new Integer(count);
                String name = (String) token.getContext().value;
                if (name != null) {
                    nameMap.put(ind, name);
                }
                String location = null;
                if ((location = locationMap.get(ind)) != null && !names.contains(name) && writeLocation(name, location, index, builder)) {
                    index++;
                }
            }
            if (token.ordinal() == JSONToken.JSON_OBJECT_ITEM.ordinal() && token.getContext().key.equals(LocationResource.LOCATION)) {
                Integer ind = new Integer(count);
                String location = (String) token.getContext().value;
                if (location != null) {
                    locationMap.put(ind, location);
                }
                String name = null;
                if ((name = nameMap.get(ind)) != null && !names.contains(name) && writeLocation(name, location, index, builder)) {
                    index++;
                }
            }
        }
        builder.append("]}");
        return builder.toString();
    }

    boolean writeLocation(String name, String location, int index, StringBuilder builder) {
        try {
            String[] locationElements = location.split(":");

            double latitude = Double.parseDouble(locationElements[0]);
            double longitude = Double.parseDouble(locationElements[1]);
            STATEMENT.apply("latitude", latitude);
            STATEMENT.apply("longitude", longitude);
            STATEMENT.apply("name", name);
            if (index > 0) {
                builder.append(",");
            }
            builder.append(STATEMENT.toString());
            return true;

        } catch (Exception e) {
            mediator.error(e);

        } finally {
            STATEMENT.reset();
        }
        return false;
    }

    /**
     * @inheritDoc
     * @see org.eclipse.sensinact.gateway.core.Filtering#getLDAPComponent()
     */
    @Override
    public String getLDAPComponent(String definition) {
        String ldapFilter = null;
        try {
            JSONObject obj = new JSONObject(definition);
            double lat = obj.getDouble("latitude");
            double lng = obj.getDouble("longitude");
            double distance = obj.getDouble("distance");

            Segment rad0 = null;
            Segment rad90 = null;

            if (distance < 200) {
                rad0 = getSphericalEarthModelCoordinates(lat, lng, 0, distance);
                rad90 = getSphericalEarthModelCoordinates(lat, lng, 90, distance);

            } else {
                rad0 = getElipsoidEarthModelCoordinates(lat, lng, 0, distance);
                rad90 = getElipsoidEarthModelCoordinates(lat, lng, 90, distance);
            }
            double diffLat = Math.abs((rad0.lat1 - rad0.lat2));
            double diffLng = Math.abs((rad90.lng1 - rad90.lng2));

            ldapFilter = String.format("(&(latitude <= %s)(latitude >= %s)(longitude <= %s)(longitude >= %s))", (lat + diffLat), (lat - diffLat), (lng + diffLng), (lng - diffLng));
        } catch (JSONException e) {
            return null;
        }
        return ldapFilter;
    }
}

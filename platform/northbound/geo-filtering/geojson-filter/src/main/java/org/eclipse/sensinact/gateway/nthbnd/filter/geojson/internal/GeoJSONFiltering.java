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
    private static class Radius {
        final double lat1;
        final double lng1;
        final double lat2;
        final double lng2;
        final double bearing;

        Radius(double lat1, double lng1, double lat2, double lng2, double bearing) {
            this.lat1 = lat1;
            this.lng1 = lng1;
            this.lat2 = lat2;
            if (lng2 < -180) {
                this.lng2 = lng2 + 360;
            } else if (lng2 > 180) {
                this.lng2 = lng2 - 360;
            } else {
                this.lng2 = lng2;
            }
            if (bearing < 0) {
                this.bearing = bearing + 360;
            } else if (bearing > 360) {
                this.bearing = bearing - 360;
            } else {
                this.bearing = bearing;
            }
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

    private static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double a = EARTH_ELIPSOID_MODEL_RADIUS * 1000D;
        double dlong = ((lng2 - lng1) * DEGREES_TO_RADIUS_COEF) / 2;
        double dlat = ((lat2 - lat1) * DEGREES_TO_RADIUS_COEF) / 2;
        double lat1_rad = lat1 * DEGREES_TO_RADIUS_COEF;
        double lat2_rad = lat2 * DEGREES_TO_RADIUS_COEF;
        double c = Math.pow(Math.sin(dlat), 2D) + Math.pow(Math.sin(dlong), 2D) * Math.cos(lat1_rad) * Math.cos(lat2_rad);
        double d = 2 * Math.atan2(Math.sqrt(c), Math.sqrt(1 - c));
        return (a * d);
    }

    private static Radius getSphericalEarthModelCoordinates(double lat, double lng, int bearing, double distance) {
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
        return new Radius(lat, lng, lat2, lng2, finalBrg);
    }

    //	The WGS 84 datum surface is an oblate spheroid (ellipsoid)
//	with major (equatorial) radius a = 6378137 m at the equator 
//	and flattening f = 1/298.257223563.[6] The polar semi-minor 
//	axis b then equals a × (1 − f) = 6356752.3142 m
    private static Radius getElipsoidEarthModelCoordinates(double lat, double lng, int bearing, double distance) {
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
        return new Radius(lat, lng, lat2, lng2, finalBrg);
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

            Radius rad0 = null;
            Radius rad90 = null;

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

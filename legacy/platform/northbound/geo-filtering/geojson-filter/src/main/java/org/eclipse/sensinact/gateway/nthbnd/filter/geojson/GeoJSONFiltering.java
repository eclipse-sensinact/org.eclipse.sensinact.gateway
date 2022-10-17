/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.nthbnd.filter.geojson;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.gateway.core.LocationResource;
import org.eclipse.sensinact.gateway.core.Resource;
import org.eclipse.sensinact.gateway.core.filtering.Filtering;
import org.eclipse.sensinact.gateway.core.filtering.FilteringType;
import org.eclipse.sensinact.gateway.util.GeoJsonUtils;
import org.eclipse.sensinact.gateway.util.LocationUtils;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.eclipse.sensinact.gateway.util.location.Segment;
import org.eclipse.sensinact.gateway.util.location.geojson.GeoJson;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;

import jakarta.json.JsonObject;

/**
 * {@link Filtering} implementation allowing to apply a location discrimination
 * to the result object to be filtered
 *
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@FilteringType(GeoJSONFiltering.GEOJSON)
@Component(immediate=true, service = Filtering.class)
public class GeoJSONFiltering implements Filtering {
    public static final String GEOJSON = "geojson";
    
    private final ObjectMapper mapper = JsonMapper.builder()
    		.addModule(new JSONPModule(JsonProviderFactory.getProvider()))
    		.build();

	//********************************************************************//
    //						NESTED DECLARATIONS			  			      //
    //********************************************************************//
    //********************************************************************//
    //						ABSTRACT DECLARATIONS						  //
    //********************************************************************//
    //********************************************************************//
    //						STATIC DECLARATIONS							  //
    //********************************************************************//
    
    private static Logger LOG = LoggerFactory.getLogger(GeoJSONFiltering.class.getCanonicalName());

    private static final String POINT_FEATURE_TEMPLATE = 
    		"{" + 
		    " \"type\": \"Feature\"," + 
		    " \"properties\": {}," + 
		    "  \"geometry\": {" + 
		    "     \"type\": \"Point\"," + 
		    "     \"coordinates\": [ %d, %d ] " + 
		    "  }" + 
		    "}";

    //********************************************************************//
    //						INSTANCE DECLARATIONS						  //
    //********************************************************************//

    @Override
    public boolean handle(String type) {
        return GEOJSON.equals(type);
    }

    @Override
    public String apply(String definition, Object result) {
    	JsonObject obj;
		try {
			obj = mapper.readValue(definition, JsonObject.class);
		} catch (Exception e) {
			throw new RuntimeException("Unable to read definition " + definition, e);
		}
		
        boolean output = obj.getBoolean("output", false);
        if (!output) {
            return String.valueOf(result);
        }

        try {
	        JsonParser parser = mapper.createParser(String.valueOf(result));
	        StringBuilder builder = new StringBuilder();
	        builder.append("{\"type\": \"FeatureCollection\", \"features\": [");
	
	        int count = 0;
	        int index = 0;
	
	        Set<String> names = new HashSet<String>();
	        Map<Integer, String> locationMap = new HashMap<Integer, String>();
	        Map<Integer, String> nameMap = new HashMap<Integer, String>();
	
	        while (true) {
	            JsonToken token = parser.nextToken();
	            if (token == null) {
	                break;
	            }
	            if (token.ordinal() == JsonToken.START_OBJECT.ordinal()) {
	                count++;
	            }
	            if (token.ordinal() == JsonToken.END_OBJECT.ordinal()) {
	                Integer ind = Integer.valueOf(count);
	                nameMap.remove(ind);
	                locationMap.remove(ind);
	                count--;
	            }
	            if (token.ordinal() == JsonToken.FIELD_NAME.ordinal() && parser.getCurrentName().equals(Resource.NAME)) {
	                Integer ind = Integer.valueOf(count);
	                parser.nextToken();
	                String name = parser.readValueAs(String.class);
	                if (name != null) {
	                    nameMap.put(ind, name);
	                }
	                String location = null;
	                if ((location = locationMap.get(ind)) != null && !names.contains(name) && writeLocation(name, location, index, builder)) {
	                    index++;
	                }
	            }
	            if (token.ordinal() == JsonToken.FIELD_NAME.ordinal() && parser.getCurrentName().equals(LocationResource.LOCATION)) {
	                Integer ind = Integer.valueOf(count);
	                parser.nextToken();
	                String location =  parser.readValueAs(String.class);
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
        } catch (Exception e) {
        	throw new RuntimeException("Failed to correctly process the location of one or more providers", e);
        }
    }

    boolean writeLocation(String name, String location, int index, StringBuilder builder) {
        try {
        	GeoJson g = GeoJsonUtils.readGeoJson(location);
        	if(g != null) {
                if (index > 0) 
                    builder.append(",");
        		builder.append(GeoJsonUtils.geoJsonFeaturesToString(g));
        		return true;
        	}
        	char[] seps = new char[] {':',',',' '};
        	int pos = 0;
        	for(;pos < seps.length; pos++) {
        		int sep = location.indexOf(':');
        		if(sep >= 0)
        			break;
        	}
        	if(pos == seps.length) {
        		return false;
        	}        	
        	char sep = seps[pos];        	
            String[] locationElements = location.split(new String(new char[] {sep}));
            double latitude = Double.parseDouble(locationElements[0]);
            double longitude = Double.parseDouble(locationElements[1]);
            if (index > 0) 
                builder.append(",");
            
            builder.append(String.format(POINT_FEATURE_TEMPLATE, longitude, latitude));
            return true;

        } catch (Exception e) {
        	LOG.error("could not write location",e);
        }
        return false;
    }

    @Override
    public String getLDAPComponent(String definition) {
        String ldapFilter = null;
        try {
            JsonObject obj = mapper.readValue(definition, JsonObject.class);
            double lat = obj.getJsonNumber("latitude").doubleValue();
            double lng = obj.getJsonNumber("longitude").doubleValue();
            double distance = obj.getJsonNumber("distance").doubleValue();

            Segment rad0 = null;
            Segment rad90 = null;
            int bearing0 =  Double.valueOf(LocationUtils.fromReverseClockedDegreesAngleToNorthOrientedBearing(0)).intValue();
            int bearing90 =  Double.valueOf(LocationUtils.fromReverseClockedDegreesAngleToNorthOrientedBearing(90)).intValue();
            if (distance < 200) {
                rad0 = LocationUtils.getSphericalEarthModelCoordinates(lat, lng, bearing0	, distance);
                rad90 = LocationUtils.getSphericalEarthModelCoordinates(lat, lng, bearing90, distance);
            } else {
                rad0 = LocationUtils.getElipsoidEarthModelCoordinates(lat, lng, bearing0,distance);
                rad90 = LocationUtils.getElipsoidEarthModelCoordinates(lat, lng, bearing90, distance);
            }
            double diffLat = Math.abs((rad0.getLat1() - rad0.getLat2()));
            double diffLng = Math.abs((rad90.getLng1() - rad90.getLng2()));

            ldapFilter = String.format("(&(latitude <= %s)(latitude >= %s)(longitude <= %s)(longitude >= %s))",
            		(lat + diffLat), (lat - diffLat), (lng + diffLng), (lng - diffLng));
            
        } catch (Exception e) {
        	e.printStackTrace();
            return null;
        }
        return ldapFilter;
    }
}

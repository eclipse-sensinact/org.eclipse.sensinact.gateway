/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.openweather.internal;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.eclipse.sensinact.gateway.core.LocationResource;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.SimplePacketReader;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;

import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

/**
 *
 */
public class OpenWeatherPacketReader extends SimplePacketReader<HttpPacket> {
	private static final Logger LOG = LoggerFactory.getLogger(OpenWeatherPacketReader.class);
	
	private final ObjectMapper mapper = JsonMapper.builder()
    		.addModule(new JSONPModule(JsonProviderFactory.getProvider()))
    		.build();

	class WeatherSubPacket {
		String serviceProvider;
		String service;
		String resource;
		Object data;
		long timestamp;
	}
	
	private List<WeatherSubPacket> subPackets;
	private HttpPacket packet = null;
    
	/**
     */
    public OpenWeatherPacketReader() {
        super();
    }

    @Override
    public void load(HttpPacket packet) throws InvalidPacketException {
        this.packet = packet;
        this.subPackets = new ArrayList<>();
    }

    @Override
    public void parse() throws InvalidPacketException {
    	if(this.packet == null) {
    		super.configureEOF();
    		return;
    	}
    	if(this.subPackets.isEmpty()) {
            String content = new String(packet.getBytes());
	    	String serviceProvider = null;
	        try {
	        	JsonArray jsonArray = mapper.readValue(content, JsonArray.class);
	            JsonObject jsonObject = jsonArray.getJsonObject(0);	
	            JsonObject weatherObject = jsonObject.getJsonObject("weather");
	
	            if (!JsonObject.NULL.equals(weatherObject) && weatherObject.size() != 0) {
	                serviceProvider = weatherObject.getString("name");
	                this.parseWeather(serviceProvider, weatherObject);
	            }
	            jsonObject = jsonArray.getJsonObject(1);	
	            String iconObject = jsonObject.getString("icon");
	
	            if (iconObject != null && iconObject.length() != 0) {
	            	WeatherSubPacket sp = new WeatherSubPacket();
	                sp.serviceProvider = serviceProvider;
	                sp.service= "weather";
	                sp.resource = "image";
	                sp.data = Base64.getEncoder().encode(iconObject.getBytes());
	            	this.subPackets.add(sp); 
	            }
	        } catch (Exception e) {
	        	OpenWeatherPacketReader.LOG.error(e.getMessage(), e);
	            super.configureEOF();
	            throw new InvalidPacketException(e);
	        }
	        if(!this.subPackets.isEmpty())
	        	parse();
    	} else {
    		WeatherSubPacket sub = this.subPackets.remove(0);
    		super.setServiceProviderId(sub.serviceProvider);
    		super.setServiceId(sub.service);
    		super.setResourceId(sub.resource);
    		super.setTimestamp(sub.timestamp == 0
    			?System.currentTimeMillis():sub.timestamp);
    		super.setData(sub.data);
    		if(this.subPackets.isEmpty())
    			this.packet = null;
    		super.configure();
    		return;
    	}
    	super.configureEOF();
    }

    /**
     * @param station
     * @param object
     */
    private void parseWeather(String station, JsonObject object) {
        long timestamp = object.getJsonNumber("dt").longValueExact() * 1000L;
        JsonObject coord = object.getJsonObject("coord");
        if (coord != null) {
        	WeatherSubPacket sp = new WeatherSubPacket();
            sp.serviceProvider = station;
            sp.service= ServiceProvider.ADMINISTRATION_SERVICE_NAME;
            sp.resource = LocationResource.LOCATION;
            sp.data = new StringBuilder().append(coord.getJsonNumber("lat").doubleValue())
            		.append(":")
            		.append(coord.getJsonNumber("lon").doubleValue()).toString();
            this.subPackets.add(sp);
        }
        JsonArray weather = object.getJsonArray("weather");
        JsonObject content = null;
        if (weather != null && !weather.isEmpty() && (content = weather.getJsonObject(0)) != null) {
        	WeatherSubPacket sp = new WeatherSubPacket();
            sp.serviceProvider = station;
            sp.service= "weather";
            sp.resource = "state";
            handleOptData(content, "main", sp);
            sp.timestamp = timestamp;
            this.subPackets.add(sp);
            
            sp = new WeatherSubPacket();
            sp.serviceProvider = station;
            sp.service= "weather";
            sp.resource = "description";
            handleOptData(content, "description", sp);
            sp.timestamp = timestamp;
            this.subPackets.add(sp);
        }
        JsonObject wind = object.getJsonObject("wind");
        if (wind != null) {
        	WeatherSubPacket sp = new WeatherSubPacket();
            sp.serviceProvider = station;
            sp.service= "weather";
            sp.resource = "wind";
			handleOptData(wind, "speed", sp);
            sp.timestamp = timestamp;
            this.subPackets.add(sp);

        	sp = new WeatherSubPacket();
            sp.serviceProvider = station;
            sp.service= "weather";
            sp.resource = "orientation";
            handleOptData(wind, "deg", sp);
            sp.timestamp = timestamp;
            this.subPackets.add(sp);
        }
        JsonObject main = object.getJsonObject("main");
        if (main != null) {       	

        	WeatherSubPacket sp = new WeatherSubPacket();
            sp.serviceProvider = station;
            sp.service= "weather";
            sp.resource = "temperature";
            handleOptData(main, "temp", sp);
            sp.timestamp = timestamp;
            this.subPackets.add(sp);
            
            sp = new WeatherSubPacket();
            sp.serviceProvider = station;
            sp.service= "weather";
            sp.resource = "humidity";
            handleOptData(main, "humidity", sp);
            sp.timestamp = timestamp;
            this.subPackets.add(sp);

            sp = new WeatherSubPacket();
            sp.serviceProvider = station;
            sp.service= "weather";
            sp.resource = "pressure";
            handleOptData(main, "pressure", sp);
            sp.timestamp = timestamp;
            this.subPackets.add(sp);
        }
    }

	private void handleOptData(JsonObject jo, String name, WeatherSubPacket sp) {
		JsonValue jv = jo.get(name);
		if(jv != null && jv.getValueType() != ValueType.NULL) {
		    sp.data = jv.getValueType() == ValueType.NUMBER ? ((JsonNumber)jv).doubleValue() : 
		    		((JsonString)jv).getString();
		}
	}
}

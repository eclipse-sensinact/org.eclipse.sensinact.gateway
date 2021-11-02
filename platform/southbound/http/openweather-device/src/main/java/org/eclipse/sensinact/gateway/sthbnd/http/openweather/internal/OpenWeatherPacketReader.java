/*
* Copyright (c) 2020 Kentyou.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
*    Kentyou - initial API and implementation
 */
package org.eclipse.sensinact.gateway.sthbnd.http.openweather.internal;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.LocationResource;
import org.eclipse.sensinact.gateway.core.ServiceProvider;
import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.SimplePacketReader;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.util.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class OpenWeatherPacketReader extends SimplePacketReader<HttpPacket> {
	private static final Logger LOG = LoggerFactory.getLogger(OpenWeatherPacketReader.class);

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
     * @param mediator
     */
    public OpenWeatherPacketReader(Mediator mediator) {
        super(mediator);
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
            JSONArray jsonArray = new JSONArray(content);
	    	String serviceProvider = null;
	        try {
	            JSONObject jsonObject = jsonArray.optJSONObject(0);	
	            JSONObject weatherObject = jsonObject.optJSONObject("weather");
	
	            if (!JSONObject.NULL.equals(weatherObject) && weatherObject.length() != 0) {
	                serviceProvider = weatherObject.getString("name");
	                this.parseWeather(serviceProvider, weatherObject);
	            }
	            jsonObject = jsonArray.optJSONObject(1);	
	            String iconObject = jsonObject.optString("icon");
	
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
    private void parseWeather(String station, JSONObject object) {
        long timestamp = object.optLong("dt") * 1000L;
        JSONObject coord = object.optJSONObject("coord");
        if (coord != null) {
        	WeatherSubPacket sp = new WeatherSubPacket();
            sp.serviceProvider = station;
            sp.service= ServiceProvider.ADMINISTRATION_SERVICE_NAME;
            sp.resource = LocationResource.LOCATION;
            sp.data = new StringBuilder().append(coord.optDouble("lat")).append(JSONUtils.COLON).append(coord.optDouble("lon")).toString();
            this.subPackets.add(sp);
        }
        JSONArray weather = object.optJSONArray("weather");
        JSONObject content = null;
        if (weather != null && (content = weather.optJSONObject(0)) != null) {
        	WeatherSubPacket sp = new WeatherSubPacket();
            sp.serviceProvider = station;
            sp.service= "weather";
            sp.resource = "state";
            sp.data = content.opt("main");
            sp.timestamp = timestamp;
            this.subPackets.add(sp);
            
            sp = new WeatherSubPacket();
            sp.serviceProvider = station;
            sp.service= "weather";
            sp.resource = "description";
            sp.data = content.opt("description");
            sp.timestamp = timestamp;
            this.subPackets.add(sp);
        }
        JSONObject wind = object.optJSONObject("wind");
        if (wind != null) {
        	WeatherSubPacket sp = new WeatherSubPacket();
            sp.serviceProvider = station;
            sp.service= "weather";
            sp.resource = "wind";
            sp.data = wind.opt("speed");
            sp.timestamp = timestamp;
            this.subPackets.add(sp);

        	sp = new WeatherSubPacket();
            sp.serviceProvider = station;
            sp.service= "weather";
            sp.resource = "orientation";
            sp.data = wind.opt("deg");
            sp.timestamp = timestamp;
            this.subPackets.add(sp);
        }
        JSONObject main = object.optJSONObject("main");
        if (main != null) {       	

        	WeatherSubPacket sp = new WeatherSubPacket();
            sp.serviceProvider = station;
            sp.service= "weather";
            sp.resource = "temperature";
            sp.data = main.opt("temp");
            sp.timestamp = timestamp;
            this.subPackets.add(sp);
            
            sp = new WeatherSubPacket();
            sp.serviceProvider = station;
            sp.service= "weather";
            sp.resource = "humidity";
            sp.data = main.opt("humidity");
            sp.timestamp = timestamp;
            this.subPackets.add(sp);

            sp = new WeatherSubPacket();
            sp.serviceProvider = station;
            sp.service= "weather";
            sp.resource = "pressure";
            sp.data = main.opt("pressure");
            sp.timestamp = timestamp;
            this.subPackets.add(sp);
        }
    }
}

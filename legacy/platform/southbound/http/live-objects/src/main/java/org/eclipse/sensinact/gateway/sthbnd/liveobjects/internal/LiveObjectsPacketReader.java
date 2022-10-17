/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.liveobjects.internal;

import java.util.List;

import org.eclipse.sensinact.gateway.generic.packet.InvalidPacketException;
import org.eclipse.sensinact.gateway.generic.packet.SimplePacketReader;
import org.eclipse.sensinact.gateway.sthbnd.http.HttpPacket;
import org.eclipse.sensinact.gateway.sthbnd.liveobjects.LiveObjectsConstant;
import org.eclipse.sensinact.gateway.util.json.JsonProviderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

/**
 * @author Rémi Druilhe
 */
public class LiveObjectsPacketReader extends SimplePacketReader<HttpPacket> {
	
	private final ObjectMapper mapper = JsonMapper.builder()
    		.addModule(new JSONPModule(JsonProviderFactory.getProvider()))
    		.build();
	
	class LiveObjectsSubPacket {
		String serviceProvider;
		String service;
		String resource;
		Object data;
	}
	
	private HttpPacket packet;
	private List<LiveObjectsSubPacket> subPackets;
	
	/**
     * Constructor
     *
     */
    public LiveObjectsPacketReader() {
        super();
    }

    @Override
    public void load(HttpPacket packet) throws InvalidPacketException {
    	this.packet = packet;
    }

    @Override
    public void parse() throws InvalidPacketException {
    	if(this.packet == null) {
    		super.configureEOF();
    		return;
    	}
    	if(this.subPackets.isEmpty()) {
            try {
            	JsonValue jv = mapper.readValue(packet.getBytes(), JsonValue.class);
		        if (jv.getValueType() == ValueType.OBJECT) {
		            JsonObject jsonObject = jv.asJsonObject();
		            if (jsonObject.containsKey("data")) {
		                JsonArray devices = jsonObject.getJsonArray("data");
		                int length = devices == null ? 0 : devices.size();
		
		                for (int i = 0; i < length; i++) {
	                        JsonObject jo = devices.getJsonObject(i);
	                        if (jo == null) {
	                            continue;
	                        }
	                        String serviceProviderId = jo.getString("namespace") + ":" + jo.getString("id").replace(" ", "");
	                        LiveObjectsSubPacket sub = new LiveObjectsSubPacket();
	                        sub.serviceProvider = serviceProviderId;
	                        sub.service = "admin";
	                        sub.resource = "connected";
	                        sub.data = jo.getBoolean("connected");
	                        this.subPackets.add(sub);	
		                }
		            }
		        } else if (jv.getValueType() == ValueType.ARRAY) {
		            JsonArray jsonArray = (JsonArray) jv.asJsonArray();
		            JsonObject jsonObject = jsonArray.getJsonObject(0);
		            if (jsonArray.getJsonObject(0).containsKey("streamId")) {	                
		            	String serviceProvider = jsonObject.getString("streamId").replace(LiveObjectsConstant.URN, "");
		            	LiveObjectsSubPacket sub = new LiveObjectsSubPacket();
	                    sub.serviceProvider = serviceProvider;
	                    sub.service = "test";
	                    sub.resource = "temp";
	                    sub.data = jsonObject.getJsonObject("value").getJsonNumber("temp").doubleValue();
	                    this.subPackets.add(sub);
		                
		                String location = jsonObject.getJsonObject("location").getJsonNumber("lat").doubleValue() + 
		                		":" + jsonObject.getJsonObject("location").getJsonNumber("lon").doubleValue();
		                sub = new LiveObjectsSubPacket();
	                    sub.serviceProvider = serviceProvider;
	                    sub.service = "admin";
	                    sub.resource = "location";
	                    sub.data = location;
	                    this.subPackets.add(sub);
		            }
		        } else {
		        	throw new IllegalArgumentException("Expected a JSON object or array, but got " + new String(packet.getBytes()));
		        }
            } catch (Exception e) {
            	super.configureEOF();
                throw new InvalidPacketException(e);
            }
		    if(!this.subPackets.isEmpty())
		        parse();
    	} else {
    		LiveObjectsSubPacket sub =this.subPackets.remove(0);
    		super.setServiceProviderId(sub.serviceProvider);
            super.setServiceId(sub.service);
            super.setResourceId(sub.resource);
            super.setData(sub.data);
            if(this.subPackets.isEmpty())
            	this.packet = null;
            super.configure();
            return;
    	}
    	super.configureEOF();
    }
}

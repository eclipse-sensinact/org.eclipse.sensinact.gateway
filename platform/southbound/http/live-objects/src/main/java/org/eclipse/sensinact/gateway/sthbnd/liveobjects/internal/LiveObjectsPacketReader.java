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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * @author Rémi Druilhe
 */
public class LiveObjectsPacketReader extends SimplePacketReader<HttpPacket> {
	
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
		        String content = new String(packet.getBytes());
		        Object json = new JSONTokener(content).nextValue();
		        if (json instanceof JSONObject) {
		            JSONObject jsonObject = (JSONObject) json;
		            if (jsonObject.has("data")) {
		                JSONArray devices = jsonObject.optJSONArray("data");
		                int length = devices == null ? 0 : devices.length();
		
		                for (int i = 0; i < length; i++) {
	                        JSONObject jo = devices.optJSONObject(i);
	                        if (JSONObject.NULL.equals(jo)) {
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
		        } else if (json instanceof JSONArray) {
		            JSONArray jsonArray = (JSONArray) json;
		            JSONObject jsonObject = jsonArray.getJSONObject(0);
		            if (jsonArray.getJSONObject(0).has("streamId")) {	                
		            	String serviceProvider = jsonObject.getString("streamId").replace(LiveObjectsConstant.URN, "");
		            	LiveObjectsSubPacket sub = new LiveObjectsSubPacket();
	                    sub.serviceProvider = serviceProvider;
	                    sub.service = "test";
	                    sub.resource = "temp";
	                    sub.data = jsonObject.getJSONObject("value").getDouble("temp");
	                    this.subPackets.add(sub);
		                
		                String location = jsonObject.getJSONObject("location").getInt("lat") + ":" + jsonObject.getJSONObject("location").getInt("lon");
		                sub = new LiveObjectsSubPacket();
	                    sub.serviceProvider = serviceProvider;
	                    sub.service = "admin";
	                    sub.resource = "location";
	                    sub.data = location;
	                    this.subPackets.add(sub);
		            }
		        }
            } catch (JSONException e) {
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

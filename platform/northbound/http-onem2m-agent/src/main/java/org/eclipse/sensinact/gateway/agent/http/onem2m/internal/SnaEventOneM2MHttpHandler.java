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

package org.eclipse.sensinact.gateway.agent.http.onem2m.internal;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.core.message.AbstractMidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.MidAgentCallback;
import org.eclipse.sensinact.gateway.core.message.SnaErrorMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaLifecycleMessageImpl;
import org.eclipse.sensinact.gateway.core.message.SnaResponseMessage;
import org.eclipse.sensinact.gateway.core.message.SnaUpdateMessageImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfiguration;
import org.eclipse.sensinact.gateway.protocol.http.client.ConnectionConfigurationImpl;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleRequest;
import org.eclipse.sensinact.gateway.protocol.http.client.SimpleResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * AE = sNa Provider
 * Container = sNa Service
 * Container = sNa Resource
 * Instance = sNa Attribute
 */
public class SnaEventOneM2MHttpHandler extends AbstractMidAgentCallback 
{
	private static final int createRequest(String cseBase, String method, String origin, 
	String path, String contentType, JSONObject content) throws IOException
	{
		ConnectionConfiguration<SimpleResponse,SimpleRequest> configuration = 
				new ConnectionConfigurationImpl<SimpleResponse,SimpleRequest>();
		configuration.setHttpMethod(method);
		if(path!=null)
		{
			configuration.setUri(new StringBuilder().append(cseBase).append(
				path).toString());
		} else
		{
			configuration.setUri(cseBase);			
		}
		if(origin != null)
		{
			configuration.addHeader("X-M2M-Origin", origin);
		} else
		{
			configuration.addHeader("X-M2M-Origin", "C");
		}
		configuration.setAccept("application/json");
		configuration.setContentType(contentType);
		configuration.setContent(content.toString());
		SimpleRequest req = new SimpleRequest(configuration);
		SimpleResponse resp =  req.send();
		System.out.println(resp.toString());
		return resp.getStatusCode();
	}

	private static final String buildOrigin(String aeName)
	{
       // char[] originArray = new char[20];
      //  Arrays.fill(originArray, '0');
        
        char[] originInitArray = new StringBuilder().append("CEA"
        ).append(aeName.toUpperCase()).toString().toCharArray();
        
        //System.arraycopy(originInitArray, 0, originArray, 0,
        //originInitArray.length<10?originInitArray.length:10);
        
        return new String(originInitArray/*originArray*/);
	}
	
	private class OneM2MDevice
	{
		final String name;
		final String origin;
		
		private Double latitude;
		private Double longitude;
		private Double battery;
		
		private volatile boolean created;
		
		OneM2MDevice(String name)
		{
			this.name = name;
			this.origin = buildOrigin(name);
			this.created = false;
		}
		
		boolean complete()
		{
			return latitude!=null && longitude!=null && battery!=null;
		}
		
		boolean create()
		{
			if(created)
			{
				System.out.println("ALREADY CREATED");
				return false;
			}
			if(!complete())
			{
				System.out.println(String.format("NOT COMPLETE [%s,%s,%s]",
						longitude,latitude,battery));
				return false;
			}
			try 
			{
			    int status = createRequest(cseBase, "POST",origin,null,
			    		"application/json;ty=2", this.getAEDescription());
			
			    if(status != 201 && status != 200 && status != 409)
			    {
			    	throw new IOException("Unable to create AE");
			    }
			    createRequest(cseBase, "POST",origin,"/" + name,"application/json;ty=3",
			    	this.getBatteryContainerDescription());
			    
			    createRequest(cseBase, "POST",origin,"/" + name,"application/json;ty=3",
			    	this.getLatitudeContainerDescription());
			    
			    createRequest(cseBase, "POST",origin,"/" + name,"application/json;ty=3",
			    	this.getLongitudeContainerDescription());
			    
			    this.created = true;
			    this.setBattery(battery); 
			    this.setLatitude(latitude);
			    this.setLongitude(longitude);
			    return true;
			    
			} catch (IOException e) 
			{
			    e.printStackTrace();
			}
			return false;
		}

		boolean delete()
		{
			if(this.created)
			{ 
		        try 
		        {
	               JSONObject content = new JSONObject();
	               content.put("rn","battery");
	               createRequest(cseBase, "DELETE",origin,"/" + name, "application/json;ty=3", 
	                		new JSONObject().put("m2m:cnt", content));
	               content = new JSONObject();
	               content.put("rn","longitude");
	               createRequest(cseBase, "DELETE",origin,"/" + name, "application/json;ty=3", 
	                		new JSONObject().put("m2m:cnt", content));
	               content = new JSONObject();
	               content.put("rn","latitude");
	               createRequest(cseBase, "DELETE",origin,"/" + name, "application/json;ty=3", 
	                		new JSONObject().put("m2m:cnt", content));
	               content = new JSONObject();
	               content.put("rn",name);
	               createRequest(cseBase, "DELETE",origin,null, "application/json;ty=3", 
	                		new JSONObject().put("m2m:ae", content));
	               this.created = false;
	               
				} catch (IOException e) 
		        {
					e.printStackTrace();
					return false;
				}
			}
			return true;
		}
		
		JSONObject getAEDescription()
		{
			JSONObject content = new JSONObject();
			//create AE
	        content.put("rn", name);
	        content.put("api", name);
	        content.put("lbl", new JSONArray().put("key1").put("key2"));
	        content.put("rr", false);
	        
	        return new JSONObject().put("m2m:ae", content);
		}

		JSONObject getLongitudeContainerDescription()
		{
			JSONObject content = new JSONObject();
            content.put("rn", "longitude");
            content.put("lbl", new JSONArray().put(name));
            return new JSONObject().put("m2m:cnt", content);
		}
		
		void setLongitude(Double longitude)
		{
			if(longitude == null)
			{
				System.out.println("Longitude NULL");
				return;
			}
			this.longitude = longitude;
			if(!created)
			{
				System.out.println("Longitude NOT CREATED");
				return;
			}
			JSONObject content = new JSONObject();
        	content.put("con", String.valueOf(longitude.doubleValue()));
            try
            {
				createRequest(cseBase, "POST", origin,"/" + name + "/longitude",
					"application/json;ty=4",  new JSONObject().put("m2m:cin", content) );
				
			} catch (JSONException | IOException e) 
            {
				e.printStackTrace();
			}
		}

		JSONObject getLatitudeContainerDescription()
		{
			JSONObject content = new JSONObject();
		    content.put("rn", "latitude");
		    content.put("lbl", new JSONArray().put(name));
		    return new JSONObject().put("m2m:cnt", content);
		}

		void setLatitude(Double latitude)
		{
			if(latitude == null)
			{
				System.out.println("Latitude NULL");
				return;
			}
			this.latitude = latitude;
			if(!created)
			{
				System.out.println("Latitude NOT CREATED");
				return;
			}
			JSONObject content = new JSONObject();
        	content.put("con", String.valueOf(latitude.doubleValue()));
            try
            {
				createRequest(cseBase, "POST", origin,"/" + name + "/latitude",
				"application/json;ty=4",  new JSONObject().put("m2m:cin", content) );
				
			} catch (JSONException | IOException e) 
            {
				e.printStackTrace();
			}
		}
		
		JSONObject getBatteryContainerDescription()
		{
			JSONObject content = new JSONObject();
			content.put("rn", "battery");
			content.put("lbl", new JSONArray().put(name));
			return new JSONObject().put("m2m:cnt", content);
		}

		void setBattery(Double battery)
		{
			if(battery == null)
			{
				System.out.println("Battery NULL");
				return;
			}
			this.battery = battery;
			if(!created)
			{
				System.out.println("Battery NOT CREATED");
				return;
			}
			JSONObject content = new JSONObject();
        	content.put("con", String.valueOf(battery.doubleValue()));
            try
            {
				createRequest(cseBase, "POST", origin,"/" + name + "/battery",
				"application/json;ty=4",  new JSONObject().put("m2m:cin", content) );
				
			} catch (JSONException | IOException e) 
            {
				e.printStackTrace();
			}
		}
	}
	
    private final String cseBase;
    private Map<String,OneM2MDevice> map;
    
	public SnaEventOneM2MHttpHandler(String cseBase) throws IOException 
	{
		super();
		this.cseBase = cseBase;
		this.map = new HashMap<String,OneM2MDevice>();
	}

	/**
	 * Treats the RegisteredUpdatedSnaEvent passed as parameter
	 *
	 * @param event the RegisteredUpdatedSnaEvent to process
	 */
	public void doHandle(SnaUpdateMessageImpl event) 
	{
		JSONObject eventJson = new JSONObject(event.getJSON()
				).getJSONObject("notification");
		
		String aeName = event.getPath().split("/")[1];
		OneM2MDevice om2m = map.get(aeName);
		
		if(om2m == null)
		{
			System.out.println("Unknown OneM2M device : " + aeName);
			return;
		}
		switch (event.getType()) {
		
		    // Create contentInstance
		    case ATTRIBUTE_VALUE_UPDATED:
		    	
			Object value = eventJson.get(DataResource.VALUE);
		    	if(event.getPath().endsWith("/admin/location/value"))
		    	{
		    		String[] locs = String.valueOf(value).split(":");
		    		if(locs.length != 2)
		    		{
		    			return;
		    		}
		    		try
		    		{
		    			Double latitude = Double.parseDouble(locs[0]);
		    			Double longitude = Double.parseDouble(locs[1]);
		    			om2m.setLatitude(latitude);
		    			om2m.setLongitude(longitude);
		    			
		    		} catch(NumberFormatException e)
		    		{
		    			e.printStackTrace();
		    		}            		
		    	} else if(!JSONObject.NULL.equals(value))
		    	{
		    		try
		    		{
		    			Double battery = Double.parseDouble(String.valueOf(value));
		    			om2m.setBattery(battery);
		    			
		        	} catch(NumberFormatException e)
		        	{
		        		e.printStackTrace();
		        	}
		    	}
		        break;
		    default:
		        return;
		}
	}

	/**
	 * Treats the ServiceRegisteredSnaEvent passed as parameter
	 *
	 * @param event the ServiceRegisteredSnaEvent to process
	 */
	public void doHandle(SnaLifecycleMessageImpl event)	
	{
		JSONObject eventJson = new JSONObject(event.getJSON()
				).getJSONObject("initial");

		String aeName = event.getPath().split("/")[1];

		switch (event.getType()) 
		{
		    case PROVIDER_APPEARING:
		    case PROVIDER_DISAPPEARING:
		    case SERVICE_APPEARING:
		    case SERVICE_DISAPPEARING:
		    case RESOURCE_DISAPPEARING:
		    	break;
		    case RESOURCE_APPEARING:
		        OneM2MDevice om2m = map.get(aeName);
		        if(om2m == null)
		        {
		        	om2m = new OneM2MDevice(aeName);
		        	map.put(aeName, om2m);
		        }
		    	Object value = eventJson.get(DataResource.VALUE);
		    	
		    	if(event.getPath().endsWith("/admin/location"))
		    	{
		    		String[] locs = String.valueOf(value).split(":");
		    		if(locs.length != 2)
		    		{
		    			return;
		    		}
		    		try
		    		{
		    			Double lat = Double.parseDouble(locs[0]);
		    			Double lon = Double.parseDouble(locs[1]);            			
		    			om2m.setLatitude(lat);
		    			om2m.setLongitude(lon);

						//Automatically create battery
						om2m.setBattery(0d);
		    			
		    		} catch(NumberFormatException e)
		    		{
		    			e.printStackTrace();
		    		}            		
		    	}
//		    	else
//		    	{
//		    		if(JSONObject.NULL.equals(value))
//		    		{
//		    			om2m.setBattery(0d);
//
//		    		} else
//		    		{
//			    		try
//			    		{
//			    			Double bat = Double.parseDouble(String.valueOf(value));
//			    			om2m.setBattery(bat);
//
//			    		} catch(NumberFormatException e)
//			    		{
//			    			e.printStackTrace();
//			    		}
//		    		}
//		    	}
		    	if(om2m.complete())
		    	{
		    		om2m.create();
		    	}            		
		        break;
		    default:
		        return;
		}
    }

	/**
	 * @see MidAgentCallback#stop()
	 */
    public void stop() {}

	/**
	 * @see AbstractMidAgentCallback#doHandle(SnaErrorMessageImpl)
	 */
    public void doHandle(SnaErrorMessageImpl event) {}

	/**
	 * @see AbstractMidAgentCallback#doHandle(SnaResponseMessage)
	 */
    public void doHandle(SnaResponseMessage event) {}
}

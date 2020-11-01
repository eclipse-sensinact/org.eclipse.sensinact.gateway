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
package org.eclipse.sensinact.gateway.agent.storage.influxdb.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.sensinact.gateway.agent.storage.generic.StorageConnection;
import org.eclipse.sensinact.gateway.common.bundle.Mediator;
import org.eclipse.sensinact.gateway.core.DataResource;
import org.eclipse.sensinact.gateway.tools.connector.influxdb.InfluxDbDatabase;
import org.eclipse.sensinact.gateway.util.json.JSONObjectStatement;
import org.eclipse.sensinact.gateway.util.json.JSONTokenerStatement;
import org.json.JSONObject;

/**
 * Extended {@link SorageConnection} dedicated to InfluxDB data store
 */
public class InfluxDBStorageConnection extends StorageConnection {
	
	private static final JSONObjectStatement STATEMENT = 
    		new JSONObjectStatement(new JSONTokenerStatement(
			    "{" + 
			    " \"type\": \"Feature\"," + 
			    " \"properties\": {" + 
			    "	    \"name\": $(name)" + 
			    "  }," + 
			    "  \"geometry\": {" + 
			    "     \"type\": \"Point\"," + 
			    "     \"coordinates\": [ $(longitude),$(latitude)] " + 
			    "  }" + 
			    "}"));
	
	private String measurement;
	private InfluxDbDatabase database;
	
	/**
	 * Constructor
	 * 
	 * @param mediator the {@link Mediator} allowing the InfluxDbAgentCallback to be instantiated
	 * to interact with the OSGi host environment
	 * @param database the {@link InfluxDbDatabase} in which data will be stored
	 * @param measurement the String name of the measurement in which data will be stored
	 * 
	 * @throws IOException 
	 */
	public InfluxDBStorageConnection(Mediator mediator, InfluxDbDatabase database, String measurement){
		super(mediator);
		this.database = database;
		this.measurement = measurement;		
	}
	
	@Override
	public void store(JSONObject obj)  {
		String uri = (String) obj.opt("path");
		String location = (String) obj.opt("location");
		double latitude = -1;
		double longitude = -1;
		String geolocation = null;
		
		if(location != null) {
			String[] locationElements = location.split(":");
			if(locationElements.length == 2) {
				latitude = Double.parseDouble(locationElements[0]);
				longitude = Double.parseDouble(locationElements[1]);				
			    STATEMENT.apply("latitude", latitude);
			    STATEMENT.apply("longitude", longitude);
			    STATEMENT.apply("name", obj.opt("provider"));			    
			    geolocation = STATEMENT.toString();
			} else
				geolocation = location;
		}		
		final Dictionary<String,String> ts = new Hashtable<>();
		ts.put("path",uri);
		ts.put("latitude", latitude==-1?"N/A":String.valueOf(latitude));
		ts.put("longitude", longitude==-1?"N/A":String.valueOf(longitude));
		ts.put("geolocation", geolocation==null?"N/A":geolocation);
		
		this.database.add(measurement, ts, obj.opt(DataResource.VALUE));	
	}
}
